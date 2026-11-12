# Contributing

Thanks for your interest in improving the Golf Club Handicap Committee App.

## Development workflow

1. Fork the repository and create a branch for your change.
2. Keep changes focused and aligned with the app's current architecture.
3. Run the relevant tests for the area you changed before opening a pull request.
4. Update documentation when behavior or setup changes.

## Local setup

From the repository root, start with:

```bash
./gradlew test
./gradlew runApp
```

If you are working on a change that affects the desktop front end or release packaging, also review the VS Code debug configuration in `.vscode/launch.json` and `.vscode/tasks.json`.

## Pull request expectations

- keep commits small and easy to review
- explain the problem and the fix in the PR description
- include screenshots or short walkthroughs when UI changes are involved
- make sure the app still builds and the relevant automated checks pass

## Reporting issues

When you report a bug or request a feature, include:

- the version or commit you are using
- the OS and environment
- steps to reproduce
- the expected behavior and the actual behavior
- any screenshots, logs, or sample files that help explain the issue
