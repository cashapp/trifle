//
//  KeychainQueries.swift
//  SecuritySdk
//

import Foundation

/// Prepared set of query dictionaries used for keychain operations
public protocol KeychainQueries {
    
    /**
     Constructs a query dictionary using the a given application tag
     
     - parameter applicationTag: an application tag used to distinguish the key from other keys in the keychain
     - parameter returnRef: whether a reference to `SecKey` should be returned
     */
    static func getQuery(with applicationTag: String, returnRef: Bool) -> NSMutableDictionary
}

// MARK: -

public struct KeychainAccessError: Error, LocalizedError {
    public let status: OSStatus
    public let tag: String

    public var errorDescription: String? {
        let errorString = SecCopyErrorMessageString(status, nil) as String?
        return """
        Keychain Error: \(errorString ?? "Unknown")
        Error code: \(status)
        Tag: \(tag)
        """
    }
}
