# FOG Mobile

FOG Mobile is a local-first Android network diagnostics app inspired by the UI/Agent/Engine
separation in [FOG Prime](https://github.com/DenisRapira/FOG-Prime). It targets Android 8.0+
(`minSdk 26`, `targetSdk 36`) and uses Kotlin, Jetpack Compose, Hilt, DataStore, and
`VpnService`.

## What works

- Real DNS and certificate-validated TLS handshakes for Instagram and YouTube endpoints.
- CDN/media endpoint checks and an explicitly non-authoritative UDP/443 route probe.
- Network observation, bounded AUTO profile selection, per-network preferences, diagnostics,
  anonymized log export, onboarding, settings, and status UI.
- Official-package allowlist resolution for Instagram, YouTube, and YouTube Music.
- Foreground `VpnService` lifecycle and Android VPN permission flow.
- Signed release APK/AAB, adaptive and monochrome icons, R8 shrinking, unit tests, lint, and
  compiled instrumentation tests.
- No remote VPN backend, MITM, user CA, account, analytics, or traffic-content logging.

## Important status

The included `BaselineLocalNetworkEngine` is intentionally marked non-operational: it does not
contain a userspace TCP/IP forwarding stack. `FogVpnService` therefore refuses to establish the
TUN interface and reports `FAILED` when bypass processing would be required. This prevents the
selected apps from being routed into a packet sink.

Consequently, this release is a tested Android application foundation and network diagnostic,
not a working censorship-bypass release. A production build must supply an audited,
license-compatible `NetworkEngine` that forwards TCP/UDP through protected sockets and has real
device tests. The UI never reports the local engine as running without that capability.

## Build

Use JDK 17 and Android SDK Platform 36:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug assembleRelease bundleRelease assembleDebugAndroidTest
```

Release signing is loaded from the ignored `keystore.properties`; see
[`docs/RELEASE.md`](docs/RELEASE.md). Never commit the private `.jks` or passwords. The public
upload certificate is in `certificates/fog-mobile-upload-cert.pem`.

## Verification

The repository was verified with Gradle 8.11.1 and AGP 8.10.1 on Windows. Unit tests and lint
pass; debug APK, signed release APK, signed AAB, and androidTest APK build successfully. No Android
device was connected in the build environment, so the compiled Compose instrumentation smoke test
was not executed on hardware/emulator.

## License

GPL-3.0. See [`LICENSE`](LICENSE).
