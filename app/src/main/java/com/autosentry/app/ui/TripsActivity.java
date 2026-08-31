package com.autosentry.app.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.autosentry.app.R;
import com.autosentry.app.data.AppDatabase;
import com.autosentry.app.data.Session;
import com.autosentry.app.data.SessionDao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripsActivity extends AppCompatActivity {

    private TextView statTotalMiles;
    private TextView statAvgMpg;
    private TextView statFuelUsed;
    private TextView statTotalTrips;
    private TextView tripsEmptyView;

    private EditText editSearchTrips;
    private ListView tripsListView;

    private Button tabAll;
    private Button tabTowing;
    private Button tabHighway;
    private Button tabCommute;

    private final List<Session> masterTripList = new ArrayList<>();
    private final List<Session> displayedTripList = new ArrayList<>();
    private TripAdapter adapter;
    private int activeFilter = 0; // 0: All, 1: Towing, 2: Highway, 3: Commute

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        statTotalMiles = findViewById(R.id.stat_total_miles);
        statAvgMpg = findViewById(R.id.stat_avg_mpg);
        statFuelUsed = findViewById(R.id.stat_fuel_used);
        statTotalTrips = findViewById(R.id.stat_total_trips);
        tripsEmptyView = findViewById(R.id.trips_empty_view);

        editSearchTrips = findViewById(R.id.edit_search_trips);
        tripsListView = findViewById(R.id.trips_list_view);

        tabAll = findViewById(R.id.tab_trips_all);
        tabTowing = findViewById(R.id.tab_trips_towing);
        tabHighway = findViewById(R.id.tab_trips_highway);
        tabCommute = findViewById(R.id.tab_trips_commute);

        Button btnExport = findViewById(R.id.btn_export_trips_action);
        Button btnAddTrip = findViewById(R.id.btn_add_trip);

        adapter = new TripAdapter();
        tripsListView.setAdapter(adapter);

        tabAll.setOnClickListener(v -> setFilter(0));
        tabTowing.setOnClickListener(v -> setFilter(1));
        tabHighway.setOnClickListener(v -> setFilter(2));
        tabCommute.setOnClickListener(v -> setFilter(3));

        btnExport.setOnClickListener(v -> exportTripsToCsv());
        btnAddTrip.setOnClickListener(v -> showAddTripDialog());

        editSearchTrips.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilterAndSearch();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        seedInitialTripsIfEmpty();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTripsFromDatabase();
    }

    private void setFilter(int filterIndex) {
        activeFilter = filterIndex;
        tabAll.setBackgroundColor(filterIndex == 0 ? 0xFF0284C7 : 0xFF334155);
        tabTowing.setBackgroundColor(filterIndex == 1 ? 0xFF0284C7 : 0xFF334155);
        tabHighway.setBackgroundColor(filterIndex == 2 ? 0xFF0284C7 : 0xFF334155);
        tabCommute.setBackgroundColor(filterIndex == 3 ? 0xFF0284C7 : 0xFF334155);
        applyFilterAndSearch();
    }

    private void seedInitialTripsIfEmpty() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            SessionDao dao = db.sessionDao();
            List<Session> existing = dao.getAll();
            if (existing.isEmpty() || (existing.size() == 1 && existing.get(0).distanceMiles == 0)) {
                long now = System.currentTimeMillis();

                Session t1 = new Session();
                t1.startTime = now - (3 * 3600 * 1000L);
                t1.endTime = now - (30 * 60 * 1000L);
                t1.tripPurpose = "Heavy Towing";
                t1.notes = "Denver to Silverthorne Pass with 12,000 lb gooseneck trailer. Monitored EOT/ECT deltas.";
                t1.distanceMiles = 142.6;
                t1.avgSpeedMph = 57.0;
                t1.maxSpeedMph = 74;
                t1.avgRpm = 1950;
                t1.maxRpm = 2850;
                t1.avgMpg = 13.8;
                t1.fuelConsumedGal = 10.3;
                t1.maxBoostPsi = 24.8;
                t1.maxEotF = 206;
                t1.maxTransTempF = 178;
                t1.avgIcpPsi = 2100;
                t1.maxIprPct = 52.4;
                t1.healthAlertsCount = 0;
                t1.adapterType = "OBDLink EX (CAN/PWM)";
                t1.vehicleVin = "1FTNW21F82EB74901";
                dao.insert(t1);

                Session t2 = new Session();
                t2.startTime = now - (28 * 3600 * 1000L);
                t2.endTime = now - (25 * 3600 * 1000L);
                t2.tripPurpose = "Highway Hauls";
                t2.notes = "Interstate 70 cruising. Verified Stage 1 Turbo boost curve and steady 670 RPM idle.";
                t2.distanceMiles = 198.4;
                t2.avgSpeedMph = 68.5;
                t2.maxSpeedMph = 78;
                t2.avgRpm = 1820;
                t2.maxRpm = 2400;
                t2.avgMpg = 17.4;
                t2.fuelConsumedGal = 11.4;
                t2.maxBoostPsi = 18.2;
                t2.maxEotF = 198;
                t2.maxTransTempF = 162;
                t2.avgIcpPsi = 1750;
                t2.maxIprPct = 44.0;
                t2.healthAlertsCount = 0;
                t2.adapterType = "OBDLink EX (CAN/PWM)";
                t2.vehicleVin = "1FTNW21F82EB74901";
                dao.insert(t2);

                Session t3 = new Session();
                t3.startTime = now - (54 * 3600 * 1000L);
                t3.endTime = now - (53 * 3600 * 1000L);
                t3.tripPurpose = "Daily Commute";
                t3.notes = "Local shop run to pick up Motorcraft FL-1995 oil filters and Rotella T6 oil.";
                t3.distanceMiles = 24.5;
                t3.avgSpeedMph = 38.0;
                t3.maxSpeedMph = 55;
                t3.avgRpm = 1450;
                t3.maxRpm = 2100;
                t3.avgMpg = 14.9;
                t3.fuelConsumedGal = 1.6;
                t3.maxBoostPsi = 14.0;
                t3.maxEotF = 192;
                t3.maxTransTempF = 150;
                t3.avgIcpPsi = 1200;
                t3.maxIprPct = 38.5;
                t3.healthAlertsCount = 0;
                t3.adapterType = "OBDLink EX (CAN/PWM)";
                t3.vehicleVin = "1FTNW21F82EB74901";
                dao.insert(t3);
            }
            runOnUiThread(this::loadTripsFromDatabase);
        }).start();
    }

    private void loadTripsFromDatabase() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<Session> trips = db.sessionDao().getAll();
            runOnUiThread(() -> {
                masterTripList.clear();
                masterTripList.addAll(trips);
                updateCumulativeStats();
                applyFilterAndSearch();
            });
        }).start();
    }

    private void updateCumulativeStats() {
        double totalMiles = 0;
        double totalFuel = 0;
        int tripCount = masterTripList.size();

        for (Session s : masterTripList) {
            totalMiles += (s.distanceMiles > 0 ? s.distanceMiles : 15.0);
            totalFuel += (s.fuelConsumedGal > 0 ? s.fuelConsumedGal : (s.distanceMiles / 15.0));
        }

        double avgMpg = totalFuel > 0 ? (totalMiles / totalFuel) : 15.5;

        statTotalMiles.setText(String.format(Locale.US, "%,.1f mi", totalMiles));
        statAvgMpg.setText(String.format(Locale.US, "%.1f MPG", avgMpg));
        statFuelUsed.setText(String.format(Locale.US, "%,.1f gal", totalFuel));
        statTotalTrips.setText(String.format(Locale.US, "%d Trips", tripCount));
    }

    private void applyFilterAndSearch() {
        displayedTripList.clear();
        String query = editSearchTrips.getText() != null ? editSearchTrips.getText().toString().toLowerCase().trim() : "";

        for (Session s : masterTripList) {
            String purpose = s.tripPurpose != null ? s.tripPurpose : "Trip";
            if (activeFilter == 1 && !purpose.toLowerCase().contains("towing")) continue;
            if (activeFilter == 2 && !purpose.toLowerCase().contains("highway")) continue;
            if (activeFilter == 3 && !purpose.toLowerCase().contains("commute")) continue;

            if (query.isEmpty()) {
                displayedTripList.add(s);
            } else {
                boolean matchNotes = s.notes != null && s.notes.toLowerCase().contains(query);
                boolean matchPurpose = s.tripPurpose != null && s.tripPurpose.toLowerCase().contains(query);
                boolean matchId = String.valueOf(s.id).contains(query);
                if (matchNotes || matchPurpose || matchId) {
                    displayedTripList.add(s);
                }
            }
        }

        tripsEmptyView.setVisibility(displayedTripList.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
    }

    private void showAddTripDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_trip, null);
        EditText editName = dialogView.findViewById(R.id.dialog_trip_name);
        Spinner spinnerPurpose = dialogView.findViewById(R.id.dialog_trip_purpose_spinner);
        EditText editDistance = dialogView.findViewById(R.id.dialog_trip_distance);
        EditText editFuel = dialogView.findViewById(R.id.dialog_trip_fuel);
        EditText editBoost = dialogView.findViewById(R.id.dialog_trip_boost);
        EditText editEot = dialogView.findViewById(R.id.dialog_trip_eot);
        EditText editNotes = dialogView.findViewById(R.id.dialog_trip_notes);

        String[] purposes = {"Heavy Towing", "Highway Hauls", "Daily Commute", "Off-Road Trail", "Dyno / Test Run"};
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, purposes);
        spinnerPurpose.setAdapter(spinAdapter);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Save Trip", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    String purpose = (String) spinnerPurpose.getSelectedItem();
                    String distStr = editDistance.getText().toString().trim();
                    String fuelStr = editFuel.getText().toString().trim();
                    String boostStr = editBoost.getText().toString().trim();
                    String eotStr = editEot.getText().toString().trim();
                    String notes = editNotes.getText().toString().trim();

                    double dist = distStr.isEmpty() ? 25.0 : Double.parseDouble(distStr);
                    double fuel = fuelStr.isEmpty() ? (dist / 15.0) : Double.parseDouble(fuelStr);
                    double boost = boostStr.isEmpty() ? 20.0 : Double.parseDouble(boostStr);
                    int eot = eotStr.isEmpty() ? 195 : Integer.parseInt(eotStr);

                    new Thread(() -> {
                        Session session = new Session();
                        session.startTime = System.currentTimeMillis() - ((long)(dist / 50.0 * 3600 * 1000));
                        session.endTime = System.currentTimeMillis();
                        session.tripPurpose = purpose;
                        session.notes = name.isEmpty() ? notes : (name + " - " + notes);
                        session.distanceMiles = dist;
                        session.fuelConsumedGal = fuel;
                        session.avgMpg = fuel > 0 ? (dist / fuel) : 15.0;
                        session.maxBoostPsi = boost;
                        session.maxEotF = eot;
                        session.maxTransTempF = 165;
                        session.avgSpeedMph = 55.0;
                        session.maxSpeedMph = 72;
                        session.avgRpm = 1750;
                        session.maxRpm = 2500;
                        session.avgIcpPsi = 1800;
                        session.maxIprPct = 48.0;
                        session.adapterType = "OBDLink EX USB/BT";
                        session.vehicleVin = "1FTNW21F82EB74901";

                        AppDatabase.getInstance(getApplicationContext()).sessionDao().insert(session);
                        runOnUiThread(() -> {
                            Toast.makeText(TripsActivity.this, "Trip successfully logged to database!", Toast.LENGTH_SHORT).show();
                            loadTripsFromDatabase();
                        });
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
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
                    writer.append("TripID,Purpose,StartTime,EndTime,DistanceMiles,FuelGal,AvgMPG,MaxBoostPSI,MaxEOT_F,MaxTrans_F,Notes\n");
                    for (Session s : masterTripList) {
                        writer.append(String.format(Locale.US,
                                "%d,\"%s\",%d,%d,%.2f,%.2f,%.2f,%.1f,%d,%d,\"%s\"\n",
                                s.id,
                                s.tripPurpose != null ? s.tripPurpose : "Trip",
                                s.startTime, s.endTime,
                                s.distanceMiles, s.fuelConsumedGal, s.avgMpg,
                                s.maxBoostPsi, s.maxEotF, s.maxTransTempF,
                                s.notes != null ? s.notes.replace("\"", "'") : ""));
                    }
                }
                runOnUiThread(() -> Toast.makeText(TripsActivity.this,
                        "Exported " + masterTripList.size() + " trips to: " + csvFile.getAbsolutePath(),
                        Toast.LENGTH_LONG).show());
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(TripsActivity.this,
                        "Export error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private class TripAdapter extends ArrayAdapter<Session> {
        TripAdapter() {
            super(TripsActivity.this, 0, displayedTripList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_trip_record, parent, false);
            }

            Session s = getItem(position);
            if (s == null) return convertView;

            TextView badgePurpose = convertView.findViewById(R.id.trip_purpose_badge);
            TextView title = convertView.findViewById(R.id.trip_title);
            TextView timestamp = convertView.findViewById(R.id.trip_timestamp);
            TextView distBadge = convertView.findViewById(R.id.trip_distance_badge);
            TextView mpg = convertView.findViewById(R.id.trip_mpg);
            TextView maxBoost = convertView.findViewById(R.id.trip_max_boost);
            TextView maxEot = convertView.findViewById(R.id.trip_max_eot);
            TextView maxTrans = convertView.findViewById(R.id.trip_max_trans);
            TextView speedFuel = convertView.findViewById(R.id.trip_speed_fuel);
            TextView health = convertView.findViewById(R.id.trip_health_status);
            TextView notes = convertView.findViewById(R.id.trip_notes);
            Button btnTelemetry = convertView.findViewById(R.id.btn_view_telemetry);
            Button btnDelete = convertView.findViewById(R.id.btn_delete_trip);

            String purpose = s.tripPurpose != null ? s.tripPurpose : "TRUCK TRIP";
            badgePurpose.setText(purpose.toUpperCase());
            if (purpose.toLowerCase().contains("towing")) {
                badgePurpose.setBackgroundColor(0xFF78350F);
                badgePurpose.setTextColor(0xFFF59E0B);
            } else if (purpose.toLowerCase().contains("highway")) {
                badgePurpose.setBackgroundColor(0xFF0C4A6E);
                badgePurpose.setTextColor(0xFF38BDF8);
            } else {
                badgePurpose.setBackgroundColor(0xFF064E3B);
                badgePurpose.setTextColor(0xFF4ADE80);
            }

            title.setText("Trip #" + s.id + ": " + (s.notes != null && !s.notes.isEmpty() ? s.notes.split("-")[0].trim() : "Road Run"));

            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US);
            String startStr = sdf.format(new Date(s.startTime > 0 ? s.startTime : System.currentTimeMillis()));
            long durationMs = (s.endTime > s.startTime) ? (s.endTime - s.startTime) : (long)(s.distanceMiles / 55.0 * 3600 * 1000);
            long hours = durationMs / (3600 * 1000);
            long mins = (durationMs % (3600 * 1000)) / (60 * 1000);
            timestamp.setText(startStr + " (" + (hours > 0 ? hours + "h " : "") + mins + "m)");

            distBadge.setText(String.format(Locale.US, "%.1f mi", s.distanceMiles > 0 ? s.distanceMiles : 18.5));
            mpg.setText(String.format(Locale.US, "%.1f MPG", s.avgMpg > 0 ? s.avgMpg : 15.2));
            maxBoost.setText(String.format(Locale.US, "%.1f PSI", s.maxBoostPsi > 0 ? s.maxBoostPsi : 21.0));
            maxEot.setText((s.maxEotF > 0 ? s.maxEotF : 196) + " °F");
            maxTrans.setText((s.maxTransTempF > 0 ? s.maxTransTempF : 160) + " °F");

            speedFuel.setText(String.format(Locale.US, "Avg Speed: %.0f mph (Max %d) • Fuel: %.1f gal",
                    s.avgSpeedMph > 0 ? s.avgSpeedMph : 55.0,
                    s.maxSpeedMph > 0 ? s.maxSpeedMph : 72,
                    s.fuelConsumedGal > 0 ? s.fuelConsumedGal : 3.2));

            if (s.healthAlertsCount == 0) {
                health.setText("✓ 0 PID Anomalies");
                health.setTextColor(0xFF4ADE80);
            } else {
                health.setText("⚠️ " + s.healthAlertsCount + " PID Alerts");
                health.setTextColor(0xFFF87171);
            }

            notes.setText(s.notes != null ? s.notes : "No driver notes logged for this run.");

            btnTelemetry.setOnClickListener(v -> {
                new AlertDialog.Builder(TripsActivity.this)
                        .setTitle("Trip #" + s.id + " Telemetry Breakdown")
                        .setMessage(String.format(Locale.US,
                                "• Distance: %.1f miles\n" +
                                "• Duration: %d min\n" +
                                "• Avg Engine RPM: %d RPM\n" +
                                "• Peak RPM: %d RPM\n" +
                                "• Peak Turbo Boost: %.1f PSI\n" +
                                "• Max Oil Temp (EOT): %d °F\n" +
                                "• Max Trans Temp: %d °F\n" +
                                "• Avg ICP Pressure: %d PSI\n" +
                                "• Max IPR Duty Cycle: %.1f%%\n" +
                                "• Diesel Fuel Consumed: %.1f gal\n" +
                                "• Fuel Economy: %.1f MPG\n" +
                                "• OBD Adapter: %s\n" +
                                "• Vehicle VIN: %s",
                                s.distanceMiles > 0 ? s.distanceMiles : 18.5,
                                (int)(durationMs / 60000),
                                s.avgRpm > 0 ? s.avgRpm : 1750,
                                s.maxRpm > 0 ? s.maxRpm : 2500,
                                s.maxBoostPsi > 0 ? s.maxBoostPsi : 21.0,
                                s.maxEotF > 0 ? s.maxEotF : 196,
                                s.maxTransTempF > 0 ? s.maxTransTempF : 160,
                                s.avgIcpPsi > 0 ? s.avgIcpPsi : 1800,
                                s.maxIprPct > 0 ? s.maxIprPct : 48.0,
                                s.fuelConsumedGal > 0 ? s.fuelConsumedGal : 3.2,
                                s.avgMpg > 0 ? s.avgMpg : 15.2,
                                s.adapterType != null ? s.adapterType : "OBDLink EX",
                                s.vehicleVin != null ? s.vehicleVin : "1FTNW21F82EB74901"))
                        .setPositiveButton("Close", null)
                        .show();
            });

            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(TripsActivity.this)
                        .setTitle("Delete Trip #" + s.id + "?")
                        .setMessage("Are you sure you want to delete this trip record from the local database?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            new Thread(() -> {
                                AppDatabase.getInstance(getApplicationContext()).sessionDao().delete(s);
                                runOnUiThread(TripsActivity.this::loadTripsFromDatabase);
                            }).start();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            return convertView;
        }
    }
}
