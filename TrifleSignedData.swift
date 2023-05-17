//
//  TrifleSignedData.swift
//  Trifle
//
//

import Foundation
import Wire

public class TrifleSignedData : Codable {
    private let proto: SignedData
    
    public init(data: Data) throws {
        self.proto = try ProtoDecoder().decode(SignedData.self, from: data)
    }
    
    internal func getSignedData() -> SignedData {
        return proto
    }    
}
