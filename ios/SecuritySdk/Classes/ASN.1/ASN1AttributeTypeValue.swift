//
//  ASN1AttributeTypeValue.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 constructed Attribute Type Value type, which is a tuple of the
/// Object Identifer as the key and a `ASN1Type` as the value,
/// with DER (Distingushed Encoding Rules) encodable
public struct ASN1AttributeTypeValue: ASN1Type {
    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    public let priority: Int
    
    // MARK: - Initialization
    
    public init(
        _ oid: [UInt],
        _ value: ASN1Type,
        _ type: Type = Type.none
    ) throws {
        let seq = try ASN1Sequence([
            try ASN1ObjectIdentifier(oid),
            value
        ], type)
        self.octets = seq.octets
        self.priority = seq.priority
        self.tag = octets.first!
    }
}
