# golf-club-handicap-committee-app
Desktop application used by Handicap Committees to verify members

Includes the Golf Canada SSL certificate at `src/main/resources/certs/golfcanada.pem`, loaded into the application's SSL trust configuration during startup.

Sign in with a valid Golf Canada username and password when the desktop window opens the login page.

For local development or testing against a mock auth endpoint, the Golf Canada base URL can be overridden with `APP_GOLF_CANADA_BASE_URL`.

## Local run and debug

- Run app from Gradle: `./gradlew runApp`
- Run app with remote debug on port 5005: `./gradlew debugApp`
- VS Code tasks/launch configs are included in `.vscode/tasks.json` and `.vscode/launch.json`.

## AI Integration

Choose AI mode in **Settings → AI Features → AI Integration**.

### None

Disables AI features entirely.

### Ollama

Use Ollama-backed local inference. Select a model in Settings before running AI review prompts.

#### Local (internal Ollama install)

Use this when Ollama is installed directly on your machine. The app connects to `http://localhost:11434` by default (override with `APP_AI_OLLAMA_BASE_URL`).

- In app settings, choose **AI Integration → Local**
- Select a model in the model picker
- Use the download button to pull models from Ollama when needed

#### External (Ollama sidecar / Docker)

Use this when Ollama runs outside the app (for example as a Docker container). You manage container lifecycle and model availability.

This repository ships a development sidecar Dockerfile at `Dockerfile.ollama-sidecar` plus a root-level `Modelfile` that creates a custom `golf-compliance` model (base: `llama3`) with a handicap-committee system prompt.

- Build the sidecar image: `docker build -f Dockerfile.ollama-sidecar -t golf-club-handicap-committee-app/ollama-sidecar:dev .`
- Run it on the default Ollama port: `docker run -d --name golf-club-handicap-ollama -p 11434:11434 -v ollama-data:/root/.ollama golf-club-handicap-committee-app/ollama-sidecar:dev`
- Set the application endpoint to the sidecar: `APP_AI_OLLAMA_BASE_URL=http://localhost:11434`
- In app settings, choose **AI Integration → External (Docker)**

On startup, the sidecar initializes the custom model by:
- starting Ollama,
- pulling `llama3`,
- creating `golf-compliance` from `./Modelfile`.

The model is stored under `/root/.ollama` and persists via the `ollama-data` Docker volume, so subsequent runs are immediately ready.

For convenience in development, run:

```bash
./scripts/run-ollama-sidecar.sh
```

The helper script starts the sidecar, waits for `golf-compliance` model initialization, and prints role guidance for AI reviews.

### Gemini API

Use Google Gemini with a user-managed API key.

- In app settings, choose **AI Integration → Gemini API**
- Enter your Gemini API key in the **Gemini API Key** field
- Keep your selected review workflow/prompt text the same as other integration types

### AI review guidance

Use AI assistance in the role of a handicap committee member and golf professional.
Review member scoring history for suspicious patterns (for example, missing rounds compared to schedules, or very strong front holes followed by repeated double/triple-bogey finishes).

## Release builds

- Build a GraalVM native image locally: `./gradlew nativeCompile -Pvaadin.productionMode=true`
  - Output binary: `build/native/nativeCompile/HandicapCommitteeApp` (or `.exe` on Windows)
- Prepare a date-based snapshot tag without bumping the project version: `./gradlew prepareSnapshotRelease`
- Prepare a semantic patch release and bump to the next patch snapshot: `./gradlew preparePatchRelease`
- Prepare a semantic minor release and bump to the next minor snapshot: `./gradlew prepareMinorRelease`
- Preview any release task without changing git state: append `-Prelease.dryRun=true`
- After preparing a release locally, push the version bump commit and tag with: `git push origin HEAD --follow-tags`
- GitHub-hosted release preparation is also available from the **Prepare Release** workflow via Actions → Run workflow and choose both the target branch and `snapshot`, `patch`, or `minor`
  - Use `main` for snapshot and minor releases
  - Use the matching `releases/patch/...` branch for patch releases
- Pushing a tag such as `v1.2.3` triggers the `Release` workflow, which:
  - builds GraalVM AOT-compiled native image binaries for Linux, macOS, and Windows
  - publishes all three zipped binaries to the matching GitHub Release
  - enables GitHub-generated release notes as the release changelog

### 🍏 Running on macOS

Because the macOS application bundle is distributed as an open-source, unsigned app, macOS may show a **"Developer cannot be verified"** warning the first time you open it.

- **Option A (GUI):** Right-click (or Control-click) the application icon, choose **Open**, then click **Open** again in the confirmation dialog.
- **Option B (Terminal):** Remove the quarantine flag before opening the app:

  ```bash
  xattr -cr /Applications/GolfClubHandicapCommitteeApp.app
  ```
