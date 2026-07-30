# Changelog

All notable changes to FOG Mobile are documented here. The project follows semantic versioning after the first stable release.

## [Unreleased]

### Planned

- Audited userspace TCP/UDP forwarding engine
- Protected outbound sockets and packet-level tests
- Multi-API real-device test matrix

## [0.1.0] - 2026-07-30

### Added

- Kotlin and Jetpack Compose Android application targeting API 36
- Local DNS, TLS, CDN, and UDP route diagnostics against configured sample endpoints
- Hilt, Coroutines, StateFlow, Navigation Compose, and DataStore architecture
- Android VpnService permission, foreground-service, package allowlist, and engine boundaries
- Dashboard, onboarding, diagnostics, settings, and anonymized log export
- Adaptive launcher, monochrome, and notification icons
- Unit tests, Android Lint, instrumentation smoke-test source, signed APK, and AAB builds

### Security

- Baseline engine declares itself non-operational and cannot establish a traffic-sink TUN
- Private signing key and passwords excluded from Git
- No remote backend, MITM, custom CA, analytics, or payload logging
