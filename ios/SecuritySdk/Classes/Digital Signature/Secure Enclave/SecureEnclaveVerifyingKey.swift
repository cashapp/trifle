//
//  SecureEnclaveVerifyingKey.swift
//  SecuritySdk
//

import Foundation

public struct SecureEnclaveVerifyingKey: VerifyingKey {

    // MARK: - Internal Properties

    internal static let algorithm: SecKeyAlgorithm = .ecdsaSignatureMessageX962SHA256
    internal let publicKey: SecKey

    // MARK: - Initialization

    init(secKey: SecKey) throws {
        guard SecKeyIsAlgorithmSupported(secKey, .verify, Self.algorithm) else {
            throw CryptographicKeyError.unsupportedAlgorithm
        }
        self.publicKey = secKey
    }

    // MARK: - Public Class Methods (VerifyingKey)
    
    public func exportAsData() throws -> Data {
        var error: Unmanaged<CFError>?
        guard let data = SecKeyCopyExternalRepresentation(
            publicKey,
            &error
        ) as? Data else {
            throw CryptographicKeyError.unexportablePublicKey
        }
        return data
    }

    public func verify(data: Data, with signature: Data) -> Bool {
        var error: Unmanaged<CFError>?
        guard SecKeyVerifySignature(
            self.publicKey,
            Self.algorithm,
            data as CFData,
            signature as CFData,
            &error
        ) else {
            return false
        }

        return true
    }
}
