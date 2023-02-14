//
//  DERNull.swift
//  SecuritySdk
//

import Foundation

/// Null DER (Distingushed Encoding Rules) encodable
public struct DERNull: ASN1Type, DEREncodable {
    public typealias T = Any?

    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    
    // MARK: - Initialization
    
    public init(_ type: Type = Type.none) throws {
        self.octets = try Self.encode(nil, .null(type))
        self.tag = octets.first!
    }
    
    internal static func encodeValue(_ rawValue: Any? = nil) -> [Octet] {
        return []
    }
}
