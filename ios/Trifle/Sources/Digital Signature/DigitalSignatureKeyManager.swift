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
     Fetches the signing key. If key does not exist, generate a new key.
     
     - throws: an error type `CryptographicKeyError`
     - returns: the private key for signing
     */
    func createOrLoadSigningKey() throws -> PrivateKey

    /**
     Fetches the verifying key
     
     - throws: an error type `CryptographicKeyError`
     - returns: the public key for verifying
     */
    func verifyingKey() throws -> PublicKey
}
