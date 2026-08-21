package com.autosentry.app.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.autosentry.app.data.AppDatabase;
import com.autosentry.app.data.PIDRecord;
import com.autosentry.app.data.PIDRecordDao;
import com.autosentry.app.obd.OBDSimulator;

public class OBDWorker extends Worker {
    private final OBDSimulator simulator = new OBDSimulator();

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

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
