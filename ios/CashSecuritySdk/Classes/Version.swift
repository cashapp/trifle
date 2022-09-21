//
//  Version.swift
//  CashSecuritySdk
//
//  Trivial implementation of *something*.
//

import Foundation
import semver

public protocol Version {
    func complete() -> String
    
    func major() -> Int
    
    func minor() -> Int
}

public class LibraryVersion : Version {
    let recordedVersion: String
    
    public init() {
        
        let sdkBundle = Bundle(identifier: "app.cash.security_sdk")
        
        let bundleVersion = sdkBundle.flatMap({
            $0.object(forInfoDictionaryKey: "CFBundleShortVersionString")
        }) ?? "-1"
        
        recordedVersion = String(describing: bundleVersion)
    }
    
    public func complete() -> String {
        return recordedVersion
    }
    
    public func major() -> Int {
        let versionComponents = Semver.clean(recordedVersion).components(separatedBy: ".")
        let major = Int(versionComponents[0])
        return major ?? -1
    }
    
    public func minor() -> Int {
        let versionComponents = Semver.clean(recordedVersion).components(separatedBy: ".")
        let major = Int(versionComponents[1])
        return major ?? -2
    }
    
}
