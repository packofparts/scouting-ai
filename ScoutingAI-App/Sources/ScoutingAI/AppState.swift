import Foundation
import Combine

class AppState: ObservableObject {
    @Published var projectPath: String {
        didSet { UserDefaults.standard.set(projectPath, forKey: "projectPath") }
    }
    @Published var apiKey: String {
        didSet { UserDefaults.standard.set(apiKey, forKey: "apiKey") }
    }

    // Stage completion tracking
    @Published var videoDownloaded = false
    @Published var detectionComplete = false
    @Published var analysisComplete = false

    init() {
        self.projectPath = UserDefaults.standard.string(forKey: "projectPath")
            ?? FileManager.default.homeDirectoryForCurrentUser.path
        self.apiKey = UserDefaults.standard.string(forKey: "apiKey") ?? ""
    }

    var isConfigured: Bool {
        !projectPath.isEmpty && !apiKey.isEmpty
    }

    var matchVideoPath: String {
        (projectPath as NSString).appendingPathComponent("matches/match.mp4")
    }

    var detectionOutputPath: String {
        (projectPath as NSString).appendingPathComponent("temp/output.json")
    }

    var dataDirectoryPath: String {
        (projectPath as NSString).appendingPathComponent("data")
    }

    func videoExists() -> Bool {
        FileManager.default.fileExists(atPath: matchVideoPath)
    }

    func detectionOutputExists() -> Bool {
        FileManager.default.fileExists(atPath: detectionOutputPath)
    }

    func csvFilesExist() -> [String] {
        let dataURL = URL(fileURLWithPath: dataDirectoryPath)
        let files = (try? FileManager.default.contentsOfDirectory(
            at: dataURL,
            includingPropertiesForKeys: nil
        )) ?? []
        return files
            .filter { $0.pathExtension == "csv" }
            .map { $0.deletingPathExtension().lastPathComponent }
            .sorted()
    }
}
