//
//  DEREncodableTest.swift
//  Trifle_Tests
//

import Trifle
import XCTest

final class DEREncodableTest: XCTestCase {
    func testASN1BitString_encodesToDER() throws {
        let values = [
            try ASN1BitString(Data([0b01101110, 0b01011101, 0b11000000])),
            try ASN1BitString(Data([0b01101110, 0b01011101, 0b00000000])),
        ]
        let expectedValues: [[Octet]] = [
            [0x03, 0x04, 0x06, 0x6e, 0x5d, 0xc0],
            [0x03, 0x04, 0x00, 0x6e, 0x5d, 0x00]
        ]

        for index in 0...1 {
            XCTAssertEqual(values[index].octets, expectedValues[index])
        }
    }

    func testASN1Boolean_encodesToDER() throws {
        let values = [
            try ASN1Boolean(true),
            try ASN1Boolean(false),
        ]
        let expectedValues: [[Octet]] = [
            [0x01, 0x01, 0xff],
            [0x01, 0x01, 0x00],
        ]

        for index in 0...1 {
            XCTAssertEqual(values[index].octets, expectedValues[index])
        }
    }

    func testASN1Integer_encodesToDER() throws {
        let values = [
            try ASN1Integer(0),
            try ASN1Integer(127),
            try ASN1Integer(128),
            try ASN1Integer(256),
            try ASN1Integer(-128),
            try ASN1Integer(-129),
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

    func testASN1Null_encodesToDER() throws {
        let value = try ASN1Null()
        let expectedValue: [Octet] = [0x05, 0x00]

        XCTAssertEqual(value.octets, expectedValue)
    }

    func testASN1ObjectIdentifier_encodesToDER() throws {
        let value = try ASN1ObjectIdentifier([1, 2, 840, 113549, 1, 1, 11])
        let expectedValue: [Octet] = [
            0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01, 0x0b
        ]

        XCTAssertEqual(value.octets, expectedValue)
    }

    func testASN1Sequence_encodesToDER() throws {
        let value = try ASN1Sequence([
            try ASN1ObjectIdentifier([1, 2, 840, 113549, 1, 1, 11]),
            try ASN1Null()
        ])
        let expectedValue: [Octet] = [
            0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01, 0x0b, 0x05, 0x00
        ]

        XCTAssertEqual(value.octets, expectedValue)
    }

    func testASN1Set_encodesToDER() throws {
        let value = try ASN1Set([
            try ASN1ObjectIdentifier([1, 2, 840, 113549, 1, 1, 11]),
            try ASN1Null()
        ])
        let expectedValue: [Octet] = [
            0x31, 0x0d, 0x05, 0x00, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01, 0x0b
        ]

        XCTAssertEqual(value.octets, expectedValue)
    }
    
    func testASN1IA5String_encodesToDER() throws {
        let value = try ASN1IA5String("hi")
        let expectedValue: [Octet] = [0x16, 0x02, 0x68, 0x69]

        XCTAssertEqual(value.octets, expectedValue)
    }
    
    func testASN1OctetString_encodesToDER() throws {
        let value = try ASN1OctetString(Data([0x03, 0x02, 0x06, 0xa0]))
        let expectedValue: [Octet] = [0x04, 0x04, 0x03, 0x02, 0x06, 0xa0]

        XCTAssertEqual(value.octets, expectedValue)
    }
    
    func testASN1PrintableString_encodesToDER() throws {
        let value = try ASN1PrintableString("hi")
        let expectedValue: [Octet] = [0x13, 0x02, 0x68, 0x69]

        XCTAssertEqual(value.octets, expectedValue)
    }
    
    func testASN1UTCTime_encodesToDER() throws {
        // "2023-02-13T20:09:30Z"
        let value = try ASN1UTCTime(Date(timeIntervalSince1970: TimeInterval(1676318970)))
        // "230213200930Z in the format of yyMMddHHmmssZ UTC"
        let expectedValue: [Octet] = [0x17, 0x0d, 0x32, 0x33, 0x30, 0x32, 0x31, 0x33, 0x32, 0x30, 0x30, 0x39, 0x33, 0x30, 0x5a]

        XCTAssertEqual(value.octets, expectedValue)
    }
    
    func testASN1UTF8String_encodesToDER() throws {
        let value = try ASN1UTF8String("😎")
        let expectedValue: [Octet] = [0x0c, 0x04, 0xf0, 0x9f, 0x98, 0x8e]

        XCTAssertEqual(value.octets, expectedValue)
    }
    
    func testASN1PrintableStringFails_withIllegalCharacters() throws {
        XCTAssertThrowsError(try ASN1PrintableString("hi@me.com"))
    }
    
    func testASN1ObjectIdentifierFails_withSingleComponent() throws {
        XCTAssertThrowsError(try ASN1ObjectIdentifier([1]))
    }
    
    func testASN1ObjectIdentifierFails_withFirstComponentOutOfRange() throws {
        XCTAssertThrowsError(try ASN1ObjectIdentifier([3]))
    }
    
    func testASN1ObjectIdentifierFails_withSecondComponentOutOfRange() throws {
        XCTAssertThrowsError(try ASN1ObjectIdentifier([1, 40]))
    }
}
