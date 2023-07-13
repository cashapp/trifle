//
//  Trifle.swift
//  Trifle
//

import Foundation
import Wire

public class Trifle {
    
    public static let version = "0.2.3"
    
    private static let mobileCertificateRequestVersion = UInt32(0)
    
    private let envelopeDataVersion = UInt32(0)
        
    private let contentSigner: ContentSigner

    /**
     Initialize the SDK with the key tag that is passed in.
     
     Create a new mobile Trifle keypair for which can be used to create a
     certificate request and to sign messages. The library (Trifle) will
     automatically try to choose the best algorithm and key type available on
     this device.
     */
    public init(reverseDomain: String) throws {
        self.contentSigner =
        try SecureEnclaveDigitalSignatureKeyManager(reverseDomain: reverseDomain)
    }
    
    /**
     Create a new mobile Trifle keypair for which can be used to create a
     certificate request and to sign messages. The library (Trifle) will
     automatically try to choose the best algorithm and key type available on
     this device.
     
     - returns: KeyHandle An opaque Trifle representation of the key-pair,
     which the client will need to store.
     If a new key handle could not be generated, a `KeychainAccessError`
     exception is thrown.
     */
    public func generateKeyHandle() throws -> KeyHandle {
        // currently we support only (Secure Enclave, EC-P256)
        return TrifleKeyHandle(tag: try contentSigner.generateTag())
    }
        
    /**
     Check if the TrifleKeyHandle exists and is valid.
          
     - returns: Bool value for validity
     */
    public func isValid(keyHandle: KeyHandle) throws -> Bool {
        
        // Other types of validity check to be added later eg type of key
        // right now we only check if key exists in key chain
        return try SecureEnclaveDigitalSignatureKeyManager.keyExists(keyHandle.tag)
    }
    
    /**
     Delete the TrifleKeyHandle.
          
     - returns: True for successful deletion of keyHandle from Key Chain. If
        keyHandle is not found or the keyHandle did not successfully delete, a
        `KeychainAccessError` exception is thrown with OSStatus error code
        from Security/SecBase.h
     
        errSecItemNotFound - The specified item could not be found in the keychain
     */
    public func delete(keyHandle: KeyHandle) throws -> Bool {
        
        // Other types of validity check to be added later eg type of key
        // right now we only check if key exists in key chain
        return try SecureEnclaveDigitalSignatureKeyManager.deleteKeypair(keyHandle.tag)
    }
    
    /**
     Generate a TrifleCertificateRequest object, signed by the provided
     keyHandle, that can be presented to the Certificate Authority (CA) for
     verification.

     - parameters: entity - the name associated with the public key.
     - parameters: keyHandle - key handle used for the signing.

     - returns: An opaque Trifle representation
     `TrifleCertificateRequest` of the certificate request.
     */
    public func generateMobileCertificateRequest(
        entity: String,
        keyHandle: KeyHandle
    ) throws -> TrifleCertificateRequest {
        let csr = try PKCS10CertificationRequest.Builder()
            .addName(.commonName(entity))
            .sign(for: keyHandle.tag, with: contentSigner)
            
        let csrData =  try ProtoEncoder().encode(MobileCertificateRequest(
            version: Self.mobileCertificateRequestVersion,
            pkcs10_request: Data(csr.octets)
        ))
        return try TrifleCertificateRequest.deserialize(data: csrData)
    }
    
    /**
     Sign the provided data with the provided key, including appropriate Trifle
     metadata, such as the accompanying Trifle certificate.
     
     - parameters: data - raw data to be signed.
     - parameters: keyHandle - key handle used for the signing.
     - parameters: certificate list - Trifle certificate chain to be included in the SignedData message.
     Must match the key in keyHandle.
     
     - returns:`TrifleSignedData` - signed data message in the Trifle format.
     */
    public func createSignedData(
        data: Data,
        keyHandle: KeyHandle,
        certificates: Array<TrifleCertificate>
    ) throws -> TrifleSignedData {
                
        guard let leafCert = certificates.first, !data.isEmpty else {
            throw TrifleError.invalidInput("Data or Certificate should not be empty.")
        }
        
        // TODO: (gelareh) check key handle domain matches the one in trifle
        
        // TODO: (gelareh) check leaf cert matches the public key that will be used for signing
        
        // check cert chain validates
        guard try leafCert.verify(intermediateTrifleChain: Array(certificates.dropFirst(1))) else {
            throw TrifleError.invalidCertificateChain
        }
        
        let signingDataAlgorithm: SignedData.Algorithm
        switch contentSigner.signingAlgorithm {
        case .ecdsaSha256:
            signingDataAlgorithm = SignedData.Algorithm.ECDSA_SHA256
        }
        
        // create serialized data
        let serializedData = try ProtoEncoder().encode(
            SignedData.EnvelopedData(
                version: envelopeDataVersion,
                signing_algorithm: signingDataAlgorithm,
                data: data
            )
        )
        
        // sign data
        // if key handle is invalid, an error is thrown
        let signedData = try ProtoEncoder().encode(SignedData(
                enveloped_data: serializedData,
                signature: try contentSigner.sign(for: keyHandle.tag, with: serializedData).data,
                certificates: certificates.map({ trifleCert in return trifleCert.getCertificate() })))

        return try TrifleSignedData.deserialize(data: signedData)
    }
}

public enum TrifleError: LocalizedError {
    case invalidInput(String)
    case unhandled(Error)
    case invalidCertificateChain
    case expiredCertificate
    case invalidCertificate

    public var errorDescription: String? {
        switch self {
        case .invalidCertificateChain:
            return "Invalid certificate chain."
        case .expiredCertificate:
            return "Certificate is expired."
        case .invalidCertificate:
            return "Certificate is invalid."
        case let .invalidInput(error):
            return error

        case let .unhandled(error):
            return error.localizedDescription
        }
    }
}
