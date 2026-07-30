# Release signing

Android packages are signed with `release/fog-mobile-release.jks`. The private key and
`keystore.properties` are intentionally excluded from Git. Keep both files backed up in a
separate encrypted location: losing this key prevents future updates signed as the same app.

The public certificate can be exported without exposing the private key:

```powershell
keytool -exportcert -rfc -alias fog-mobile -keystore release/fog-mobile-release.jks -file certificates/fog-mobile-upload-cert.pem
```

Do not commit the generated PEM unless publishing the signing certificate is intentional.
Google Play App Signing can manage the distribution key after the first upload; the local key
then serves as the upload key.
