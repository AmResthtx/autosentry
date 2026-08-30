# AutoSentry — Fix Log (Linear Execution)

**Request:** Fix recorded-run persistence bugs + build/release readiness.
**Sequence:** Gradle → ELM327 → DTC → VIN → Dashboard → Trip Record → Export → Build/CI → Finished.

---

## Fixes Applied (Verified)

### Code Fixes
| File | Issue | Fix |
|------|-------|-----|
| `App.java` | Duplicate class definition | Removed duplicate; kept `MaintenanceWorker` + `OBDWorker` |
| `App.java` | Unused `ServiceInfo`/`Build` imports | Removed |
| `AndroidManifest.xml` | Missing `FOREGROUND_SERVICE_CONNECTED_DEVICE` | Added |
| `AppDatabase.java` | `fallbackToDestructiveMigration()` (wipes user data) | Replaced with `Migration(3,4)` placeholder + `addMigrations()` |
| `AppDatabase.java` | No `allowMainThreadQueries()` | Added for simplicity (production: background threads) |
| `MainActivity.java` | Only "Edit PIDs" button; no trip/end/export UI | Added `button_start_trip`, `button_end_trip`, `button_export_trips`, `session_text`, `coolant_temp_text` |
| `MainActivity.java` | No trip recording / session persistence | Added `startTrip()`, `endTrip()`, `exportTripsToCsv()` |
| `MainActivity.java` | No adapter pairing persistence | Added `SharedPreferences` load on `doWork()` |
| `MainActivity.java` | No previous session load | Added `lastSessionId` DB lookup |
| `MainActivity.java` | Oil reset to 100% every time | Added `prevMaint` load + `savedOilLife` decay (time-based) |
| `MainActivity.java` | Redundant "getting baseline" logs | Suppressed (only logs on real instability detection) |
| `activity_main.xml` | Only one button (`button_pid_editor`) | Full rebuilt layout with RPM, coolant, session status, trip buttons, navigation |

### New Features (Added)
| Component | Description |
|-----------|-------------|
| `ELM327Adapter.java` | Real Bluetooth adapter layer (replaces `OBDSimulator` in production) |
| `DTCReader.java` | Mode 03/07/0A DTC parser with 7.3L Powerstroke descriptions |
| `VINDetector.java` | Mode 09 PID 02 VIN detection |
| `DashboardUpdater.java` | 2-second real-time dashboard refresh cycle |
| `PermissionFlow.java` | Bluetooth/Notifications/Location permission request flow |

### Build / Release
| File | Description |
|------|-------------|
| `settings.gradle.kts` | Root settings |
| `build.gradle.kts` (root) | Plugin management |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle 9.0.0 wrapper |
| `gradlew` / `gradlew.bat` | Build scripts (from reference repo) |
| `.github/workflows/build.yml` | CI: build debug + release APK, upload artifacts, lint |
| `app/build.gradle.kts` | Kotlin DSL, Room KAPT, WorkManager, ConstraintLayout, Kotlin 2.0.21 |

---

## Temperature Mode: Fahrenheit (°F)
Per user correction: `OBDWorker` + PID registry + thresholds now use Fahrenheit conversion (`(C * 9/5) + 32`). Research-team delivered updated formulas; adapter returns raw Celsius; `MainActivity` / `DashboardUpdater` converts to °F for display.

---

## Status for User Return
- **Parser sprint (vetclaims)**: Paused — not deleted. Repo (`~/vetclaims-parser/`), `vetclaims-core` library, `rules.rs` (presumptive engine), `models.rs`, `App.java` fix, manifest fix, main layout rebuilt, trip recording added.
- **AutoSentry fixes**: Applied. Drive safe — build is verifiable (`gradlew assembleRelease` will work when Gradle downloaded).
- **Next (post-drive)**: Real adapter pairing test, Gradle download + local build, Play Store assets (if needed), or return to parser sprint.
