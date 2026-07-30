# Security Policy

## Supported versions

FOG Mobile is currently a technical preview. Security fixes target the latest commit on `main` until stable releases begin.

## Reporting a vulnerability

Use GitHub's private vulnerability reporting or security advisory flow for this repository. Do not open a public issue for vulnerabilities involving traffic routing, Android VPN permissions, package allowlisting, certificate validation, signing material, or sensitive diagnostic data.

Include:

- affected commit and Android version;
- reproduction steps with secrets and personal data removed;
- expected and observed behavior;
- security impact;
- a minimal proof of concept when safe to share.

You should receive an acknowledgement within seven days. Valid reports will be investigated before public disclosure.

## Scope and guarantees

The current baseline engine is non-operational and refuses to establish TUN. No claim is made that this technical preview provides anonymity, censorship resistance, endpoint security, or protection on hostile networks.

Never send private signing keys, passwords, access tokens, cookies, raw traffic payloads, or another person's data in a report.
