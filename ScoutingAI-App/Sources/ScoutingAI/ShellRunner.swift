import Foundation

class ShellRunner: ObservableObject {
    @Published var output: String = ""
    @Published var isRunning = false
    @Published var exitCode: Int32?

    private var process: Process?
    private var outPipe: Pipe?
    private var errPipe: Pipe?

    // Expanded PATH to cover Homebrew (both Intel and Apple Silicon) + pyenv, etc.
    static let shellPath = [
        "/opt/homebrew/bin",
        "/opt/homebrew/sbin",
        "/usr/local/bin",
        "/usr/bin",
        "/bin",
        "/usr/sbin",
        "/sbin",
        "/opt/homebrew/opt/python@3.12/bin",
        "/usr/local/opt/python@3.12/bin"
    ].joined(separator: ":")

    func run(
        command: String,
        workingDirectory: String,
        extraEnv: [String: String] = [:],
        onComplete: ((Int32) -> Void)? = nil
    ) {
        guard !isRunning else { return }

        output = ""
        isRunning = true
        exitCode = nil

        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            guard let self else { return }

            let task = Process()
            task.executableURL = URL(fileURLWithPath: "/bin/bash")
            // -l loads the user's shell profile so tools like yt-dlp, java, python are on PATH
            task.arguments = ["-l", "-c", command]
            task.currentDirectoryURL = URL(fileURLWithPath: workingDirectory)

            var env = ProcessInfo.processInfo.environment
            env["PATH"] = ShellRunner.shellPath + ":" + (env["PATH"] ?? "")
            for (k, v) in extraEnv { env[k] = v }
            task.environment = env

            let out = Pipe()
            let err = Pipe()
            task.standardOutput = out
            task.standardError = err
            self.process = task
            self.outPipe = out
            self.errPipe = err

            out.fileHandleForReading.readabilityHandler = { [weak self] handle in
                let data = handle.availableData
                guard !data.isEmpty, let str = String(data: data, encoding: .utf8) else { return }
                DispatchQueue.main.async { self?.output += str }
            }

            err.fileHandleForReading.readabilityHandler = { [weak self] handle in
                let data = handle.availableData
                guard !data.isEmpty, let str = String(data: data, encoding: .utf8) else { return }
                DispatchQueue.main.async { self?.output += str }
            }

            do {
                try task.run()
                task.waitUntilExit()
            } catch {
                DispatchQueue.main.async { [weak self] in
                    self?.output += "\nFailed to launch: \(error.localizedDescription)\n"
                }
            }

            // Drain remaining output
            let remaining = out.fileHandleForReading.readDataToEndOfFile()
            if let str = String(data: remaining, encoding: .utf8), !str.isEmpty {
                DispatchQueue.main.async { self.output += str }
            }

            DispatchQueue.main.async { [weak self] in
                guard let self else { return }
                self.outPipe?.fileHandleForReading.readabilityHandler = nil
                self.errPipe?.fileHandleForReading.readabilityHandler = nil
                self.isRunning = false
                self.exitCode = task.terminationStatus
                onComplete?(task.terminationStatus)
            }
        }
    }

    func terminate() {
        process?.terminate()
    }

    func clear() {
        output = ""
        exitCode = nil
    }
}
