//
//  SigningPublicKey.swift
//  Trifle
//

import Foundation

/// SigningPublicKey is an external wrapper that provides both the
///  key type used and the public key data
public struct SigningPublicKey {
    public let keyInfo: KeyInfo
    public let data: Data
    
    init(keyInfo: KeyInfo, data: Data) {
        self.keyInfo = keyInfo
        self.data = data
    }
}
