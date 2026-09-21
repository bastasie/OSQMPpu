# PPU Android Runtime

Comprehensive native Android host for the portable PPU architecture.

This branch is the source build of the same runtime distributed in the local comprehensive bundle.

## One press

Open **Actions → PPU Android APK → Run workflow**. The workflow installs the Android toolchain, builds the debug APK, and publishes it as an artifact.

## Runtime

- Native Android PPU host
- Local WebView runtime
- Safe PPUJ carrier inspection with CRC validation
- External JPG/PNG carrier opening
- Native deterministic Monte Carlo kernel
- Native prime-domain kernel
- SVG Snake application
- FPS-driven internal pole rotation
- Touch, swipe, keyboard and trigger-command input
- Share/export path
- Generic host design: carrier is optional; the PPU runtime is the primary execution layer
