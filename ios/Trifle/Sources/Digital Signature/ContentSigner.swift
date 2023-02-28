//
//  ContentSigner.swift
//  Trifle
//

import Foundation

/// Inherits `DigitalSignatureSigner` to add an additional
///  requirement of exporting the public key
protocol ContentSigner: DigitalSignatureSigner {
    /**
     Exports a `SigningPublicKey`
     
     - throws: an error type `CryptographicKeyError` when the
        operation fails if the key is not exportable
     - returns: the signing public key along with its key type
     */
    func exportPublicKey() throws -> SigningPublicKey
}
