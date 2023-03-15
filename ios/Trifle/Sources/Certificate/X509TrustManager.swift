//
//  TrustManager.swift
//  Trifle
//

import Foundation

internal struct X509TrustManager {
    
    // MARK: - Internal Methods
    
    internal static func evaluate(_ certificates: Array<Certificate>) -> Bool {
        let secCerts = certificates.map {
            SecCertificateCreateWithData(nil, $0.certificate! as CFData)!
        }
        var optionalTrust: SecTrust?
        let status = SecTrustCreateWithCertificates(
            secCerts as AnyObject,
            SecPolicyCreateBasicX509(),
            &optionalTrust
        )
        
        guard let trust = optionalTrust, status == errSecSuccess else {
            return false
        }

        // sets the root certificate (tail of the array as the trust anchor
        SecTrustSetAnchorCertificates(trust, [secCerts.last!] as CFArray)
        // disables trusting any built-in anchors
        SecTrustSetAnchorCertificatesOnly(trust, true)
        // disables fetching from the network if missing intermediate chain(s)
        SecTrustSetNetworkFetchAllowed(trust, false)
        
        return SecTrustEvaluateWithError(trust, nil)
    }
}
