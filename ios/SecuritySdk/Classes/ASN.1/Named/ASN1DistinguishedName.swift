//
//  ASN1DistinguishedName.swift
//  SecuritySdk
//

import Foundation

/// Set of X.500 namespace where each enum entry is
/// unambiguously identified by distinguished name.
enum ASN1DistinguishedName {
    case commonName(_ value: String)
    case countryName(_ value: String)
    case locality(_ value: String)
    case stateOrProvinceName(_ value: String)
    case organizationName(_ value: String)
    case organizationUnitName(_ value: String)
    
    /**
     Returns the ASN.1 construction of the distinguished name
     
     - returns: the asn.1 DER encoded octets
     */
    func asn1() throws -> ASN1AttributeTypeValue {
        return try ASN1AttributeTypeValue(oid, value)
    }
    
    // MARK: -

    private var oid: [UInt] {
        get throws {
            switch self {
            case .commonName:
                /// https://oidref.com/2.5.4.3
                return [2, 5, 4, 3]
            case .countryName:
                /// https://oidref.com/2.5.4.6
                return [2, 5, 4, 6]
            case .locality:
                /// https://oidref.com/2.5.4.7
                return [2, 5, 4, 7]
            case .stateOrProvinceName:
                /// https://oidref.com/2.5.4.8
                return [2, 5, 4, 8]
            case .organizationName:
                /// https://oidref.com/2.5.4.10
                return [2, 5, 4, 10]
            case .organizationUnitName:
                /// https://oidref.com/2.5.4.11
                return [2, 5, 4, 11]
            }
        }
    }
    
    private var value: any ASN1Type {
        get throws {
            switch self {
            case let .commonName(value),
                let .countryName(value),
                let .locality(value),
                let .stateOrProvinceName(value),
                let .organizationName(value),
                let .organizationUnitName(value):
                return try ASN1PrintableString(value)
            }
        }
    }
}
