// swift-tools-version:5.10

import PackageDescription

let package = Package(
	name: "Trifle",
	platforms: [
		.iOS(.v14),
		.macOS(.v10_15),
	],
	products: [
		.library(
			name: "Trifle",
			targets: ["Trifle"]
		),
	],
	dependencies: [
		.package(
			url: "https://github.com/square/wire",
			exact: "4.9.3"
		),
	],
	targets: [
		.target(
			name: "Trifle",
			dependencies: [
				.product(name: "Wire", package: "wire"),
			],
			path: "ios/Trifle/Sources"
		),
	]
)
