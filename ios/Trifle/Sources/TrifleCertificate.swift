//
//  TrifleCertificate.swift
//  Trifle
//

import Foundation
import Wire

public class TrifleCertificate: Equatable {
    private let proto: Certificate
    
    /**
     Constructor is private. This class can be constructed only via the deserialization
     */
    private init(proto: Certificate) {
        self.proto = proto
    }
    
    /**
     Deserializes the data and returns a TrifleCertificate object.
     
     This is a static method.

     - parameters: data - the serialized TrifleCertificate object
     
     - returns: TrifleCertificate object
     */
    public static func deserialize(data: Data) throws -> TrifleCertificate {
        return TrifleCertificate(proto: try ProtoDecoder().decode(Certificate.self, from: data))
    }
    
    /**
     Serializes the TrifleCertificate object into Data object.
     
     - returns: Data object
     */
    public func serialize() throws -> Data {
        return try ProtoEncoder().encode(proto)
    }
    
    internal func getCertificate() -> Certificate {
        return proto
    }
    
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
        certificateRequest: TrifleCertificateRequest? = nil,
        intermediateTrifleChain: Array<TrifleCertificate>,
        rootTrifleCertificate: TrifleCertificate? = nil
    ) throws -> Bool {
        // TODO: validate PK in MobileCertificateRequest against certificate
        
        let intermediateChain = intermediateTrifleChain.map({ trifleCert in
            return trifleCert.getCertificate()
        })
        
        var csr: MobileCertificateRequest? = nil
        if let tcsr = certificateRequest {
            csr = try ProtoDecoder().decode(MobileCertificateRequest.self, from: tcsr.serialize())
        } 
        return try self.proto.verify(certificateRequest: csr,
                                     intermediateChain: intermediateChain,
                                     rootCertificate: rootTrifleCertificate?.getCertificate())
    }
    
    public static func ==(lhs: TrifleCertificate, rhs: TrifleCertificate) -> Bool {
        return lhs.proto == rhs.proto
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
    internal func verify(
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
        
        do {
            let result = try X509TrustManager.evaluate(chain)
            return result
        } catch let error as NSError {
            throw TrifleError.securityFramework(error.code)
        }
    }
}
