//
//  PKCS10CertificateRequestBuilder.swift
//  Trifle
//

import Foundation

extension PKCS10CertificationRequest {
    /// PKCS#10 Certification Request Builder
    final class Builder {
        private var version: UInt
        private var subject: Array<ASN1AttributeTypeValue>
        private var attributes: Array<ASN1Attribute>
        
        init() {
            self.version = 0
            self.subject = []
            self.attributes = []
        }
        
        func setVersion(_ version: UInt) -> Builder {
            self.version = version
            return self
        }
        
        func addName(_ name: ASN1DistinguishedName) throws
        -> Builder {
            self.subject.append(try name.asn1())
            return self
        }
        
        func addAttribute(_ attribute: ASN1Attribute)
        -> Builder {
            self.attributes.append(attribute)
            return self
        }
        
        func sign(with signer: ContentSigner) throws
        -> PKCS10CertificationRequest {
            let certificateRequestInfo =
            try buildCertificateRequestInfo(
                with: try signer.exportPublicKey()
            )
            
            let signature = try signer.sign(
                with: Data(certificateRequestInfo.octets)
            )

            let certificationRequest = try ASN1Sequence([
                certificateRequestInfo,
                try signature.signingAlgorithm.asn1(),
                try ASN1BitString(
                    signature.data,
                    ignorePadding: true
                )
            ])
            
            
            return PKCS10CertificationRequest(certificationRequest)
        }
        
        func buildCertificateRequestInfo(
            with publicKey: SigningPublicKey
        ) throws -> ASN1Sequence {
            let version = try ASN1Integer(Int64(version))
            let subject = try ASN1Sequence([
                try ASN1Set(subject)
            ])
            let attributes = try ASN1Set(
                attributes,
                .implicit(0x00)
            )
            
            let subjectPublicKeyInfo = try ASN1Sequence([
                publicKey.keyInfo.asn1(),
                try ASN1BitString(publicKey.data)
            ])
            
            return try ASN1Sequence([
                version,
                subject,
                subjectPublicKeyInfo,
                attributes
            ])
        }
    }
}

extension KeyInfo {
    func asn1() throws -> ASN1Sequence {
        switch self {
        case let .ecKey(curve, _):
            switch curve {
            case .p256:
                return try ASN1Sequence([
                    /// ecPublicKey OID
                    /// http://www.oid-info.com/get/1.2.840.10045.2.1
                    try ASN1ObjectIdentifier(
                        [1, 2, 840, 10045, 2, 1]
                    ),
                    /// prime256v1 OID
                    /// https://oidref.com/1.2.840.10045.3.1.7
                    try ASN1ObjectIdentifier(
                        [1, 2, 840, 10045, 3, 1, 7]
                    )
                ])
            }
        }
    }
}

extension SigningAlgorithm {
    func asn1() throws -> ASN1Sequence {
        switch self {
        /// ecdsa-with-SHA256 OID
        /// http://oid-info.com/get/1.2.840.10045.4.3.2
        case .ecdsaSha256:
            return try ASN1Sequence([
                try ASN1ObjectIdentifier(
                    [1, 2, 840, 10045, 4, 3, 2]
                )
            ])
        }
    }
}
