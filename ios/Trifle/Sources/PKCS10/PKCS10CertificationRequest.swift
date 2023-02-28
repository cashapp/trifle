//
//  PKCS10CertificationRequest.swift
//  Trifle
//

import Foundation

/// PKCS#10 Certification Request
final class PKCS10CertificationRequest {
    private let certificationRequest: ASN1Sequence
    private let octets: [Octet]

    init(_ certificationRequest: ASN1Sequence) {
        self.certificationRequest = certificationRequest
        self.octets = certificationRequest.octets
    }
    
    /**
     Returns the Privacy Enhanced Mail (.PEM) format of the
      Certificate Signing Request encoded in DER
     
     - returns: Base64 string representation of the CSR
     */
    func pem() throws -> String {
        return """
        -----BEGIN CERTIFICATE REQUEST-----
        \(Self.format(octets))
        -----END CERTIFICATE REQUEST-----
        """
    }
    
    // MARK: -

    private static func format(
        _ octets: [Octet],
        _ length: Int = 64
    ) -> String {
        let csrBase64 = Data(octets).base64EncodedString()
        return stride(from: 0, to: csrBase64.count, by: length).map {
            let start = csrBase64.index(
                csrBase64.startIndex,
                offsetBy: $0
            )
            let end = csrBase64.index(
                start,
                offsetBy: length,
                limitedBy: csrBase64.endIndex
            ) ?? csrBase64.endIndex
            return String(csrBase64[start..<end])
        }.joined(separator: "\n")
    }
}
