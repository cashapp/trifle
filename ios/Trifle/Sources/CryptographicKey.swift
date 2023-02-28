//
//  CryptographicKey.swift
//  Trifle
//

import Foundation

protocol CryptographicKey {
    /**
     Initializer that wraps an instance of `SecKey` and `SecKeyAlgorithm`
     
     - throws: an error type `CryptographicKeyError`
     */
    init(_ secKey: SecKey, _ algorithm: SecKeyAlgorithm) throws
}

/// Signing key of the digital signature keypair
protocol SigningKey: CryptographicKey {
    var privateKey: SecKey { get }

    /**
     Signs the provided data using the private key
     
     - parameter data: the data to sign
     - throws: an error type `CryptographicKeyError`
     - returns: the digital signature
     */
    func sign(with data: Data) throws -> Data
}

/// Verifying key of the digital signature keypair
protocol VerifyingKey: CryptographicKey {
    var publicKey: SecKey { get }

    /**
     Verifies the signature using the public key with the provided data
     
     - parameter data: the data to verify
     - parameter signature: the signature data  to verify against
     - returns: true if the signature is verified, false otherwise
     */
    func verify(data: Data, with signature: Data) -> Bool
    
    /**
     Returns an external representation of the given key
     depending on its key type (pkcs#1 for RSA or ANSI X9.63 format for EC).
         
     - throws: an error type `CryptographicKeyError` when the operation
        fails if the key is not exportable, for example if it is bound to a smart card
        or to the Secure Enclave
     - returns: the encoded public key as bytes
     */
    func export() throws -> Data
}

// MARK: -

public enum CryptographicKeyError: LocalizedError {
    case unavailablePublicKey
    case unexportablePublicKey
    case unsupportedAlgorithm
    case unhandled(Error)
    
    public var errorDescription: String? {
        switch self {
        case .unavailablePublicKey:
            return "Public key is unavailable."
        case .unexportablePublicKey:
            return "Public key cannot be exported."
        case .unsupportedAlgorithm:
            return "Invalid algorithm used."
        case let .unhandled(error):
            return error.localizedDescription
        }
    }
}
