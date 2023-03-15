//
//  DigitalSignatureSigner.swift
//  Trifle
//

import Foundation

/// Signer to perform digital signature signing operations
public protocol DigitalSignatureSigner {
    /**
     Signs a blob of data with the underlying signing key.
     
     - throws: an error type `CryptographicKeyError` if the
        signing operation cannot be completed due to the underlying
        signing key being unavailable.
     - returns: the digital signature which includes the signature data
        and the algorithm used for signing
     */
    func sign(with data: Data) throws -> DigitalSignature
}
