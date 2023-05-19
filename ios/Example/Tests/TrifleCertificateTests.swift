//
//  TrifleCertificateTests.swift
//  Trifle_Tests
//

import Foundation
import XCTest
@testable import Trifle

final class TrifleCertificateTests: XCTestCase {
    
    func testVerifyTrifleCertificateWithRedundantChain_succeeds() throws {
        let deviceCertificate = try TrifleCertificate.deserialize(data: TestFixtures.deviceTrifleCertEncoded3!)
        let rootCertificate = try TrifleCertificate.deserialize(data: TestFixtures.rootTrifleCertEncoded3!)

        let isVerified = try deviceCertificate.verify(
            certificateRequest: nil,
            intermediateTrifleChain: [deviceCertificate],
            rootTrifleCertificate: rootCertificate
        )
        
        XCTAssertTrue(isVerified)
    }
    
    func testVerifyTrifleCertificateWithRoot_succeeds() throws {
        let deviceCertificate = try TrifleCertificate.deserialize(data: TestFixtures.deviceTrifleCertEncoded3!)
        let rootCertificate = try TrifleCertificate.deserialize(data: TestFixtures.rootTrifleCertEncoded3!)

        let isVerified = try deviceCertificate.verify(
            certificateRequest: nil,
            intermediateTrifleChain: [],
            rootTrifleCertificate: rootCertificate
        )
        
        XCTAssertTrue(isVerified)
    }
    
    func testVerifyTrifleCertificateWithIntermediate_succeeds() throws {
        let deviceCertificate = try TrifleCertificate.deserialize(data: TestFixtures.deviceTrifleCertEncoded3!)
        let rootCertificate = try TrifleCertificate.deserialize(data: TestFixtures.rootTrifleCertEncoded3!)

        let isVerified = try deviceCertificate.verify(
            certificateRequest: nil,
            intermediateTrifleChain: [rootCertificate],
            rootTrifleCertificate: nil
        )
        
        XCTAssertTrue(isVerified)
    }
    
    func testSerializeTrifleCertificate_succeeds() throws {
        let cert = try TrifleCertificate.deserialize(data: TestFixtures.deviceTrifleCertEncoded3!)

        let certData = try cert.serialize()
        
        let decoded_cert = try TrifleCertificate.deserialize(data: certData)
        
        // verify equality of decoded certificate and initial deviceCertificate
        XCTAssertTrue(cert == decoded_cert)
    }
}
