# Contributing to FOG Mobile

Thank you for helping improve FOG Mobile. Contributions should preserve the project's local-first privacy model and honest capability reporting.

## Before opening a change

1. Search existing issues and discussions.
2. Open an issue for packet-engine changes, new dependencies, or architecture changes before implementation.
3. Keep pull requests focused and explain user-visible behavior.
4. Never include signing keys, credentials, traffic payloads, cookies, tokens, personal diagnostic logs, or copyrighted third-party binaries.

## Development setup

Use JDK 17, Android SDK Platform 36, and the included Gradle Wrapper.

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest
```

Run device tests when an emulator or physical Android device is available:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

## Pull request checklist

- The change matches the existing package boundaries and Compose style.
- Success states are backed by real checks.
- New retries and profile loops are bounded.
- Network code has timeouts and does not log payloads.
- VPN changes cannot route traffic into a non-forwarding TUN.
- Tests cover policy or state-machine changes.
- `testDebugUnitTest`, `lintDebug`, and `assembleDebug` pass.
- Documentation describes limitations and privacy impact.

## Packet-engine proposals

A production `NetworkEngine` must have a license compatible with MIT distribution, support protected outbound sockets, implement TCP and UDP forwarding correctly, and include packet/flow tests plus real-device validation. A wrapper around an unreviewed binary is not sufficient.

## Commit and review style

Use concise imperative commit messages. Keep unrelated refactors separate. Reviews prioritize network correctness, lifecycle safety, privacy, licensing, and regression coverage.
