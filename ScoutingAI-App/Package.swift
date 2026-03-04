// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "ScoutingAI",
    platforms: [.macOS(.v14)],
    targets: [
        .executableTarget(
            name: "ScoutingAI",
            path: "Sources/ScoutingAI"
        )
    ]
)
