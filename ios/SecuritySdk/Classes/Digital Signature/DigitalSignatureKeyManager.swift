//
//  DigitalSignatureKeyManager.swift
//  SecuritySdk
//

import Foundation

/// Key manager for digital signature keypairs
protocol DigitalSignatureKeyManager {
    associatedtype PrivateKey
    associatedtype PublicKey

    /**
     Fetches the signing key
     
     - throws: an error type `CryptographicKeyError`
     - returns: the private key for signing
     */
    func signingKey() throws -> PrivateKey

    /**
     Fetches the verifying key
     
     - throws: an error type `CryptographicKeyError`
     - returns: the public key for verifying
     */
    func verifyingKey() throws -> PublicKey
    
    /**
     Returns an external representation of the given key
     depending on its key type (pkcs#1 for RSA or ANSI X9.63 format for EC).
         
     - throws: an error type `CryptographicKeyError` when the operation
        fails if the key is not exportable, for example if it is bound to a smart card
        or to the Secure Enclave
     - returns: the encoded public key as bytes
     */
    func exportVerifyingKey() throws -> Data
}
