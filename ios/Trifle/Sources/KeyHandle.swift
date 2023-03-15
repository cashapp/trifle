//
//  KeyHandle.swift
//  Trifle
//

import Foundation

public struct KeyHandle : Codable, Equatable {
    var tag: String
    
    static public func == (lhs: KeyHandle, rhs: KeyHandle) -> Bool {
            return lhs.tag == rhs.tag
        }
}
