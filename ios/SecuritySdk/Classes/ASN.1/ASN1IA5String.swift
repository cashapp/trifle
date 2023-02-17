//
//  ASN1IA5String.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 IA5String with DER (Distingushed Encoding Rules) encodable
public struct ASN1IA5String: ASN1Type, DEREncodable {
    public typealias T = String

    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    
    // MARK: - Initialization
    
    public init(_ rawValue: String, _ type: Type = Type.none) throws {
        self.octets = try Self.encode(rawValue, .ia5String(type))
        self.tag = octets.first!
    }

    // MARK: - Internal static methods (DEREncodable)

    internal static func encodeValue(_ rawValue: String) -> [Octet] {
        return rawValue.compactMap { $0.asciiValue }
    }
}
