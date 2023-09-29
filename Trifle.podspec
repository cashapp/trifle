Pod::Spec.new do |s|
  s.name             = 'Trifle'
  s.version          = '0.2.5'
  s.summary          = 'Security related functions.'

  s.description      = <<-DESC
Security functionality for interoperability/interaction with core services.
                       DESC

  s.homepage         = 'https://github.com/cashapp/trifle/tree/main/ios'
  s.license          = { :type => 'Apache License, Version 2.0', :file => 'LICENSE' }
  s.author           = 'Cash App'
  s.source           = { :git => 'https://github.com/cashapp/trifle.git', :tag => s.version.to_s }

  s.ios.deployment_target = '14.0'

  s.source_files = 'ios/Trifle/Sources/**/*.swift'

  s.dependency 'Wire', '~> 4'

  s.swift_versions = '5.0.1'
end
