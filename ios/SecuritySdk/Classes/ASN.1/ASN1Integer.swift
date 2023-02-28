//
//  ASN1Integer.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 Integer with DER (Distingushed Encoding Rules) encodable
public struct ASN1Integer: ASN1Type, DEREncodable {
    typealias T = Int64

    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    public let priority: Int
    
    // MARK: - Initialization
    
    public init(_ rawValue: Int64, _ type: Type = Type.none) throws {
        (self.octets, self.priority) = try Self.encode(rawValue, .integer(type))
        self.tag = octets.first!
    }

    // MARK: - Internal static methods (DEREncodable)

    internal static func encodeValue(_ rawValue: Int64, _ tag: Tag) -> [Octet] {
        if (rawValue == 0) {
            return [0x00]
        }
        
        let result = base256(rawValue)
        let signum = rawValue.signum()
            
        if (signum == 1) {
            if (result[0] & 0x80 == 0x80) {
                return [0x00] + result
            }
        } else {
            if (result[0] & 0x80 == 0x00) {
                return [0xff] + result
            }
        }
        
        return result
    }
}
