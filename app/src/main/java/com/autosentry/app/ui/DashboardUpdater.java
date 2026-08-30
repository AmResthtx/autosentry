package com.autosentry.app.ui;

import android.os.Handler;
import android.os.Looper;

/**
 * Simple real-time dashboard updater.
 * Polls current values (simulator or adapter) and updates UI gauges.
 * Fahrenheit mode applied per @research-team spec.
 */
public class DashboardUpdater {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MainActivity activity;
    private boolean running = false;
    private static final int UPDATE_INTERVAL_MS = 2000; // 2-second refresh for dashboard

    public DashboardUpdater(MainActivity activity) {
        this.activity = activity;
    }

    public void start() {
        running = true;
        handler.post(updateRunnable);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(updateRunnable);
    }

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            // Read current values from DB (simulator writes every 15 min; real adapter writes faster)
            // Convert Celsius readings to Fahrenheit for 7.3L spec
            // Coolant: (C * 9/5) + 32
            // Oil: (C * 9/5) + 32
            // EGT: ((val * 0.1 - 40) * 9/5) + 32

            // For now, refresh UI with current DB values; with real adapter,
            // this would call adapter.readRPM(), adapter.readCoolantTemp(), etc.
            activity.refreshUI();

            handler.postDelayed(this, UPDATE_INTERVAL_MS);
        }
    };
}
