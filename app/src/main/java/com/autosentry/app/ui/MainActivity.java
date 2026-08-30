package com.autosentry.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.autosentry.app.R;
import com.autosentry.app.data.AppDatabase;
import com.autosentry.app.data.PIDRecord;
import com.autosentry.app.data.Session;
import com.autosentry.app.data.SessionDao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView rpmView;
    private TextView sessionText;
    private Button startTripBtn;
    private Button endTripBtn;
    private Session activeSession = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rpmView = findViewById(R.id.textView);
        sessionText = findViewById(R.id.session_text);
        startTripBtn = findViewById(R.id.button_start_trip);
        endTripBtn = findViewById(R.id.button_end_trip);

        Button alertsBtn = findViewById(R.id.button_alerts);
        alertsBtn.setOnClickListener(v -> startActivity(new Intent(this, AlertsActivity.class)));

        Button maintBtn = findViewById(R.id.button_maintenance);
        maintBtn.setOnClickListener(v -> startActivity(new Intent(this, MaintenanceActivity.class)));

        Button pidBtn = findViewById(R.id.button_pid_editor);
        pidBtn.setOnClickListener(v -> startActivity(new Intent(this, PIDEditorActivity.class)));

        Button exportBtn = findViewById(R.id.button_export_trips);
        exportBtn.setOnClickListener(v -> exportTripsToCsv());

        startTripBtn.setOnClickListener(v -> startTrip());
        endTripBtn.setOnClickListener(v -> endTrip());

        refreshUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    private void startTrip() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            SessionDao sessionDao = db.sessionDao();
            Session session = new Session();
            session.startTime = System.currentTimeMillis();
            session.adapterType = "OBDLink"; // Default; could detect from prefs
            session.vehicleVin = ""; // Could read from shared prefs / VIN scan
            session.notes = "Recorded trip";
            long sessionId = sessionDao.insert(session);
            activeSession = session;
            activeSession.id = sessionId;
            runOnUiThread(this::refreshUI);
        }).start();
    }

    private void endTrip() {
        if (activeSession == null) {
            Toast.makeText(this, "No active trip to end", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            SessionDao sessionDao = db.sessionDao();
            activeSession.endTime = System.currentTimeMillis();
            sessionDao.update(activeSession);
            activeSession = null;
            runOnUiThread(this::refreshUI);
        }).start();
    }

    private void exportTripsToCsv() {
        new Thread(() -> {
            try {
                File exportsDir = new File(Environment.getExternalStorageDirectory(), "AutoSentry/Exports");
                if (!exportsDir.exists()) {
                    exportsDir.mkdirs();
                }
                String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date());
                File csvFile = new File(exportsDir, "autosentry_trips_" + timestamp + ".csv");

                try (FileWriter writer = new FileWriter(csvFile)) {
                    writer.append("TripID,StartTime,EndTime,AdapterType,VehicleVIN,Notes,DurationMs\n");
                    AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                    List<Session> sessions = db.sessionDao().getAll();
                    for (Session s : sessions) {
                        long duration = (s.endTime > s.startTime) ? (s.endTime - s.startTime) : 0;
                        writer.append(String.format(Locale.US,
                            "%d,%d,%d,%s,%s,%s,%d\n",
                            s.id, s.startTime, s.endTime,
                            s.adapterType != null ? s.adapterType : "",
                            s.vehicleVin != null ? s.vehicleVin : "",
                            s.notes != null ? s.notes : "",
                            duration));
                    }
                }
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Exported to: " + csvFile.getAbsolutePath(), Toast.LENGTH_LONG).show());
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    public void refreshUI() {
        refreshLatestRPM();
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<Session> allSessions = db.sessionDao().getAll();
            boolean hasActive = (activeSession != null);
            int count = allSessions.size();
            runOnUiThread(() -> {
                if (hasActive) {
                    sessionText.setText("Trip active (ID: " + activeSession.id + ") | Total trips: " + count);
                    startTripBtn.setEnabled(false);
                    endTripBtn.setEnabled(true);
                } else {
                    sessionText.setText("No active trip | Total trips: " + count);
                    startTripBtn.setEnabled(true);
                    endTripBtn.setEnabled(false);
                }
            });
        }).start();
    }

    private void refreshLatestRPM() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<PIDRecord> latest = db.pidRecordDao().latest(1);
            runOnUiThread(() -> {
                if (latest != null && !latest.isEmpty()) {
                    rpmView.setText("RPM: " + latest.get(0).pidValue);
                } else {
                    rpmView.setText("RPM: --");
                }
            });
        }).start();
    }
}
