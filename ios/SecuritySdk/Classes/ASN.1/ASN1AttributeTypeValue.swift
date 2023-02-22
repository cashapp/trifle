//
//  ASN1AttributeTypeValue.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 constructed Attribute Type Value type, which is a tuple of the
/// Object Identifer as the key and a `ASN1Type` as the value,
/// with DER (Distingushed Encoding Rules) encodable
struct ASN1AttributeTypeValue: ASN1Type {
    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    
    // MARK: - Initialization
    
    public init(
        _ oid: [UInt],
        _ value: ASN1Type,
        _ type: Type = Type.none
    ) throws {
        self.octets = try ASN1Sequence([
            try ASN1ObjectIdentifier(oid),
            value
        ]).octets
        self.tag = octets.first!
    }
}
