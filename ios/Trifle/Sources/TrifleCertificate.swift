//
//  KeyHandle.swift
//  Trifle
//

import Foundation

public struct TrifleCertificate : Codable {
    public let proto: Certificate
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
        certificateRequest: MobileCertificateRequest? = nil,
        intermediateChain: Array<Certificate>,
        rootCertificate: Certificate? = nil
    ) throws -> Bool {
        
        // TODO: validate PK in MobileCertificateRequest against certificate
        
        let chain : Array<Certificate>
        if (rootCertificate != nil) {
            chain = [self] + intermediateChain + [rootCertificate!]
        } else {
            chain = [self] + intermediateChain
        }
        
        let result = X509TrustManager.evaluate(chain)
        var error: NSError?
        
        if !result {
            if let error = error {
                if error.code == errSecCertificateExpired {
                    throw TrifleError.expiredCertificate
                } else {
                    throw TrifleError.invalidCertificate
                }
            } else {
                // There was an error, but error object is nil
                throw TrifleError.invalidCertificate
            }
        }
        return result
    }
}
