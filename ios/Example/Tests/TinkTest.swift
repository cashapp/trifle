//
//  TinkTest.swift
//  Trifle_Tests
//

import Foundation

@testable import Trifle
import XCTest

final class TinkTests: XCTestCase {

    func testgenerateMobileCertificateRequest() throws {
        let trifle = try Trifle(tag: "app.cash.trifle.keys.digital_signature")
        let mobileCertReq = try trifle.generateMobileCertificateRequest()

        XCTAssertEqual(mobileCertReq.version, 0)
        XCTAssertNotNil(mobileCertReq.pkcs10_request)
    }
}
