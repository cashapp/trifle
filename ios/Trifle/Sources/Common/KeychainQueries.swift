//
//  KeychainQueries.swift
//  Trifle
//

import Foundation

/// Prepared set of query dictionaries used for keychain operations
protocol KeychainQueries {
    
    /**
     Constructs a query dictionary using the a given application tag
     
     - parameter applicationTag: an application tag used to distinguish the key from other keys in the keychain
     - parameter returnRef: whether a reference to `SecKey` should be returned
     - parameter accessGroup: the access group the security item belongs to. If no access group is set, then the
        calling app's default access group is used.
     */
    static func getQuery(with applicationTag: String, 
                         returnRef: Bool,
                         _ accessGroup: String? ) -> NSMutableDictionary
}

// MARK: -

struct KeychainAccessError: Error, LocalizedError {
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
