//
//  ContentSigner.swift
//  Trifle
//

import Foundation

/// Inherits `DigitalSignatureSigner` to add an additional
///  requirement of exporting the public key
protocol ContentSigner: DigitalSignatureSigner {
    var signingAlgorithm: SigningAlgorithm { get }

    /**
     Exports a `SigningPublicKey`
     
     - throws: an error type `CryptographicKeyError` when the
     operation fails if the key is not exportable
     - returns: the signing public key along with its key type
     */
    func exportPublicKey(_ tag: String) throws -> SigningPublicKey
}
