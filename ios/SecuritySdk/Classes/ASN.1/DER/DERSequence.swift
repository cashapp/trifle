//
//  DERSequence.swift
//  SecuritySdk
//

import Foundation

/// Sequence DER (Distingushed Encoding Rules) encodable
public struct DERSequence: ASN1Type, DEREncodable {
    public typealias T = [any ASN1Type]

    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    
    // MARK: - Initialization
    
    public init(_ rawValue: [any ASN1Type], _ type: Type = Type.none) throws {
        self.octets = try Self.encode(rawValue, .sequence(type))
        self.tag = octets.first!
    }
    
    internal static func encodeValue(_ rawValue: [any ASN1Type]) -> [Octet] {
        return rawValue.flatMap { $0.octets }
    }
}
