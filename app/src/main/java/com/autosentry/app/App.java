package com.autosentry.app;

import android.app.Application;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;

import com.autosentry.app.worker.OBDWorker;
import com.autosentry.app.worker.MaintenanceWorker;
import com.autosentry.app.notifications.NotificationUtils;

import java.util.concurrent.TimeUnit;

public class App extends Application {
    private static final String WORK_NAME_OBD = "autosentry_obd_poll";
    private static final String WORK_NAME_MAINTENANCE = "autosentry_maintenance_check";

    @Override
    public void onCreate() {
        super.onCreate();

        // Create notification channels
        NotificationUtils.createChannels(this);

        // Schedule periodic OBD polling (every 15 minutes)
        PeriodicWorkRequest obdRequest = new PeriodicWorkRequest.Builder(
                OBDWorker.class, 15, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                WORK_NAME_OBD, ExistingPeriodicWorkPolicy.KEEP, obdRequest);

        // Schedule periodic maintenance checks (every 6 hours)
        PeriodicWorkRequest maintRequest = new PeriodicWorkRequest.Builder(
                MaintenanceWorker.class, 6, TimeUnit.HOURS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                WORK_NAME_MAINTENANCE, ExistingPeriodicWorkPolicy.KEEP, maintRequest);
    }
}
