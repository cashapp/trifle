//
//  PKCS10CertificateRequestTest.swift
//  SecuritySdk_Tests
//

@testable import SecuritySdk
import XCTest

final class PKCS10CertificationRequestTests: XCTestCase {
    func testDERPkcs10() throws {
        let manager = SecureEnclaveDigitalSignatureKeyManager(
            tag: "app.cash.trifle.keys.digital_signature.pkcs10"
        )
        
        let pkcs10CertReq = try PKCS10CertificationRequest.Builder()
            .addName(.countryName("US"))
            .addName(.commonName("cash.app"))
            .addName(.stateOrProvinceName("California"))
            .addName(.locality("San Francisco"))
            .addAttribute(
                ASN1Attribute(
                    [1, 2, 840, 113549, 1, 9, 7],
                    [try ASN1IA5String("helloworld")]
                )
            )
            .sign(with: manager)
        
        print(try pkcs10CertReq.pem())
    }
}
