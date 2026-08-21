# Checkpoint 2 — DB + Agent Skeleton

This checkpoint introduces:

- Room database (AppDatabase) with entities: Session, PIDRecord
- DAOs for Session and PIDRecord
- A simple OBD simulator (OBDSimulator) for development without hardware
- A WorkManager worker (OBDWorker) that polls the simulator and writes PID samples to the DB
- RPMDetector utility with unit tests

Branch: checkpoint-2-db-agent

Next steps:
- Add WorkManager scheduling (PeriodicWorkRequest) in the app startup
- Add UI to view latest PID samples
- Add configuration for sampling frequency and thresholds

Note: We are proceeding without decompiling the APK; this is a clean-room implementation of the core agent and data model so we can continue development without needing the original binary.
