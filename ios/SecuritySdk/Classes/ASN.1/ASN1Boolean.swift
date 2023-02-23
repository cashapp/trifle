//
//  DERBoolean.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 Boolean with DER (Distingushed Encoding Rules) encodable
public struct ASN1Boolean: ASN1Type, DEREncodable {
    public typealias T = Bool

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
    
    public init(_ rawValue: Bool, _ type: Type = Type.none) throws {
        (self.tag, self.length, self.value) = try Self.encode(rawValue, .boolean(type))
    }

    // MARK: - Internal static methods (DEREncodable)

    internal static func encodeValue(_ rawValue: Bool, _ tag: Tag) -> [Octet] {
        if (rawValue) {
            return [0xff]
        }
        return [0x00]
    }
}
