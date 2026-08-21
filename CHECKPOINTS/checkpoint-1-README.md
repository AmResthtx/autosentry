# AutoSentry — Checkpoint 1: Rebuild skeleton

This checkpoint contains an initial Android Gradle project skeleton reconstructed to the best extent possible before decompilation results are merged.

Goal:
- Provide a buildable Gradle project baseline (app module, manifest, a minimal MainActivity) so we can iteratively add decompiled code and reimplemented features on top of it.

Included files:
- settings.gradle
- build.gradle (root)
- app/build.gradle
- app/src/main/AndroidManifest.xml
- app/src/main/java/com/autosentry/MainActivity.java
- app/src/main/res/layout/activity_main.xml
- app/src/main/res/values/strings.xml
- .gitignore

Notes:
- This is a scaffold. Decompiled code and resources will be integrated in import/decompiled-3.0.10 once the APK has been decompiled and reviewed.
- ZIP SHA256 (provided by owner): ff2a3b4017ddcbdf1c54f207293d2c5c460a89a0d4c2ddfd3bd5645cf403c84b

Next steps:
- Merge decompiled artifacts into this skeleton (manual fixes may be required).
- Implement Room DB, WorkManager agent, PID editor, RPM detector in subsequent checkpoints.
