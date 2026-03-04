import SwiftUI

struct DetectorView: View {
    @EnvironmentObject var appState: AppState
    @StateObject private var runner = ShellRunner()

    @State private var videoExists = false
    @State private var outputExists = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                StageHeader(
                    title: "AI Detector",
                    subtitle: "Runs detector.py — sends each video frame to the Roboflow model and outputs detections to temp/output.json.",
                    isRunning: runner.isRunning,
                    canRun: canRun,
                    onRun: runDetector,
                    onCancel: runner.terminate
                )

                Divider()

                // Status badges
                HStack(spacing: 12) {
                    StatusBadge(
                        label: videoExists ? "match.mp4 ready" : "No video — run Download first",
                        status: videoExists ? .ok : .warning
                    )
                    StatusBadge(
                        label: outputExists ? "output.json exists" : "No previous output",
                        status: outputExists ? .ok : .idle
                    )
                    if !appState.isConfigured {
                        StatusBadge(label: "Project not configured", status: .warning)
                    }
                }

                // Info box
                GroupBox(label: Label("What this does", systemImage: "info.circle.fill")) {
                    VStack(alignment: .leading, spacing: 6) {
                        infoRow("Uses Roboflow model: 1294-ai-scouting/10")
                        infoRow("Reads matches/match.mp4 frame by frame")
                        infoRow("Outputs robot bounding boxes to temp/output.json")
                        infoRow("Takes several minutes depending on video length")
                        infoRow("API key is read from .env in the project folder")
                    }
                    .padding(8)
                }

                // Warning if no video
                if !videoExists && !runner.isRunning {
                    HStack(spacing: 8) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundColor(.orange)
                        Text("No match video found at matches/match.mp4. Run the Download stage first.")
                            .font(.callout)
                            .foregroundColor(.secondary)
                    }
                    .padding()
                    .background(Color.orange.opacity(0.1))
                    .cornerRadius(8)
                }

                // Terminal output
                TerminalView(text: runner.output, isRunning: runner.isRunning)
                    .frame(minHeight: 260)

                if let code = runner.exitCode {
                    HStack(spacing: 8) {
                        StatusBadge(
                            label: code == 0 ? "Detection complete — output.json saved" : "Detection failed (exit \(code))",
                            status: code == 0 ? .ok : .warning
                        )
                        if code == 0 {
                            Button("Reveal output.json") { revealOutput() }
                                .buttonStyle(.bordered)
                                .font(.caption)
                        }
                    }
                }

                Spacer()
            }
            .padding(24)
        }
        .onAppear { refresh() }
    }

    private var canRun: Bool {
        videoExists && appState.isConfigured && !runner.isRunning
    }

    private func refresh() {
        videoExists = appState.videoExists()
        outputExists = appState.detectionOutputExists()
    }

    private func runDetector() {
        runner.run(
            command: "python3.12 detector.py",
            workingDirectory: appState.projectPath
        ) { code in
            self.refresh()
        }
    }

    private func revealOutput() {
        NSWorkspace.shared.selectFile(appState.detectionOutputPath, inFileViewerRootedAtPath: "")
    }

    private func infoRow(_ text: String) -> some View {
        HStack(alignment: .top, spacing: 6) {
            Image(systemName: "checkmark.circle.fill")
                .foregroundColor(.green)
                .font(.caption)
                .padding(.top, 2)
            Text(text)
                .font(.callout)
        }
    }
}
