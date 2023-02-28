//
//  ASN1OctetString.swift
//  Trifle
//

import Foundation

/// ASN.1 OctetString with DER (Distingushed Encoding Rules) encodable
public struct ASN1OctetString: ASN1Type, DEREncodable {
    typealias T = Data

    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    public let priority: Int
    
    // MARK: - Initialization
    
    public init(_ rawValue: Data, _ type: Type = Type.none) throws {
        (self.octets, self.priority) = try Self.encode(rawValue, .octetString(type))
        self.tag = octets.first!
    }
    
    // MARK: - Internal static methods (DEREncodable)

    internal static func encodeValue(_ rawValue: Data, _ tag: Tag) -> [Octet] {
        return [Octet](rawValue)
    }
}
