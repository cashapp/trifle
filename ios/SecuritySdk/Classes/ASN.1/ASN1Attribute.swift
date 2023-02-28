//
//  ASN1Attribute.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 constructed Attribute type, which is a tuple of the
/// Object Identifer as the key and a collection of `ASN1Type`
/// as its value, with DER (Distingushed Encoding Rules) encodable
public struct ASN1Attribute: ASN1Type {
    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    public let priority: Int
    
    // MARK: - Initialization
    
    public init(
        _ oid: [UInt],
        _ attributeSetValue: Array<ASN1Type>,
        _ type: Type = Type.none
    ) throws {
        let seq = try ASN1Sequence([
            try ASN1ObjectIdentifier(oid),
            try ASN1Set(attributeSetValue)
        ], type)
        self.octets = seq.octets
        self.priority = seq.priority
        self.tag = octets.first!
    }
}
