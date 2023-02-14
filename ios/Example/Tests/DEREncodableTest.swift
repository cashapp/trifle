//
//  DEREncodableTest.swift
//  SecuritySdk_Tests
//

import SecuritySdk
import XCTest

final class DEREncodableTest: XCTestCase {
    func testDERBitString() throws {
        let values = [
            try DERBitString(Data([0b01101110, 0b01011101, 0b11000000])),
            try DERBitString(Data([0b01101110, 0b01011101, 0b00000000])),
        ]
        let expectedValues: [[Octet]] = [
            [0x03, 0x04, 0x06, 0x6e, 0x5d, 0xc0],
            [0x03, 0x04, 0x00, 0x6e, 0x5d, 0x00]
        ]

        for index in 0...1 {
            XCTAssertEqual(values[index].octets, expectedValues[index])
        }
    }

    func testDERBoolean() throws {
        let values = [
            try DERBoolean(true),
            try DERBoolean(false),
        ]
        let expectedValues: [[Octet]] = [
            [0x01, 0x01, 0xff],
            [0x01, 0x01, 0x00],
        ]

        for index in 0...1 {
            XCTAssertEqual(values[index].octets, expectedValues[index])
        }
    }

    func testDERInteger() throws {
        let values = [
            try DERInteger(0),
            try DERInteger(127),
            try DERInteger(128),
            try DERInteger(256),
            try DERInteger(-128),
            try DERInteger(-129),
        ]
        let expectedValues: [[Octet]] = [
            [0x02, 0x01, 0x00],
            [0x02, 0x01, 0x7f],
            [0x02, 0x02, 0x00, 0x80],
            [0x02, 0x02, 0x01, 0x00],
            [0x02, 0x01, 0x80],
            [0x02, 0x02, 0xff, 0x7f],
        ]

        for index in 0...5 {
            XCTAssertEqual(values[index].octets, expectedValues[index])
        }
    }

    func testDERNull() throws {
        let value = try DERNull()
        let expectedValue: [Octet] = [0x05, 0x00]

        XCTAssertEqual(value.octets, expectedValue)
    }

    func testDERObjectIdentifier() throws {
        let value = try DERObjectIdentifier([1, 2, 840, 113549, 1, 1, 11])
        let expectedValue: [Octet] = [
            0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01, 0x0b
        ]

        XCTAssertEqual(value.octets, expectedValue)
    }

    func testDERSequence() throws {
        let value = try DERSequence([
            try DERObjectIdentifier([1, 2, 840, 113549, 1, 1, 11]),
            try DERNull()
        ])
        let expectedValue: [Octet] = [
            0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01, 0x0b, 0x05, 0x00
        ]

        XCTAssertEqual(value.octets, expectedValue)
    }

    func testDERSet() throws {
        let value = try DERSet([
            try DERObjectIdentifier([1, 2, 840, 113549, 1, 1, 11]),
            try DERNull()
        ])
        let expectedValue: [Octet] = [
            0x31, 0x0d, 0x05, 0x00, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01, 0x0b
        ]

        XCTAssertEqual(value.octets, expectedValue)
    }
    
    func testDERIA5String() throws {
        let value = try DERIA5String("hi")
        let expectedValue: [Octet] = [0x16, 0x02, 0x68, 0x69]

        XCTAssertEqual(value.octets, expectedValue)
    }
    
    func testDEROctetString() throws {
        let value = try DEROctetString(Data([0x03, 0x02, 0x06, 0xa0]))
        let expectedValue: [Octet] = [0x04, 0x04, 0x03, 0x02, 0x06, 0xa0]

        XCTAssertEqual(value.octets, expectedValue)
    }
    
    func testDERPrintableString() throws {
        let value = try DERPrintableString("hi")
        let expectedValue: [Octet] = [0x13, 0x02, 0x68, 0x69]

        XCTAssertEqual(value.octets, expectedValue)
    }
    
    func testDERUTCTime() throws {
        // "2023-02-13T20:09:30Z"
        let value = try DERUTCTime(Date(timeIntervalSince1970: TimeInterval(1676318970)))
        let expectedValue: [Octet] = [0x17, 0x0d, 0x31, 0x36, 0x37, 0x36, 0x33, 0x31, 0x38, 0x39, 0x37, 0x30, 0x2e, 0x30, 0x5a]

        XCTAssertEqual(value.octets, expectedValue)
    }
    
    func testDERUTF8String() throws {
        let value = try DERUTF8String("😎")
        let expectedValue: [Octet] = [0x0c, 0x04, 0xf0, 0x9f, 0x98, 0x8e]

        XCTAssertEqual(value.octets, expectedValue)
    }
    
    func testDERPrintableStringFails_withIllegalCharacters() throws {
        XCTAssertThrowsError(try DERPrintableString("hi@me.com"))
    }
    
    func testDERObjectIdentifierFails_withSingleComponent() throws {
        XCTAssertThrowsError(try DERObjectIdentifier([1]))
    }
    
    func testDERObjectIdentifierFails_withFirstComponentOutOfRange() throws {
        XCTAssertThrowsError(try DERObjectIdentifier([3]))
    }
    
    func testDERObjectIdentifierFails_withSecondComponentOutOfRange() throws {
        XCTAssertThrowsError(try DERObjectIdentifier([1, 40]))
    }
}
