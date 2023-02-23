//
//  ASN1Type.swift
//  SecuritySdk
//

import Foundation

public typealias Octet = UInt8

/// Base ASN.1 type
public protocol ASN1Type: Hashable {
    /// tag octet
    var tag: Octet { get }
    var length: [Octet] { get }
    var value: [Octet] { get }
    /// value-length-value octets
    var octets: [Octet] { get }
}

public extension ASN1Type {
    /// Allows ASN.1 type to conform as a `Hashable` for
    /// equatability by their value octets
    func hash(into hasher: inout Hasher) {
        hasher.combine(value)
    }
}

// MARK: - Type

public enum Type {
    case none
    case implicit(_ specificTag: Octet)
    case explicit(_ specificTag: Octet)
}

// MARK: - Tag

public enum Tag {
    case boolean(_ type: Type)
    case integer(_ type: Type)
    case bitString(_ type: Type, _ ignorePadding: Bool)
    case octetString(_ type: Type)
    case null(_ type: Type)
    case objectIdentifier(_ type: Type)
    case utf8String(_ type: Type)
    case sequence(_ type: Type)
    case set(_ type: Type)
    case printableString(_ type: Type)
    case ia5String(_ type: Type)
    case utcTime(_ type: Type)
    
    /// ASN.1 types
    var type: Type {
        get {
            switch self {
            case let .boolean(type),
                let .integer(type),
                let .bitString(type, _),
                let .octetString(type),
                let .null(type),
                let .objectIdentifier(type),
                let .utf8String(type),
                let .sequence(type),
                let .set(type),
                let .printableString(type),
                let .ia5String(type),
                let .utcTime(type):
                return type
            }
        }
    }
    
    /// ASN.1 tag value (non-encoding form)
    var value: Octet {
        get {
            switch self {
            case .boolean:
                return 0x01
            case .integer:
                return 0x02
            case .bitString:
                return 0x03
            case .octetString:
                return 0x04
            case .null:
                return 0x05
            case .objectIdentifier:
                return 0x06
            case .utf8String:
                return 0x0c
            case .sequence:
                return 0x10
            case .set:
                return 0x11
            case .printableString:
                return 0x13
            case .ia5String:
                return 0x16
            case .utcTime:
                return 0x17
            }
        }
    }
    
    var ignorePadding: Bool {
        get {
            switch self {
            case let .bitString(_, ignorePadding):
                return ignorePadding
            default:
                return false
            }
        }
    }
}
