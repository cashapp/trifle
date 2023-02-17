//
//  KeyInfo.swift
//  SecuritySdk
//

import Foundation

/// Represents the key type and algorithm used for the construction of the managed key
public enum KeyInfo {
    /// Elliptic Curve Cryptographic Key that carries the context
    /// of the curve and signing algorithm used.
    case ecKey(
        _ curve: EllipticCurve,
        _ signingAlgorithm: SigningAlgorithm
    )
    
    /// The `EllipticCurve` type of the `KeyInfo` instance.
    var curve: EllipticCurve {
        get {
            switch self {
            case let .ecKey(curve, _):
                return curve
            }
        }
    }
    
    /// The `SigningAlgorithm` of the `KeyInfo` instance.
    var signingAlgorithm: SigningAlgorithm {
        get {
            switch self {
            case let .ecKey(_, signingAlgorithm):
                return signingAlgorithm
            }
        }
    }
}
