package com.autosentry.app.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.autosentry.app.R;
import com.autosentry.app.data.AppDatabase;
import com.autosentry.app.data.PIDDefinition;
import com.autosentry.app.data.PIDRecord;
import com.autosentry.app.data.Session;
import com.autosentry.app.data.VehicleProfile;
import com.autosentry.app.util.PIDAIMonitor;
import com.autosentry.app.util.SymbolRegistry;
import com.autosentry.app.util.VehicleManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements VehicleManager.OnVehicleChangeListener {
    private TextView truckSymbolIcon;
    private TextView truckModelLabel;
    private TextView truckConnStatus;
    private TextView vinLabel;

    private TextView tachometerTitle;
    private TextView rpmView;
    private ProgressBar truckRpmBar;

    private TextView gaugeSpeedVal;
    private TextView gaugeSpeedTitle;
    private TextView gaugeBoostVal;
    private TextView gaugeBoostTitle;
    private TextView gaugeEotVal;
    private TextView gaugeEotTitle;

    private TextView coolantText;
    private TextView gaugeCoolantTitle;
    private TextView gaugeIcpVal;
    private TextView gaugeIcpTitle;
    private TextView gaugeTransVal;
    private TextView gaugeTransTitle;

    private TextView pedalPctLabel;
    private TextView sampleRateLabel;
    private TextView sessionText;
    private TextView tripAutoBadge;
    private TextView aiMonitorStatus;
    private TextView aiMonitorBadge;

    private SeekBar throttleSeekBar;
    private Button startTripBtn;
    private Button endTripBtn;
    private Button btnEngineToggle;
    private Button btnSwitchVehicle;

    private VehicleProfile currentVehicle;
    private Session activeSession = null;
    private boolean isEngineRunning = true;
    private int throttleProgress = 42;
    private int packetCount = 0;

    private final Handler simHandler = new Handler(Looper.getMainLooper());
    private Runnable simLoop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentVehicle = VehicleManager.getActiveVehicle(this);
        VehicleManager.addListener(this);

        truckSymbolIcon = findViewById(R.id.truck_symbol_icon);
        truckModelLabel = findViewById(R.id.truck_model_label);
        truckConnStatus = findViewById(R.id.truck_conn_status);
        vinLabel = findViewById(R.id.vin_label);

        tachometerTitle = findViewById(R.id.tachometer_title);
        rpmView = findViewById(R.id.textView);
        truckRpmBar = findViewById(R.id.truck_rpm_bar);

        gaugeSpeedVal = findViewById(R.id.gauge_speed_val);
        gaugeSpeedTitle = findViewById(R.id.gauge_speed_title);
        gaugeBoostVal = findViewById(R.id.gauge_boost_val);
        gaugeBoostTitle = findViewById(R.id.gauge_boost_title);
        gaugeEotVal = findViewById(R.id.gauge_eot_val);
        gaugeEotTitle = findViewById(R.id.gauge_eot_title);

        coolantText = findViewById(R.id.coolant_temp_text);
        gaugeCoolantTitle = findViewById(R.id.gauge_coolant_title);
        gaugeIcpVal = findViewById(R.id.gauge_icp_val);
        gaugeIcpTitle = findViewById(R.id.gauge_icp_title);
        gaugeTransVal = findViewById(R.id.gauge_trans_val);
        gaugeTransTitle = findViewById(R.id.gauge_trans_title);

        pedalPctLabel = findViewById(R.id.pedal_pct_label);
        sampleRateLabel = findViewById(R.id.sample_rate_label);
        sessionText = findViewById(R.id.session_text);
        tripAutoBadge = findViewById(R.id.trip_auto_badge);
        aiMonitorStatus = findViewById(R.id.ai_monitor_status);
        aiMonitorBadge = findViewById(R.id.ai_monitor_health_badge);

        throttleSeekBar = findViewById(R.id.throttle_seekbar);
        startTripBtn = findViewById(R.id.button_start_trip);
        endTripBtn = findViewById(R.id.button_end_trip);
        btnEngineToggle = findViewById(R.id.btn_engine_toggle);
        btnSwitchVehicle = findViewById(R.id.btn_switch_vehicle);

        updateVehicleUI();

        Button viewTripsBtn = findViewById(R.id.button_view_trips);
        if (viewTripsBtn != null) {
            viewTripsBtn.setOnClickListener(v -> startActivity(new Intent(this, TripsActivity.class)));
        }

        Button aiPartsBtn = findViewById(R.id.button_ai_parts);
        if (aiPartsBtn != null) {
            aiPartsBtn.setOnClickListener(v -> startActivity(new Intent(this, AIPartsActivity.class)));
        }

        Button alertsBtn = findViewById(R.id.button_alerts);
        alertsBtn.setOnClickListener(v -> startActivity(new Intent(this, AlertsActivity.class)));

        Button maintBtn = findViewById(R.id.button_maintenance);
        maintBtn.setOnClickListener(v -> startActivity(new Intent(this, MaintenanceActivity.class)));

        Button crankNoStartBtn = findViewById(R.id.button_crank_no_start);
        crankNoStartBtn.setOnClickListener(v -> startActivity(new Intent(this, CrankNoStartActivity.class)));

        Button pidBtn = findViewById(R.id.button_pid_editor);
        pidBtn.setOnClickListener(v -> startActivity(new Intent(this, PIDEditorActivity.class)));

        startTripBtn.setOnClickListener(v -> startTrip(false));
        endTripBtn.setOnClickListener(v -> endTrip(false));

        if (btnSwitchVehicle != null) {
            btnSwitchVehicle.setOnClickListener(v -> showVehicleSelectionDialog());
        }

        btnEngineToggle.setOnClickListener(v -> {
            isEngineRunning = !isEngineRunning;
            if (isEngineRunning) {
                btnEngineToggle.setText("Engine: RUN");
                btnEngineToggle.setBackgroundColor(0xFF22C55E);
                truckConnStatus.setText("● CONNECTED (" + currentVehicle.protocol + ")");
                truckConnStatus.setTextColor(0xFF4ADE80);
                if (activeSession == null) {
                    startTrip(true);
                }
            } else {
                btnEngineToggle.setText("Engine: OFF");
                btnEngineToggle.setBackgroundColor(0xFFEF4444);
                truckConnStatus.setText("○ KEY ON / ENGINE OFF (0 RPM)");
                truckConnStatus.setTextColor(0xFFFBBF24);
                if (activeSession != null) {
                    endTrip(true);
                }
            }
        });

        throttleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                throttleProgress = progress;
                pedalPctLabel.setText(progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Initialize sample database records if empty
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            if (db.pidDefinitionDao().all().isEmpty()) {
                PIDDefinition p1 = new PIDDefinition();
                p1.name = "Engine RPM";
                p1.command = "010C";
                p1.pollIntervalSeconds = 1;
                db.pidDefinitionDao().insert(p1);

                PIDDefinition p2 = new PIDDefinition();
                p2.name = "Vehicle Speed";
                p2.command = "010D";
                p2.pollIntervalSeconds = 1;
                db.pidDefinitionDao().insert(p2);

                PIDDefinition p3 = new PIDDefinition();
                p3.name = "Engine Oil Temp (EOT)";
                p3.command = "221310";
                p3.pollIntervalSeconds = 2;
                db.pidDefinitionDao().insert(p3);

                PIDDefinition p4 = new PIDDefinition();
                p4.name = "ICP Pressure";
                p4.command = "221446";
                p4.pollIntervalSeconds = 1;
                db.pidDefinitionDao().insert(p4);
            }
        }).start();

        startTrip(true);
        setupLiveTruckSimulation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VehicleManager.removeListener(this);
        if (simLoop != null) {
            simHandler.removeCallbacks(simLoop);
        }
    }

    @Override
    public void onVehicleChanged(VehicleProfile newProfile) {
        currentVehicle = newProfile;
        runOnUiThread(this::updateVehicleUI);
    }

    private void updateVehicleUI() {
        if (currentVehicle == null) return;

        String symbol = currentVehicle.symbol != null ? currentVehicle.symbol : SymbolRegistry.getSymbol(currentVehicle.make);
        if (truckSymbolIcon != null) {
            truckSymbolIcon.setText(symbol);
        }
        if (truckModelLabel != null) {
            truckModelLabel.setText(currentVehicle.year + " " + currentVehicle.make + " " + currentVehicle.model + " " + currentVehicle.architecture.displayName);
        }
        if (truckConnStatus != null) {
            truckConnStatus.setText(isEngineRunning ? "● CONNECTED (" + currentVehicle.protocol + ")" : "○ KEY ON / ENGINE OFF");
            truckConnStatus.setTextColor(isEngineRunning ? 0xFF4ADE80 : 0xFFFBBF24);
        }
        if (vinLabel != null) {
            vinLabel.setText("VIN: " + currentVehicle.vin + " • " + currentVehicle.pcmStrategy);
        }

        // Configure Tachometer & Gauge Titles tailored strictly to this engine family
        if (tachometerTitle != null) {
            String fuelType = currentVehicle.isDiesel() ? "DIESEL" : "GASOLINE";
            tachometerTitle.setText("⏱️ " + fuelType + " TACHOMETER");
        }

        if (currentVehicle.hasHeui()) {
            // 7.3L or 6.0L PowerStroke
            if (gaugeBoostTitle != null) gaugeBoostTitle.setText("🌀 Turbo Boost");
            if (gaugeEotTitle != null) gaugeEotTitle.setText("🛢️ Oil Temp (EOT)");
            if (gaugeCoolantTitle != null) gaugeCoolantTitle.setText("🌡️ Coolant (ECT)");
            if (gaugeIcpTitle != null) gaugeIcpTitle.setText("⚡ ICP Pressure");
            if (gaugeTransTitle != null) gaugeTransTitle.setText("⚙️ Trans Temp");
        } else if (currentVehicle.hasCommonRail()) {
            // Modern Common Rail Diesel (Duramax, Cummins, 6.7L PowerStroke)
            if (gaugeBoostTitle != null) gaugeBoostTitle.setText("🌀 Turbo Boost");
            if (gaugeEotTitle != null) gaugeEotTitle.setText("⛽ Rail Pressure (FRP)");
            if (gaugeCoolantTitle != null) gaugeCoolantTitle.setText("🌡️ Coolant (ECT)");
            if (gaugeIcpTitle != null) gaugeIcpTitle.setText("🌫️ DPF Soot Load");
            if (gaugeTransTitle != null) gaugeTransTitle.setText("⚙️ Trans Temp");
        } else {
            // Gasoline Engine (Coyote V8 / Generic OBD2)
            if (gaugeBoostTitle != null) gaugeBoostTitle.setText("⚡ Spark Advance");
            if (gaugeEotTitle != null) gaugeEotTitle.setText("💨 Air Flow (MAF)");
            if (gaugeCoolantTitle != null) gaugeCoolantTitle.setText("🌡️ Coolant (ECT)");
            if (gaugeIcpTitle != null) gaugeIcpTitle.setText("⚖️ Fuel Trim (STFT)");
            if (gaugeTransTitle != null) gaugeTransTitle.setText("🔋 Battery Volts");
        }
    }

    private void showVehicleSelectionDialog() {
        List<VehicleProfile> presets = VehicleManager.getPresetVehicles();
        String[] options = new String[presets.size() + 2];
        for (int i = 0; i < presets.size(); i++) {
            VehicleProfile p = presets.get(i);
            options[i] = p.symbol + " " + p.year + " " + p.make + " " + p.model + "\n   " + p.architecture.displayName;
        }
        options[presets.size()] = "🔍 Auto-Interrogate OBD-II Port (Mode 09 VIN)";
        options[presets.size() + 1] = "⌨️ Enter Custom VIN / Vehicle";

        new AlertDialog.Builder(this)
            .setTitle("🚗 Connected Vehicle & Protocol Profiles")
            .setItems(options, (dialog, which) -> {
                if (which < presets.size()) {
                    VehicleProfile selected = presets.get(which);
                    VehicleManager.setActiveVehicle(MainActivity.this, selected);
                    Toast.makeText(MainActivity.this, "Switched to " + selected.getFullVehicleTitle(), Toast.LENGTH_SHORT).show();
                } else if (which == presets.size()) {
                    // Auto detect OBD port
                    Toast.makeText(MainActivity.this, "Interrogating OBD-II Mode 09 PID 02...", Toast.LENGTH_SHORT).show();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        VehicleProfile detected = presets.get(0); // Detected 7.3L Ford
                        VehicleManager.setActiveVehicle(MainActivity.this, detected);
                        Toast.makeText(MainActivity.this, "✓ Auto-Identified: " + detected.getFullVehicleTitle(), Toast.LENGTH_LONG).show();
                    }, 800);
                } else {
                    promptCustomVIN();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void promptCustomVIN() {
        final EditText input = new EditText(this);
        input.setHint("Enter 17-character VIN (e.g. 1FTNW21F82EB74901)");
        input.setPadding(24, 24, 24, 24);

        new AlertDialog.Builder(this)
            .setTitle("⌨️ Decode & Connect VIN")
            .setView(input)
            .setPositiveButton("Decode & Connect", (dialog, which) -> {
                String vin = input.getText().toString().trim();
                if (!vin.isEmpty()) {
                    VehicleProfile decoded = VehicleManager.decodeVIN(vin);
                    VehicleManager.setActiveVehicle(MainActivity.this, decoded);
                    Toast.makeText(MainActivity.this, "Identified: " + decoded.getFullVehicleTitle(), Toast.LENGTH_LONG).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void setupLiveTruckSimulation() {
        simLoop = new Runnable() {
            @Override
            public void run() {
                updateLiveTruckTelemetry();
                simHandler.postDelayed(this, 600);
            }
        };
        simHandler.post(simLoop);
    }

    private void updateLiveTruckTelemetry() {
        packetCount++;
        if (!isEngineRunning) {
            rpmView.setText("RPM: 0");
            truckRpmBar.setProgress(0);
            gaugeSpeedVal.setText("0 mph");
            gaugeBoostVal.setText("0.0");
            gaugeIcpVal.setText("0");
            gaugeEotVal.setText("140 °F");
            coolantText.setText("Coolant: 135 °F");
            gaugeTransVal.setText("110 °F");
            sampleRateLabel.setText("Auto Trip Logger: Standby (Engine Stopped)");
            if (tripAutoBadge != null) {
                tripAutoBadge.setText("STANDBY");
                tripAutoBadge.setTextColor(0xFFFBBF24);
                tripAutoBadge.setBackgroundColor(0xFF78350F);
            }
            return;
        }

        double noise = (Math.random() - 0.5);
        int calcRpm;
        int maxRpm = currentVehicle.isDiesel() ? 4000 : 6500;
        truckRpmBar.setMax(maxRpm);

        if (throttleProgress < 5) {
            calcRpm = currentVehicle.isDiesel() ? (670 + (int)(noise * 30)) : (750 + (int)(noise * 40));
        } else {
            int range = maxRpm - 800;
            calcRpm = 750 + (int)((throttleProgress / 100.0) * range) + (int)(noise * 60);
        }

        int speedMph = (calcRpm < 850) ? 0 : Math.max(0, (int)((calcRpm - 750) * 0.027));
        int ectF = 190 + (int)(Math.cos(packetCount / 20.0) * 3);

        rpmView.setText(String.format(Locale.US, "RPM: %,d", calcRpm));
        truckRpmBar.setProgress(calcRpm);
        gaugeSpeedVal.setText(speedMph + " mph");
        coolantText.setText("Coolant: " + ectF + " °F");

        // Vehicle-tailored secondary metric values
        PIDAIMonitor.PIDSnapshot snapshot = new PIDAIMonitor.PIDSnapshot();
        snapshot.rpm = calcRpm;
        snapshot.speedMph = speedMph;
        snapshot.ectF = ectF;
        snapshot.batteryVolts = 14.1;

        if (currentVehicle.hasHeui()) {
            double boostPsi = (calcRpm < 1000) ? 0.0 : Math.min(26.5, ((calcRpm - 900) * 0.011) + (noise * 0.4));
            int eotF = 195 + (int)(Math.sin(packetCount / 20.0) * 4);
            int icpPsi = (calcRpm < 800) ? 580 : Math.min(3200, 600 + (int)((calcRpm / 3300.0) * 2400) + (int)(noise * 50));
            int transF = 165 + (int)(noise * 2);

            gaugeBoostVal.setText(String.format(Locale.US, "%.1f PSI", boostPsi));
            gaugeEotVal.setText(eotF + " °F");
            gaugeIcpVal.setText(String.format(Locale.US, "%,d PSI", icpPsi));
            gaugeTransVal.setText(transF + " °F");

            snapshot.boostPsi = boostPsi;
            snapshot.eotF = eotF;
            snapshot.icpPsi = icpPsi;
            snapshot.iprPct = (calcRpm < 800) ? 10.5 : Math.min(65.0, 12.0 + (throttleProgress * 0.45));
            snapshot.transTempF = transF;
        } else if (currentVehicle.hasCommonRail()) {
            double boostPsi = (calcRpm < 1000) ? 0.0 : Math.min(32.0, ((calcRpm - 900) * 0.013) + (noise * 0.5));
            int frpPsi = (calcRpm < 800) ? 5500 : Math.min(29000, 6000 + (int)((calcRpm / 3500.0) * 22000) + (int)(noise * 150));
            int sootPct = 42 + (int)(Math.sin(packetCount / 50.0) * 8);
            int transF = 170 + (int)(noise * 2);

            gaugeBoostVal.setText(String.format(Locale.US, "%.1f PSI", boostPsi));
            gaugeEotVal.setText(String.format(Locale.US, "%,d PSI", frpPsi));
            gaugeIcpVal.setText(sootPct + "%");
            gaugeTransVal.setText(transF + " °F");

            snapshot.boostPsi = boostPsi;
            snapshot.eotF = 200;
            snapshot.icpPsi = frpPsi;
            snapshot.transTempF = transF;
        } else {
            // Gasoline V8 / Standard OBD2
            double sparkAdv = (calcRpm < 1000) ? 12.0 : (16.0 + (throttleProgress * 0.18) + (noise * 0.5));
            double maf = (calcRpm < 1000) ? 4.8 : (5.0 + (calcRpm / 25.0) + (noise * 1.5));
            double stft = (noise * 2.5);
            double batt = 14.2 + (noise * 0.1);

            gaugeBoostVal.setText(String.format(Locale.US, "%.1f°", sparkAdv));
            gaugeEotVal.setText(String.format(Locale.US, "%.1f g/s", maf));
            gaugeIcpVal.setText(String.format(Locale.US, "%+.1f%%", stft));
            gaugeTransVal.setText(String.format(Locale.US, "%.1f V", batt));

            snapshot.boostPsi = 0.0;
            snapshot.eotF = 195;
            snapshot.icpPsi = 60; // 60 PSI gas fuel rail
            snapshot.batteryVolts = batt;
        }

        sampleRateLabel.setText("Auto Trip Logger: " + packetCount + " telemetry records | 10 Hz " + currentVehicle.protocol);
        if (tripAutoBadge != null) {
            tripAutoBadge.setText("AUTO-LOGGING");
            tripAutoBadge.setTextColor(0xFF4ADE80);
            tripAutoBadge.setBackgroundColor(0xFF064E3B);
        }

        // Live AI PID Sentinel Evaluation
        PIDAIMonitor.AnomalyResult eval = PIDAIMonitor.evaluateSnapshot(snapshot);
        if (aiMonitorBadge != null && aiMonitorStatus != null) {
            if (eval.hasAnomaly) {
                aiMonitorBadge.setText("⚠️ " + eval.severity + " ANOMALY");
                aiMonitorBadge.setTextColor(0xFFFEF08A);
                aiMonitorBadge.setBackgroundColor(0xFF854D0E);
                aiMonitorStatus.setText(eval.summary);
                aiMonitorStatus.setTextColor(0xFFFDE047);
            } else {
                aiMonitorBadge.setText("✓ " + currentVehicle.architecture.displayName.toUpperCase(Locale.US) + " NOMINAL");
                aiMonitorBadge.setTextColor(0xFF4ADE80);
                aiMonitorBadge.setBackgroundColor(0xFF064E3B);
                if (currentVehicle.hasHeui()) {
                    aiMonitorStatus.setText(String.format(Locale.US,
                            "EOT %d°F | ECT %d°F (Delta: %d°F) | ICP %s | IPR %.1f%%",
                            snapshot.eotF, ectF, (snapshot.eotF - ectF), gaugeIcpVal.getText(), snapshot.iprPct));
                } else if (currentVehicle.hasCommonRail()) {
                    aiMonitorStatus.setText(String.format(Locale.US,
                            "Rail Press: %s | DPF Soot: %s | ECT: %d°F | Trans: %s",
                            gaugeEotVal.getText(), gaugeIcpVal.getText(), ectF, gaugeTransVal.getText()));
                } else {
                    aiMonitorStatus.setText(String.format(Locale.US,
                            "Spark: %s | MAF: %s | STFT: %s | Batt: %s",
                            gaugeBoostVal.getText(), gaugeEotVal.getText(), gaugeIcpVal.getText(), gaugeTransVal.getText()));
                }
                aiMonitorStatus.setTextColor(0xFF94A3B8);
            }
        }

        if (eval.hasAnomaly && (packetCount % 20 == 0)) {
            PIDAIMonitor.checkAndAlert(getApplicationContext(), snapshot);
        }

        if (activeSession != null) {
            final long sId = activeSession.id;
            final int rpmVal = calcRpm;
            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                PIDRecord record = new PIDRecord();
                record.sessionId = sId;
                record.pidName = "RPM";
                record.pidValue = rpmVal;
                record.timestamp = System.currentTimeMillis();
                db.pidRecordDao().insert(record);
            }).start();
        }
    }

    public void refreshUI() {
        runOnUiThread(this::updateLiveTruckTelemetry);
    }

    private void startTrip(boolean isAutomatic) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            Session s = new Session();
            s.startTime = System.currentTimeMillis();
            s.vehicleVin = currentVehicle != null ? currentVehicle.vin : "1FTNW21F82EB74901";
            s.id = db.sessionDao().insert(s);
            activeSession = s;

            runOnUiThread(() -> {
                sessionText.setText("Trip #" + s.id + " active • Logged to Database");
                sessionText.setTextColor(0xFF4ADE80);
                startTripBtn.setEnabled(false);
                endTripBtn.setEnabled(true);
                if (isAutomatic) {
                    Toast.makeText(MainActivity.this, "Engine Running: Trip #" + s.id + " auto-logging started!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Started Trip #" + s.id, Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void endTrip(boolean isAutomatic) {
        if (activeSession == null) return;
        final long endedId = activeSession.id;
        activeSession = null;

        sessionText.setText("Trip #" + endedId + " completed & saved.");
        sessionText.setTextColor(0xFF94A3B8);
        startTripBtn.setEnabled(true);
        endTripBtn.setEnabled(false);

        if (isAutomatic) {
            Toast.makeText(MainActivity.this, "Engine Stopped: Trip #" + endedId + " auto-saved to Database", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Ended Trip #" + endedId + " & Saved to Database", Toast.LENGTH_SHORT).show();
        }

        new Thread(() -> exportTripToStorage(endedId)).start();
    }

    private void exportTripToStorage(long sessionId) {
        try {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<PIDRecord> records = db.pidRecordDao().recordsForSession(sessionId);

            File exportDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AutoSentry_Trips");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            String fileName = "Trip_" + sessionId + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".csv";
            File csvFile = new File(exportDir, fileName);

            FileWriter writer = new FileWriter(csvFile);
            writer.append("Timestamp,SessionID,PID_Name,Value,Vehicle\n");
            for (PIDRecord r : records) {
                writer.append(String.valueOf(r.timestamp)).append(",")
                        .append(String.valueOf(r.sessionId)).append(",")
                        .append(r.pidName).append(",")
                        .append(String.valueOf(r.pidValue)).append(",")
                        .append(currentVehicle.vin).append("\n");
            }
            writer.flush();
            writer.close();

            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Trip #" + sessionId + " exported: " + records.size() + " data points", Toast.LENGTH_SHORT).show());
        } catch (IOException e) {
            // Ignore file error
        }
    }
}
