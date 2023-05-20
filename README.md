# Trifle
Security functionality for interoperability/interaction with core services.

## Usage

```
// App start up

let trifle = try Trifle(reverseDomain: abc)

// Check if a key already exists.
// If no key exists, generate a public key pair

let keyHandle = try trifle.generateKeyHandle()
                
// Storing keys. Keys are codable.
let encoder = JSONEncoder()
let jsonKeyHandle = try encoder.encode(keyHandle)

// Load the key from storage when we need to use it
let decoder = JSONDecoder()
let decoded = try decoder.decode(TrifleKeyHandle.self, from: jsonKeyHandle)

// Check the validity of loaded key
// TODO: keyHandle.isValid()

// Destroy key that is no longer in use or is invalid
// TODO: keyHandle.destroy()

        
// Check if loaded key already has a cert. If yes, skip to checking for cert validity
// Else if key does not have a cert OR if a new cert must be generated (eg because of existing
// cert is already expired, or app needs to re-attest, app is re-installed, app is restored
// from backup, ... etc)

// Create cert request
let certReq = try trifle.generateMobileCertificateRequest(keyHandle: keyHandle)

// Serialize to proto to be sent over wire
let encoded = try certReq.serialize()

// Send certificate request to Certificate Authority endpoint. Response will be [Data]
let response: [Data]

// Iterate over each Data to convert to TrifleCertificate
let certs = try response.map({ try TrifleCertificate.deserialize(data: $0) })

// certs is an array of certificates where [0] will be device certificate
// and the rest of the elements will be intermediate chain.

// Check if app has the root cert of Certificate Authority (CA). 

// Validate cert matches the certificate request (so generated key) 
// and the root (so it has been generated by the right CA).
let isValid = certs[0].verify(
            certificateRequest: certRequest,
            intermediateTrifleChain: certs,
            rootTrifleCertificate: root)

// Once it passes validation, certReq is no longer needed and it can be deleted
// Store cert along with the respective keyHandle
        
// To check only for the validity of a stored cert, you can do either of below choices
// Option 1 is a more complete check of the device cert and the full chain
isValid = certs[0].verify(intermediateChain: certs )
// option 2 only checks the validity of the device cert
isValid = certs[0].verify(intermediateChain: [] )

// Sign the data
let trifleSignedData = try trifle.createSignedData(
                                        data: dataThatIsSigned,
                                        keyHandle: keyHandle,
                                        certificates: certs )

// Serialize to proto to be sent over wire
let encodedTrifleSignedDataProto = try trifleSignedData.serialize()
```

## Key Lifecycle

TBD


## Cert Lifecycle

TBD
