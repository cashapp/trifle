//
//  ASN1Sequence.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 Sequence with DER (Distingushed Encoding Rules) encodable
public struct ASN1Sequence: ASN1Type, DEREncodable {
    public typealias T = [any ASN1Type]

    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    
    // MARK: - Initialization
    
    public init(_ rawValue: [any ASN1Type], _ type: Type = Type.none) throws {
        self.octets = try Self.encode(rawValue, .sequence(type))
        self.tag = octets.first!
    }

    // MARK: - Internal static methods (DEREncodable)

    internal static func encodeValue(_ rawValue: [any ASN1Type], _ tag: Tag) -> [Octet] {
        return rawValue.flatMap { $0.octets }
    }
}
