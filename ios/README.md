# CashSecuritySdk

[![CI Status](https://img.shields.io/travis/squareup/cash-security-sdk.svg?style=flat)](https://travis-ci.org/squareup/cash-security-sdk)
[![Version](https://img.shields.io/cocoapods/v/CashSecuritySdk.svg?style=flat)](https://cocoapods.org/pods/CashSecuritySdk)
[![License](https://img.shields.io/cocoapods/l/CashSecuritySdk.svg?style=flat)](https://cocoapods.org/pods/CashSecuritySdk)
[![Platform](https://img.shields.io/cocoapods/p/CashSecuritySdk.svg?style=flat)](https://cocoapods.org/pods/CashSecuritySdk)

## Example

To run the example project, clone the repo, and run `pod install` from the
Example directory first.

## Requirements

## Installation

CashSecuritySdk is available through [CocoaPods](https://cocoapods.org). To
install it, simply add the following line to your Podfile:

```ruby
pod 'CashSecuritySdk'
```

### Apple silicon

In order for the SDK to run on Apple silicon macs (M1) 2 additional steps are required.

First the app target needs an additional build step added to build step to convert the Tink framework to run on Apple silicon.

In your target build settings add an extra Run Script step after `[CP] Check Pods manifest Lock`.

The script contents should be (TODO: This needs to be redone for a non local pod)
```shell
../Scripts/FixTinkForAppleSilicon`
```

In your project xcconfig file add an extra framework search path for the fixed Tink framework
```
FRAMEWORK_SEARCH_PATHS[sdk=iphonesimulator*][arch=arm64] = "$DERIVED_FILE_DIR/apple_silicon" $(inherited)
```
If you are not using xcconfig files then make the change directly in the project build settings.


## Authors

Cash Security Engineering, cash-security-triage@squareup.com

Cameron Hotchkies, chotchkies@squareup.com

## License

CashSecuritySdk is available under the Apache 2.0 license. See the LICENSE file
for more info.
