//
//  SecureEnclaveDigitalSignatureKeyManagerTests.swift
//  Trifle_Tests
//

import Trifle
import XCTest

final class SecureEnclaveDigitalSignatureKeyManagerTests: XCTestCase {
    lazy var manager = SecureEnclaveDigitalSignatureKeyManager(
        tag: "app.cash.trifle.s2dk.keys.digital_signature"
    )
    lazy var otherManager = SecureEnclaveDigitalSignatureKeyManager(
        tag: "com.squareup.trifle.s2dk.keys.digital_signature"
    )
    
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
