//
//  SecureEnclaveKeychainQueries.swift
//  Trifle
//

import Foundation

internal struct SecureEnclaveKeychainQueries: KeychainQueries {
    
    // MARK: - Private Properties

    private static var access: SecAccessControl {
        get throws {
            var error: Unmanaged<CFError>?
            defer {
                error?.release()
            }
            
            guard let access = SecAccessControlCreateWithFlags(
                kCFAllocatorDefault,
                kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
                .privateKeyUsage,
                &error
            ) else {
                throw AccessControlError.invalidAccess(error as? Error)
            }
            return access
        }
    }
    
    // MARK: - Internal Methods

    internal static func attributes(
        with applicationTag: String,
        keyType: CFString,
        keySize: Int,
        accessGroup: String?
    ) throws -> NSMutableDictionary {
        let attributes: NSMutableDictionary = [
            kSecAttrKeyType: keyType,
            kSecAttrKeySizeInBits: keySize,
            kSecPrivateKeyAttrs: [
                kSecAttrIsPermanent: true,
                kSecAttrApplicationTag: applicationTag.data(using: .utf8)!,
                kSecAttrAccessControl: try Self.access,
            ] as [CFString: Any],
        ]
        // if accessGroup is nil, the calling application's access group will be used by default
        if let accessGroup = accessGroup {
            attributes[kSecAttrAccessGroup] = accessGroup
        }

        #if !targetEnvironment(simulator)
        attributes.setValue(kSecAttrTokenIDSecureEnclave, forKey: kSecAttrTokenID as String)
        #endif
        
        return attributes
    }

    // MARK: - KeychainQueries
    
    internal static func getQuery(with applicationTag: String,
                                  _ accessGroup: String?,
                                  returnRef: Bool = false
    ) -> NSMutableDictionary {
        let attributes: NSMutableDictionary =  [
            kSecClass as String: kSecClassKey,
            kSecAttrApplicationTag as String: applicationTag.data(using: .utf8)!,
            kSecAttrKeyType as String: kSecAttrKeyTypeECSECPrimeRandom,
            kSecReturnRef as String: returnRef, 
        ]
        // if accessGroup is nil, the calling application's access group will be used by default
        if let accessGroup = accessGroup {
            attributes[kSecAttrAccessGroup] = accessGroup
        }
        return attributes
    }
}

// MARK: -

enum AccessControlError: LocalizedError {
    case invalidAccess(Error?)
    
    public var errorDescription: String? {
        switch self {
        case let .invalidAccess(error):
            return error?.localizedDescription
        }
    }
}
