//
//  ASN1ObjectIdentifier.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 ObjectIdentifier with DER (Distingushed Encoding Rules) encodable
public struct ASN1ObjectIdentifier: ASN1Type, DEREncodable {
    public typealias T = [UInt]

    // MARK: - Public Stored Properties

    public let tag: Octet
    public let length: [Octet]
    public let value: [Octet]

    // MARK: - Public Computed Properties
    
    public var octets: [Octet] {
        get {
            [tag] + length + value
        }
    }
    
    // MARK: - Initialization
    
    public init(_ rawValue: [UInt], _ type: Type = Type.none) throws {
        guard rawValue.count >= 2 else {
            throw DEREncodableError.invalidInput(
                "Requires at least two components"
            )
        }
        /**
         - 0: ITU-T
         - 1: ISO
         - 2: joint-iso-itu-t
            are the roo nodet identifiers of the OID tree
         */
        guard (0...2).contains(rawValue[0]) else {
            throw DEREncodableError.invalidInput(
                "Out of Range - Component[0] must be within (0, 2)"
            )
        }
        guard (0...39).contains(rawValue[1]) else {
            throw DEREncodableError.invalidInput(
                "Out of Range - Component[1] must be within (0, 39)"
            )
        }
        
        (self.tag, self.length, self.value) = try Self.encode(rawValue, .objectIdentifier(type))
    }

    // MARK: - Internal static methods (DEREncodable)

    internal static func encodeValue(_ rawValue: [UInt], _ tag: Tag) -> [Octet] {
        var result: [Octet] = [Octet(40 * rawValue[0] + rawValue[1])]
        
        if (rawValue.count == 2) {
            return result
        }
        
        for component in rawValue[2...] {
            result += base128(component, 0x80)
        }

        return result
    }
}
