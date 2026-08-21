# Pull Request: Checkpoint 4 — RPM instability detection + notifications

This PR integrates RPM instability detection into the OBD polling worker and adds local notification support and an alerts UI.

What it contains:
- AgentLog entity and DAO
- NotificationUtils to create notification channel and post alerts
- OBDWorker updated to run detection on recent RPM samples and insert AgentLogs & notifications
- AlertsActivity to view recent alerts
- AppDatabase updated for AgentLog

Testing:
- The OBD simulator already injects occasional oscillations. Build and run the app, then open Alerts to see logs.

Notes:
- No decompiled code included; this is a clean-room reimplementation for the agent features.
