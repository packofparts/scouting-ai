import SwiftUI

struct TeamSlot: Identifiable {
    let id: Int
    var number: String = ""
    var noShow: Bool = false

    var value: String {
        noShow ? "no_show" : (number.isEmpty ? "no_show" : number)
    }
}

struct AnalyzerView: View {
    @EnvironmentObject var appState: AppState
    @StateObject private var runner = ShellRunner()

    // Left side: index 0 = closest to camera, 2 = furthest
    @State private var leftTeams  = [TeamSlot(id: 0), TeamSlot(id: 1), TeamSlot(id: 2)]
    // Right side: index 0 = closest to camera, 2 = furthest
    @State private var rightTeams = [TeamSlot(id: 3), TeamSlot(id: 4), TeamSlot(id: 5)]

    @State private var outputExists = false
    @State private var csvTeams: [String] = []

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                StageHeader(
                    title: "Analyzer",
                    subtitle: "Assigns detections to team numbers and outputs per-team CSV tracking data.",
                    isRunning: runner.isRunning,
                    canRun: canRun,
                    onRun: runAnalyzer,
                    onCancel: runner.terminate
                )

                Divider()

                // Status badges
                HStack(spacing: 12) {
                    StatusBadge(
                        label: outputExists ? "output.json ready" : "No detection output — run Detect first",
                        status: outputExists ? .ok : .warning
                    )
                    if !csvTeams.isEmpty {
                        StatusBadge(label: "Teams: \(csvTeams.joined(separator: ", "))", status: .ok)
                    }
                }

                // Team number inputs
                HStack(alignment: .top, spacing: 16) {
                    teamColumn(title: "Left Side (Red or Blue)", slots: $leftTeams)
                    Divider()
                    teamColumn(title: "Right Side", slots: $rightTeams)
                }

                // Info
                GroupBox(label: Label("Order matters", systemImage: "info.circle.fill")) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Enter teams ordered by camera distance: **closest → middle → furthest**.")
                        Text("Check **No Show** for any team that didn't participate. You must supply all 6 slots.")
                    }
                    .font(.callout)
                    .padding(8)
                }

                // Terminal
                TerminalView(text: runner.output, isRunning: runner.isRunning)
                    .frame(minHeight: 220)

                if let code = runner.exitCode {
                    HStack(spacing: 8) {
                        StatusBadge(
                            label: code == 0 ? "Analysis complete — CSVs saved to data/" : "Analysis failed (exit \(code))",
                            status: code == 0 ? .ok : .warning
                        )
                        if code == 0 {
                            Button("Reveal data/ folder") { revealData() }
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

    // MARK: - Subviews

    @ViewBuilder
    private func teamColumn(title: String, slots: Binding<[TeamSlot]>) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.headline)

            ForEach(slots.wrappedValue.indices, id: \.self) { i in
                HStack(spacing: 8) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(positionLabel(i))
                            .font(.caption)
                            .foregroundColor(.secondary)
                        TextField("Team #", text: Binding(
                            get: { slots.wrappedValue[i].number },
                            set: { slots.wrappedValue[i].number = $0 }
                        ))
                        .textFieldStyle(.roundedBorder)
                        .frame(width: 90)
                        .disabled(slots.wrappedValue[i].noShow)
                    }
                    Toggle("No Show", isOn: Binding(
                        get: { slots.wrappedValue[i].noShow },
                        set: { slots.wrappedValue[i].noShow = $0 }
                    ))
                    .toggleStyle(.checkbox)
                    .font(.caption)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private func positionLabel(_ index: Int) -> String {
        switch index {
        case 0: return "Closest to camera"
        case 1: return "Middle"
        case 2: return "Furthest from camera"
        default: return "Robot \(index + 1)"
        }
    }

    // MARK: - Logic

    private var canRun: Bool {
        outputExists && appState.isConfigured && !runner.isRunning
    }

    private func refresh() {
        outputExists = appState.detectionOutputExists()
        csvTeams = appState.csvFilesExist()
    }

    private func runAnalyzer() {
        let allTeams = leftTeams.map(\.value) + rightTeams.map(\.value)
        let args = allTeams.joined(separator: " ")

        let cmd = """
        javac src/*.java json/*.java && \
        java -cp "src:json" AIScout \(args)
        """
        runner.run(command: cmd, workingDirectory: appState.projectPath) { code in
            self.refresh()
        }
    }

    private func revealData() {
        let dataURL = URL(fileURLWithPath: appState.dataDirectoryPath)
        NSWorkspace.shared.open(dataURL)
    }
}
