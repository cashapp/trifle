//
//  SecureEnclaveDigitalSignatureKeyManager.swift
//  SecuritySdk
//

import Foundation

public class SecureEnclaveDigitalSignatureKeyManager: DigitalSignatureKeyManager {
    
    // MARK: - Internal Properties
    
    internal typealias PrivateKey = SecureEnclaveSigningKey
    internal typealias PublicKey = SecureEnclaveVerifyingKey
    
    // MARK: - Private Properties
    
    private let tag: String
    
    // MARK: - Initialization
    
    public init(tag: String) {
        self.tag = tag
    }

    // MARK: - Public Methods (DigitalSignatureKeyManager)
    
    public func signingKey() throws -> SecureEnclaveSigningKey {
        guard try keyExists() else {
            return try SecureEnclaveSigningKey(secKey: generateKeypair())
        }
        
        var keyRef: CFTypeRef?
        let preparedQuery = SecureEnclaveKeychainQueries.getQuery(with: tag, returnRef: true)
        SecItemCopyMatching(preparedQuery, &keyRef)
        
        return try SecureEnclaveSigningKey(secKey: keyRef as! SecKey)
    }
    
    public func verifyingKey() throws -> SecureEnclaveVerifyingKey {
        let signingKey = try signingKey()

        guard let publicKey = SecKeyCopyPublicKey(signingKey.privateKey) else {
            throw CryptographicKeyError.unavailablePublicKey
        }

        return try SecureEnclaveVerifyingKey(secKey: publicKey)
    }
    
    // MARK: - Private Methods
    
    private func generateKeypair() throws -> SecKey {
        let attributes = try SecureEnclaveKeychainQueries.attributes(with: tag)
        
        var error: Unmanaged<CFError>?
        guard let keypair = SecKeyCreateRandomKey(attributes, &error) else {
            throw AccessControlError.invalidAccess(error?.takeRetainedValue() as? Error)
        }

        return keypair
    }
    
    private func keyExists() throws -> Bool {
        let preparedQuery = SecureEnclaveKeychainQueries.getQuery(with: tag)
        let status = SecItemCopyMatching(preparedQuery, nil)
        switch status {
        case errSecSuccess:
            return true
        case errSecItemNotFound:
            return false
        default:
            throw KeychainAccessError.unhandled(with: status, and: tag)
        }
    }
}

// MARK: -

extension KeychainAccessError {
    static func unhandled(with status: OSStatus, and tag: String) -> KeychainAccessError {
        return .init(status: status, tag: tag)
    }
}
