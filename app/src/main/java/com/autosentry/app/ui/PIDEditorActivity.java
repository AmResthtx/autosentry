package com.autosentry.app.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.autosentry.app.R;
import com.autosentry.app.data.AppDatabase;
import com.autosentry.app.data.PIDDefinition;
import com.autosentry.app.data.VehicleProfile;
import com.autosentry.app.util.SymbolRegistry;
import com.autosentry.app.util.VehicleManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PIDEditorActivity extends AppCompatActivity {
    private ListView listView;
    private AutoCompleteTextView editName;
    private AutoCompleteTextView editCommand;
    private AutoCompleteTextView editSearch;
    private TextView pidCountLabel;
    private TextView pidVehicleSymbol;
    private TextView pidVehicleTitle;
    private TextView pidVehicleProtocol;
    private Button btnAdd;
    private Button btnReset;
    private Button btnToggleApplicable;

    private boolean filterApplicableOnly = true;
    private VehicleProfile currentVehicle;

    private final List<PIDDefinition> masterList = new ArrayList<>();
    private final List<PIDDefinition> displayedList = new ArrayList<>();
    private PIDAdapter adapter;

    private final List<String> searchSuggestions = new ArrayList<>();
    private final List<String> nameSuggestions = new ArrayList<>();
    private final List<String> commandSuggestions = new ArrayList<>();
    private final Map<String, String> nameToCommandMap = new HashMap<>();

    private ArrayAdapter<String> searchSugAdapter;
    private ArrayAdapter<String> nameSugAdapter;
    private ArrayAdapter<String> cmdSugAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pid_editor);

        currentVehicle = VehicleManager.getActiveVehicle(this);

        listView = findViewById(R.id.pid_list_view);
        editName = findViewById(R.id.edit_name);
        editCommand = findViewById(R.id.edit_command);
        editSearch = findViewById(R.id.edit_search_pid);
        pidCountLabel = findViewById(R.id.pid_count_label);
        pidVehicleSymbol = findViewById(R.id.pid_vehicle_symbol);
        pidVehicleTitle = findViewById(R.id.pid_vehicle_title);
        pidVehicleProtocol = findViewById(R.id.pid_vehicle_protocol);
        btnAdd = findViewById(R.id.btn_add_pid);
        btnReset = findViewById(R.id.btn_reset_pids);
        btnToggleApplicable = findViewById(R.id.btn_toggle_applicable_only);

        updateVehicleHeader();

        adapter = new PIDAdapter();
        listView.setAdapter(adapter);

        // Setup AutoComplete suggestion adapters
        searchSugAdapter = new ArrayAdapter<>(this, R.layout.item_pid_suggestion, searchSuggestions);
        editSearch.setAdapter(searchSugAdapter);

        nameSugAdapter = new ArrayAdapter<>(this, R.layout.item_pid_suggestion, nameSuggestions);
        editName.setAdapter(nameSugAdapter);

        cmdSugAdapter = new ArrayAdapter<>(this, R.layout.item_pid_suggestion, commandSuggestions);
        editCommand.setAdapter(cmdSugAdapter);

        btnAdd.setOnClickListener(v -> addCustomPID());
        btnReset.setOnClickListener(v -> reloadForscanPresets(true));

        btnToggleApplicable.setOnClickListener(v -> {
            filterApplicableOnly = !filterApplicableOnly;
            btnToggleApplicable.setText(filterApplicableOnly ? "Applicable Only" : "Show All PIDs");
            btnToggleApplicable.setBackgroundColor(filterApplicableOnly ? 0xFF0284C7 : 0xFF475569);
            filterPIDs(editSearch.getText() != null ? editSearch.getText().toString() : "");
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            PIDDefinition item = displayedList.get(position);
            showPIDOptionsDialog(item);
        });

        // Instant search text change listener with real-time list filtering
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPIDs(s != null ? s.toString() : "");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        editSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            editSearch.setText(selected);
            editSearch.setSelection(selected.length());
            filterPIDs(selected);
        });

        editName.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            if (nameToCommandMap.containsKey(selectedName)) {
                editCommand.setText(nameToCommandMap.get(selectedName));
            }
        });

        loadPIDsFromDatabase();
    }

    private void updateVehicleHeader() {
        if (currentVehicle != null) {
            pidVehicleSymbol.setText(currentVehicle.symbol != null ? currentVehicle.symbol : SymbolRegistry.getSymbol(currentVehicle.make));
            pidVehicleTitle.setText("Connected: " + currentVehicle.year + " " + currentVehicle.make + " " + currentVehicle.model);
            pidVehicleProtocol.setText("Architecture: " + currentVehicle.architecture.displayName + " • " + currentVehicle.protocol);
        }
    }

    private void loadPIDsFromDatabase() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<PIDDefinition> current = db.pidDefinitionDao().all();
            if (current.size() < 15) {
                reloadForscanPresets(false);
            } else {
                updateLists(current);
            }
        }).start();
    }

    private void updateLists(List<PIDDefinition> items) {
        runOnUiThread(() -> {
            masterList.clear();
            masterList.addAll(items);

            searchSuggestions.clear();
            nameSuggestions.clear();
            commandSuggestions.clear();
            nameToCommandMap.clear();

            for (PIDDefinition p : items) {
                searchSuggestions.add(p.name);
                searchSuggestions.add(p.command);
                nameSuggestions.add(p.name);
                commandSuggestions.add(p.command);
                nameToCommandMap.put(p.name, p.command);
            }

            searchSugAdapter.notifyDataSetChanged();
            nameSugAdapter.notifyDataSetChanged();
            cmdSugAdapter.notifyDataSetChanged();

            filterPIDs(editSearch.getText() != null ? editSearch.getText().toString() : "");
        });
    }

    private void filterPIDs(String query) {
        displayedList.clear();
        String q = query != null ? query.trim().toLowerCase(Locale.US) : "";

        for (PIDDefinition p : masterList) {
            // Apply vehicle-specific applicability filter if enabled
            if (filterApplicableOnly && currentVehicle != null) {
                if (!currentVehicle.isPidApplicable(p.command, p.name)) {
                    continue;
                }
            }

            if (q.isEmpty()) {
                displayedList.add(p);
            } else {
                if (p.name.toLowerCase(Locale.US).contains(q) || p.command.toLowerCase(Locale.US).contains(q)) {
                    displayedList.add(p);
                }
            }
        }
        adapter.notifyDataSetChanged();

        if (pidCountLabel != null) {
            String filterMode = filterApplicableOnly ? "Applicable to " + currentVehicle.architecture.displayName : "All Master PIDs";
            if (q.isEmpty()) {
                pidCountLabel.setText("Showing " + displayedList.size() + " (" + filterMode + ")");
            } else {
                pidCountLabel.setText("Showing " + displayedList.size() + " of " + masterList.size() + " (" + filterMode + ")");
            }
        }
    }

    private void addCustomPID() {
        String name = editName.getText().toString().trim();
        String command = editCommand.getText().toString().trim();
        if (name.isEmpty() || command.isEmpty()) {
            Toast.makeText(this, "Please enter both a PID Name and Command Hex", Toast.LENGTH_SHORT).show();
            return;
        }

        PIDDefinition def = new PIDDefinition();
        def.name = name;
        def.command = command;
        def.pollIntervalSeconds = 1;

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.pidDefinitionDao().insert(def);
            List<PIDDefinition> all = db.pidDefinitionDao().all();
            updateLists(all);
            runOnUiThread(() -> {
                editName.setText("");
                editCommand.setText("");
                Toast.makeText(PIDEditorActivity.this, "Added " + name, Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void showPIDOptionsDialog(PIDDefinition item) {
        String symbol = SymbolRegistry.getSymbol(item.name, item.command);
        boolean isApplicable = currentVehicle == null || currentVehicle.isPidApplicable(item.command, item.name);
        String applicabilityStr = isApplicable ? "✓ Applicable to " + (currentVehicle != null ? currentVehicle.architecture.displayName : "Connected Vehicle")
                : "⚠️ Non-applicable to " + (currentVehicle != null ? currentVehicle.architecture.displayName : "Current Engine");

        new AlertDialog.Builder(this)
            .setTitle(symbol + " " + item.name)
            .setMessage("PID Command: " + item.command + "\nPoll Interval: " + item.pollIntervalSeconds + "s\nVehicle Compatibility: " + applicabilityStr + "\nModule: Powertrain Control Module (PCM)")
            .setPositiveButton("OK", null)
            .setNegativeButton("Delete", (dialog, which) -> {
                new Thread(() -> {
                    AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                    db.pidDefinitionDao().delete(item);
                    List<PIDDefinition> all = db.pidDefinitionDao().all();
                    updateLists(all);
                }).start();
            })
            .show();
    }

    private void reloadForscanPresets(boolean showToast) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<PIDDefinition> current = db.pidDefinitionDao().all();
            for (PIDDefinition p : current) {
                db.pidDefinitionDao().delete(p);
            }

            // Universal & Vehicle-Specific PID Master Library
            addDef(db, "RPM - Engine Revolutions Per Minute", "010C", 1);
            addDef(db, "VSS - Vehicle Speed Sensor (MPH/KPH)", "010D", 1);
            addDef(db, "ECT - Engine Coolant Temperature (°F/°C)", "0105", 2);
            addDef(db, "VPWR - Vehicle Battery / Alternator Voltage (V)", "221155", 2);
            addDef(db, "APP - Accelerator Pedal Position (%)", "22032B", 1);
            addDef(db, "TP - Throttle Position Sensor (%)", "0111", 1);
            addDef(db, "LOAD_PCT - Calculated Engine Load Value (%)", "0104", 1);
            addDef(db, "RUN_TIME - Engine Run Time (Seconds)", "011F", 2);
            addDef(db, "DTC_COUNT - Number of Active Trouble Codes", "0101", 5);
            addDef(db, "MIL_DIST - Distance Since Check Engine Light (Miles)", "0121", 10);
            addDef(db, "WARM_UPS - Warm-Ups Since DTCs Cleared", "0130", 10);

            // Ford 7.3L / 6.0L HEUI & PowerStroke Specialized PIDs
            addDef(db, "ICP - Injector Control Pressure (PSI)", "221446", 1);
            addDef(db, "IPR_DC - Injector Pressure Regulator Duty Cycle (%)", "221434", 1);
            addDef(db, "EOT - Engine Oil Temperature (°F/°C)", "221310", 2);
            addDef(db, "BST_PSI - Turbocharger Boost Pressure (PSI)", "221440", 1);
            addDef(db, "MAP - Manifold Absolute Pressure (PSI/kPa)", "010B", 1);
            addDef(db, "BARO - Barometric Pressure Sensor (Hz/PSI)", "221127", 5);
            addDef(db, "TFT - Transmission Fluid Temperature (°F)", "221E1C", 3);
            addDef(db, "EBP - Exhaust Back Pressure (PSI)", "221445", 1);
            addDef(db, "FLI - Fuel Level Indicator (%)", "012F", 10);
            addDef(db, "FP - Fuel Pressure (Lift Pump PSI)", "010A", 2);
            addDef(db, "IAT - Intake Air Temperature (°F/°C)", "010F", 5);
            addDef(db, "IAT2 - Intake Air Temp Downstream Intercooler", "2216A8", 5);
            addDef(db, "MAF - Mass Air Flow Sensor (g/s)", "0110", 1);
            addDef(db, "INJ_PW - Fuel Injector Pulse Width (ms)", "221410", 1);
            addDef(db, "CMP - Camshaft Position Sensor Sync (Yes/No)", "2209D4", 1);
            addDef(db, "CKP - Crankshaft Position Sensor Sync", "2209D5", 1);
            addDef(db, "EOT_ECT_DIFF - Oil vs Coolant Delta Temp (°F)", "22C001", 3);
            addDef(db, "WG_DC - Turbo Wastegate Solenoid Duty Cycle (%)", "2216C1", 1);
            addDef(db, "GP_DC - Glow Plug Relay Commanded Duty Cycle (%)", "221430", 5);
            addDef(db, "GPC - Glow Plug Control Module Status", "221431", 5);
            addDef(db, "TC_SLIP - Torque Converter Clutch Slip RPM", "221E1E", 1);
            addDef(db, "TCC - Torque Converter Lockup Solenoid State", "221E1F", 2);
            addDef(db, "GEAR - Transmission Commanded Gear Ratio", "221E20", 1);
            addDef(db, "TR - Transmission Range Selector (PRNDL)", "221E21", 2);
            addDef(db, "BRAKE_SW - Brake Pedal Switch Position (ON/OFF)", "221103", 2);
            addDef(db, "CRUISE_SW - Speed Control Switch Inputs", "221104", 2);

            // 6.0L Specific FICM & VGT
            addDef(db, "FICM_MPWR - FICM Main Power Voltage (Target 48.0V)", "2209CE", 1);
            addDef(db, "FICM_LPWR - FICM Logic Power Voltage (12V)", "2209CF", 2);
            addDef(db, "VGT_DC - Variable Geometry Turbo Duty Cycle (%)", "22096C", 1);

            // Common Rail Diesel (6.7L PowerStroke, Duramax 6.6L, Cummins 6.7L)
            addDef(db, "FRP - High-Pressure Fuel Rail Pressure (PSI)", "22022D", 1);
            addDef(db, "FRP_DES - Commanded Fuel Rail Pressure (PSI)", "22022E", 1);
            addDef(db, "DPF_SOOT - Diesel Particulate Filter Soot Load (%)", "220436", 5);
            addDef(db, "DPF_REGEN - Active DPF Regeneration Status", "220437", 5);
            addDef(db, "DEF_LVL - Diesel Exhaust Fluid Level (%)", "220438", 10);
            addDef(db, "EGT1_TEMP - Exhaust Gas Temperature Sensor 1 (°F)", "220439", 2);
            addDef(db, "EGT2_TEMP - Pre-DPF Catalyst Temperature (°F)", "22043A", 2);

            // Gasoline Specific PIDs (Coyote V8 / Generic OBD2)
            addDef(db, "SPARK_ADV - Ignition Timing Advance Cylinder 1 (deg)", "010E", 1);
            addDef(db, "STFT_B1 - Short Term Fuel Trim Bank 1 (%)", "0106", 1);
            addDef(db, "LTFT_B1 - Long Term Fuel Trim Bank 1 (%)", "0107", 2);
            addDef(db, "STFT_B2 - Short Term Fuel Trim Bank 2 (%)", "0108", 1);
            addDef(db, "LTFT_B2 - Long Term Fuel Trim Bank 2 (%)", "0109", 2);
            addDef(db, "O2S11_V - O2 Sensor Voltage Bank 1 Sensor 1 (V)", "0114", 1);
            addDef(db, "CAT_TEMP11 - Catalyst Temperature Bank 1 Sensor 1 (°F)", "013C", 3);
            addDef(db, "EVAP_VP - EVAP System Vapor Pressure (Pa)", "0132", 5);

            List<PIDDefinition> all = db.pidDefinitionDao().all();
            updateLists(all);
            if (showToast) {
                runOnUiThread(() -> Toast.makeText(PIDEditorActivity.this, "Loaded PID master library with vehicle filters!", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void addDef(AppDatabase db, String name, String cmd, int interval) {
        PIDDefinition p = new PIDDefinition();
        p.name = name;
        p.command = cmd;
        p.pollIntervalSeconds = interval;
        db.pidDefinitionDao().insert(p);
    }

    private class PIDAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return displayedList.size();
        }

        @Override
        public Object getItem(int position) {
            return displayedList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return displayedList.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(PIDEditorActivity.this).inflate(R.layout.item_pid, parent, false);
            }
            PIDDefinition item = displayedList.get(position);
            TextView symbolView = convertView.findViewById(R.id.pid_item_symbol);
            TextView nameView = convertView.findViewById(R.id.pid_item_name);
            TextView detailsView = convertView.findViewById(R.id.pid_item_details);
            TextView applicView = convertView.findViewById(R.id.pid_item_applicability);
            TextView badgeView = convertView.findViewById(R.id.pid_item_badge);

            // Assign crisp visual symbol
            String symbol = SymbolRegistry.getSymbol(item.name, item.command);
            symbolView.setText(symbol);

            nameView.setText(item.name);
            detailsView.setText("OBD Command: " + item.command + " • Poll: " + item.pollIntervalSeconds + "s • PCM");

            boolean isApplicable = currentVehicle == null || currentVehicle.isPidApplicable(item.command, item.name);
            if (isApplicable) {
                applicView.setText("✓ Matched to " + (currentVehicle != null ? currentVehicle.architecture.displayName : "Vehicle"));
                applicView.setTextColor(0xFF4ADE80);
                badgeView.setText("ACTIVE");
                badgeView.setTextColor(0xFF4ADE80);
                badgeView.setBackgroundColor(0xFF064E3B);
            } else {
                applicView.setText("⚠️ Not applicable to " + (currentVehicle != null ? currentVehicle.architecture.displayName : "Engine"));
                applicView.setTextColor(0xFFFBBF24);
                badgeView.setText("INACTIVE");
                badgeView.setTextColor(0xFF94A3B8);
                badgeView.setBackgroundColor(0xFF334155);
            }

            return convertView;
        }
    }
}
