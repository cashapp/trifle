//
//  DERBitString.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 BitString with DER (Distingushed Encoding Rules) encodable
public struct ASN1BitString: ASN1Type, DEREncodable {
    public typealias T = Data

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
    
    public init(_ rawValue: Data, _ type: Type = Type.none, ignorePadding: Bool = false) throws {
        (self.tag, self.length, self.value) = try Self.encode(rawValue, .bitString(type, ignorePadding))
    }

    // MARK: - Internal static methods (DEREncodable)

    internal static func encodeValue(_ rawValue: Data, _ tag: Tag) -> [Octet] {
        let value = [Octet](rawValue)
        
        let leastSignificantOctet = value.last!
        let unusedBits: Octet
        if (tag.ignorePadding ||
            leastSignificantOctet.trailingZeroBitCount == Octet.bitWidth) {
            unusedBits = Octet(0x00)
        } else {
            unusedBits = Octet(leastSignificantOctet.trailingZeroBitCount)
        }
        
        return [unusedBits] + value
    }
}
