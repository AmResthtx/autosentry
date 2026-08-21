# Release upload instructions for APK

This repository: AmResthtx/autosentry

Purpose: store the APK in a GitHub Release (recommended) or in the repository under /binaries if you prefer.

Recommended workflow (fastest, minimal Git history impact):

1) Create a GitHub Release (web UI)
   - Go to: https://github.com/AmResthtx/autosentry/releases
   - Click "Draft a new release"
   - Tag version: `v0.1` (or whatever you prefer)
   - Title: `autosentry APK v0.1`
   - Description: Add any notes about this build.
   - Attach binaries: drag the APK file onto the release form (or click "Attach binaries by dropping them here or selecting them")
   - Publish release

2) After publishing, paste the release URL here in the chat. Example:
   https://github.com/AmResthtx/autosentry/releases/tag/v0.1

3) I will then:
   - Download the attached APK from the release
   - Compute and paste its SHA256 checksum here so we both confirm integrity
   - Optionally create an import branch that includes a pointer file at `/binaries/autosentry.apk` (if you want the binary inside the repo tree)
   - Start analysis/decompilation per your instructions

If you prefer me to add the APK myself, I need either:
- A direct public download link (transfer.sh or similar) I can fetch, or
- You can grant me permission to push a new branch and upload the binary via the GitHub web UI (I cannot download directly from your Google Drive link in this environment).

Alternative: quick upload via transfer.sh (no account)
- On your machine (macOS / Linux / WSL / Git Bash):
  curl --upload-file ./app.apk https://transfer.sh/app.apk
- Copy the returned link and paste it here. I can download from that link and proceed.

How to compute SHA256 on Windows (PowerShell):
  Get-FileHash .\path\to\app.apk -Algorithm SHA256

Notes:
- Your APK (zip) is ~31 MB — that's fine for a GitHub release and for adding to the repo directly.
- I cannot fetch files from Google Drive links from this environment. Please upload to a GitHub release or provide a direct public download link (transfer.sh or similar) and paste it here.

If you want, I can also scaffold an `import/apk-import` branch that includes a small README and a placeholder file to show where the APK will go; then after you attach the APK to the release or share a direct link, I'll add the binary to the repo and open a PR.

Tell me which action you prefer next:
- I will create a release entry and you upload the APK to that release (then paste release URL), or
- You provide a transfer.sh (or similar direct) link and I will download + create the release and/or add the file to the repo, or
- I create an `import/apk-import` branch now and add scaffolding for the import (I can do this with a follow-up commit).