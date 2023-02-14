//
//  DERObjectIdentifier.swift
//  SecuritySdk
//

import Foundation

/// ObjectIdentifier DER (Distingushed Encoding Rules) encodable
public struct DERObjectIdentifier: ASN1Type, DEREncodable {
    public typealias T = [UInt]

    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    
    // MARK: - Initialization
    
    public init(_ rawValue: [UInt], _ type: Type = Type.none) throws {
        guard rawValue.count >= 2 else {
            throw DEREncodableError.invalidInput(
                "Requires at least two components"
            )
        }
        guard (0...1).contains(rawValue[0]) else {
            throw DEREncodableError.invalidInput(
                "Out of Range - Component[0] must be within (0, 1)"
            )
        }
        guard (0...39).contains(rawValue[1]) else {
            throw DEREncodableError.invalidInput(
                "Out of Range - Component[1] must be within (0, 39)"
            )
        }
        
        self.octets = try Self.encode(rawValue, .objectIdentifier(type))
        self.tag = octets.first!
    }
    
    internal static func encodeValue(_ rawValue: [UInt]) -> [Octet] {
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
