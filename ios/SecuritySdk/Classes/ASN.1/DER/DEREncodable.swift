//
//  DEREncodable.swift
//  SecuritySdk
//

import Foundation

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
     - returns: the asn.1 DER encoded octets along with its sort priority
     */
    static func encode(_ rawValue: T, _ tag: Tag) throws -> ([Octet], Int) {
        let value = encodeValue(rawValue, tag)
        let length = try encodeLength(value.count)
        let content = length + value
        let priority: Int
        switch tag {
        case .set, .sequence:
            priority = computePriority(rawValue)
        default:
            priority = computePriority(value)
        }
        
        switch tag.type {
        case .none:
            return ([tag.value | tag.encodingForm] + content, priority)
        case .implicit(let specificTag):
            return ([0x80 | (0x0f & specificTag) | tag.encodingForm] + content, priority)
        case .explicit(let specificTag):
            let innerContent = [tag.value | tag.encodingForm] + content

            return ([0xa0 | (0x0f & specificTag)]
            + (try encodeLength(innerContent.count))
            + innerContent, priority)
        }
    }
    
    // MARK: - Private Methods

    /**
     Computes the priority of ASN.1 primitve types
     
     - parameter encodedValue: DER encoded value octets
     - returns: the priority value which is the summation of the value octets
     */
    private static func computePriority(_ encodedValue: [Octet]) -> Int {
        encodedValue.reduce(0, { acc, nextVal in acc + Int(nextVal) })
    }

    /**
     Computes the priority of ASN.1 constructed types which expects that the raw value
     is of type `[any ASN1Type]`
     
     - parameter rawValue: a collection of `ASN1Type`
     - returns: the priority value which is the summation of the value octets
     */
    private static func computePriority(_ rawValue: T) -> Int {
        return (rawValue as! [any ASN1Type]).reduce(
            0, { acc, next in acc + next.priority}
        )
    }

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
