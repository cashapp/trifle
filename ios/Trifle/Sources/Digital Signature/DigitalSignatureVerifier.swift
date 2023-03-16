//
//  DigitalSignatureVerifier.swift
//  Trifle
//

import Foundation

/// Verifier to perform digital signature verification operations
public protocol DigitalSignatureVerifier {
    /**
     Verifies a blob of data with a supplied signature using the underlying verifying key
     
     - throws: an error type `CryptographicKeyError` if the
        verifying operation cannot be completed due to the underlying
        verifying key being unavailable.
     - returns: true if the signature is verified, false otherwise
     */
    func verify(for tag: String, data: Data, with signature: Data) throws -> Bool
}
