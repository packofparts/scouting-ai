import SwiftUI

struct DownloadView: View {
    @EnvironmentObject var appState: AppState
    @StateObject private var runner = ShellRunner()

    @State private var youtubeURL = ""
    @State private var videoExists = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                StageHeader(
                    title: "Download Match Video",
                    subtitle: "Downloads a YouTube match video using yt-dlp at 720p.",
                    isRunning: runner.isRunning,
                    canRun: canRun,
                    onRun: runDownload,
                    onCancel: runner.terminate
                )

                Divider()

                // Status row
                HStack(spacing: 12) {
                    StatusBadge(
                        label: videoExists ? "match.mp4 present" : "No video yet",
                        status: videoExists ? .ok : .idle
                    )
                    if !appState.isConfigured {
                        StatusBadge(label: "Project not configured", status: .warning)
                    }
                }

                // URL input
                GroupBox(label: Label("YouTube URL", systemImage: "play.rectangle.fill")) {
                    VStack(alignment: .leading, spacing: 10) {
                        TextField("https://www.youtube.com/watch?v=…", text: $youtubeURL)
                            .textFieldStyle(.roundedBorder)

                        Text("Use a wide-angle match recording. The video will be saved to matches/match.mp4.")
                            .font(.caption)
                            .foregroundColor(.secondary)

                        HStack(spacing: 8) {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .foregroundColor(.orange)
                                .font(.caption)
                            Text("A cookies file must be present in the project folder to bypass YouTube bot detection.")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(8)
                }

                // Terminal
                TerminalView(text: runner.output, isRunning: runner.isRunning)
                    .frame(minHeight: 200)

                if let code = runner.exitCode {
                    HStack(spacing: 8) {
                        StatusBadge(
                            label: code == 0 ? "Download complete" : "Download failed (exit \(code))",
                            status: code == 0 ? .ok : .warning
                        )
                        if code == 0 {
                            Button("Reveal in Finder") { revealVideo() }
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
        !youtubeURL.isEmpty && appState.isConfigured && !runner.isRunning
    }

    private func refresh() {
        videoExists = appState.videoExists()
    }

    private func runDownload() {
        let url = youtubeURL.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !url.isEmpty else { return }
        let cmd = """
        yt-dlp -f "bestvideo[height<=720]+bestaudio/best[height<=720]" \
            -o "matches/match.mp4" \
            --cookies cookies \
            --merge-output-format mp4 \
            "\(url)"
        """
        runner.run(command: cmd, workingDirectory: appState.projectPath) { code in
            self.refresh()
        }
    }

    private func revealVideo() {
        let path = appState.matchVideoPath
        NSWorkspace.shared.selectFile(path, inFileViewerRootedAtPath: "")
    }
}
