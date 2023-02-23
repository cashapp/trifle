//
//  ASN1AttributeTypeValue.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 constructed Attribute Type Value type, which is a tuple of the
/// Object Identifer as the key and a `ASN1Type` as the value,
/// with DER (Distingushed Encoding Rules) encodable
struct ASN1AttributeTypeValue: ASN1Type {
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
    
    public init(
        _ oid: [UInt],
        _ value: any ASN1Type,
        _ type: Type = Type.none
    ) throws {
        let asn1 = try ASN1Sequence([
            try ASN1ObjectIdentifier(oid),
            value
        ], type)
        
        self.tag = asn1.tag
        self.length = asn1.length
        self.value = asn1.value
    }
}
