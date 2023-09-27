//
//  DigitalSignatureSigner.swift
//  Trifle
//

import Foundation

/// Signer to perform digital signature signing operations
public protocol DigitalSignatureSigner {
    /**
     Generates a tag associated with a signing key pair and stores it to the keychain for the specified access group.
     Returns the associated tag value.
     
     - throws: an error type `CryptographicKeyError` when the
        operation fails if the key is not exportable
     - returns: tag value
     */
    func generateTag(_ accessGroup: String?) throws -> String

    /**
     Signs a blob of data with the underlying signing key for the specified access group.
     
     - throws: an error type `CryptographicKeyError` if the
        signing operation cannot be completed due to the underlying
        signing key being unavailable.
     - returns: the digital signature which includes the signature data
        and the algorithm used for signing
     */
    func sign(for tag: String, with data: Data, _ accessGroup: String?) throws -> DigitalSignature
}
