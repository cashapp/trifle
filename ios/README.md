# Trifle
[![CI Status](https://img.shields.io/travis/CashApp/Trifle.svg?style=flat)](https://github.com/cashapp/trifle/actions?branch%3Amain+workflow%3A%22Swift+CI+on+PR%22)
[![Version](https://img.shields.io/cocoapods/v/Trifle.svg?style=flat)](https://cocoapods.org/pods/Trifle)
[![License](https://img.shields.io/cocoapods/l/Trifle.svg?style=flat)](https://cocoapods.org/pods/Trifle)
[![Platform](https://img.shields.io/cocoapods/p/Trifle.svg?style=flat)](https://cocoapods.org/pods/Trifle)

## Example

To run the example project, clone the repo, and run `pod install` from the
Example directory first.

## Requirements

For some users, the ruby version has been a sticking point, but we recommend:

```
rvm use 2.7.6
```

Cocoapods will require `CFPropertyList` to be installed as well.

```
gem install CFPropertyList
```

## Installation

Trifle is available through [CocoaPods](https://cocoapods.org). To
install it, simply add the following line to your Podfile:

```ruby
pod 'Trifle'
```

### Generating Wire for iOS

Wire does not have an equivalent plugin for XCode to autogenerate
from the source protobuf files at build time. 

Instead, the following command must be manually executed:

```bash
# This compiles WireCompiler from source to output compiler.jar
# under Example/Pods/WireCompiler/compiler.jar
pod install

java -jar trifle/ios/Example/Pods/WireCompiler/compiler.jar \
    "--proto_path=trifle/proto/app/cash/trifle/api/alpha" \
    "--swift_out=trifle/ios/Trifle/Sources/Generated" \
    "--experimental-module-manifest=trifle/ios/Trifle/manifest.yml"
```

## Authors

Cash App

## License

Trifle is available under the Apache 2.0 license. See the LICENSE file
for more info.
