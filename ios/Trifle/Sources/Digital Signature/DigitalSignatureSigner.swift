//
//  DigitalSignatureSigner.swift
//  Trifle
//

import Foundation

/// Signer to perform digital signature signing operations
public protocol DigitalSignatureSigner {
    /**
     Generates a tag associated with a signing key pair and stores it to the keychain.
     Returns the associated tag value.
     
     - throws: an error type `CryptographicKeyError` when the
        operation fails if the key is not exportable
     - returns: tag value
     */
    func generateTag() throws -> String

    /**
     Signs a blob of data with the underlying signing key
     
     - throws: an error type `CryptographicKeyError` if the
        signing operation cannot be completed due to the underlying
        signing key being unavailable.
     - returns: the digital signature which includes the signature data
        and the algorithm used for signing
     */
    func sign(for tag: String, with data: Data) throws -> DigitalSignature
}
