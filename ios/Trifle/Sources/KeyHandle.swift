//
//  KeyHandle.swift
//  Trifle
//

import Foundation

public protocol KeyHandle: Codable {
    var tag: String { get }
}
