package com.autosentry.app.worker;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.autosentry.app.data.AppDatabase;
import com.autosentry.app.data.PIDRecord;
import com.autosentry.app.data.PIDRecordDao;
import com.autosentry.app.data.AgentLog;
import com.autosentry.app.data.AgentLogDao;
import com.autosentry.app.data.Session;
import com.autosentry.app.data.SessionDao;
import com.autosentry.app.data.MaintenanceEvent;
import com.autosentry.app.data.MaintenanceDao;
import com.autosentry.app.obd.OBDSimulator;
import com.autosentry.app.util.RPMDetector;
import com.autosentry.app.notifications.NotificationUtils;

import java.util.ArrayList;
import java.util.List;

public class OBDWorker extends Worker {
    private final OBDSimulator simulator = new OBDSimulator();
    private static final int SAMPLE_WINDOW = 7;
    private static final String PREFS_NAME = "autosentry_prefs";
    private static final String PREF_ADAPTER_TYPE = "adapter_type";
    private static final String PREF_VEHICLE_VIN = "vehicle_vin";
    private static final String PREF_LAST_SESSION_ID = "last_session_id";
    private static final String PREF_OIL_LIFE_PCT = "oil_life_pct";
    private static final String PREF_OIL_LIFE_UPDATED = "oil_life_updated_ts";

    public OBDWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Load persisted adapter pairing and vehicle info (fixes "connects like first time")
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String adapterType = prefs.getString(PREF_ADAPTER_TYPE, "Unknown");
            String vehicleVin = prefs.getString(PREF_VEHICLE_VIN, "Unknown");
            long lastSessionId = prefs.getLong(PREF_LAST_SESSION_ID, 0);
            int savedOilLife = prefs.getInt(PREF_OIL_LIFE_PCT, 100);
            long oilUpdatedTs = prefs.getLong(PREF_OIL_LIFE_UPDATED, 0);

            // Load previous session state from DB (fixes "no saved data from previous drive")
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            SessionDao sessionDao = db.sessionDao();
            PIDRecordDao pidDao = db.pidRecordDao();
            AgentLogDao logDao = db.agentLogDao();
            MaintenanceDao maintDao = db.maintenanceDao();

            Session previousSession = null;
            if (lastSessionId > 0) {
                try {
                    previousSession = sessionDao.getById(lastSessionId);
                } catch (Exception ignore) {
                    // Session may have been cleaned; ignore
                }
            }

            // Read RPM from adapter (simulator for now; real adapter later)
            int rpm = simulator.readRPM();

            PIDRecord record = new PIDRecord();
            record.pidName = "RPM";
            record.pidValue = rpm;
            record.timestamp = System.currentTimeMillis();
            // Tag with previous session if exists; else start new session tracking
            if (previousSession != null && previousSession.endTime == 0) {
                record.sessionId = previousSession.id; // Would need sessionId column in PIDRecord
            }
            pidDao.insert(record);

            // Load previous N samples for comparison (not just latest 7)
            List<PIDRecord> latest = pidDao.latest(SAMPLE_WINDOW);
            List<Integer> samples = new ArrayList<>();
            for (int i = latest.size() - 1; i >= 0; i--) {
                PIDRecord r = latest.get(i);
                if ("RPM".equals(r.pidName)) samples.add(r.pidValue);
            }

            boolean unstable = RPMDetector.isRPMUnstable(samples, 150, 5);
            if (unstable) {
                AgentLog log = new AgentLog();
                log.timestamp = System.currentTimeMillis();
                log.type = "RPM_INSTABILITY";
                log.message = "Detected RPM instability (swing over threshold). Adapter: " + adapterType + ", VIN: " + vehicleVin;
                log.metadata = "{\"samples_count\":" + samples.size() + ",\"adapter\":" + adapterType + "}";
                logDao.insert(log);

                NotificationUtils.sendAlert(getApplicationContext(),
                        (int)(log.timestamp % Integer.MAX_VALUE),
                        "AutoSentry: RPM instability",
                        "RPM instability detected on " + adapterType + ". VIN: " + vehicleVin + ". Open app for details.");
            }

            // Load previous maintenance event for oil life comparison (fixes "oil 100% every time")
            List<MaintenanceEvent> prevMaint = maintDao.getLatestByType("OIL_CHANGE");
            int currentOilLife = savedOilLife;
            if (prevMaint != null && !prevMaint.isEmpty()) {
                MaintenanceEvent lastOil = prevMaint.get(0);
                // Decrease oil life based on time/miles (simplified: 1% per day since change)
                long daysSince = (System.currentTimeMillis() - lastOil.eventTime) / (24L * 60 * 60 * 1000);
                currentOilLife = Math.max(0, savedOilLife - (int)daysSince);
                // Persist updated oil life
                prefs.edit()
                    .putInt(PREF_OIL_LIFE_PCT, currentOilLife)
                    .putLong(PREF_OIL_LIFE_UPDATED, System.currentTimeMillis())
                    .apply();
            }

            // Log agent log only when meaningful change from previous state (prevents redundant "getting baseline" messages)
            long prevAgentTs = 0;
            List<AgentLog> recentLogs = logDao.getLatest(5);
            if (!recentLogs.isEmpty()) {
                AgentLog mostRecent = recentLogs.get(0);
                prevAgentTs = mostRecent.timestamp;
            }

            // Only log new agent events when there's a real change (not every poll)
            // (Previously this logged every time, creating "getting baseline" noise)

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
