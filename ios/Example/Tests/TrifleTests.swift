//
//  TrifleTests.swift
//  Trifle_Tests
//

import Trifle
import XCTest

final class TrifleTests: XCTestCase {

    let tag = "app.cash.trifle.s2dk.keys.digital_signature"

   
    func testInit() throws {
        let trifle = try Trifle(tag: tag)
        XCTAssertNotNil(trifle)
    }

    func testInitEmptyTag() throws {
        XCTAssertThrowsError(try Trifle(tag: ""), "Tag cannot be empty")
    }

    func testGetKeyHandle() throws {
        let trifle = try Trifle(tag: tag)
        let keyHandle = try trifle.getKeyHandle()
                
        // serialize
        let encoder = JSONEncoder()
        let jsonData = try encoder.encode(keyHandle)
        XCTAssertEqual(String(data: jsonData, encoding: .utf8)!, "{\"tag\":\"" + tag + "\"}")

        // de-serialized
        let decoder = JSONDecoder()
        let decoded = try decoder.decode(KeyHandle.self, from: jsonData)
        XCTAssert(type(of: decoded) == type(of: keyHandle))
    }

    
    func testGenerateMobileCertificateRequest() throws {
        let trifle = try Trifle(tag: tag)
        let mobileCertReq = try trifle.generateMobileCertificateRequest()
        
        XCTAssertEqual(mobileCertReq.version, 0)
        XCTAssertNotNil(mobileCertReq.pkcs10_request)
    }
    
    func testVerifyCertificate_succeeds() throws {
        let trifle = try Trifle(tag: tag)
        let mobileCertReq = try trifle.generateMobileCertificateRequest()

        let deviceCertEncoded = Data(base64Encoded: """
MIIBGTCBwaADAgECAgYBht0eNMMwCgYIKoZIzj0EAwIwGDEWMBQGA1UEAwwNaXNzdWluZ0VudGl0eTAeFw0yMzAzMTMyMjM2MjlaFw0yMzA5MDkyMjM2MjlaMBExDzANBgNVBAMMBmVudGl0eTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABICead8cmQi2cyHHTx316w9Q64L11U86PV3RK1IDsm/xiDoa5sbShjFPm0nhd+AFoTPtsXL6SJ/bt+sndXQL5gcwCgYIKoZIzj0EAwIDRwAwRAIgBQLsaQZpa93v33J/kSIxcl2UtBPCyYYDKahIGLy7xM4CIGeiGFjglmmaiqFf30esHdL4yR0/rbkVm4h6z9O+Rjfp
""")!
        
        let rootCertEncoded = Data(base64Encoded: """
MIIBZTCCAQqgAwIBAgIBATAKBggqhkjOPQQDAjAYMRYwFAYDVQQDDA1pc3N1aW5nRW50aXR5MB4XDTIzMDMxMzIyMzUzMloXDTIzMDkwOTIyMzUzMlowGDEWMBQGA1UEAwwNaXNzdWluZ0VudGl0eTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABICead8cmQi2cyHHTx316w9Q64L11U86PV3RK1IDsm/xiDoa5sbShjFPm0nhd+AFoTPtsXL6SJ/bt+sndXQL5gejRTBDMA8GA1UdEwEB/wQFMAMBAf8wDgYDVR0PAQH/BAQDAgIEMCAGA1UdDgEB/wQWBBQ/80Y00UVTlI6kiAZZ46kcrJ9a2jAKBggqhkjOPQQDAgNJADBGAiEAvffuwvImKNaolqnEr4ENB6LXEFdV0YVK3Ic3Mi+hqJ8CIQC7CiLwyvH1cChUReanIGeYiQp27LJ99M/qWLq6hmtSmQ==
""")!
        
        let deviceCertificate = Certificate(version: 0, certificate: deviceCertEncoded)
        let rootCertificate = Certificate(version: 0, certificate: rootCertEncoded)
        let isVerified = deviceCertificate.verify(
            certificateRequest: mobileCertReq,
            intermediateChain: [],
            rootCertificate: rootCertificate
        )
        
        XCTAssertTrue(isVerified)
    }
    
    func testVerifyCertificate_fails() throws {
        let trifle = try Trifle(tag: tag)
        let mobileCertReq = try trifle.generateMobileCertificateRequest()

        let deviceCertEncoded = Data(base64Encoded: """
MIIBFDCBvKADAgECAgYBhtoH23kwCgYIKoZIzj0EAwIwEzERMA8GA1\
UEAwwIY2FzaC5hcHAwHhcNMjMwMzEzMDgxMzEzWhcNMjMwOTA5MDgx\
MzEzWjARMQ8wDQYDVQQDDAZlbnRpdHkwWTATBgcqhkjOPQIBBggqhk\
jOPQMBBwNCAASGmNOZtRsOHr2XJVjL1jxRpeumY/jAF3rBDXhhbP49\
5VYvMYkHfWjeNBzJ1YIqXLQf4BHdQQzDjSvT+abzpdjEMAoGCCqGSM\
49BAMCA0cAMEQCIBXBwAm0Qn7S4vKRIHYLXDxhUQTqMuReoWBPfiOX\
fREnAiByNBjSwVxp980/Nl17UvkBUNhTvp55CNuwMmlnporNNA==
""")!

        let otherRootCertEncoded = Data(base64Encoded: """
MIHcMIGPoAMCAQICAQEwBQYDK2VwMBgxFjAUBgNVBAMMDWlzc3VpbmdFbnR\
pdHkwHhcNMjMwMzEzMDYxMzI3WhcNMjMwMzE0MDYxMzI3WjAYMRYwFAYDVQ\
QDDA1pc3N1aW5nRW50aXR5MCowBQYDK2VwAyEAm+Ac7932SHDPQLYd3p3gm\
grcArUWrBqhPEC+q/QI3lEwBQYDK2VwA0EAJrfzN7qA3VqwazsT8yMIYMvY\
Rz2iDA1898Yx5ELtlQcl7QUGXUmadwzW7rpxQB5wIk46tPTEJCFmUIYwrCB\
4BQ==
""")
        
        let deviceCertificate = Certificate(version: 0, certificate: deviceCertEncoded)
        let otherRootCertificate = Certificate(version: 0, certificate: otherRootCertEncoded)
        let isVerified = deviceCertificate.verify(
            certificateRequest: mobileCertReq,
            intermediateChain: [],
            rootCertificate: otherRootCertificate
        )
        
        XCTAssertFalse(isVerified)
    }
}
