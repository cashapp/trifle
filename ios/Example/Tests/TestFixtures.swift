//
//  TestFixtures.swift
//  Trifle_Tests
//

import Foundation

class TestFixtures {
    
    public static let reverseDomain = "app.cash.trifle.keys"

    public static let deviceCertEncoded = Data(base64Encoded: """
MIIBFDCBvKADAgECAgYBhtoH23kwCgYIKoZIzj0EAwIwEzERMA8GA1\
UEAwwIY2FzaC5hcHAwHhcNMjMwMzEzMDgxMzEzWhcNMjMwOTA5MDgx\
MzEzWjARMQ8wDQYDVQQDDAZlbnRpdHkwWTATBgcqhkjOPQIBBggqhk\
jOPQMBBwNCAASGmNOZtRsOHr2XJVjL1jxRpeumY/jAF3rBDXhhbP49\
5VYvMYkHfWjeNBzJ1YIqXLQf4BHdQQzDjSvT+abzpdjEMAoGCCqGSM\
49BAMCA0cAMEQCIBXBwAm0Qn7S4vKRIHYLXDxhUQTqMuReoWBPfiOX\
fREnAiByNBjSwVxp980/Nl17UvkBUNhTvp55CNuwMmlnporNNA==
""")
    
    public static let deviceCertEncoded2 = Data(base64Encoded: """
MIIBGTCBwaADAgECAgYBht0eNMMwCgYIKoZIzj0EAwIwGDEWMBQGA1\
UEAwwNaXNzdWluZ0VudGl0eTAeFw0yMzAzMTMyMjM2MjlaFw0yMzA5\
MDkyMjM2MjlaMBExDzANBgNVBAMMBmVudGl0eTBZMBMGByqGSM49Ag\
EGCCqGSM49AwEHA0IABICead8cmQi2cyHHTx316w9Q64L11U86PV3R\
K1IDsm/xiDoa5sbShjFPm0nhd+AFoTPtsXL6SJ/bt+sndXQL5gcwCg\
YIKoZIzj0EAwIDRwAwRAIgBQLsaQZpa93v33J/kSIxcl2UtBPCyYYD\
KahIGLy7xM4CIGeiGFjglmmaiqFf30esHdL4yR0/rbkVm4h6z9O+Rjfp
""")
    
    public static let otherRootCertEncoded = Data(base64Encoded: """
MIHcMIGPoAMCAQICAQEwBQYDK2VwMBgxFjAUBgNVBAMMDWlzc3VpbmdFbnR\
pdHkwHhcNMjMwMzEzMDYxMzI3WhcNMjMwMzE0MDYxMzI3WjAYMRYwFAYDVQ\
QDDA1pc3N1aW5nRW50aXR5MCowBQYDK2VwAyEAm+Ac7932SHDPQLYd3p3gm\
grcArUWrBqhPEC+q/QI3lEwBQYDK2VwA0EAJrfzN7qA3VqwazsT8yMIYMvY\
Rz2iDA1898Yx5ELtlQcl7QUGXUmadwzW7rpxQB5wIk46tPTEJCFmUIYwrCB\
4BQ==
""")
    
    public static let rootCertEncoded = Data(base64Encoded: """
MIIBZTCCAQqgAwIBAgIBATAKBggqhkjOPQQDAjAYMRYwFAYDVQQDDA1pc3N\
1aW5nRW50aXR5MB4XDTIzMDMxMzIyMzUzMloXDTIzMDkwOTIyMzUzMlowGD\
EWMBQGA1UEAwwNaXNzdWluZ0VudGl0eTBZMBMGByqGSM49AgEGCCqGSM49A\
wEHA0IABICead8cmQi2cyHHTx316w9Q64L11U86PV3RK1IDsm/xiDoa5sbS\
hjFPm0nhd+AFoTPtsXL6SJ/bt+sndXQL5gejRTBDMA8GA1UdEwEB/wQFMAM\
BAf8wDgYDVR0PAQH/BAQDAgIEMCAGA1UdDgEB/wQWBBQ/80Y00UVTlI6kiA\
ZZ46kcrJ9a2jAKBggqhkjOPQQDAgNJADBGAiEAvffuwvImKNaolqnEr4ENB\
6LXEFdV0YVK3Ic3Mi+hqJ8CIQC7CiLwyvH1cChUReanIGeYiQp27LJ99M/q\
WLq6hmtSmQ==
""")
    
    public static let data = "hello world".data(using: .utf8)!
}
