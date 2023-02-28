//
//  ASN1IA5String.swift
//  Trifle
//

import Foundation

/// ASN.1 IA5String with DER (Distingushed Encoding Rules) encodable
public struct ASN1IA5String: ASN1Type, DEREncodable {
    typealias T = String

    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    public let priority: Int
    
    // MARK: - Initialization
    
    public init(_ rawValue: String, _ type: Type = Type.none) throws {
        (self.octets, self.priority) = try Self.encode(rawValue, .ia5String(type))
        self.tag = octets.first!
    }

    // MARK: - Internal static methods (DEREncodable)

    internal static func encodeValue(_ rawValue: String, _ tag: Tag) -> [Octet] {
        return rawValue.compactMap { $0.asciiValue }
    }
}
