# Checkpoint 4 — RPM instability detection + notifications

This checkpoint integrates the RPMDetector with the OBD polling worker and adds local alerts:

- AgentLog entity + DAO for persistent alert logs
- NotificationUtils to create notification channel and post local notifications
- OBDWorker updated to run detection on recent RPM samples and insert AgentLogs & notifications on detection
- AlertsActivity to view recent alerts
- AppDatabase updated to include AgentLog

Branch: checkpoint-4-rpm-detection

Testing notes:
- The OBD simulator occasionally injects oscillations; the worker will detect it and create alerts.
- Use the Dashboard to view latest RPM and open Alerts to see logs.

I will open a PR for checkpoint-4 after pushing this branch. After that I will proceed immediately to checkpoint-5 (maintenance tracker).
