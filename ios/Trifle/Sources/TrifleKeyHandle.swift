//
//  TrifleKeyHandle.swift
//  Trifle
//

import Foundation

public class TrifleKeyHandle: KeyHandle, Codable {
    public let tag: String
    
    public init(tag: String) {
        self.tag = tag
    }
}
