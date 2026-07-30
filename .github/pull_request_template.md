## Summary

Describe what changed and why.

## User and developer impact

Explain visible behavior, compatibility, privacy, and maintenance impact.

## Validation

- [ ] `testDebugUnitTest`
- [ ] `lintDebug`
- [ ] `assembleDebug`
- [ ] `assembleDebugAndroidTest`
- [ ] Real-device testing, or reason it was not available

## Network and privacy checklist

- [ ] Success states are backed by real checks.
- [ ] Retries and profile loops are bounded.
- [ ] Network operations have explicit timeouts.
- [ ] No payloads, cookies, tokens, or personal data are logged.
- [ ] VPN changes cannot establish TUN without operational forwarding.
- [ ] New dependencies and binaries have compatible licenses.
