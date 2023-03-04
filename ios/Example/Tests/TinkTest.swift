//
//  TinkTest.swift
//  Trifle_Tests
//

import Foundation

@testable import Trifle
import XCTest

final class TinkTests: XCTestCase {
    lazy var trifle = Trifle(tag: "app.cash.trifle.keys.digital_signature")

    func testgenerateMobileCertificateRequest() throws {
        let mobileCertReq = try trifle.generateMobileCertificateRequest()

        XCTAssertEqual(mobileCertReq.version, 0)
        XCTAssertNotNil(mobileCertReq.pkcs10_request)
    }
}
