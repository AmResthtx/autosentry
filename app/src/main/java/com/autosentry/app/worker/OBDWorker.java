package com.autosentry.app.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.autosentry.app.data.AppDatabase;
import com.autosentry.app.data.PIDRecord;
import com.autosentry.app.data.PIDRecordDao;
import com.autosentry.app.data.AgentLog;
import com.autosentry.app.data.AgentLogDao;
import com.autosentry.app.obd.OBDSimulator;
import com.autosentry.app.util.RPMDetector;
import com.autosentry.app.notifications.NotificationUtils;

import java.util.ArrayList;
import java.util.List;

public class OBDWorker extends Worker {
    private final OBDSimulator simulator = new OBDSimulator();
    private static final int SAMPLE_WINDOW = 7;

    public OBDWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            int rpm = simulator.readRPM();

            PIDRecord record = new PIDRecord();
            record.pidName = "RPM";
            record.pidValue = rpm;
            record.timestamp = System.currentTimeMillis();

            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            PIDRecordDao dao = db.pidRecordDao();
            dao.insert(record);

            // fetch last N RPM samples
            List<PIDRecord> latest = dao.latest(SAMPLE_WINDOW);
            List<Integer> samples = new ArrayList<>();
            for (int i = latest.size() - 1; i >= 0; i--) {
                PIDRecord r = latest.get(i);
                if ("RPM".equals(r.pidName)) samples.add(r.pidValue);
            }

            boolean unstable = RPMDetector.isRPMUnstable(samples, 150, 5);
            if (unstable) {
                // create agent log
                AgentLog log = new AgentLog();
                log.timestamp = System.currentTimeMillis();
                log.type = "RPM_INSTABILITY";
                log.message = "Detected RPM instability (swing over threshold).";
                log.metadata = "{\"samples_count\":" + samples.size() + "}" ;
                AgentLogDao logDao = db.agentLogDao();
                logDao.insert(log);

                // send local notification
                NotificationUtils.sendAlert(getApplicationContext(), (int) (log.timestamp % Integer.MAX_VALUE), "AutoSentry: RPM instability", "RPM instability detected. Open app for details.");
            }

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
