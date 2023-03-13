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
    public init(tag: String) throws {
        self.contentSigner =
        try SecureEnclaveDigitalSignatureKeyManager(tag: tag)
    }
        
    public func getKeyHandle() throws -> KeyHandle {
        // currently we support only (Secure Enclave, EC-P256)
        return try contentSigner.getKeyHandle()
    }
        
    /**
     Generate a Trifle MobileCertificateRequest, signed by the provided
     keyHandle, that can be presented to the Certificate Authority (CA) for
     verification.
     
     - returns: An opaque Trifle representation
        `MobileCertificateRequest` of the certificate request.
     */
    public func generateMobileCertificateRequest() throws
    -> MobileCertificateRequest {
        let csr = try PKCS10CertificationRequest.Builder()
            .sign(with: contentSigner)
            
        return MobileCertificateRequest(
            version: Self.mobileCertificateRequestVersion,
            pkcs10_request: Data(csr.octets)
        )
    }
    
    /**
     // TODO(dcashman): define message format //
     Sign the provided data with the provided key, including appropriate Trifle
     metadata, such as the accompanying certificate.

     - parameters: data - raw data to be signed.
     - parameters: certificate - certificate to be included in the SignedData message.
        Must match the key in keyHandle.

     - returns:`SignedData` - signed data message in the Trifle format.
    */
    public func createSignedData(
        data: Data,
        keyHandle: KeyHandle,
        certificate: Certificate
    ) -> SignedData {
        // TODO: IMPLEMENT
        SignedData()
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
     */
    public func verify(
        certificateRequest: MobileCertificateRequest,
        certificateChain: Array<Certificate>,
        rootCertificate: Certificate? = nil
    ) -> Bool {
        // TODO: Implement
        return true
    }
}

public enum TrifleError: LocalizedError {
    case invalidInput(String)

    public var errorDescription: String? {
        switch self {
        case let .invalidInput(error) :
            return error
        }
    }
}
