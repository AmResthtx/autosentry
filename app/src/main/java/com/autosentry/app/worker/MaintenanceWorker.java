package com.autosentry.app.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.autosentry.app.data.AppDatabase;
import com.autosentry.app.data.MaintenanceDao;
import com.autosentry.app.data.MaintenanceEvent;
import com.autosentry.app.data.PIDRecord;
import com.autosentry.app.data.PIDRecordDao;
import com.autosentry.app.data.AgentLog;
import com.autosentry.app.notifications.NotificationUtils;

import java.util.List;

public class MaintenanceWorker extends Worker {
    public MaintenanceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            MaintenanceDao mdao = db.maintenanceDao();
            List<MaintenanceEvent> events = mdao.dueSoon();

            // Simple simulation: check odometer from latest PID if present
            PIDRecordDao pdao = db.pidRecordDao();
            List<PIDRecord> latest = pdao.latest(1);
            int odometer = -1;
            if (!latest.isEmpty()) {
                // assume a "ODO" PID exists; if not, distance checks are skipped
                PIDRecord r = latest.get(0);
                if ("ODO".equals(r.pidName)) {
                    odometer = r.pidValue;
                }
            }

            long now = System.currentTimeMillis();

            for (MaintenanceEvent e : events) {
                boolean dueTime = (e.intervalMonths > 0 && (now - e.lastServiceAt) >= (long)e.intervalMonths * 30L * 24L * 3600L * 1000L);
                boolean dueDistance = (e.intervalKm > 0 && odometer >= 0 && (odometer - e.odometerAtLastService) >= e.intervalKm);
                if (dueTime || dueDistance) {
                    // create AgentLog & notification
                    AgentLog log = new AgentLog();
                    log.timestamp = now;
                    log.type = "MAINTENANCE_DUE";
                    log.message = "Maintenance due: " + e.title;
                    log.metadata = "{}";
                    db.agentLogDao().insert(log);

                    NotificationUtils.sendAlert(getApplicationContext(), (int)(now % Integer.MAX_VALUE), "AutoSentry: Maintenance due", e.title);
                }
            }

            return Result.success();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Result.retry();
        }
    }
}
