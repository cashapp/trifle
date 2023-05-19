//
//  TrifleSignedData.swift
//  Trifle
//

import Foundation
import Wire

public class TrifleSignedData: Equatable {
    private let proto: SignedData
    
    /**
     Constructor is private. This class can be constructed only via the deserialization
     */
    private init(proto: SignedData) {
        self.proto = proto
    }

    /**
     Deserializes the data and returns a TrifleSignedData object.
     
     This is a static method.

     - parameters: data - the serialized TrifleSignedData object
     
     - returns: TrifleSignedData object
     */
    public static func deserialize(data: Data) throws -> TrifleSignedData {
        return TrifleSignedData(proto: try ProtoDecoder().decode(SignedData.self, from: data))
    }
    
    /**
     Serializes the TrifleSignedData object into Data object.
     
     - returns: Data object
     */
    public func serialize() throws -> Data {
        return try ProtoEncoder().encode(proto)
    }
    
    public static func ==(lhs: TrifleSignedData, rhs: TrifleSignedData) -> Bool {
        return lhs.proto == rhs.proto
    }
}
