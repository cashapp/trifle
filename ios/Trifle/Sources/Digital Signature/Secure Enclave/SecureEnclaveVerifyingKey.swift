//
//  SecureEnclaveVerifyingKey.swift
//  Trifle
//

import Foundation

internal struct SecureEnclaveVerifyingKey: VerifyingKey {

    // MARK: - Internal Properties

    internal let publicKey: SecKey
    internal let algorithm: SecKeyAlgorithm

    // MARK: - Initialization

    init(_ secKey: SecKey, _ algorithm: SecKeyAlgorithm) throws {
        guard SecKeyIsAlgorithmSupported(secKey, .verify, algorithm) else {
            throw CryptographicKeyError.unsupportedAlgorithm
        }
        self.publicKey = secKey
        self.algorithm = algorithm
    }

    // MARK: - Internal Class Methods (VerifyingKey)

    internal func verify(data: Data, with signature: Data) -> Bool {
        var error: Unmanaged<CFError>?
        guard SecKeyVerifySignature(
            self.publicKey,
            self.algorithm,
            data as CFData,
            signature as CFData,
            &error
        ) else {
            return false
        }

        return true
    }
    
    internal func export() throws -> Data {
        var error: Unmanaged<CFError>?
        guard let data = SecKeyCopyExternalRepresentation(
            publicKey,
            &error
        ) as? Data else {
            throw CryptographicKeyError.unexportablePublicKey
        }
        return data
    }
}
