//
//  SecureEnclaveDigitalSignatureKeyManagerTests.swift
//  Trifle_Tests
//

import Trifle
import XCTest

final class SecureEnclaveDigitalSignatureKeyManagerTests: XCTestCase {
    var manager: SecureEnclaveDigitalSignatureKeyManager!
    var otherManager: SecureEnclaveDigitalSignatureKeyManager!
    
    override func setUpWithError() throws {
        manager = try SecureEnclaveDigitalSignatureKeyManager(
            tag: "app.cash.trifle.s2dk.keys.digital_signature")
        otherManager = try SecureEnclaveDigitalSignatureKeyManager(
            tag: "com.squareup.trifle.s2dk.keys.digital_signature")
    }
    
    func testSignAndVerifySucceeds() throws {
        let data = "hello world".data(using: .utf8)!

        let signature = try manager.sign(with: data)

        let verified = try manager.verify(data: data, with: signature.data)

        XCTAssertEqual(verified, true)
    }
    
    func testSignatureVerificationFails_whenVerifyingOtherData() throws {
        let data = "hello world".data(using: .utf8)!
        let otherData = "bye world".data(using: .utf8)!

        let signature = try manager.sign(with: data)

        let verified = try manager.verify(data: otherData, with: signature.data)

        XCTAssertEqual(verified, false)
    }
    
    func testSignatureVerificationFails_whenVerifyingWithDifferentKey() throws {
        let data = "hello world".data(using: .utf8)!

        let signature = try manager.sign(with: data)

        let verified = try otherManager.verify(data: data, with: signature.data)

        XCTAssertEqual(verified, false)
    }
}
