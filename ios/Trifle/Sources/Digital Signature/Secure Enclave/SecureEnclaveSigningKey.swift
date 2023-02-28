//
//  SecureEnclaveSigningKey.swift
//  Trifle
//

import Foundation

internal struct SecureEnclaveSigningKey: SigningKey {
    
    // MARK: - Internal Properties
    
    internal let privateKey: SecKey
    internal let algorithm: SecKeyAlgorithm
    
    // MARK: - Initialization

    init(_ secKey: SecKey, _ algorithm: SecKeyAlgorithm) throws {
        guard SecKeyIsAlgorithmSupported(secKey, .sign, algorithm) else {
            throw CryptographicKeyError.unsupportedAlgorithm
        }
        self.privateKey = secKey
        self.algorithm = algorithm
    }

    // MARK: - Internal Class Methods (SigningKey)

    internal func sign(with data: Data) throws -> Data {
        var error: Unmanaged<CFError>?
        guard let signature = SecKeyCreateSignature(
            self.privateKey,
            self.algorithm,
            data as CFData,
            &error
        ) as Data? else {
            throw CryptographicKeyError.unhandled(error!.takeRetainedValue() as Error)
        }

        return signature
    }
}
