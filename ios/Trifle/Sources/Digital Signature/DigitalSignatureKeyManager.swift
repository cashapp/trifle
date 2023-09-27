//
//  DigitalSignatureKeyManager.swift
//  Trifle
//

import Foundation

/// Key manager for digital signature keypairs
protocol DigitalSignatureKeyManager {
    associatedtype PrivateKey
    associatedtype PublicKey

    static var keyInfo: KeyInfo { get }
    
    /**
     Fetches the signing key
     
     - throws: an error type `CryptographicKeyError`
     - returns: the private key for signing
     */
    func signingKey(_ tag: String, _ accessGroup: String?) throws -> PrivateKey

    /**
     Fetches the verifying key
     
     - throws: an error type `CryptographicKeyError`
     - returns: the public key for verifying
     */
    func verifyingKey(_ tag: String, _ accessGroup: String?) throws -> PublicKey
}
