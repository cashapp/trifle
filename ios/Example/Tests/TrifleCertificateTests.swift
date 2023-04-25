//
//  TrifleCertificateTests.swift
//  Trifle_Tests
//

import Foundation
import XCTest
import Trifle

final class TrifleCertificateTests: XCTestCase {
    
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
        
        // https://www.sslshopper.com/certificate-decoder.html
        // cert valid to April 22, 2023
        let expiredCert = Data(base64Encoded: """
MIIBEzCBuqADAgECAgYBh6Zjz8EwCgYIKoZIzj0EAwIwETEPMA0GA1UEAwwGZW50aXR5MB4XDTIzMDQyMjAwMzYxMVoXDTIzMDQyMzAwMzYxMVowETEPMA0GA1UEAxMGZW50aXR5MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgIE1g6XHlFG0xegJJuHOlLCMolUJomxSFOrZzlF++MPBV+9y+CwiIczKhtEIMhoa3VJus3Vt9+JTmAGpT54HZwMVDAKBggqhkjOPQQDAgNIADBFAiBVs3NLjvIS+WpH9l424rNIGe6gWMSqoSX70qxfP5MwAQIhAPb4T+lNsNk9LgIgOhlcTeG6pqQrkTZ4Z+s2fGl9wJIf
""")
        // Valid To: October 14, 2028
        let validCert = Data(base64Encoded: """
MIIBEjCBuqADAgECAgYBh7WhKSUwCgYIKoZIzj0EAwIwETEPMA0GA1UEAwwGZW50aXR5MB4XDTIzMDQyNDIzMzczMFoXDTI4MTAxNDIzMzczMFowETEPMA0GA1UEAxMGZW50aXR5MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgIE1g6XHlFG0xegJJuHOlLCMolUJomxSFOrZzlF++MPBV+9y+CwiIczKhtEIMhoa3VJus3Vt9+JTmAGpT54HZwMVDAKBggqhkjOPQQDAgNHADBEAiBI/myZDONM/aqwI9ie69rxhzwKX6bZ/8SG20v6LkLvtQIgeBtZOpN/Gx0Gkf5dijVLZHT2TNMFbYDXK9k7EH1yRw0=
""")
        
        let validCertificate = Certificate(version: 0, certificate: validCert)
        let expiredCertificate = Certificate(version: 0, certificate: expiredCert)

        XCTAssertTrue(try validCertificate.verify(intermediateChain: Array<Certificate>()))
    
        XCTAssertThrowsError(try expiredCertificate.verify(intermediateChain: Array<Certificate>()),
                             "Certificate is expired.")
    }
    
}
