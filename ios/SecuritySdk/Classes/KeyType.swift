//
//  KeyType.swift
//  SecuritySdk
//

import Foundation

/// Represents the key type and algorithm used for the construction of the managed key
public enum KeyType {
    /// Elliptic Curve Cryptographic key type
    case ecKey(_ curve: EllipticCurve, _ signingAlgorithm: SigningAlgorithm)
    
    var curve: EllipticCurve {
        get {
            switch self {
            case let .ecKey(curve, _):
                return curve
            }
        }
    }
    
    var signingAlgorithm: SigningAlgorithm {
        get {
            switch self {
            case let .ecKey(_, signingAlgorithm):
                return signingAlgorithm
            }
        }
    }
}
