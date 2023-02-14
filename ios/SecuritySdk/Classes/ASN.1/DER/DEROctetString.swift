//
//  DEROctetString.swift
//  SecuritySdk
//

import Foundation

/// OctetString DER (Distingushed Encoding Rules) encodable
public struct DEROctetString: ASN1Type, DEREncodable {
    public typealias T = Data

    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    
    // MARK: - Initialization
    
    public init(_ rawValue: Data, _ type: Type = Type.none) throws {
        self.octets = try Self.encode(rawValue, .octetString(type))
        self.tag = octets.first!
    }
    
    internal static func encodeValue(_ rawValue: Data) -> [Octet] {
        return [Octet](rawValue)
    }
}
