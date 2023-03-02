//
//  CertificateDecodableTest.swift
//  Trifle_Tests
//

import Foundation
import Trifle 
import XCTest

final class CertificateDecodableTest: XCTestCase {
    /// create CSR from iOS SDK,
    /// create signed device certificate signed by JVM SDK
    func testDecodesDeviceSignedCertificate() throws {
        let derEncodedCert = Data(base64Encoded: """
MIIBRDCB8qADAgECAgYBhpWf8vowBQYDK2VwMBgxFjAUBgNVBAMM\
DWlzc3VpbmdFbnRpdHkwHhcNMjMwMjI4MDEyNTMzWhcNMjMwODI3\
MDEyNTMzWjBHMUUwCQYDVQQGEwJVUzAPBgNVBAMTCGNhc2guYXBw\
MBEGA1UECBMKQ2FsaWZvcm5pYTAUBgNVBAcTDVNhbiBGcmFuY2lz\
Y28wWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQNkp7f37RmuWxm\
ybKevG8sCxu7tam07HDZuKpw35l41llH39mgNDsNZ9xgK87Ix5q1\
WGIWbsKLsEjdvpg/d8ubMAUGAytlcANGAAEmroDFGvkG0JQJN+Er\
g3IS3nzRtc3AqS/J/sr5xN3uU/wtaLewynZy2GO6SCfr/zfAY0ok\
VicuTchtwvoEIq0lXZiJDQ==
""")!
        
        let certificate = SecCertificateCreateWithData(nil, derEncodedCert as CFData)!
        
        XCTAssertEqual((SecCertificateCopySubjectSummary(certificate) as? String)!, "cash.app")
    }
    
    func testDecodesIssuerCertificate() throws {
        /// self-signed issuer certificate generated from JVM SDK
        let derEncodedCert = Data(base64Encoded: """
MIIBRjCB9KADAgECAgEBMAUGAytlcDAYMRYwFAYDVQQDDA1pc3N1\
aW5nRW50aXR5MB4XDTIzMDIyODAxMjUzMloXDTIzMDMwMTAxMjUz\
MlowGDEWMBQGA1UEAwwNaXNzdWluZ0VudGl0eTCBjjAWBgcqhkjO\
PQIBMAsGCSsGAQQB2kcPAQN0AAjFgbq1AhJrCl8KN3R5cGUuZ29v\
Z2xlYXBpcy5jb20vZ29vZ2xlLmNyeXB0by50aW5rLkVkMjU1MTlQ\
dWJsaWNLZXkSIhIge1OAR3AF7czQTa5vto8gY/ZOnPm8Brnm/RJ+\
I+BvCsoYAxABGMWBurUCIAEwBQYDK2VwA0YAASaugMW9V51Bt4az\
BPdFheJaVnONFx1ybDViog7qFBL4uTybofiVd8wGwxXhtv9+LDZy\
hjqHInJkLUH6UpcrHBd3ThAM
""")!
        
        let certificate = SecCertificateCreateWithData(nil, derEncodedCert as CFData)!
        
        XCTAssertEqual((SecCertificateCopySubjectSummary(certificate) as? String)!, "issuingEntity")
    }
}
