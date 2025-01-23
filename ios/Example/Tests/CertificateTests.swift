//
//  CertificateTests.swift
//  Trifle_Tests
//

import Foundation
import XCTest
import Wire
@testable import Trifle

final class CertificateTests: XCTestCase {
    
    func testVerifyCertificate_succeeds() throws {
        let trifle = try Trifle(reverseDomain: TestFixtures.reverseDomain)
        let keyHandle = try trifle.generateKeyHandle()
        
        let mobileCertReq = try trifle.generateMobileCertificateRequest(entity: "trifleEntity", keyHandle: keyHandle)
        
        let deviceCertificate = try TrifleCertificate.deserialize(data: TestFixtures.deviceTrifleCertEncoded3!).getCertificate()
        let rootCertificate = try TrifleCertificate.deserialize(data: TestFixtures.rootTrifleCertEncoded3!).getCertificate()
        
        let isVerified = try deviceCertificate.verify(
            certificateRequest: try ProtoDecoder().decode(MobileCertificateRequest.self, from: mobileCertReq.serialize()),
            intermediateChain: [],
            rootCertificate: rootCertificate
        )
        
        XCTAssertTrue(isVerified)
    }
    
    func testVerifyCertificate_fails() throws {
        let trifle = try Trifle(reverseDomain: TestFixtures.reverseDomain)
        let keyHandle = try trifle.generateKeyHandle()
        
        let mobileCertReq = try trifle.generateMobileCertificateRequest(entity: "trifleEntity", keyHandle: keyHandle)
        
        let deviceCertificate = try TrifleCertificate.deserialize(data: TestFixtures.deviceTrifleCertEncoded!).getCertificate()
        let otherRootCertificate = try TrifleCertificate.deserialize(data: TestFixtures.otherRootTrifleCertEncoded!).getCertificate()
        XCTAssertThrowsError(try deviceCertificate.verify(
            certificateRequest: try ProtoDecoder().decode(MobileCertificateRequest.self, from: mobileCertReq.serialize()),
            intermediateChain: [],
            rootCertificate: otherRootCertificate ), "Certificate is invalid.")
    }
    
    func testExpiredCertificate() throws {
                
        let validCertificate = Certificate(configure: { cert in
            cert.version = 0
            cert.certificate = TestFixtures.validCertEncoded
        })
        let expiredCertificate = Certificate(configure: { cert in
            cert.version = 0
            cert.certificate = TestFixtures.expiredCertEncoded
        })

        XCTAssertTrue(try validCertificate.verify(intermediateChain: Array<Certificate>()))
    
        XCTAssertThrowsError(try expiredCertificate.verify(intermediateChain: Array<Certificate>())) { error in
            XCTAssertTrue(( ((error as? TrifleError)?.errorDescription?.contains(
                "Security Framework error ( -67818 )") == true ) ))
        }
    }
    
}
