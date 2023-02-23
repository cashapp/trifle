//
//  ASN1Null.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 Null with DER (Distingushed Encoding Rules) encodable
public struct ASN1Null: ASN1Type, DEREncodable {
    public typealias T = Any?

    // MARK: - Public Stored Properties

    public let tag: Octet
    public let length: [Octet]
    public let value: [Octet]

    // MARK: - Public Computed Properties
    
    public var octets: [Octet] {
        get {
            [tag] + length
        }
    }
    
    // MARK: - Initialization
    
    public init(_ type: Type = Type.none) throws {
        (self.tag, self.length, self.value) = try Self.encode(nil, .null(type))
    }

    // MARK: - Internal static methods (DEREncodable)

    internal static func encodeValue(_ rawValue: Any? = nil, _ tag: Tag) -> [Octet] {
        return []
    }
}
