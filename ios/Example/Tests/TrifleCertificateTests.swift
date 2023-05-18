//
//  TrifleCertificateTests.swift
//  Trifle_Tests
//

import Foundation
import XCTest
import Trifle

final class TrifleCertificateTests: XCTestCase {
    
    func testVerifyTrifleCertificateWithRoot_succeeds() throws {
        let deviceCertificate = try TrifleCertificate(data: TestFixtures.deviceTrifleCertEncoded3!)
        let rootCertificate = try TrifleCertificate(data: TestFixtures.rootTrifleCertEncoded3!)

        let isVerified = try deviceCertificate.verify(
            certificateRequest: nil,
            intermediateTrifleChain: [],
            rootTrifleCertificate: rootCertificate
        )
        
        XCTAssertTrue(isVerified)
    }
    
    func testVerifyTrifleCertificateWithIntermediate_succeeds() throws {
        let deviceCertificate = try TrifleCertificate(data: TestFixtures.deviceTrifleCertEncoded3!)
        let rootCertificate = try TrifleCertificate(data: TestFixtures.rootTrifleCertEncoded3!)

        let isVerified = try deviceCertificate.verify(
            certificateRequest: nil,
            intermediateTrifleChain: [rootCertificate],
            rootTrifleCertificate: nil
        )
        
        XCTAssertTrue(isVerified)
    }
    
    func testSerializeTrifleCertificate_succeeds() throws {
        let deviceCertificate = try TrifleCertificate(data: TestFixtures.deviceTrifleCertEncoded3!)

        // serialize
        let encoder = JSONEncoder()
        let jsonData = try encoder.encode(deviceCertificate)

        // de-serialized
        let decoder = JSONDecoder()
        let decoded = try decoder.decode(TrifleCertificate.self, from: jsonData)
        
        // verify the decoded certificate
        let isVerified = try decoded.verify(
            certificateRequest: nil,
            intermediateTrifleChain: [],
            rootTrifleCertificate: nil
        )
        XCTAssertTrue(isVerified)
    }
}
