//
//  ASN1Attribute.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 constructed Attribute type, which is a tuple of the
/// Object Identifer as the key and a collection of `ASN1Type`
/// as its value, with DER (Distingushed Encoding Rules) encodable
struct ASN1Attribute: ASN1Type {
    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    
    // MARK: - Initialization
    
    public init(
        _ oid: [UInt],
        _ attributeSetValue: Array<ASN1Type>,
        _ type: Type = Type.none
    ) throws {
        self.octets = try ASN1Sequence([
            try ASN1ObjectIdentifier(oid),
            try ASN1Set(attributeSetValue)
        ]).octets
        self.tag = octets.first!
    }
}
