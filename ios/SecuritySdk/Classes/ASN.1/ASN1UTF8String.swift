//
//  ASN1UTF8String.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 UTF8String with DER (Distingushed Encoding Rules) encodable
public struct ASN1UTF8String: ASN1Type, DEREncodable {
    public typealias T = String
    
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
    
    public init(_ rawValue: String, _ type: Type = Type.none) throws {
        (self.tag, self.length, self.value) = try Self.encode(rawValue, .utf8String(type))
    }
    
    // MARK: - Internal static methods (DEREncodable)

    internal static func encodeValue(_ rawValue: String, _ tag: Tag) -> [Octet] {
        return Array(rawValue.utf8)
    }
}
