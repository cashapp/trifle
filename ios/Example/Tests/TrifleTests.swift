//
//  TrifleTests.swift
//  Trifle_Tests
//

import Trifle
import XCTest
import Wire

final class TrifleTests: XCTestCase {

    let reverseDomain = "app.cash.trifle.keys"

    let deviceCertEncoded = Data(base64Encoded: """
MIIBFDCBvKADAgECAgYBhtoH23kwCgYIKoZIzj0EAwIwEzERMA8GA1\
UEAwwIY2FzaC5hcHAwHhcNMjMwMzEzMDgxMzEzWhcNMjMwOTA5MDgx\
MzEzWjARMQ8wDQYDVQQDDAZlbnRpdHkwWTATBgcqhkjOPQIBBggqhk\
jOPQMBBwNCAASGmNOZtRsOHr2XJVjL1jxRpeumY/jAF3rBDXhhbP49\
5VYvMYkHfWjeNBzJ1YIqXLQf4BHdQQzDjSvT+abzpdjEMAoGCCqGSM\
49BAMCA0cAMEQCIBXBwAm0Qn7S4vKRIHYLXDxhUQTqMuReoWBPfiOX\
fREnAiByNBjSwVxp980/Nl17UvkBUNhTvp55CNuwMmlnporNNA==
""")
    
    let deviceCertEncoded2 = Data(base64Encoded: """
MIIBGTCBwaADAgECAgYBht0eNMMwCgYIKoZIzj0EAwIwGDEWMBQGA1\
UEAwwNaXNzdWluZ0VudGl0eTAeFw0yMzAzMTMyMjM2MjlaFw0yMzA5\
MDkyMjM2MjlaMBExDzANBgNVBAMMBmVudGl0eTBZMBMGByqGSM49Ag\
EGCCqGSM49AwEHA0IABICead8cmQi2cyHHTx316w9Q64L11U86PV3R\
K1IDsm/xiDoa5sbShjFPm0nhd+AFoTPtsXL6SJ/bt+sndXQL5gcwCg\
YIKoZIzj0EAwIDRwAwRAIgBQLsaQZpa93v33J/kSIxcl2UtBPCyYYD\
KahIGLy7xM4CIGeiGFjglmmaiqFf30esHdL4yR0/rbkVm4h6z9O+Rjfp
""")
    
    let otherRootCertEncoded = Data(base64Encoded: """
MIHcMIGPoAMCAQICAQEwBQYDK2VwMBgxFjAUBgNVBAMMDWlzc3VpbmdFbnR\
pdHkwHhcNMjMwMzEzMDYxMzI3WhcNMjMwMzE0MDYxMzI3WjAYMRYwFAYDVQ\
QDDA1pc3N1aW5nRW50aXR5MCowBQYDK2VwAyEAm+Ac7932SHDPQLYd3p3gm\
grcArUWrBqhPEC+q/QI3lEwBQYDK2VwA0EAJrfzN7qA3VqwazsT8yMIYMvY\
Rz2iDA1898Yx5ELtlQcl7QUGXUmadwzW7rpxQB5wIk46tPTEJCFmUIYwrCB\
4BQ==
""")
    
    let rootCertEncoded = Data(base64Encoded: """
MIIBZTCCAQqgAwIBAgIBATAKBggqhkjOPQQDAjAYMRYwFAYDVQQDDA1pc3N\
1aW5nRW50aXR5MB4XDTIzMDMxMzIyMzUzMloXDTIzMDkwOTIyMzUzMlowGD\
EWMBQGA1UEAwwNaXNzdWluZ0VudGl0eTBZMBMGByqGSM49AgEGCCqGSM49A\
wEHA0IABICead8cmQi2cyHHTx316w9Q64L11U86PV3RK1IDsm/xiDoa5sbS\
hjFPm0nhd+AFoTPtsXL6SJ/bt+sndXQL5gejRTBDMA8GA1UdEwEB/wQFMAM\
BAf8wDgYDVR0PAQH/BAQDAgIEMCAGA1UdDgEB/wQWBBQ/80Y00UVTlI6kiA\
ZZ46kcrJ9a2jAKBggqhkjOPQQDAgNJADBGAiEAvffuwvImKNaolqnEr4ENB\
6LXEFdV0YVK3Ic3Mi+hqJ8CIQC7CiLwyvH1cChUReanIGeYiQp27LJ99M/q\
WLq6hmtSmQ==
""")
    
