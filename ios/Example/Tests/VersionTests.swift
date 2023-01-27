
import SecuritySdk
import XCTest

class VersionTests: XCTestCase {
    
    // XCode will *only* test methods beginning with "test", so don't get
    // fancy on your naming here.
    func testCurrentVersionCanBeExtracted() {
        
        let version = SecuritySdk.LibraryVersion()
        let versionString = version.complete()
        
        XCTAssertEqual(versionString, "0.1.0")
    }
    
    func testMajorVersionExtracts() {
        let version = SecuritySdk.LibraryVersion()
        let versionString = version.major()
        
        XCTAssertEqual(versionString, 0)
    }
    
    func testMinorVersionExtracts() {
        let version = SecuritySdk.LibraryVersion()
        let versionString = version.minor()
        
        XCTAssertEqual(versionString, 1)
    }
}
