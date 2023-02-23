//
//  ASN1OctetString.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 OctetString with DER (Distingushed Encoding Rules) encodable
public struct ASN1OctetString: ASN1Type, DEREncodable {
    public typealias T = Data

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
    
    public init(_ rawValue: Data, _ type: Type = Type.none) throws {
        (self.tag, self.length, self.value) = try Self.encode(rawValue, .octetString(type))
    }
    
    // MARK: - Internal static methods (DEREncodable)

    internal static func encodeValue(_ rawValue: Data, _ tag: Tag) -> [Octet] {
        return [Octet](rawValue)
    }
}
