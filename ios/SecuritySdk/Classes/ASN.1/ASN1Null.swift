//
//  ASN1Null.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 Null with DER (Distingushed Encoding Rules) encodable
public struct ASN1Null: ASN1Type, DEREncodable {
    public typealias T = Any?

    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    
    // MARK: - Initialization
    
    public init(_ type: Type = Type.none) throws {
        self.octets = try Self.encode(nil, .null(type))
        self.tag = octets.first!
    }

    // MARK: - Internal static methods (DEREncodable)

    internal static func encodeValue(_ rawValue: Any? = nil) -> [Octet] {
        return []
    }
}
