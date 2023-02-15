//
//  CryptographicKey.swift
//  SecuritySdk
//

import Foundation

protocol CryptographicKey {
    static var algorithm: SecKeyAlgorithm { get }
    
    /**
     Initializer that wraps an instance of `SecKey`
     
     - throws: an error type `CryptographicKeyError`
     */
    init(secKey: SecKey) throws
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
     - parameter signature: the signature to verify against
     - returns: true if the signature is verified, false otherwise
     */
    func verify(data: Data, with signature: Data) -> Bool
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
