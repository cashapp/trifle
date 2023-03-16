//
//  PKCS10CertificateRequestTest.swift
//  Trifle_Tests
//

@testable import Trifle
import XCTest

final class PKCS10CertificationRequestTests: XCTestCase {
    func testDERPkcs10() throws {
        let manager = try SecureEnclaveDigitalSignatureKeyManager(
            reverseDomain: "app.cash.trifle.keys"
        )
        let tag = try manager.generateTag()
        
        let pkcs10CertReq = try PKCS10CertificationRequest.Builder()
            .addName(.commonName("cash.app"))
            .addName(.countryName("US"))
            .addName(.locality("San Francisco"))
            .addName(.stateOrProvinceName("California"))
            .addAttribute(
                ASN1Attribute(
                    [1, 2, 840, 113549, 1, 9, 7],
                    [try ASN1IA5String("helloworld")]
                )
            )
            .sign(for: tag, with: manager)
        
        XCTAssertNoThrow(try pkcs10CertReq.pem())
    }
}
