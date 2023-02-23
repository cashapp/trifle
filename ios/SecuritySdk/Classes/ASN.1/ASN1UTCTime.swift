//
//  ASN1UTCTime.swift
//  SecuritySdk
//

import Foundation

/// ASN.1 UTCTime with DER (Distingushed Encoding Rules) encodable
public struct ASN1UTCTime: ASN1Type, DEREncodable {
    public typealias T = Date
        
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
    
    public init(_ rawValue: Date, _ type: Type = Type.none) throws {
        (self.tag, self.length, self.value) = try Self.encode(rawValue, .utcTime(type))
    }

    // MARK: - Internal static methods (DEREncodable)

    internal static func encodeValue(_ rawValue: Date, _ tag: Tag) -> [Octet] {
        let dateFormatter = DateFormatter()
        dateFormatter.timeZone = TimeZone(abbreviation: "UTC")
        dateFormatter.dateFormat = "yyMMddHHmmss'Z'"
        return dateFormatter.string(for: rawValue)!.compactMap { $0.asciiValue }
    }
}
