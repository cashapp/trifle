//
//  DERSet.swift
//  SecuritySdk
//

import Foundation

/// Set DER (Distingushed Encoding Rules) encodable
public struct DERSet: ASN1Type, DEREncodable {
    public typealias T = [any ASN1Type]

    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    
    // MARK: - Initialization
    
    public init(_ rawValue: [any ASN1Type], _ type: Type = Type.none) throws {
        self.octets = try Self.encode(rawValue, .set(type))
        self.tag = octets.first!
    }
    
    internal static func encodeValue(_ rawValue: [any ASN1Type]) -> [Octet] {
        return rawValue.sorted(by: { $0.tag < $1.tag }).flatMap { $0.octets }
    }
}
