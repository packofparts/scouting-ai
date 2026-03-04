import SwiftUI

struct SetupView: View {
    @EnvironmentObject var appState: AppState
    @StateObject private var runner = ShellRunner()

    @State private var showingFolderPicker = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                // Header
                VStack(alignment: .leading, spacing: 4) {
                    Text("Setup")
                        .font(.title)
                        .fontWeight(.bold)
                    Text("Configure your project folder and Roboflow API key before running the pipeline.")
                        .foregroundColor(.secondary)
                }

                Divider()

                // Project folder
                GroupBox(label: Label("Project Folder", systemImage: "folder.fill")) {
                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            Text(appState.projectPath.isEmpty ? "No folder selected" : appState.projectPath)
                                .font(.system(.body, design: .monospaced))
                                .foregroundColor(appState.projectPath.isEmpty ? .secondary : .primary)
                                .lineLimit(1)
                                .truncationMode(.middle)
                            Spacer()
                            Button("Choose…") {
                                chooseFolder()
                            }
                            .buttonStyle(.bordered)
                        }
                        if !appState.projectPath.isEmpty {
                            HStack(spacing: 8) {
                                fileCheck("matches/", exists: dirExists("matches"))
                                fileCheck("temp/",    exists: dirExists("temp"))
                                fileCheck("data/",    exists: dirExists("data"))
                                fileCheck("field.png",exists: fileExists("field.png"))
                            }
                        }
                    }
                    .padding(8)
                }

                // API Key
                GroupBox(label: Label("Roboflow API Key", systemImage: "key.fill")) {
                    VStack(alignment: .leading, spacing: 10) {
                        SecureField("Enter your Roboflow API key…", text: $appState.apiKey)
                            .textFieldStyle(.roundedBorder)
                        Text("Your key is stored only in macOS UserDefaults (not synced to iCloud).")
                            .font(.caption)
                            .foregroundColor(.secondary)

                        HStack {
                            Button("Save & Write .env") {
                                writeEnvFile()
                            }
                            .buttonStyle(.borderedProminent)
                            .disabled(appState.apiKey.isEmpty || appState.projectPath.isEmpty)

                            if let code = runner.exitCode {
                                StatusBadge(
                                    label: code == 0 ? ".env written" : "Write failed",
                                    status: code == 0 ? .ok : .warning
                                )
                            }
                        }
                    }
                    .padding(8)
                }

                // Dependency check
                GroupBox(label: Label("Dependency Check", systemImage: "checkmark.shield.fill")) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Checks that required tools are on your PATH.")
                            .font(.caption)
                            .foregroundColor(.secondary)

                        Button("Check Dependencies") {
                            checkDeps()
                        }
                        .buttonStyle(.bordered)
                        .disabled(runner.isRunning)

                        if !runner.output.isEmpty {
                            TerminalView(text: runner.output, isRunning: runner.isRunning)
                                .frame(height: 180)
                        }
                    }
                    .padding(8)
                }

                Spacer()
            }
            .padding(24)
        }
    }

    // MARK: - Helpers

    private func fileCheck(_ name: String, exists: Bool) -> some View {
        HStack(spacing: 3) {
            Image(systemName: exists ? "checkmark.circle.fill" : "xmark.circle.fill")
                .foregroundColor(exists ? .green : .red)
                .font(.caption)
            Text(name)
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }

    private func dirExists(_ rel: String) -> Bool {
        var isDir: ObjCBool = false
        let path = (appState.projectPath as NSString).appendingPathComponent(rel)
        return FileManager.default.fileExists(atPath: path, isDirectory: &isDir) && isDir.boolValue
    }

    private func fileExists(_ rel: String) -> Bool {
        let path = (appState.projectPath as NSString).appendingPathComponent(rel)
        return FileManager.default.fileExists(atPath: path)
    }

    private func chooseFolder() {
        let panel = NSOpenPanel()
        panel.title = "Select the scouting-ai project folder"
        panel.canChooseFiles = false
        panel.canChooseDirectories = true
        panel.allowsMultipleSelection = false
        panel.prompt = "Select"
        if panel.runModal() == .OK, let url = panel.url {
            appState.projectPath = url.path
        }
    }

    private func writeEnvFile() {
        let envPath = (appState.projectPath as NSString).appendingPathComponent(".env")
        let content = "ROBOFLOW_API_KEY=\(appState.apiKey)\n"
        runner.clear()
        do {
            try content.write(toFile: envPath, atomically: true, encoding: .utf8)
            runner.output = "Wrote .env successfully.\n"
            runner.exitCode = 0
        } catch {
            runner.output = "Failed to write .env: \(error.localizedDescription)\n"
            runner.exitCode = 1
        }
    }

    private func checkDeps() {
        runner.clear()
        let checks = ["java -version", "python3.12 --version", "yt-dlp --version", "ffmpeg -version | head -1"].joined(separator: " && echo '---' && ")
        runner.run(command: checks, workingDirectory: appState.projectPath.isEmpty ? "/" : appState.projectPath)
    }
}
