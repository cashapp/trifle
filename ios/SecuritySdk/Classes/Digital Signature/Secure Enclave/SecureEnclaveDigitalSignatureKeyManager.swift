//
//  SecureEnclaveDigitalSignatureKeyManager.swift
//  SecuritySdk
//

import Foundation

public class SecureEnclaveDigitalSignatureKeyManager
: DigitalSignatureKeyManager, ContentSigner, DigitalSignatureVerifier {
    
    // MARK: - Internal Properties

    internal typealias PrivateKey = SecureEnclaveSigningKey
    internal typealias PublicKey = SecureEnclaveVerifyingKey

    internal static let keyInfo = KeyInfo.ecKey(
        EllipticCurve.p256(kSecAttrKeyTypeECSECPrimeRandom, 256),
        SigningAlgorithm.ecdsaSha256(.ecdsaSignatureMessageX962SHA256)
    )

    // MARK: - Private Properties

    private let tag: String

    // MARK: - Initialization

    public init(tag: String) {
        self.tag = tag
    }
 
    // MARK: - Public Methods (DigitalSignatureSigner)

    public func sign(with data: Data) throws -> DigitalSignature {
        let signature = try signingKey().sign(with: data)
        return DigitalSignature(
            signingAlgorithm: Self.keyInfo.signingAlgorithm,
            data: signature
        )
    }
 
    // MARK: - Public Methods (DigitalSignatureVerifier)

    public func verify(data: Data, with signature: Data) throws -> Bool {
        return try verifyingKey().verify(data: data, with: signature)
    }
    
    // MARK: - Internal Methods (ContentSigner)
 
    internal func exportPublicKey() throws -> SigningPublicKey {
        return SigningPublicKey(
            keyInfo: Self.keyInfo,
            data: try verifyingKey().export()
        )
    }

    // MARK: - Internal Methods (DigitalSignatureKeyManager)

    internal func signingKey() throws -> SecureEnclaveSigningKey {
        let signingAlgorithm = Self.keyInfo.signingAlgorithm.attrs

        guard try keyExists() else {
            return try SecureEnclaveSigningKey(generateKeypair(), signingAlgorithm)
        }
        
        var keyRef: CFTypeRef?
        let preparedQuery = SecureEnclaveKeychainQueries.getQuery(with: tag, returnRef: true)
        SecItemCopyMatching(preparedQuery, &keyRef)
        
        return try SecureEnclaveSigningKey(keyRef as! SecKey, signingAlgorithm)
    }
    
    internal func verifyingKey() throws -> SecureEnclaveVerifyingKey {
        let signingKey = try signingKey()

        guard let publicKey = SecKeyCopyPublicKey(signingKey.privateKey) else {
            throw CryptographicKeyError.unavailablePublicKey
        }

        return try SecureEnclaveVerifyingKey(publicKey, Self.keyInfo.signingAlgorithm.attrs)
    }
    
    // MARK: -
    
    private func generateKeypair() throws -> SecKey {
        let (keyType, keySize) = Self.keyInfo.curve.attrs
        let attributes = try SecureEnclaveKeychainQueries.attributes(
            with: tag,
            keyType: keyType,
            keySize: keySize
        )

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
