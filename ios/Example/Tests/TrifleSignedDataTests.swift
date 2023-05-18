//
//  TrifleSignedDataTests.swift
//  Trifle_Tests
//

import Foundation
import XCTest
@testable import Trifle

final class TrifleSignedDataTests: XCTestCase {
 
    func testSerializeTrifleSignedData_succeeds() throws {
        
        let trifle = try Trifle(reverseDomain: TestFixtures.reverseDomain)
        let keyHandle = try trifle.generateKeyHandle()

        let cert = try TrifleCertificate.deserialize(data: TestFixtures.deviceTrifleCertEncoded2!)
        
        let trifleSignedData = try trifle.createSignedData(data: TestFixtures.data,
                                     keyHandle: keyHandle,
                                     certificates: [cert] )
        XCTAssertNotNil(trifleSignedData, "This is TODO - once this is validated, this test should FAIL")

        let encodedTrifleSignedData = try trifleSignedData.serialize()
        
        let decodedTrifleSignedData = try TrifleSignedData.deserialize(data: encodedTrifleSignedData)
        
        // verify equality of decoded and initial signed data object
        XCTAssertTrue(trifleSignedData == decodedTrifleSignedData)
    }

}