    let data = "hello world".data(using: .utf8)!

    func testInit() throws {
        let trifle = try Trifle(reverseDomain: reverseDomain)
        XCTAssertNotNil(trifle)
    }

    func testInitEmptyTag() throws {
        XCTAssertThrowsError(try Trifle(reverseDomain: ""), "Tag cannot be empty")
    }

    func testGetKeyHandle() throws {
        let trifle = try Trifle(reverseDomain: reverseDomain)
        let keyHandle = try trifle.generateKeyHandle()
                
        // serialize
        let encoder = JSONEncoder()
        let jsonData = try encoder.encode(keyHandle)
        XCTAssertEqual(String(data: jsonData, encoding: .utf8)!, "{\"tag\":\"" + keyHandle.tag + "\"}")

        // de-serialized
        let decoder = JSONDecoder()
        let decoded = try decoder.decode(KeyHandle.self, from: jsonData)
        XCTAssert(type(of: decoded) == type(of: keyHandle))
    }

    
    func testGenerateMobileCertificateRequest() throws {
        let trifle = try Trifle(reverseDomain: reverseDomain)
        let keyHandle = try trifle.generateKeyHandle()
        let mobileCertReq = try trifle.generateMobileCertificateRequest(keyHandle: keyHandle)
        
        XCTAssertEqual(mobileCertReq.version, 0)
        XCTAssertNotNil(mobileCertReq.pkcs10_request)

        // serialize
        let encoder = JSONEncoder()
        let jsonData = try encoder.encode(mobileCertReq)

        // de-serialized
        let decoder = JSONDecoder()
        let decodedmobileCertReq = try decoder.decode(MobileCertificateRequest.self, from: jsonData)


        XCTAssertEqual(decodedmobileCertReq.version, 0)
        XCTAssertNotNil(decodedmobileCertReq.pkcs10_request)
    }
    
    func testVerifyCertificate_succeeds() throws {
        let trifle = try Trifle(reverseDomain: reverseDomain)
        let keyHandle = try trifle.generateKeyHandle()
        
        let mobileCertReq = try trifle.generateMobileCertificateRequest(keyHandle: keyHandle)
        
        let deviceCertificate = Certificate(version: 0, certificate: deviceCertEncoded2)
        let rootCertificate = Certificate(version: 0, certificate: rootCertEncoded)
        
        let isVerified = deviceCertificate.verify(
            certificateRequest: mobileCertReq,
            intermediateChain: [],
            rootCertificate: rootCertificate
        )
        
        XCTAssertTrue(isVerified)
    }
    
    func testVerifyCertificate_fails() throws {
        let trifle = try Trifle(reverseDomain: reverseDomain)
        let keyHandle = try trifle.generateKeyHandle()
        
        let mobileCertReq = try trifle.generateMobileCertificateRequest(keyHandle: keyHandle)
        
        let deviceCertificate = Certificate(version: 0, certificate: deviceCertEncoded)
        let otherRootCertificate = Certificate(version: 0, certificate: otherRootCertEncoded)
        
        let isVerified = deviceCertificate.verify(
            certificateRequest: mobileCertReq,
            intermediateChain: [],
            rootCertificate: otherRootCertificate
        )
        
        XCTAssertFalse(isVerified)
    }
    
    func testSign_succeeds() throws {
        // NOTE: Right now this test passes because we are not validating
        // that the certificate contains the public key matching
        // the signing key
        
        let trifle = try Trifle(reverseDomain: reverseDomain)
        let keyHandle = try trifle.generateKeyHandle()

        let deviceCertificate = Certificate(version: 0, certificate: deviceCertEncoded2)
        let rootCertificate = Certificate(version: 0, certificate: rootCertEncoded)

        
        // cert chain of length 1
        let signDataWithoutRoot = try trifle.createSignedData(data: data,
                                     keyHandle: keyHandle,
                                     certificates: [deviceCertificate] )
        XCTAssertNotNil(signDataWithoutRoot, "This is TODO - once this is validated, this test should FAIL")

        
        let envData = try ProtoDecoder().decode(SignedData.EnvelopedData.self, from: signDataWithoutRoot.enveloped_data!)
        XCTAssertEqual(envData.version, 0)
        XCTAssertEqual(envData.data, data)
        XCTAssertEqual(envData.signing_algorithm, SignedData.Algorithm.ECDSA_SHA256)
        
        // cert chain of length 2
        let signDataWithRoot = try trifle.createSignedData(data: data,
                                    keyHandle: keyHandle,
                                    certificates: [deviceCertificate, rootCertificate])
        XCTAssertNotNil(signDataWithRoot, "This is TODO - once this is validated, this test should FAIL")
    }
    
