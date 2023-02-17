//
//  ASN1UTF8String.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 UTF8String with DER (Distingushed Encoding Rules) encodable
public struct ASN1UTF8String: ASN1Type, DEREncodable {
    public typealias T = String
    
    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]

    // MARK: - Initialization
    
    public init(_ rawValue: String, _ type: Type = Type.none) throws {
        self.octets = try Self.encode(rawValue, .utf8String(type))
        self.tag = octets.first!
    }
    
    // MARK: - Internal static methods (DEREncodable)

    internal static func encodeValue(_ rawValue: String, _ tag: Tag) -> [Octet] {
        return Array(rawValue.utf8)
    }
}
