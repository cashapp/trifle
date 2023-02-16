//
//  DERBitString.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 BitString with DER (Distingushed Encoding Rules) encodable
public struct ASN1BitString: ASN1Type, DEREncodable {
    public typealias T = Data

    // MARK: - Public Properties

    public let tag: Octet
    public let octets: [Octet]
    
    // MARK: - Initialization
    
    public init(_ rawValue: Data, _ type: Type = Type.none) throws {
        self.octets = try Self.encode(rawValue, .bitString(type))
        self.tag = octets.first!
    }

    // MARK: - Internal static methods (DEREncodable)

    internal static func encodeValue(_ rawValue: Data) -> [Octet] {
        let value = [Octet](rawValue)
        
        let leastSignificantOctet = value.last!
        let unusedBits: Octet
        if (leastSignificantOctet.trailingZeroBitCount < Octet.bitWidth) {
            unusedBits = Octet(leastSignificantOctet.trailingZeroBitCount)
        } else {
            unusedBits = Octet(0x00)
        }
        
        return [unusedBits] + value
    }
}
