//
//  EllipticCurve.swift
//  Trifle
//

import Foundation

/// The cryptographic curve algorithm
public enum EllipticCurve {
    /// Prime256v1 (256-bit prime field Weierstrass curve)
    case p256(_ kAttr: CFString, _ bitSize: Int)
    
    /// Returns a tuple of the SecKey key type attribute
    /// and key size (in bits)
    var attrs: (CFString, Int) {
        get {
            switch self {
            case let .p256(kAttr, bitSize):
                return (kAttr, bitSize)
            }
        }
    }
}
