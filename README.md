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
- Pushing a tag such as `v1.2.3` triggers the `Release` workflow, which:
  - builds zipped application bundles for macOS and Windows
  - publishes both zip files to the matching GitHub Release
  - enables GitHub-generated release notes as the release changelog
