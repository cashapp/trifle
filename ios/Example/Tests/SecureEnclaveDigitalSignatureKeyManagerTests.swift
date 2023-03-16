//
//  SecureEnclaveDigitalSignatureKeyManagerTests.swift
//  Trifle_Tests
//

import Trifle
import XCTest

final class SecureEnclaveDigitalSignatureKeyManagerTests: XCTestCase {
    var manager: SecureEnclaveDigitalSignatureKeyManager!
    
    override func setUpWithError() throws {
        manager = try SecureEnclaveDigitalSignatureKeyManager(
            reverseDomain: "app.cash.trifle.keys")
    }
    
    func testSignAndVerifySucceeds() throws {
        let data = "hello world".data(using: .utf8)!
        let tag = try manager.generateTag()

        let signature = try manager.sign(for: tag, with: data)

        let verified = try manager.verify(for: tag, data: data, with: signature.data)

        XCTAssertEqual(verified, true)
    }
    
    func testSignatureVerificationFails_whenVerifyingOtherData() throws {
        let data = "hello world".data(using: .utf8)!
        let otherData = "bye world".data(using: .utf8)!
        let tag = try manager.generateTag()

        let signature = try manager.sign(for: tag, with: data)

        let verified = try manager.verify(for: tag, data: otherData, with: signature.data)

        XCTAssertEqual(verified, false)
    }
    
    func testSignatureVerificationFails_whenVerifyingWithDifferentKey() throws {
        let data = "hello world".data(using: .utf8)!
        let tag = try manager.generateTag()
        let otherTag = try manager.generateTag()

        let signature = try manager.sign(for: tag, with: data)

        let verified = try manager.verify(for: otherTag, data: data, with: signature.data)

        XCTAssertEqual(verified, false)
    }
}
