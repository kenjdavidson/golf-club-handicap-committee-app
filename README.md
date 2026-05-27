# golf-club-handicap-committee-app
Desktop application used by Handicap Committees to verify members

Includes the Golf Canada SSL certificate at `src/main/resources/certs/golfcanada.pem`, loaded into the application's SSL trust configuration during startup.

Sign in with a valid Golf Canada username and password when the application opens the login page.

For local development or testing against a mock auth endpoint, the Golf Canada base URL can be overridden with `APP_GOLF_CANADA_BASE_URL`.

## Local run and debug

- Run app from Gradle: `./gradlew runApp`
- Run app with remote debug on port 5005: `./gradlew debugApp`
- VS Code tasks/launch configs are included in `.vscode/tasks.json` and `.vscode/launch.json`.

## Release builds

- Build a self-contained zip for the current platform: `./gradlew -Pproduction jpackageAppArchive`
- Create a Windows installer locally: `./gradlew -Pproduction jpackageInstaller`
- Desktop package icons are sourced from `src/main/resources/static/icons` (same icon used by browser toolbar and jpackage bundles)
- Override package metadata when needed: `-Pjpackage.mac.package.identifier=... -Pjpackage.mac.package.name=... -Pjpackage.win.upgrade.uuid=...`
- Prepare a date-based snapshot tag without bumping the project version: `./gradlew prepareSnapshotRelease`
- Prepare a semantic patch release and bump to the next patch snapshot: `./gradlew preparePatchRelease`
- Prepare a semantic minor release and bump to the next minor snapshot: `./gradlew prepareMinorRelease`
- Preview any release task without changing git state: append `-Prelease.dryRun=true`
- After preparing a release locally, push the version bump commit and tag with: `git push origin HEAD --follow-tags`
- GitHub-hosted release preparation is also available from the **Prepare Release** workflow via Actions → Run workflow and choose both the target branch and `snapshot`, `patch`, or `minor`
  - Use `main` for snapshot and minor releases
  - Use the matching `releases/patch/...` branch for patch releases
- Pushing a tag such as `v1.2.3` triggers the `Release` workflow, which:
  - builds zipped application bundles for macOS and Windows
  - publishes both zip files to the matching GitHub Release
  - enables GitHub-generated release notes as the release changelog

### 🍏 Running on macOS

Because the macOS application bundle is distributed as an open-source, unsigned app, macOS may show a **"Developer cannot be verified"** warning the first time you open it.

- **Option A (GUI):** Right-click (or Control-click) the application icon, choose **Open**, then click **Open** again in the confirmation dialog.
- **Option B (Terminal):** Remove the quarantine flag before opening the app:

  ```bash
  xattr -cr /Applications/GolfClubHandicapCommitteeApp.app
  ```
