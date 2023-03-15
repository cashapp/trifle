//
//  SecureEnclaveDigitalSignatureKeyManager.swift
//  Trifle
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

    public init(tag: String) throws {
        guard !tag.isEmpty else {
            // tag should not be empty
            throw TrifleError.invalidInput
        }
        self.tag = tag
    }
 
    // MARK: - Public Methods (DigitalSignatureSigner)

    public func sign(with data: Data) throws -> DigitalSignature {
        // key must exist already
        let signature = try loadKey().sign(with: data)
        return DigitalSignature(
            signingAlgorithm: Self.keyInfo.signingAlgorithm,
            data: signature
        )
    }
 
    // MARK: - Public Methods (DigitalSignatureVerifier)

    public func verify(data: Data, with signature: Data) throws -> Bool {
        return try verifyingKey().verify(data: data, with: signature)
    }

    // MARK: - Public Methods (getKeyHandle)
    
    public func getKeyHandle() throws -> KeyHandle {
        // generates a signing key and store in keychain
        // if key tag already exists, then return that key
        // if key tag is new, then generate new key
        // we don't need the key right now, so throw it away
        _ = try createOrLoadSigningKey()
        
        // create a keyHandle
        return KeyHandle(tag: self.tag)
    }

    // MARK: - Internal Methods (ContentSigner)
 
    internal func exportPublicKey() throws -> SigningPublicKey {
        return SigningPublicKey(
            keyInfo: Self.keyInfo,
            data: try verifyingKey().export()
        )
    }

    // MARK: - Internal Methods (DigitalSignatureKeyManager)

    internal func createOrLoadSigningKey() throws -> SecureEnclaveSigningKey {
        let signingAlgorithm = Self.keyInfo.signingAlgorithm.attrs

        if try keyExists() {
            return try loadSigningKey()
        } else {
            return try SecureEnclaveSigningKey(generateKeypair(), signingAlgorithm)
        }
    }
    
    // MARK: - Internal Methods (DigitalSignatureKeyManager)

    internal func verifyingKey() throws -> SecureEnclaveVerifyingKey {
        // load key if it exists, else throw
        let signingKey = try loadKey()

        guard let publicKey = SecKeyCopyPublicKey(signingKey.privateKey) else {
            throw CryptographicKeyError.unavailablePublicKey
        }

        return try SecureEnclaveVerifyingKey(publicKey, Self.keyInfo.signingAlgorithm.attrs)
    }

    // MARK: - Private Methods

    private func loadKey() throws -> SecureEnclaveSigningKey {
        // if key exists, return it
        // otherwise throw
        if try keyExists() {
            return try loadSigningKey()
        } else {
            throw CryptographicKeyError.unavailableKey
        }
    }

    private func loadSigningKey() throws -> SecureEnclaveSigningKey {
        // assume key exists. (caller must have checked with keyExists())
        // return key
        let signingAlgorithm = Self.keyInfo.signingAlgorithm.attrs

        var keyRef: CFTypeRef?
        let preparedQuery = SecureEnclaveKeychainQueries.getQuery(with: tag, returnRef: true)
        SecItemCopyMatching(preparedQuery, &keyRef)
        
        return try SecureEnclaveSigningKey(keyRef as! SecKey, signingAlgorithm)
    }

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
