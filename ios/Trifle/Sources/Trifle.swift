//
//  Trifle.swift
//  Trifle
//

import Foundation

public class Trifle {
    
    public static let version = "0.1.3"
    
    private static let mobileCertificateRequestVersion = UInt32(0)
    
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
    */
    public func generateKeyHandle() throws -> KeyHandle {
        // currently we support only (Secure Enclave, EC-P256)
        return KeyHandle(tag: try contentSigner.generateTag())
    }
        
    /**
     Generate a Trifle MobileCertificateRequest, signed by the provided
     keyHandle, that can be presented to the Certificate Authority (CA) for
     verification.

     - parameters: keyHandle - key handle used for the signing.

     - returns: An opaque Trifle representation
        `MobileCertificateRequest` of the certificate request.
     */
    public func generateMobileCertificateRequest(keyHandle: KeyHandle) throws
    -> MobileCertificateRequest {
        let csr = try PKCS10CertificationRequest.Builder()
            .sign(for: keyHandle.tag, with: contentSigner)
            
        return MobileCertificateRequest(
            version: Self.mobileCertificateRequestVersion,
            pkcs10_request: Data(csr.octets)
        )
    }
    
    /**
     Sign the provided data with the provided key, including appropriate Trifle
     metadata, such as the accompanying certificate.

     - parameters: data - raw data to be signed.
     - parameters: keyHandle - key handle used for the signing.
     - parameters: certificate list - certificate chain to be included in the SignedData message.
        Must match the key in keyHandle.

     - returns:`SignedData` - signed data message in the Trifle format.
    */
    public func createSignedData(
        data: Data,
        keyHandle: KeyHandle,
        certificate: Array<Certificate>
    ) throws -> SignedData {

        guard !data.isEmpty && !certificate.isEmpty else {
            throw TrifleError.invalidInput("Data or Certifcate should not be empty.")
        }
        
        // TODO: check key handle domain matches the one in trifle
        
        // TODO: check leaf cert mactches the public key that will be used for signing
        
        //check cert chain validates
        // already checked certificate chain is not empty
        if (certificate.count == 1 ) {
            guard certificate[0].verify(
                certificateRequest: nil,
                intermediateChain: nil,
                rootCertificate: nil ) else {
                throw TrifleError.invalidCertificateChain
            }
        } else {
            let chain = certificate.dropFirst(1)
            guard certificate[0].verify(
                certificateRequest: nil,
                intermediateChain: Array(chain),
                rootCertificate: nil ) else {
                throw TrifleError.invalidCertificateChain
            }
        }
        
        // sign data
        // if key handle is invalid, an error is thrown
        return SignedData(raw_data: data,
                          signature: try contentSigner.sign(for: keyHandle.tag, with: data).data,
                          certificates: certificate)
    }
}

extension Certificate {
    /**
     Verify that the provided certificate matches what we expected.
     It matches the CSR that we have and the root cert is what
     we expect.

     - parameters: certificateRequest request used to generate this certificate
     - parameters: certificateChain - list of certificates between this cert and
        the root certificate.
     - parameters: rootCertificate - certificate to use as root of chain.
        Defaults to the root certificate bundled with Trifle.
     
     - returns: true if validated, false otherwise
     */
    public func verify(
        certificateRequest: MobileCertificateRequest?,
        intermediateChain: Array<Certificate>?,
        rootCertificate: Certificate?
    ) -> Bool {
        
        // TODO: validate PK in MobileCertificateRequest against certificate
        var chain : Array<Certificate> = [self]
        if (intermediateChain != nil) {
            chain = intermediateChain!
        }
        if (rootCertificate != nil) {
            chain = chain + [ rootCertificate!]
        }
        return X509TrustManager.evaluate( chain )
    }
}

public enum TrifleError: LocalizedError {
    case invalidInput(String)
    case unhandled(Error)
    case invalidCertificateChain

    public var errorDescription: String? {
        switch self {
        case .invalidCertificateChain:
            return "Invalid certificate chain."
        case let .invalidInput(error):
            return error

        case let .unhandled(error):
            return error.localizedDescription
        }
    }
}
