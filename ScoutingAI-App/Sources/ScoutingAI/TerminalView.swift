import SwiftUI

/// A scrollable, dark-background terminal-style output view.
/// Automatically scrolls to the bottom as new output arrives.
struct TerminalView: View {
    let text: String
    var isRunning: Bool = false

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            ScrollViewReader { proxy in
                ScrollView(.vertical) {
                    Text(text.isEmpty ? "No output yet." : text)
                        .font(.system(.caption, design: .monospaced))
                        .foregroundColor(text.isEmpty ? .secondary : .green)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(10)
                        .id("bottom")
                }
                .onChange(of: text) {
                    withAnimation(.easeOut(duration: 0.1)) {
                        proxy.scrollTo("bottom", anchor: .bottom)
                    }
                }
            }
            .background(Color(nsColor: .black))
            .cornerRadius(8)

            if isRunning {
                HStack(spacing: 4) {
                    ProgressView()
                        .scaleEffect(0.6)
                        .progressViewStyle(.circular)
                    Text("Running…")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
                .padding(6)
                .background(.ultraThinMaterial)
                .cornerRadius(6)
                .padding(8)
            }
        }
    }
}

/// A standard section header + run/cancel button row used by each stage.
struct StageHeader: View {
    let title: String
    let subtitle: String
    let isRunning: Bool
    let canRun: Bool
    let onRun: () -> Void
    let onCancel: () -> Void

    var body: some View {
        HStack(alignment: .center) {
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.title2)
                    .fontWeight(.bold)
                Text(subtitle)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            Spacer()
            if isRunning {
                Button("Cancel", role: .cancel, action: onCancel)
                    .buttonStyle(.bordered)
            } else {
                Button("Run", action: onRun)
                    .buttonStyle(.borderedProminent)
                    .disabled(!canRun)
            }
        }
    }
}

/// Small inline status indicator.
struct StatusBadge: View {
    enum Status { case ok, warning, idle }
    let label: String
    let status: Status

    var body: some View {
        HStack(spacing: 4) {
            Circle()
                .fill(color)
                .frame(width: 8, height: 8)
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
        .background(color.opacity(0.12))
        .cornerRadius(20)
    }

    private var color: Color {
        switch status {
        case .ok:      return .green
        case .warning: return .orange
        case .idle:    return .gray
        }
    }
}
