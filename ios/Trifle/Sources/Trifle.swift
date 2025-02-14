//
//  Trifle.swift
//  Trifle
//

import Foundation
import Wire

public class Trifle {
    
    public static let version = "0.2.6"
    
    private static let mobileCertificateRequestVersion = UInt32(0)
    
    private let envelopeDataVersion = UInt32(0)
        
    private let contentSigner: ContentSigner
    
    private let accessGroup: String?

    /**
     Initialize the SDK with the key tag that is passed in.
     
     Create a new mobile Trifle keypair for which can be used to create a
     certificate request and to sign messages. The library (Trifle) will
     automatically try to choose the best algorithm and key type available on
     this device.
     
     AccessGroup specifies the access group the Trifle key belongs to. Specifying this
     attribute will mean that all key related APIs will be limited to the specified access group
     (of which the calling application must be a member to obtain matching results.)
     It is recommended that this value is set.
    ** This value must be added to the App Group entitlement file. **
     
     If the access group is not set, Trifle keys are created in the application's default access group.
     */
    public init(reverseDomain: String, accessGroup: String? = nil) throws {
        self.contentSigner = try SecureEnclaveDigitalSignatureKeyManager(
            reverseDomain: reverseDomain,
            accessGroup: accessGroup
        )
        self.accessGroup = accessGroup
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
        return try SecureEnclaveDigitalSignatureKeyManager.keyExists(keyHandle.tag, accessGroup)
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
        return try SecureEnclaveDigitalSignatureKeyManager.deleteKeypair(keyHandle.tag, accessGroup)
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
            
        let csrData =  try ProtoEncoder().encode(MobileCertificateRequest(configure: { request in
            request.version = Self.mobileCertificateRequestVersion
            request.pkcs10_request = Data(csr.octets)
        }))
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
                
        guard !certificates.isEmpty, !data.isEmpty else {
            throw TrifleError.invalidInput("Data or Certificate should not be empty.")
        }
        
        // TODO: (gelareh) check key handle domain matches the one in trifle
        
        // TODO: (gelareh) check leaf cert matches the public key that will be used for signing
                
        let signingDataAlgorithm: SignedData.Algorithm
        switch contentSigner.signingAlgorithm {
        case .ecdsaSha256:
            signingDataAlgorithm = SignedData.Algorithm.ECDSA_SHA256
        }
        
        // create serialized data
        let serializedData = try ProtoEncoder().encode(
            SignedData.EnvelopedData(configure: { envelope in
                envelope.version = envelopeDataVersion
                envelope.signing_algorithm = signingDataAlgorithm
                envelope.data = data
            })
        )
        
        // sign data
        // if key handle is invalid, an error is thrown
        let signature = try contentSigner.sign(for: keyHandle.tag, with: serializedData).data
        let signedData = try ProtoEncoder().encode(
            SignedData(configure: { data in
                data.enveloped_data = serializedData
                data.signature = signature
                data.certificates = certificates.map({ trifleCert in return trifleCert.getCertificate() })
            })
        )

        return try TrifleSignedData.deserialize(data: signedData)
    }
}

public enum TrifleError: LocalizedError {
    case invalidInput(String)
    case unhandled(Error)
    case invalidCertificateChain
    case securityFramework(Int)

    public var errorDescription: String? {
        switch self {
        case .invalidCertificateChain:
            return "Invalid certificate chain."
        case let .securityFramework(errorCode):
            // https://developer.apple.com/documentation/security/1542001-security_framework_result_codes
            // check error code https://www.osstatus.com/
            return "Security Framework error ( \(errorCode) )."
        case let .invalidInput(error):
            return error
        case let .unhandled(error):
            return error.localizedDescription
        }
    }
}
