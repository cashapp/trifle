//
//  SigningAlgorithm.swift
//  SecuritySdk
//

import Foundation

/// The signing algorithm
public enum SigningAlgorithm {
    /// Elliptic Curve Digital Signature Algorithm (DSA) coupled
    /// with the Secure Hash Algorithm 256 (SHA256) algorithm
    case ecdsaSha256(_ keyAlgorithm: SecKeyAlgorithm)
    
    /// Signing algorithm used for the specific curve type
    var attrs: SecKeyAlgorithm {
        get {
            switch self {
            case let .ecdsaSha256(keyAlgorithm):
                return keyAlgorithm
            }
        }
    }
}
