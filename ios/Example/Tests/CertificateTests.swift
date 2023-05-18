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
        
        let mobileCertReq = try trifle.generateMobileCertificateRequest(keyHandle: keyHandle)
        
        let deviceCertificate = try TrifleCertificate.deserialize(data: TestFixtures.deviceTrifleCertEncoded2!).getCertificate()
        let rootCertificate = try TrifleCertificate.deserialize(data: TestFixtures.rootTrifleCertEncoded!).getCertificate()
        
        let isVerified = try deviceCertificate.verify(
            certificateRequest: mobileCertReq,
            intermediateChain: [],
            rootCertificate: rootCertificate
        )
        
        XCTAssertTrue(isVerified)
    }
    
    func testVerifyCertificate_fails() throws {
        let trifle = try Trifle(reverseDomain: TestFixtures.reverseDomain)
        let keyHandle = try trifle.generateKeyHandle()
        
        let mobileCertReq = try trifle.generateMobileCertificateRequest(keyHandle: keyHandle)
        
        let deviceCertificate = try TrifleCertificate.deserialize(data: TestFixtures.deviceTrifleCertEncoded!).getCertificate()
        let otherRootCertificate = try TrifleCertificate.deserialize(data: TestFixtures.otherRootTrifleCertEncoded!).getCertificate()
        XCTAssertThrowsError(try deviceCertificate.verify(
            certificateRequest: mobileCertReq,
            intermediateChain: [],
            rootCertificate: otherRootCertificate ),
                             "Certificate is invalid.")
    }
    
    func testExpiredCertificate() throws {
                
        let validCertificate = Certificate(version: 0, certificate: TestFixtures.validCertEncoded)
        let expiredCertificate = Certificate(version: 0, certificate: TestFixtures.expiredCertEncoded)

        XCTAssertTrue(try validCertificate.verify(intermediateChain: Array<Certificate>()))
    
        XCTAssertThrowsError(try expiredCertificate.verify(intermediateChain: Array<Certificate>()),
                             "Certificate is expired.")
    }
    
}
