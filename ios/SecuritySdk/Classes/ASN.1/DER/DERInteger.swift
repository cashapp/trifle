//
//  DERInteger.swift
//  SecuritySdk
//

import Foundation

/// Integer DER (Distingushed Encoding Rules) encodable
public struct DERInteger: ASN1Type, DEREncodable {
    public typealias T = Int64

    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    
    // MARK: - Initialization
    
    public init(_ rawValue: Int64, _ type: Type = Type.none) throws {
        self.octets = try Self.encode(rawValue, .integer(type))
        self.tag = octets.first!
    }
    
    internal static func encodeValue(_ rawValue: Int64) -> [Octet] {
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
