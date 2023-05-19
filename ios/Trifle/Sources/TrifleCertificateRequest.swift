//
//  TrifleCertificateRequest.swift
//  Trifle
//

import Foundation
import Wire

public class TrifleCertificateRequest: Equatable {
    private let proto: MobileCertificateRequest
    
    /**
     Constructor is private. This class can be constructed only via the deserialization
     */
    private init(proto: MobileCertificateRequest) {
        self.proto = proto
    }

    /**
     Deserializes the data and returns a TrifleCertificateRequest object.
     
     This is a static method.

     - parameters: data - the serialized MobileCertificateRequest object
     
     - returns: TrifleCertificateRequest object
     */
    public static func deserialize(data: Data) throws -> TrifleCertificateRequest {
        return TrifleCertificateRequest(proto: try ProtoDecoder().decode(MobileCertificateRequest.self, from: data))
    }
    
    /**
     Serializes the TrifleCertificateRequest object into Data object.
     
     - returns: Data object
     */
    public func serialize() throws -> Data {
        return try ProtoEncoder().encode(proto)
    }
 
    public static func ==(lhs: TrifleCertificateRequest, rhs: TrifleCertificateRequest) -> Bool {
        return lhs.proto == rhs.proto
    }
}
