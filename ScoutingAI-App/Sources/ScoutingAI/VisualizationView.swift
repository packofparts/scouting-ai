import SwiftUI

struct VisualizationView: View {
    @EnvironmentObject var appState: AppState
    @StateObject private var runner = ShellRunner()

    @State private var teamNumber = ""
    @State private var displayMode = 0   // 0 = Auto + Teleop, 1 = Auto only, 2 = Teleop only
    @State private var csvTeams: [String] = []

    let displayModes = ["Auto + Teleop", "Auto Only", "Teleop Only"]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Header
                VStack(alignment: .leading, spacing: 4) {
                    Text("Visualize")
                        .font(.title)
                        .fontWeight(.bold)
                    Text("Launches the Java Swing visualization window showing robot paths overlaid on the field.")
                        .foregroundColor(.secondary)
                }

                Divider()

                // Status
                HStack(spacing: 12) {
                    StatusBadge(
                        label: csvTeams.isEmpty ? "No CSV data — run Analyze first" : "\(csvTeams.count) teams available",
                        status: csvTeams.isEmpty ? .warning : .ok
                    )
                }

                // Available teams
                if !csvTeams.isEmpty {
                    GroupBox(label: Label("Available Teams", systemImage: "person.3.fill")) {
                        FlowLayout(spacing: 8) {
                            ForEach(csvTeams, id: \.self) { team in
                                Button(team) { teamNumber = team }
                                    .buttonStyle(TeamChipStyle(selected: teamNumber == team))
                            }
                        }
                        .padding(8)
                    }
                }

                // Controls
                GroupBox(label: Label("Launch Settings", systemImage: "slider.horizontal.3")) {
                    VStack(alignment: .leading, spacing: 14) {
                        HStack {
                            Text("Team Number:")
                                .frame(width: 120, alignment: .leading)
                            TextField("e.g. 1294", text: $teamNumber)
                                .textFieldStyle(.roundedBorder)
                                .frame(width: 120)
                        }

                        HStack {
                            Text("Display Mode:")
                                .frame(width: 120, alignment: .leading)
                            Picker("", selection: $displayMode) {
                                ForEach(0..<displayModes.count, id: \.self) { i in
                                    Text(displayModes[i]).tag(i)
                                }
                            }
                            .pickerStyle(.segmented)
                            .frame(maxWidth: 300)
                        }

                        HStack {
                            Button {
                                launchVisualization()
                            } label: {
                                Label("Launch Visualization", systemImage: "play.fill")
                            }
                            .buttonStyle(.borderedProminent)
                            .disabled(!canLaunch)

                            if runner.isRunning {
                                Button("Close", role: .cancel) {
                                    runner.terminate()
                                }
                                .buttonStyle(.bordered)
                            }
                        }

                        Text("The visualization opens as a separate Java window. You can launch multiple windows for different teams.")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    .padding(8)
                }

                // Color key
                GroupBox(label: Label("Color Key", systemImage: "paintpalette.fill")) {
                    HStack(spacing: 20) {
                        colorKey(color: Color(red: 1.0, green: 0.4, blue: 0.8), label: "Autonomous period")
                        colorKey(color: Color(red: 0.0, green: 0.9, blue: 0.9), label: "Teleop period")
                    }
                    .padding(8)
                }

                // Terminal (shows Java stderr/stdout while the process runs)
                if !runner.output.isEmpty {
                    TerminalView(text: runner.output, isRunning: runner.isRunning)
                        .frame(minHeight: 120)
                }

                if let code = runner.exitCode, code != 0 {
                    StatusBadge(label: "Visualization exited with code \(code)", status: .warning)
                }

                Spacer()
            }
            .padding(24)
        }
        .onAppear { refresh() }
    }

    // MARK: - Subviews

    private func colorKey(color: Color, label: String) -> some View {
        HStack(spacing: 6) {
            RoundedRectangle(cornerRadius: 3)
                .fill(color)
                .frame(width: 32, height: 12)
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }

    // MARK: - Logic

    private var canLaunch: Bool {
        !teamNumber.isEmpty && appState.isConfigured && !csvTeams.isEmpty
    }

    private func refresh() {
        csvTeams = appState.csvFilesExist()
    }

    private func launchVisualization() {
        // displayMode: 0 = both (input "3"), 1 = auto only (input "1"), 2 = teleop only (input "2")
        let modeInput = displayMode == 0 ? "3" : displayMode == 1 ? "1" : "2"
        // Pipe team number and display mode into Visualization's stdin
        let cmd = """
        printf '\(teamNumber)\\n\(modeInput)\\n' | java -cp "src:json" Visualization
        """
        runner.run(command: cmd, workingDirectory: appState.projectPath)
    }
}

// MARK: - Supporting Types

struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        layout(in: proposal.width ?? 400, subviews: subviews).size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = layout(in: bounds.width, subviews: subviews)
        for (index, frame) in result.frames.enumerated() {
            subviews[index].place(at: CGPoint(x: bounds.minX + frame.minX, y: bounds.minY + frame.minY), proposal: .unspecified)
        }
    }

    private func layout(in width: CGFloat, subviews: Subviews) -> (size: CGSize, frames: [CGRect]) {
        var frames: [CGRect] = []
        var currentX: CGFloat = 0
        var currentY: CGFloat = 0
        var lineHeight: CGFloat = 0
        var maxX: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if currentX + size.width > width && currentX > 0 {
                currentY += lineHeight + spacing
                currentX = 0
                lineHeight = 0
            }
            frames.append(CGRect(origin: CGPoint(x: currentX, y: currentY), size: size))
            lineHeight = max(lineHeight, size.height)
            currentX += size.width + spacing
            maxX = max(maxX, currentX)
        }

        return (CGSize(width: maxX, height: currentY + lineHeight), frames)
    }
}

struct TeamChipStyle: ButtonStyle {
    var selected: Bool

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.callout.monospaced())
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(selected ? Color.accentColor : Color.secondary.opacity(0.15))
            .foregroundColor(selected ? .white : .primary)
            .cornerRadius(20)
            .scaleEffect(configuration.isPressed ? 0.95 : 1)
    }
}
