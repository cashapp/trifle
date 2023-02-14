//
//  SecureEnclaveDigitalSignatureKeyManagerTests.swift
//  SecuritySdk_Tests
//

import XCTest
import SecuritySdk

final class SecureEnclaveDigitalSignatureKeyManagerTests: XCTestCase {
    lazy var manager = SecureEnclaveDigitalSignatureKeyManager(
        tag: "app.cash.trifle.s2dk.keys.digital_signature"
    )
    lazy var otherManager = SecureEnclaveDigitalSignatureKeyManager(
        tag: "com.squareup.trifle.s2dk.keys.digital_signature"
    )
    
    func testSignAndVerifySucceeds() throws {
        let data = "hello world".data(using: .utf8)!

        let signingKey = try manager.signingKey()
        let signature = try signingKey.sign(with: data)

        let verifyingKey = try manager.verifyingKey()
        let verified = verifyingKey.verify(data: data, with: signature)

        XCTAssertEqual(verified, true)
    }
    
    func testExportVerifyingKeyDataSucceeds() throws {
        XCTAssertNoThrow(try manager.verifyingKey().exportAsData())
    }
    
    func testSignatureVerificationFails_whenVerifyingOtherData() throws {
        let data = "hello world".data(using: .utf8)!
        let otherData = "bye world".data(using: .utf8)!

        let signingKey = try manager.signingKey()
        let signature = try signingKey.sign(with: data)

        let verifyingKey = try manager.verifyingKey()
        let verified = verifyingKey.verify(data: otherData, with: signature)

        XCTAssertEqual(verified, false)
    }
    
    func testSignatureVerificationFails_whenVerifyingWithDifferentKey() throws {
        let data = "hello world".data(using: .utf8)!

        let signingKey = try manager.signingKey()
        let signature = try signingKey.sign(with: data)

        let otherVerifyingKey = try otherManager.verifyingKey()
        let verified = otherVerifyingKey.verify(data: data, with: signature)

        XCTAssertEqual(verified, false)
    }
}
