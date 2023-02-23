//
//  ASN1PrintableString.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 PrintableString with DER (Distingushed Encoding Rules) encodable
public struct ASN1PrintableString: ASN1Type, DEREncodable {
    public typealias T = String

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
    
    public init(_ rawValue: String, _ type: Type = Type.none) throws {
        guard rawValue.range(
            of: "^[a-zA-Z0-9'()+,-./:=? ]*$",
            options: .regularExpression,
            range: nil,
            locale: nil
        ) != nil else {
            throw DEREncodableError.invalidInput("Illegal character(s) present")
        }

        (self.tag, self.length, self.value) = try Self.encode(rawValue, .printableString(type))
    }

    // MARK: - Internal static methods (DEREncodable)

    internal static func encodeValue(_ rawValue: String, _ tag: Tag) -> [Octet] {
        return rawValue.compactMap { $0.asciiValue }
    }
}
