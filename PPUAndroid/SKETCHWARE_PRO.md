# Sketchware Pro compiler path

GitHub Actions is not required for this packaging path.

This project keeps the canonical native Gradle runtime on branch `ppu-android-runtime`. For mobile/on-device APK compilation, use Sketchware Pro as the compiler and package the included `ppu_runtime.html` plus `PPU_General_Use_Cartridge.jpg` as app assets.

The runtime is a WebView-hosted PPU execution surface with:
- touch/swipe and trigger-character InputMux
- Snake state
- frame clock / pole rotation
- carrier inspection
- Monte Carlo and prime-domain kernels in the native source path

Important: the JPEG is a persistent PPUJ carrier artifact; the Android host is the execution environment.

A prepared local bundle is supplied separately as `PPU_SketchwarePro_OnePress_Bundle.zip`. Because Sketchware Pro's exported SWB internals are version-sensitive, the bundle intentionally does not fabricate an SWB; create/backup the project from the installed Sketchware Pro version after importing these assets.

Branding source: Canva design DAHV12PFG00 ("Ppu · General Use").
