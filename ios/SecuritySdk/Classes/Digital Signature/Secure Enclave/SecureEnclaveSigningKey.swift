//
//  SecureEnclaveSigningKey.swift
//  SecuritySdk
//

import Foundation

public struct SecureEnclaveSigningKey: SigningKey {
    
    // MARK: - Internal Properties
    
    internal static let algorithm: SecKeyAlgorithm = .ecdsaSignatureMessageX962SHA256
    internal let privateKey: SecKey
    
    // MARK: - Initialization

    init(secKey: SecKey) throws {
        guard SecKeyIsAlgorithmSupported(secKey, .sign, Self.algorithm) else {
            throw CryptographicKeyError.unsupportedAlgorithm
        }
        self.privateKey = secKey
    }

    // MARK: - Public Class Methods (SigningKey)

    public func sign(with data: Data) throws -> Data {
        var error: Unmanaged<CFError>?
        guard let signature = SecKeyCreateSignature(
            privateKey,
            Self.algorithm,
            data as CFData,
            &error
        ) as Data? else {
            throw CryptographicKeyError.unhandled(error!.takeRetainedValue() as Error)
        }

        return signature
    }
}
