//
//  DigitalSignature.swift
//  Trifle
//

import Foundation

/// Digital Signature is an external wrapper that provides both the
///  signing curve algorithm used for signing and the signature data
public struct DigitalSignature {
    public let signingAlgorithm: SigningAlgorithm
    public let data: Data
    
    init(signingAlgorithm: SigningAlgorithm, data: Data) {
        self.signingAlgorithm = signingAlgorithm
        self.data = data
    }
}
