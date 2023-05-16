//
//  TrifleCertificateTests.swift
//  Trifle_Tests
//

import Foundation
import XCTest
@testable import Trifle

final class TrifleCertificateTests: XCTestCase {
    
    func testVerifyTrifleCertificate_succeeds() throws {
        let trifle = try Trifle(reverseDomain: TestFixtures.reverseDomain)                
        let deviceCertificate = try TrifleCertificate(data: TestFixtures.deviceTrifleCertEncoded!)
        let rootCertificate = try TrifleCertificate(data: TestFixtures.rootTrifleCertEncoded!)

        var isVerified = try deviceCertificate.verify(
            certificateRequest: nil,
            intermediateTrifleChain: [rootCertificate],
            rootTrifleCertificate: nil
        )
        
        XCTAssertTrue(isVerified)

        isVerified = try deviceCertificate.verify(
            certificateRequest: nil,
            intermediateTrifleChain: [],
            rootTrifleCertificate: rootCertificate
        )
        
        XCTAssertTrue(isVerified)
    }
    
    func testVerifyCertificate_succeeds() throws {
        let trifle = try Trifle(reverseDomain: TestFixtures.reverseDomain)
        let keyHandle = try trifle.generateKeyHandle()
        
        let mobileCertReq = try trifle.generateMobileCertificateRequest(keyHandle: keyHandle)
        
        let deviceCertificate = Certificate(version: 0, certificate: TestFixtures.deviceCertEncoded2)
        let rootCertificate = Certificate(version: 0, certificate: TestFixtures.rootCertEncoded)
        
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
        
        let deviceCertificate = Certificate(version: 0, certificate: TestFixtures.deviceCertEncoded)
        let otherRootCertificate = Certificate(version: 0, certificate: TestFixtures.otherRootCertEncoded)
        
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
