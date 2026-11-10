# Golf Club Handicap Committee App

This desktop application helps golf club handicap committees review member submissions quickly and consistently. It brings together Golf Canada member data, round history, and schedule comparisons so committee members can focus on anomalies instead of manual spreadsheet work.

The app loads the Golf Canada SSL certificate from `src/main/resources/certs/golfcanada.pem` during startup and signs in with a valid Golf Canada username and password when the desktop window opens the login page. For local development or testing against a mock authentication endpoint, you can override the base URL with `APP_GOLF_CANADA_BASE_URL`.

## Contributing

Contributions are welcome. Please review the project guidelines in [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

## Features

The app is designed for committee review workflows, including:

- member verification using Golf Canada profile and history data
- scheduled-round versus played-round comparisons to spot mismatches or missing submissions
- support for PDF scorecard and membership data import
- AI-assisted review prompts that highlight suspicious scoring patterns
- a desktop workflow suitable for committee members and volunteers reviewing member files

### Screenshots

Screenshots of the review workflow will be added here as the desktop experience is finalized.

## Local run and debug

To run the app locally, use the commands below from the repository root. VS Code tasks and launch configurations are included in `.vscode/tasks.json` and `.vscode/launch.json`.

```bash
./gradlew runApp
./gradlew debugApp
```

## AI Integration

Choose AI mode in Settings → AI Features → AI Integration.

### None

Disables AI features entirely.

### Ollama

Use Ollama-backed local inference. Select a model in Settings before running AI review prompts.

#### Local (internal Ollama install)

Use this when Ollama is installed directly on your machine. The app connects to `http://localhost:11434` by default and can be overridden with `APP_AI_OLLAMA_BASE_URL`.

In app settings, choose AI Integration → Local, select a model, and use the model download action when needed.

#### External (Ollama sidecar / Docker)

Use this when Ollama runs outside the app, such as in a Docker container. The repository includes a development sidecar Dockerfile at `Dockerfile.ollama-sidecar` and a root-level `Modelfile` that creates a custom `golf-compliance` model based on `llama3`.

To start the sidecar and configure the application, run:

```bash
docker build -f Dockerfile.ollama-sidecar -t golf-club-handicap-committee-app/ollama-sidecar:dev .
docker run -d --name golf-club-handicap-ollama -p 11434:11434 -v ollama-data:/root/.ollama golf-club-handicap-committee-app/ollama-sidecar:dev
APP_AI_OLLAMA_BASE_URL=http://localhost:11434
./scripts/run-ollama-sidecar.sh
```

The helper script starts the sidecar, waits for `golf-compliance` model initialization, and prints guidance for AI review usage.

### Gemini API

Use Google Gemini with a user-managed API key.

In app settings, choose AI Integration → Gemini API and enter your Gemini API key in the Gemini API Key field.

### AI review guidance

Use AI assistance in the role of a handicap committee member and golf professional. Review member scoring history for suspicious patterns such as missing rounds compared with schedules or unusually strong front holes followed by repeated double- or triple-bogey finishes.

## Release builds

To prepare and package a release from the repository root, use the commands below.

```bash
./gradlew nativeCompile -Pvaadin.productionMode=true
./gradlew prepareSnapshotRelease
./gradlew preparePatchRelease
./gradlew prepareMinorRelease
./gradlew prepareSnapshotRelease -Prelease.dryRun=true
./gradlew preparePatchRelease -Prelease.dryRun=true
./gradlew prepareMinorRelease -Prelease.dryRun=true
git push origin HEAD --follow-tags
```

The native build writes to `build/native/nativeCompile/HandicapCommitteeApp` on Linux/macOS and `.exe` on Windows. GitHub-hosted release preparation is also available from the Prepare Release workflow in GitHub Actions. Pushing a tag such as `v1.2.3` triggers the Release workflow, which builds native binaries for Linux, macOS, and Windows and publishes them with generated release notes.

### 🍏 Running on macOS

Because the macOS application bundle is distributed as an open-source, unsigned app, macOS may show a "Developer cannot be verified" warning on first launch.

Use one of the following options:

```bash
# GUI option
# Right-click the app icon, choose Open, then confirm in the dialog.

# Terminal option
xattr -cr /Applications/GolfClubHandicapCommitteeApp.app
```
