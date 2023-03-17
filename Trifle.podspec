Pod::Spec.new do |s|
  s.name             = 'Trifle'
  s.version          = '0.1.3'
  s.summary          = 'Security related functions.'

  s.description      = <<-DESC
Security functionality for interoperability/interaction with core services.
                       DESC

  s.homepage         = 'https://github.com/cashapp/trifle/tree/main/ios'
  s.license          = { :type => 'Apache License, Version 2.0', :file => 'ios/LICENSE' }
  s.author           = 'Cash App'
  s.source           = { :git => 'https://github.com/cashapp/trifle.git', :tag => s.version.to_s }

  s.ios.deployment_target = '14.0'

  s.static_framework = true

  s.script_phase = {
    name: 'Fix Tink for Apple Silicon',
    script: '"$PODS_ROOT"/../../Scripts/XcodeBuildPhases/FixTinkForAppleSilicon',
    execution_position: :before_compile
  }

  s.preserve_paths = 'ios/Scripts/**/*'

  s.pod_target_xcconfig = {
      'FRAMEWORK_SEARCH_PATHS[sdk=iphonesimulator*][arch=arm64]' => '"$DERIVED_FILE_DIR/apple_silicon" $FRAMEWORK_SEARCH_PATHS',
  }

  s.source_files = 'ios/Trifle/Sources/**/*.swift'

  s.dependency 'Wire', '~> 4.5.1'
  s.dependency 'Tink', '~> 1.6.1'

  s.swift_versions = '5.0.1'
end
