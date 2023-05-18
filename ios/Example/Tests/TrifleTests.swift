//
//  TrifleTests.swift
//  Trifle_Tests
//

import Trifle
import XCTest
import Wire

final class TrifleTests: XCTestCase {


    func testInit() throws {
        let trifle = try Trifle(reverseDomain: TestFixtures.reverseDomain)
        XCTAssertNotNil(trifle)
    }

    func testInitEmptyTag() throws {
        XCTAssertThrowsError(try Trifle(reverseDomain: ""), "Tag cannot be empty")
    }

    func testGetKeyHandle() throws {
        let trifle = try Trifle(reverseDomain: TestFixtures.reverseDomain)
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
        let trifle = try Trifle(reverseDomain: TestFixtures.reverseDomain)
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
    
    func testSign_succeeds() throws {
        // NOTE: Right now this test passes because we are not validating
        // that the certificate contains the public key matching
        // the signing key
        
        let trifle = try Trifle(reverseDomain: TestFixtures.reverseDomain)
        let keyHandle = try trifle.generateKeyHandle()

        let deviceCertificate = try TrifleCertificate(data: TestFixtures.deviceTrifleCertEncoded2!)
        let rootCertificate = try TrifleCertificate(data: TestFixtures.rootTrifleCertEncoded!)
        
        // cert chain of length 1
        let signDataWithoutRoot = try trifle.createSignedData(data: TestFixtures.data,
                                     keyHandle: keyHandle,
                                     certificates: [deviceCertificate] )
        XCTAssertNotNil(signDataWithoutRoot, "This is TODO - once this is validated, this test should FAIL")

        
        let envData = try ProtoDecoder().decode(SignedData.EnvelopedData.self, from: signDataWithoutRoot.enveloped_data!)
        XCTAssertEqual(envData.version, 0)
        XCTAssertEqual(envData.data, TestFixtures.data)
        XCTAssertEqual(envData.signing_algorithm, SignedData.Algorithm.ECDSA_SHA256)
        
        // cert chain of length 2
        let signDataWithRoot = try trifle.createSignedData(data: TestFixtures.data,
                                    keyHandle: keyHandle,
                                    certificates: [deviceCertificate, rootCertificate])
        XCTAssertNotNil(signDataWithRoot, "This is TODO - once this is validated, this test should FAIL")
    }
    
    func testSignMultipleKeys_succeeds() throws {
        // TODO: Right now this test passes because we are not validating
        // that the certificate contains the public key matching
        // the signing key

        let trifle = try Trifle(reverseDomain: TestFixtures.reverseDomain)
        let keyHandle1 = try trifle.generateKeyHandle()
        let keyHandle2 = try trifle.generateKeyHandle()

        let deviceCertificate = try TrifleCertificate(data: TestFixtures.deviceTrifleCertEncoded!)

        // Key 1
        let signData1 = try trifle.createSignedData(data: TestFixtures.data,
                                    keyHandle: keyHandle1,
                                    certificates: [deviceCertificate] )
        XCTAssertNotNil(signData1, "This is TODO - once this is validated, this test should FAIL")
        
        // Key 2
        let signData2 = try trifle.createSignedData(data: TestFixtures.data,
                                     keyHandle: keyHandle2,
                                     certificates: [deviceCertificate] )
        XCTAssertNotNil(signData2, "This is TODO - once this is validated, this test should FAIL")
    }
    
    func testSignMultipleKeysBadDomain_succeed() throws {
        
        let trifle1 = try Trifle(reverseDomain: TestFixtures.reverseDomain)
        let keyHandle1 = try trifle1.generateKeyHandle()

        let trifle2 = try Trifle(reverseDomain: "app.square.trifle.keys")
        let keyHandle2 = try trifle2.generateKeyHandle()

        let deviceCertificate = try TrifleCertificate(data: TestFixtures.deviceTrifleCertEncoded!)

        // Trifle 1, Key 1
        let signData1 = try trifle1.createSignedData(data: TestFixtures.data,
                                    keyHandle: keyHandle1,
                                    certificates: [deviceCertificate] )
        XCTAssertNotNil(signData1)
        
        // Trifle 1, Key 2
        let signData2 = try trifle1.createSignedData(data: TestFixtures.data,
                                     keyHandle: keyHandle2,
                                     certificates: [deviceCertificate] )
        // TODO: This test should ideally fail
        // Right now this is passing because we are not cross validating the domains
        // of our SDK instance and the key handle
        XCTAssertNotNil(signData2, "This is TODO - once this is validated, this test should FAIL")
    }
        
    func testSignEmptyData_fail() throws {
        let trifle = try Trifle(reverseDomain: TestFixtures.reverseDomain)
        let keyHandle = try trifle.generateKeyHandle()

        let deviceCertificate = try TrifleCertificate(data: TestFixtures.deviceTrifleCertEncoded!)

        XCTAssertThrowsError(try trifle.createSignedData(data: Data.init(),
                                                         keyHandle: keyHandle,
                                                         certificates: [deviceCertificate]),
                             "Data or Certifcate should not be empty.")
    }

    func testSignEmptyCert_fail() throws {
        let trifle = try Trifle(reverseDomain: TestFixtures.reverseDomain)
        let keyHandle = try trifle.generateKeyHandle()

        XCTAssertThrowsError(try trifle.createSignedData(data: TestFixtures.data,
                                                         keyHandle: keyHandle,
                                                         certificates: []),
                             "Data or Certificate should not be empty.")
    }

    func testSignInvalidCertChain_fail() throws {
        let trifle = try Trifle(reverseDomain: TestFixtures.reverseDomain)
        let keyHandle = try trifle.generateKeyHandle()

        let deviceCertificate = try TrifleCertificate(data: TestFixtures.deviceTrifleCertEncoded!)
        let otherRootCertificate = try TrifleCertificate(data: TestFixtures.otherRootTrifleCertEncoded!)

        XCTAssertThrowsError(try trifle.createSignedData(data: TestFixtures.data,
                                                         keyHandle: keyHandle,
                                                         certificates: [deviceCertificate]+[otherRootCertificate]),
                             "Invalid certificate chain.")
    }
}
