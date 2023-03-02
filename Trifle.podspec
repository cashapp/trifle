Pod::Spec.new do |s|
  s.name             = 'Trifle'
  s.version          = '0.1.2'
  s.summary          = 'Security related functions.'

  s.description      = <<-DESC
Security functionality for interoperability/interaction with core services.
                       DESC

  s.homepage         = 'https://github.com/cashapp/trifle/tree/main/ios'
  s.license          = { :type => 'Apache License, Version 2.0', :file => 'ios/LICENSE' }
  s.author           = 'Cash App'
  s.source           = { :git => 'https://github.com/cashapp/trifle.git', :tag => s.version.to_s }

  s.ios.deployment_target = '14.0'

  s.source_files = 'ios/Trifle/Sources/**/*'

  s.swift_versions = '5.0.1'

  s.test_spec 'UnitTests' do |test_spec|
    test_spec.source_files = 'ios/Example/Tests/**/*'
    test_spec.dependency 'Quick', '~> 2.2.0'
  end
end
