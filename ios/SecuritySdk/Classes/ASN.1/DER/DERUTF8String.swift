//
//  DERUTF8String.swift
//  SecuritySdk
//

import Foundation

/// UTF8String DER (Distingushed Encoding Rules) encodable
public struct DERUTF8String: ASN1Type, DEREncodable {
    public typealias T = String
    
    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]

    // MARK: - Initialization
    
    public init(_ rawValue: String, _ type: Type = Type.none) throws {
        self.octets = try Self.encode(rawValue, .utf8String(type))
        self.tag = octets.first!
    }
    
    internal static func encodeValue(_ rawValue: String) -> [Octet] {
        return Array(rawValue.utf8)
    }
}
