//
//  KeyHandle.swift
//  Trifle
//

import Foundation

public struct KeyHandle : Codable {
    var tag: String
}

/*
 JSON serializing/deserializing
 
 func serialize() throws -> String {
     let encoder = JSONEncoder()
     let jsonData = try encoder.encode(keyHandle)
     return String(data: jsonData, encoding: .utf8)!
 }

 func deserialize(jsonData: String) throws -> KeyHandle {
    let decoder = JSONDecoder()
    let decoded = try decoder.decode(KeyHandle.self, from: jsonData)
    // assert(type(of: decoded) == KeyHandle.type() )
    return decoded
 }
 */
