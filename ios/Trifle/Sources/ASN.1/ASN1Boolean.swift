//
//  DERBoolean.swift
//  Trifle
//

import Foundation

/// ASN.1 Boolean with DER (Distingushed Encoding Rules) encodable
public struct ASN1Boolean: ASN1Type, DEREncodable {
    typealias T = Bool

    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    public let priority: Int
    
    // MARK: - Initialization
    
    public init(_ rawValue: Bool, _ type: Type = Type.none) throws {
        (self.octets, self.priority) = try Self.encode(rawValue, .boolean(type))
        self.tag = octets.first!
    }

    // MARK: - Internal static methods (DEREncodable)

    internal static func encodeValue(_ rawValue: Bool, _ tag: Tag) -> [Octet] {
        if (rawValue) {
            return [0xff]
        }
        return [0x00]
    }
}