    func testSignMultipleKeys_succeeds() throws {
        // TODO: Right now this test passes because we are not validating
        // that the certificate contains the public key matching
        // the signing key

        let trifle = try Trifle(reverseDomain: reverseDomain)
        let keyHandle1 = try trifle.generateKeyHandle()
        let keyHandle2 = try trifle.generateKeyHandle()

        let deviceCertificate = Certificate(version: 0, certificate: deviceCertEncoded)

        // Key 1
        let signData1 = try trifle.createSignedData(data: data,
                                    keyHandle: keyHandle1,
                                    certificates: [deviceCertificate] )
        XCTAssertNotNil(signData1, "This is TODO - once this is validated, this test should FAIL")
        
        // Key 2
        let signData2 = try trifle.createSignedData(data: data,
                                     keyHandle: keyHandle2,
                                     certificates: [deviceCertificate] )
        XCTAssertNotNil(signData2, "This is TODO - once this is validated, this test should FAIL")
    }
    
    func testSignMultipleKeysBadDomain_succeed() throws {
        
        let trifle1 = try Trifle(reverseDomain: reverseDomain)
        let keyHandle1 = try trifle1.generateKeyHandle()

        let trifle2 = try Trifle(reverseDomain: "app.square.trifle.keys")
        let keyHandle2 = try trifle2.generateKeyHandle()

        let deviceCertificate = Certificate(version: 0, certificate: deviceCertEncoded)

        // Trifle 1, Key 1
        let signData1 = try trifle1.createSignedData(data: data,
                                    keyHandle: keyHandle1,
                                    certificates: [deviceCertificate] )
        XCTAssertNotNil(signData1)
        
        // Trifle 1, Key 2
        let signData2 = try trifle1.createSignedData(data: data,
                                     keyHandle: keyHandle2,
                                     certificates: [deviceCertificate] )
        // TODO: This test should ideally fail
        // Right now this is passing because we are not cross validating the domains
        // of our SDK instance and the key handle
        XCTAssertNotNil(signData2, "This is TODO - once this is validated, this test should FAIL")
    }
        
    func testSignEmptyData_fail() throws {
        let trifle = try Trifle(reverseDomain: reverseDomain)
        let keyHandle = try trifle.generateKeyHandle()

        let deviceCertificate = Certificate(version: 0, certificate: deviceCertEncoded)

        XCTAssertThrowsError(try trifle.createSignedData(data: Data.init(),
                                                         keyHandle: keyHandle,
                                                         certificates: [deviceCertificate]),
                             "Data or Certifcate should not be empty.")
    }

    func testSignEmptyCert_fail() throws {
        let trifle = try Trifle(reverseDomain: reverseDomain)
        let keyHandle = try trifle.generateKeyHandle()

        XCTAssertThrowsError(try trifle.createSignedData(data: data,
                                                         keyHandle: keyHandle,
                                                         certificates: []),
                             "Data or Certifcate should not be empty.")
    }

    func testSignInvalidCertChain_fail() throws {
        let trifle = try Trifle(reverseDomain: reverseDomain)
        let keyHandle = try trifle.generateKeyHandle()

        let deviceCertificate = Certificate(version: 0, certificate: deviceCertEncoded)
        let otherRootCertificate = Certificate(version: 0, certificate: otherRootCertEncoded)

        XCTAssertThrowsError(try trifle.createSignedData(data: data,
                                                         keyHandle: keyHandle,
                                                         certificates: [deviceCertificate]+[otherRootCertificate]),
                             "Invalid certificate chain.")
    }
}
