# Checkpoint 3 — PID editor, WorkManager registration, Dashboard UI

This checkpoint implements:

- PIDDefinition entity and PIDDefinitionDao (Room)
- PIDEditorActivity (add/list simple PIDs)
- App Application class that registers a periodic WorkManager job (OBDWorker) to poll data
- MainActivity/UI updated to show latest RPM and a button to open PID editor
- Layout files for main dashboard and PID editor

Branch: checkpoint-3-pid-editor

Notes:
- Database operations are performed on background threads to avoid blocking the UI.
- The OBDWorker is scheduled to run every 15 minutes by default; this can be changed later via settings and PID poll intervals.

Next steps (checkpoint-4):
- Integrate RPMDetector end-to-end and add notifications when instability is detected.
- Add unit tests for detection logic and working notifications.

Merge plan:
- I will open a PR for checkpoint-3 against main once this branch is pushed (PR URL will be posted here).
