//
//  SigningPublicKey.swift
//  SecuritySdk
//

import Foundation

/// SigningPublicKey is an external wrapper that provides both the
///  key type used and the public key data
public struct SigningPublicKey {
    public let keyType: KeyType
    public let data: Data
    
    init(keyType: KeyType, data: Data) {
        self.keyType = keyType
        self.data = data
    }
}
