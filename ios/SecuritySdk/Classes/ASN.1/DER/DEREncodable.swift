//
//  DEREncodable.swift
//  SecuritySdk
//

import Foundation

/// Type alias the triple to represent the tag, length, and value octets
typealias TagLengthValue = (Octet, [Octet], [Octet])

/// Base DER (Distingushed Encoding Rules) encodable that is conformed by a DER struct
protocol DEREncodable {
    associatedtype T

    /**
     Encodes the raw value to DER encoded value octets
     
     - parameter rawValue: the raw value to encode from
     - parameter tag: the tag that identifies the DER type
     - returns: the asn.1 DER encoded value octets
     */
    static func encodeValue(_ rawValue: T, _ tag: Tag) -> [Octet]
}

// MARK: -

extension DEREncodable {
    /**
     Encodes the raw value to DER encoded octets
     
     - parameter rawValue: the raw value to encode from
     - parameter tag: the tag that identifies the DER type
     - returns: the triple consisting of the tag, length, and value asn.1 DER encoded octets
     */
    static func encode(_ rawValue: T, _ tag: Tag) throws -> TagLengthValue {
        let value = encodeValue(rawValue, tag)
        let length = try encodeLength(value.count)
        
        switch tag.type {
        case .none:
            return (tag.value | tag.encodingForm, length, value)
        case .implicit(let specificTag):
            return (0x80 | (0x0f & specificTag) | tag.encodingForm, length, value)
        case .explicit(let specificTag):
            let innerContent = [tag.value | tag.encodingForm] + length + value
            return (
                0xa0 | (0x0f & specificTag),
                try encodeLength(innerContent.count),
                innerContent
            )
        }
    }
    
    // MARK: - Private Methods
    
    /**
     Encodes the length to DER encoded octets of either:
        (1) Short-form: 1 octet long
        (2) Long-form: 2...127 octets long
     
     - parameter length: the length to encode
     - returns: the asn.1 DER encoded length octets
     */
    private static func encodeLength(_ length: Int) throws -> [Octet] {
        if (length < 128) {
            return base128(UInt(length))
        }
        
        let result: [Octet] = base256(Int64(length))
        let count = result.count
        // sanity check that count is at most 126
        guard (count <= 126) else {
            throw DEREncodableError.unhandled("Exceeds definite length limits of 126 bytes")
        }
        
        return [Octet(count) | 0x80] + result
    }

    // MARK: - Internal Methods

    /**
     Encodes the value to DER encoded octets in base-128

     - parameter value: the value to encode
     - parameter highBit: optional sets high bits of the octet for certain encodings
     - returns: the asn.1 DER encoded octets in base-128
     */
    internal static func base128(_ value: UInt, _ highBit: Octet = 0x00) -> [Octet] {
        if (value == 0) {
            return [0x00]
        }

        var valCopy = value >> 7
        var result: [Octet] = [Octet(value & 0x7f)]

        while valCopy > 0 {
            result = [Octet(valCopy & 0x7f) | highBit] + result
            valCopy >>= 7
        }

        return result
    }

    /**
     Encodes the value to DER encoded octets in base-256

     - parameter value: the value to encode
     - returns: the asn.1 DER encoded octets in base-256
     */
    internal static func base256(_ value: Int64) -> [Octet] {
        if (value == 0) {
            return [0x00]
        }

        let numberOfBits = Double(Int64.bitWidth - abs(value).leadingZeroBitCount)
        let minimumOctets = Int(ceil(numberOfBits / Double(8)))
        
        var result: [Octet] = []
        var valCopy = value
        
        for _ in 1...minimumOctets {
            result = [Octet(valCopy & 0xff)] + result
            valCopy >>= 8
        }

        return result
    }
}

// MARK: -

extension Tag {
    /// ASN.1 DER encoding form
    var encodingForm: Octet {
        switch self {
        // constructed
        case .sequence, .set:
            return 0x20
        // primitive
        default:
            return 0x00
        }
    }
}

// MARK: -

public enum DEREncodableError: LocalizedError {
    case invalidInput(String)
    case unhandled(String)
    
    public var errorDescription: String? {
        switch self {
        case let .invalidInput(error),
            let .unhandled(error):
            return error
        }
    }
}
