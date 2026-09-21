PPU Android signing

PWABuilder originally supplied GitHub-unsigned.apk and GitHub-unsigned.aab.

A release upload key was generated for this build and used to sign both artifacts.

Alias: ppu-upload
Key type: RSA 2048
Certificate SHA-256: 4F:83:8D:93:25:C7:F1:D1:75:A5:8E:D8:9F:56:3B:43:10:EB:45:06:58:A6:63:34:83:DF:03:25:9F:B2:B9:55
APK: JAR/APK v1 signature verified with jarsigner -verify
AAB: JAR signature verified with jarsigner -verify

The private keystore is deliberately NOT stored in GitHub.

For Google Play, use the signed AAB as the upload artifact. Google Play App Signing distinguishes the developer upload key from the Google-held app-signing key.