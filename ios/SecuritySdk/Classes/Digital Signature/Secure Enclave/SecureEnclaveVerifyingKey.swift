//
//  SecureEnclaveVerifyingKey.swift
//  SecuritySdk
//

import Foundation

internal struct SecureEnclaveVerifyingKey: VerifyingKey {

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

    // MARK: - Internal Class Methods (VerifyingKey)

    internal func verify(data: Data, with signature: Data) -> Bool {
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
