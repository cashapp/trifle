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
    
    internal let signingAlgorithm = keyInfo.signingAlgorithm

    // MARK: - Private Properties

    private let tagFormat: String

    // MARK: - Initialization

    public init(reverseDomain: String) throws {
        guard !reverseDomain.isEmpty else {
            // tag should not be empty
            throw TrifleError.invalidInput("Reverse domain cannot be empty")
        }
        self.tagFormat = reverseDomain + ".sign.{{uuid}}"
    }
 
    // MARK: - Public Methods (DigitalSignatureSigner)

    public func generateTag() throws -> String {
        let tag = tagFormat.replacingOccurrences(
            of: "{{uuid}}",
            with: UUID().uuidString
        )
        try Self.generateKeypair(tag)
        return tag
    }
    
    public func sign(for tag: String, with data: Data) throws -> DigitalSignature {
        let signature = try signingKey(tag).sign(with: data)
        return DigitalSignature(
            signingAlgorithm: Self.keyInfo.signingAlgorithm,
            data: signature
        )
    }
 
    // MARK: - Public Methods (DigitalSignatureVerifier)

    public func verify(for tag: String, data: Data, with signature: Data) throws -> Bool {
        return try verifyingKey(tag).verify(data: data, with: signature)
    }

    // MARK: - Internal Methods (ContentSigner)
 
    internal func exportPublicKey(_ tag: String) throws -> SigningPublicKey {
        return SigningPublicKey(
            keyInfo: Self.keyInfo,
            data: try verifyingKey(tag).export()
        )
    }

    // MARK: - Internal Methods (DigitalSignatureKeyManager)

    internal func signingKey(_ tag: String) throws -> SecureEnclaveSigningKey {
        var keyRef: CFTypeRef?
        let preparedQuery = SecureEnclaveKeychainQueries.getQuery(with: tag, returnRef: true)
        guard case let status = SecItemCopyMatching(preparedQuery, &keyRef),
                status == errSecSuccess,
                keyRef != nil else {
            throw CryptographicKeyError.unavailableKeyPair
        }
        
        return try SecureEnclaveSigningKey(keyRef as! SecKey, Self.keyInfo.signingAlgorithm.attrs)
    }
    
    internal func verifyingKey(_ tag: String) throws -> SecureEnclaveVerifyingKey {
        let signingKey = try signingKey(tag)

        guard let publicKey = SecKeyCopyPublicKey(signingKey.privateKey) else {
            throw CryptographicKeyError.unavailablePublicKey
        }

        return try SecureEnclaveVerifyingKey(publicKey, Self.keyInfo.signingAlgorithm.attrs)
    }
    
    // MARK: -
    
    private static func generateKeypair(_ tag: String) throws {
        let (keyType, keySize) = Self.keyInfo.curve.attrs
        let attributes = try SecureEnclaveKeychainQueries.attributes(
            with: tag,
            keyType: keyType,
            keySize: keySize
        )

        var error: Unmanaged<CFError>?
        guard SecKeyCreateRandomKey(attributes, &error) != nil else {
            throw AccessControlError.invalidAccess(error?.takeRetainedValue() as? Error)
        }
    }
    
    internal static func deleteKeypair(_ tag: String) throws -> Bool {
        let preparedQuery = SecureEnclaveKeychainQueries.getQuery(with: tag)

        let status = SecItemDelete(preparedQuery)
        switch status {
        case errSecSuccess:
            return true
        default:
            throw KeychainAccessError.unhandled(with: status, and: tag)
        }
    }

    internal static func keyExists(_ tag: String) throws -> Bool {
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
