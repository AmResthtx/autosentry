package com.autosentry.app;

import android.app.Application;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;

import com.autosentry.app.worker.OBDWorker;

import java.util.concurrent.TimeUnit;

public class App extends Application {
    private static final String WORK_NAME_OBD = "autosentry_obd_poll";

    @Override
    public void onCreate() {
        super.onCreate();
        // Schedule periodic OBD polling (default every 15 minutes; PID definitions may suggest faster intervals later)
        PeriodicWorkRequest obdRequest = new PeriodicWorkRequest.Builder(OBDWorker.class, 15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(WORK_NAME_OBD, ExistingPeriodicWorkPolicy.KEEP, obdRequest);
    }
}
