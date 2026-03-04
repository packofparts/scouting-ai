import SwiftUI

enum AppPage: String, CaseIterable, Identifiable {
    case setup       = "Setup"
    case download    = "Download"
    case detect      = "Detect"
    case analyze     = "Analyze"
    case visualize   = "Visualize"

    var id: String { rawValue }

    var icon: String {
        switch self {
        case .setup:     return "gearshape.fill"
        case .download:  return "arrow.down.circle.fill"
        case .detect:    return "eye.fill"
        case .analyze:   return "chart.bar.fill"
        case .visualize: return "map.fill"
        }
    }

    var description: String {
        switch self {
        case .setup:     return "Configure project folder & API key"
        case .download:  return "Download match video from YouTube"
        case .detect:    return "Run AI robot detection"
        case .analyze:   return "Run tracking analysis"
        case .visualize: return "View robot path visualization"
        }
    }
}

struct ContentView: View {
    @EnvironmentObject var appState: AppState
    @State private var selection: AppPage? = .setup

    var body: some View {
        NavigationSplitView {
            sidebar
        } detail: {
            detailView(for: selection ?? .setup)
        }
        .navigationSplitViewStyle(.balanced)
    }

    @ViewBuilder
    private var sidebar: some View {
        List(selection: $selection) {
            Section("Configuration") {
                NavigationLink(value: AppPage.setup) {
                    SidebarRow(page: .setup, badge: appState.isConfigured ? "Ready" : "Setup Required")
                }
            }

            Section("Pipeline") {
                NavigationLink(value: AppPage.download) {
                    SidebarRow(page: .download, badge: appState.videoExists() ? "Video Ready" : nil)
                }
                NavigationLink(value: AppPage.detect) {
                    SidebarRow(page: .detect, badge: appState.detectionOutputExists() ? "Done" : nil)
                }
                NavigationLink(value: AppPage.analyze) {
                    let teams = appState.csvFilesExist()
                    SidebarRow(page: .analyze, badge: teams.isEmpty ? nil : "\(teams.count) teams")
                }
                NavigationLink(value: AppPage.visualize) {
                    SidebarRow(page: .visualize)
                }
            }
        }
        .listStyle(.sidebar)
        .navigationTitle("P.A.C.K.")
        .frame(minWidth: 200)
    }

    @ViewBuilder
    private func detailView(for page: AppPage) -> some View {
        switch page {
        case .setup:     SetupView()
        case .download:  DownloadView()
        case .detect:    DetectorView()
        case .analyze:   AnalyzerView()
        case .visualize: VisualizationView()
        }
    }
}

struct SidebarRow: View {
    let page: AppPage
    var badge: String? = nil

    var body: some View {
        Label {
            VStack(alignment: .leading, spacing: 2) {
                Text(page.rawValue)
                    .fontWeight(.medium)
                if let badge {
                    Text(badge)
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
            }
        } icon: {
            Image(systemName: page.icon)
                .foregroundColor(iconColor)
        }
        .padding(.vertical, 2)
    }

    private var iconColor: Color {
        switch page {
        case .setup:     return .gray
        case .download:  return .blue
        case .detect:    return .orange
        case .analyze:   return .green
        case .visualize: return .purple
        }
    }
}
