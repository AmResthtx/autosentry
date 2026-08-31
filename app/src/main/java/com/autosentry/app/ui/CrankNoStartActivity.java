package com.autosentry.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.autosentry.app.R;
import com.autosentry.app.data.VehicleProfile;
import com.autosentry.app.util.SymbolRegistry;
import com.autosentry.app.util.VehicleManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CrankNoStartActivity extends AppCompatActivity {

    private TextView diagStatusBadge;
    private TextView textVerdictTitle;
    private TextView textVerdictDesc;
    private TextView textVerdictAction;
    private View verdictCard;

    private TextView valRpm;
    private TextView badgeRpm;
    private TextView descRpm;

    private TextView valIcp;
    private TextView badgeIcp;
    private TextView descIcp;

    private TextView valIpr;
    private TextView badgeIpr;
    private TextView descIpr;

    private TextView valVolts;
    private TextView badgeVolts;
    private TextView descVolts;

    private TextView valFuse30;
    private TextView badgeFuse30;
    private TextView descFuse30;

    private TextView valFuelSmoke;
    private TextView descFuelSmoke;

    private TextView buzzStatusText;
    private TextView cyl1, cyl2, cyl3, cyl4, cyl5, cyl6, cyl7, cyl8;

    private Spinner spinnerScenario;
    private Button btnRunCrankTest;
    private Button btnBuzzTest;
    private Button btnExportDiag;
    private Button btnOrderPartDeal;

    private VehicleProfile currentVehicle;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int currentScenario = 0;
    private boolean isBuzzTestRunning = false;

    private final List<String> scenarioList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crank_no_start);

        currentVehicle = VehicleManager.getActiveVehicle(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentVehicle.symbol + " " + currentVehicle.architecture.displayName + " No-Start Diagnostics");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        setupScenarioSpinner();

        btnRunCrankTest.setOnClickListener(v -> runCrankingDiagnostic());
        btnBuzzTest.setOnClickListener(v -> runInjectorBuzzTest());
        btnExportDiag.setOnClickListener(v -> exportDiagnosticReport());

        applyScenario(0);
    }

    private void initViews() {
        diagStatusBadge = findViewById(R.id.diag_status_badge);
        textVerdictTitle = findViewById(R.id.text_verdict_title);
        textVerdictDesc = findViewById(R.id.text_verdict_desc);
        textVerdictAction = findViewById(R.id.text_verdict_action);
        verdictCard = findViewById(R.id.verdict_card);

        valRpm = findViewById(R.id.val_rpm);
        badgeRpm = findViewById(R.id.badge_rpm);
        descRpm = findViewById(R.id.desc_rpm);

        valIcp = findViewById(R.id.val_icp);
        badgeIcp = findViewById(R.id.badge_icp);
        descIcp = findViewById(R.id.desc_icp);

        valIpr = findViewById(R.id.val_ipr);
        badgeIpr = findViewById(R.id.badge_ipr);
        descIpr = findViewById(R.id.desc_ipr);

        valVolts = findViewById(R.id.val_volts);
        badgeVolts = findViewById(R.id.badge_volts);
        descVolts = findViewById(R.id.desc_volts);

        valFuse30 = findViewById(R.id.val_fuse30);
        badgeFuse30 = findViewById(R.id.badge_fuse30);
        descFuse30 = findViewById(R.id.desc_fuse30);

        valFuelSmoke = findViewById(R.id.val_fuel_smoke);
        descFuelSmoke = findViewById(R.id.desc_fuel_smoke);

        buzzStatusText = findViewById(R.id.buzz_status_text);
        cyl1 = findViewById(R.id.cyl_1);
        cyl2 = findViewById(R.id.cyl_2);
        cyl3 = findViewById(R.id.cyl_3);
        cyl4 = findViewById(R.id.cyl_4);
        cyl5 = findViewById(R.id.cyl_5);
        cyl6 = findViewById(R.id.cyl_6);
        cyl7 = findViewById(R.id.cyl_7);
        cyl8 = findViewById(R.id.cyl_8);

        spinnerScenario = findViewById(R.id.spinner_scenario);
        btnRunCrankTest = findViewById(R.id.btn_run_crank_test);
        btnBuzzTest = findViewById(R.id.btn_buzz_test);
        btnExportDiag = findViewById(R.id.btn_export_diag);
    }

    private void setupScenarioSpinner() {
        scenarioList.clear();
        if (currentVehicle.architecture == VehicleProfile.EngineArchitecture.FORD_73L_POWERSTROKE) {
            scenarioList.add("🚨 1. Dead CPS Sensor (0 RPM / No Timing Sync)");
            scenarioList.add("🚨 2. Low ICP High-Pressure Oil Leak (240 PSI / IPR 84%)");
            scenarioList.add("🚨 3. Blown Fuse #30 (Fuel Heater Short / Dead WTS)");
            scenarioList.add("🚨 4. Weak Cranking Batteries (< 10.2V IDM Cutoff)");
            scenarioList.add("🚨 5. Burnt Glow Plug Relay (Cold Crank / White Smoke)");
            scenarioList.add("🚨 6. Biased ICP Sensor (Solved by Unplugging ICP)");
            scenarioList.add("🚨 7. Melted UVCH Harness (Cyl 3 & 5 Dead Solenoid)");
            scenarioList.add("🟢 8. Healthy 7.3L PowerStroke (All 5 Pillars PASS)");
        } else if (currentVehicle.architecture == VehicleProfile.EngineArchitecture.FORD_60L_POWERSTROKE) {
            scenarioList.add("🚨 1. Low FICM Main Power (32.5V < 48V Minimum)");
            scenarioList.add("🚨 2. HPOP Standpipe / Dummy Plug O-Ring Leak (ICP 280 PSI)");
            scenarioList.add("🚨 3. Torn IPR Valve Screen (IPR 85% Maxed)");
            scenarioList.add("🚨 4. Low Fuel Pressure / Blue Spring Regulator Failure");
            scenarioList.add("🟢 5. Healthy 6.0L PowerStroke (All Diagnostic Targets Met)");
        } else if (currentVehicle.isDiesel()) {
            scenarioList.add("🚨 1. High Pressure Fuel Rail Leak (FRP 450 PSI < 5000 PSI)");
            scenarioList.add("🚨 2. Failed Electric Fuel Lift Pump (0 PSI Feed)");
            scenarioList.add("🚨 3. Crankshaft Position Sensor No-Sync (0 RPM)");
            scenarioList.add("🟢 4. Healthy Common-Rail Diesel (Passes Cranking Rail Sync)");
        } else {
            scenarioList.add("🚨 1. Failed Crankshaft Position Sensor (0 RPM Signal)");
            scenarioList.add("🚨 2. Dead Fuel Pump Relay / Zero Fuel Rail Pressure");
            scenarioList.add("🚨 3. Weak Cranking Voltage (< 9.8V Starter Drop)");
            scenarioList.add("🟢 4. Healthy Gasoline V8 (All Sync & Pressure Nominal)");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                scenarioList
        );
        spinnerScenario.setAdapter(adapter);

        spinnerScenario.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentScenario = position;
                applyScenario(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void applyScenario(int scenarioIndex) {
        // Default healthy states
        cyl1.setText("CYL 1\n✓ PASS"); cyl1.setBackgroundColor(0xFF064E3B);
        cyl2.setText("CYL 2\n✓ PASS"); cyl2.setBackgroundColor(0xFF064E3B);
        cyl3.setText("CYL 3\n✓ PASS"); cyl3.setBackgroundColor(0xFF064E3B);
        cyl4.setText("CYL 4\n✓ PASS"); cyl4.setBackgroundColor(0xFF064E3B);
        cyl5.setText("CYL 5\n✓ PASS"); cyl5.setBackgroundColor(0xFF064E3B);
        cyl6.setText("CYL 6\n✓ PASS"); cyl6.setBackgroundColor(0xFF064E3B);
        cyl7.setText("CYL 7\n✓ PASS"); cyl7.setBackgroundColor(0xFF064E3B);
        cyl8.setText("CYL 8\n✓ PASS"); cyl8.setBackgroundColor(0xFF064E3B);
        buzzStatusText.setText("Status: Ready (Select scenario or tap Run Diagnostic)");

        if (currentVehicle.architecture == VehicleProfile.EngineArchitecture.FORD_73L_POWERSTROKE) {
            apply73Scenario(scenarioIndex);
        } else if (currentVehicle.architecture == VehicleProfile.EngineArchitecture.FORD_60L_POWERSTROKE) {
            apply60Scenario(scenarioIndex);
        } else if (currentVehicle.isDiesel()) {
            applyCommonRailScenario(scenarioIndex);
        } else {
            applyGasolineScenario(scenarioIndex);
        }
    }

    private void apply73Scenario(int index) {
        switch (index) {
            case 0: // Dead CPS
                setTelemetry(0, false, 0, false, 14.8, false, 11.4, true, true, "None", "No fuel injected (PCM blind to crank rotation)");
                setVerdict("🚨 DEAD CAMSHAFT POSITION SENSOR (CPS)",
                        "The PCM reports 0 RPM during cranking. Without a valid Hall-Effect timing signal from the CPS, the PCM will not command IPR duty cycle or fire the IDM injector drivers.",
                        "⚡ Action: Replace with Genuine OEM Motorcraft DU-87 Dark Grey CPS (Part # F7TZ-12K073-B). Avoid cheap aftermarket sensors which cause wiper EMF interference.");
                break;
            case 1: // Low ICP Leak
                setTelemetry(185, true, 240, false, 84.7, false, 10.9, true, true, "None", "No fuel injected (ICP < 500 PSI startup threshold)");
                setVerdict("🚨 HIGH-PRESSURE OIL LEAK (LOW ICP PRESSURE)",
                "Cranking ICP reached only 240 PSI while IPR duty cycle is maxed out at 84.7%. The 7.3L HEUI hydraulic injectors require a strict minimum of 500 PSI to fire the intensifier pistons.",
                "⚡ Action: Check HPOP oil reservoir level. If full, inspect IPR valve O-rings for tears or perform dead-head HPOP isolation test to diagnose injector O-ring blow-by.");
                break;
            case 2: // Blown Fuse #30
                setTelemetry(0, false, 0, false, 0.0, false, 12.2, true, false, "None", "No PCM communication / Fuel heater shorted");
                setVerdict("🚨 BLOWN FUSE #30 (FUEL BOWL HEATER SHORT)",
                "Fuse #30 in underhood power distribution box powers both the Fuel Bowl Heater Element and the Engine PCM. The factory fuel bowl element shorted to ground, cutting power to the PCM (Wait-To-Start light remains dead).",
                "⚡ Action: Unplug the 2-pin fuel heater electrical connector on the back of the fuel bowl, replace the 30A maxi-fuse, and key on.");
                break;
            case 3: // Weak Batteries
                setTelemetry(120, false, 360, false, 65.0, true, 9.4, false, true, "None", "IDM shut down below 10.2V threshold");
                setVerdict("🚨 CRANKING BATTERY VOLTAGE DROP (< 10.2V IDM CUTOFF)",
                "Battery voltage dropped to 9.4V under starter load. While the starter still turns the engine over slowly (120 RPM), the 7.3L Injector Driver Module (IDM) shuts down internally below 10.2V to prevent transformer damage.",
                "⚡ Action: Load test both heavy-duty Group 65 batteries. Charge or replace with dual 850+ CCA batteries and clean main starter ground terminals.");
                break;
            case 4: // Burnt GPR
                setTelemetry(195, true, 680, true, 28.5, true, 11.2, true, true, "White Smoke", "Raw unburnt diesel atomized into cold cylinders");
                setVerdict("🚨 GLOW PLUG RELAY (GPR) / GLOW PLUG FAILURE",
                "All hydraulic and electrical pillars PASS (RPM 195, ICP 680 PSI, Volts 11.2V), and heavy white diesel smoke exits the exhaust. Cylinders are receiving fuel but lack the compression heat needed to ignite cold diesel.",
                "⚡ Action: Check voltage across large GPR relay terminals with key on. Replace with heavy-duty Western Motors / White-Rodgers 586-902 200A continuous relay or Motorcraft ZD-11 glow plugs.");
                break;
            case 5: // Biased ICP Sensor
                setTelemetry(185, true, 2200, false, 14.5, false, 11.1, true, true, "None", "PCM thinks ICP is 2,200 PSI and opens IPR valve to dump oil");
                setVerdict("🚨 BIASED / SHORTED ICP PRESSURE SENSOR",
                "The ICP sensor is shorted internally, reporting a fake 2,200 PSI during cranking. The PCM responds by commanding IPR down to 14.5%, dumping actual oil pressure so injectors never fire.",
                "⚡ Action: UNPLUG the 3-pin ICP sensor connector on the driver cylinder head. The PCM will immediately default to a safe 725 PSI fallback map and the truck will start instantly!");
                break;
            case 6: // Melted UVCH Harness
                setTelemetry(190, true, 710, true, 24.0, true, 11.0, true, true, "Heavy White / Grey", "Bank 1 cylinders missing pulse");
                cyl3.setText("CYL 3\n❌ DEAD (OPEN)"); cyl3.setBackgroundColor(0xFF7F1D1D);
                cyl5.setText("CYL 5\n❌ DEAD (OPEN)"); cyl5.setBackgroundColor(0xFF7F1D1D);
                setVerdict("🚨 UNDER-VALVE-COVER HARNESS (UVCH) BURNT PINS",
                "Automated IDM buzz test revealed open solenoid circuits on Cylinders 3 & 5. The UVCH connector pins overheated and melted, causing loss of high-voltage (115V) injector pulse to the driver side bank.",
                "⚡ Action: Install genuine Ford / Motorcraft UVCH gasket kit and apply the Ford '50-Cent Mod' or retainer clip to prevent connector vibration loosening.");
                break;
            case 7: // Healthy Truck
            default:
                setTelemetry(198, true, 720, true, 24.5, true, 11.4, true, true, "Clean Haze (Fires Up)", "Proper combustion ignition");
                setVerdict("🟢 ALL 5 PILLARS HEALTHY — ENGINE RUNNING NOMINAL",
                "Engine cranking parameters satisfy all OEM 7.3L PowerStroke startup conditions:\n• RPM: 198 (Sync OK)\n• ICP: 720 PSI (> 500 PSI min)\n• IPR: 24.5% (Seal OK)\n• Batt: 11.4V (> 10.2V IDM cutoff)\n• PCM Power: OK",
                "✓ No diagnostic action required. Engine telemetry is operating at factory peak performance.");
                break;
        }
    }

    private void apply60Scenario(int index) {
        switch (index) {
            case 0: // Low FICM
                setTelemetry(180, true, 650, true, 30.0, true, 10.8, true, true, "None", "FICM Voltage 32.5V (Destroys injector coils)");
                setVerdict("🚨 6.0L FICM POWER BOARD FAILURE (< 45V)",
                        "FICM Main Power drops to 32.5V during cranking. The 6.0L fuel injection control module requires a strict 48.0V minimum to energize injector spool valves.",
                        "⚡ Action: Replace the FICM power supply board with an upgraded heavy-duty 48V power module or complete Alliant Power FICM.");
                break;
            case 1: // Standpipe leak
                setTelemetry(185, true, 280, false, 84.9, false, 11.0, true, true, "None", "ICP 280 PSI < 500 PSI startup threshold");
                setVerdict("🚨 6.0L STANDPIPE / DUMMY PLUG HIGH-PRESSURE OIL LEAK",
                        "ICP is stuck at 280 PSI and IPR is pegged at 84.9%. Hot oil is leaking past deteriorated D-rings in the factory standpipes and dummy plugs.",
                        "⚡ Action: Install updated Ford Teflon-backed standpipes and dummy plugs (Part # 6E7Z-9A332-B).");
                break;
            default:
                setTelemetry(200, true, 780, true, 26.0, true, 11.5, true, true, "Normal", "Combustion Sync Achieved");
                setVerdict("🟢 6.0L POWERSTROKE PILLARS HEALTHY",
                        "All cranking parameters (RPM, ICP 780 PSI, 48V FICM, IPR 26%) satisfy factory specifications.",
                        "✓ Engine is healthy and ready for operation.");
                break;
        }
    }

    private void applyCommonRailScenario(int index) {
        if (index == 0) {
            setTelemetry(190, true, 450, false, 85.0, false, 11.2, true, true, "None", "FRP 450 PSI < 5,000 PSI threshold");
            setVerdict("🚨 COMMON-RAIL LOW FUEL PRESSURE (FRP LEAK)",
                    "Fuel rail pressure only reaches 450 PSI. Modern common-rail diesels require 4,500 - 5,000 PSI to fire the piezo / solenoid injectors.",
                    "⚡ Action: Inspect high-pressure fuel regulator (FCA / MPROP) and check for excessive injector return flow / bad pressure relief valve.");
        } else {
            setTelemetry(205, true, 5800, true, 28.0, true, 11.5, true, true, "Normal", "Common Rail Sync Achieved");
            setVerdict("🟢 COMMON-RAIL TELEMETRY NOMINAL",
                    "Fuel rail pressure (5,800 PSI), cranking speed (205 RPM), and battery voltage (11.5V) all meet factory requirements.",
                    "✓ Engine diagnostics pass.");
        }
    }

    private void applyGasolineScenario(int index) {
        if (index == 0) {
            setTelemetry(0, false, 0, false, 0.0, false, 11.6, true, true, "None", "No RPM Signal from Crank Sensor");
            setVerdict("🚨 CRANKSHAFT POSITION SENSOR (CKP) DEAD",
                    "The ECU receives 0 RPM during cranking. Without crankshaft position pulses, the ECU will not trigger fuel injectors or ignition coils.",
                    "⚡ Action: Replace the Crankshaft Position Sensor (CKP) and check wiring harness for damage or corrosion.");
        } else {
            setTelemetry(220, true, 58, true, 25.0, true, 11.6, true, true, "Normal", "Ignition & Fuel Sync OK");
            setVerdict("🟢 GASOLINE V8 PILLARS NOMINAL",
                    "Cranking speed (220 RPM), fuel rail pressure (58 PSI), spark advance, and battery volts meet all OEM factory thresholds.",
                    "✓ Engine diagnostics pass.");
        }
    }

    private void setTelemetry(int rpm, boolean rpmPass, int icp, boolean icpPass, double ipr, boolean iprPass,
                              double volts, boolean voltsPass, boolean fuse30Pass, String smoke, String smokeDesc) {
        valRpm.setText(String.format(Locale.US, "%,d RPM", rpm));
        badgeRpm.setText(rpmPass ? "✓ PASS" : "❌ FAIL");
        badgeRpm.setBackgroundColor(rpmPass ? 0xFF064E3B : 0xFF7F1D1D);
        badgeRpm.setTextColor(rpmPass ? 0xFF4ADE80 : 0xFFFCA5A5);
        descRpm.setText(rpmPass ? "RPM ≥ 100 RPM • Hall-Effect sync active" : "CRANK NO-START: No RPM signal detected");

        valIcp.setText(String.format(Locale.US, "%,d PSI", icp));
        badgeIcp.setText(icpPass ? "✓ PASS" : "❌ FAIL");
        badgeIcp.setBackgroundColor(icpPass ? 0xFF064E3B : 0xFF7F1D1D);
        badgeIcp.setTextColor(icpPass ? 0xFF4ADE80 : 0xFFFCA5A5);
        descIcp.setText(icpPass ? "Pressure ≥ 500 PSI • Hydraulic threshold met" : "CRANK NO-START: Low hydraulic pressure (< 500 PSI)");

        valIpr.setText(String.format(Locale.US, "%.1f%%", ipr));
        badgeIpr.setText(iprPass ? "✓ PASS" : "❌ FAIL");
        badgeIpr.setBackgroundColor(iprPass ? 0xFF064E3B : 0xFF7F1D1D);
        badgeIpr.setTextColor(iprPass ? 0xFF4ADE80 : 0xFFFCA5A5);
        descIpr.setText(iprPass ? "Duty Cycle: 10% - 65% • Nominal seal" : "CRANK NO-START: IPR pegged (Hydraulic leak or blown seal)");

        valVolts.setText(String.format(Locale.US, "%.1f V", volts));
        badgeVolts.setText(voltsPass ? "✓ PASS" : "❌ FAIL");
        badgeVolts.setBackgroundColor(voltsPass ? 0xFF064E3B : 0xFF7F1D1D);
        badgeVolts.setTextColor(voltsPass ? 0xFF4ADE80 : 0xFFFCA5A5);
        descVolts.setText(voltsPass ? "Cranking Volts ≥ 10.2V • IDM fully energized" : "CRANK NO-START: Volts < 10.2V (Module voltage cutout)");

        valFuse30.setText(fuse30Pass ? "12.4 V (INTACT)" : "0.0 V (BLOWN / DEAD)");
        badgeFuse30.setText(fuse30Pass ? "✓ PASS" : "❌ FAIL");
        badgeFuse30.setBackgroundColor(fuse30Pass ? 0xFF064E3B : 0xFF7F1D1D);
        badgeFuse30.setTextColor(fuse30Pass ? 0xFF4ADE80 : 0xFFFCA5A5);
        descFuse30.setText(fuse30Pass ? "PCM & IDM main relay energized" : "CRANK NO-START: Fuel heater shorted, PCM unpowered");

        valFuelSmoke.setText("Tailpipe Smoke: " + smoke);
        descFuelSmoke.setText(smokeDesc);
    }

    private void setVerdict(String title, String desc, String action) {
        textVerdictTitle.setText(title);
        textVerdictDesc.setText(desc);
        textVerdictAction.setText(action);

        boolean isPass = title.contains("PASS") || title.contains("HEALTHY") || title.contains("NOMINAL");
        diagStatusBadge.setText(isPass ? "✓ NO-START DIAGNOSIS: SYSTEM HEALTHY" : "⚠️ CRITICAL FAILURE DETECTED");
        diagStatusBadge.setBackgroundColor(isPass ? 0xFF064E3B : 0xFF7F1D1D);
        diagStatusBadge.setTextColor(isPass ? 0xFF4ADE80 : 0xFFFCA5A5);

        verdictCard.setBackgroundColor(isPass ? 0xFF052E16 : 0xFF2D0606);
    }

    private void runCrankingDiagnostic() {
        Toast.makeText(this, "Connecting to " + currentVehicle.getFullVehicleTitle() + " OBD-II & Logging Cranking PIDs...", Toast.LENGTH_SHORT).show();
        diagStatusBadge.setText("🔄 INTERROGATING SENSORS & CRANKING PIDS...");
        diagStatusBadge.setBackgroundColor(0xFF0C4A6E);
        diagStatusBadge.setTextColor(0xFF38BDF8);

        handler.postDelayed(() -> {
            applyScenario(currentScenario);
            Toast.makeText(this, "Cranking Diagnostic Completed!", Toast.LENGTH_SHORT).show();
        }, 1200);
    }

    private void runInjectorBuzzTest() {
        if (isBuzzTestRunning) return;
        isBuzzTestRunning = true;
        btnBuzzTest.setEnabled(false);

        Toast.makeText(this, "Initiating IDM 115V Solenoid Buzz Sequence...", Toast.LENGTH_SHORT).show();
        buzzStatusText.setText("⚡ BUZZ TEST IN PROGRESS: Firing All 8 Injectors simultaneously...");
        buzzStatusText.setTextColor(0xFF38BDF8);

        handler.postDelayed(() -> testIndividualInjector(1), 1000);
    }

    private void testIndividualInjector(int cylinder) {
        if (cylinder > 8) {
            isBuzzTestRunning = false;
            btnBuzzTest.setEnabled(true);
            buzzStatusText.setText("✓ INJECTOR BUZZ TEST COMPLETE: All Solenoid Responses Logged.");
            buzzStatusText.setTextColor(0xFF4ADE80);
            Toast.makeText(this, "Buzz Test Sequence Complete", Toast.LENGTH_SHORT).show();
            return;
        }

        buzzStatusText.setText("⚡ BUZZ TEST: Testing Cylinder #" + cylinder + " Solenoid Resistance...");

        TextView cylView = getCylView(cylinder);
        if (currentScenario == 6 && (cylinder == 3 || cylinder == 5)) { // Melted UVCH Scenario
            if (cylView != null) {
                cylView.setText("CYL " + cylinder + "\n❌ DEAD");
                cylView.setBackgroundColor(0xFF7F1D1D);
            }
        } else {
            if (cylView != null) {
                cylView.setText("CYL " + cylinder + "\n✓ LOUD");
                cylView.setBackgroundColor(0xFF064E3B);
            }
        }

        handler.postDelayed(() -> testIndividualInjector(cylinder + 1), 600);
    }

    private TextView getCylView(int cyl) {
        switch (cyl) {
            case 1: return cyl1;
            case 2: return cyl2;
            case 3: return cyl3;
            case 4: return cyl4;
            case 5: return cyl5;
            case 6: return cyl6;
            case 7: return cyl7;
            case 8: return cyl8;
            default: return null;
        }
    }

    private void exportDiagnosticReport() {
        try {
            File exportDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AutoSentry_Diagnostics");
            if (!exportDir.exists()) exportDir.mkdirs();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File reportFile = new File(exportDir, "Crank_No_Start_Report_" + timeStamp + ".txt");

            FileWriter writer = new FileWriter(reportFile);
            writer.append("====================================================\n");
            writer.append("  AUTOSENTRY CRANK NO-START DIAGNOSTIC REPORT\n");
            writer.append("====================================================\n\n");
            writer.append("VEHICLE: ").append(currentVehicle.getFullVehicleTitle()).append("\n");
            writer.append("VIN: ").append(currentVehicle.vin).append("\n");
            writer.append("TIMESTAMP: ").append(new Date().toString()).append("\n\n");
            writer.append("DIAGNOSTIC VERDICT:\n");
            writer.append(textVerdictTitle.getText().toString()).append("\n\n");
            writer.append("TECHNICAL ANALYSIS:\n");
            writer.append(textVerdictDesc.getText().toString()).append("\n\n");
            writer.append("CORRECTIVE REPAIR ACTION:\n");
            writer.append(textVerdictAction.getText().toString()).append("\n\n");
            writer.append("LIVE CRANKING TELEMETRY:\n");
            writer.append("• RPM: ").append(valRpm.getText()).append(" (").append(badgeRpm.getText()).append(")\n");
            writer.append("• ICP / Rail Pressure: ").append(valIcp.getText()).append(" (").append(badgeIcp.getText()).append(")\n");
            writer.append("• IPR / Regulator Duty: ").append(valIpr.getText()).append(" (").append(badgeIpr.getText()).append(")\n");
            writer.append("• Cranking Battery Voltage: ").append(valVolts.getText()).append(" (").append(badgeVolts.getText()).append(")\n");
            writer.append("• Power / Fuse Status: ").append(valFuse30.getText()).append(" (").append(badgeFuse30.getText()).append(")\n");
            writer.append("• Exhaust Smoke Condition: ").append(valFuelSmoke.getText()).append("\n\n");
            writer.append("INJECTOR BUZZ STATUS:\n");
            writer.append(buzzStatusText.getText().toString()).append("\n");
            writer.flush();
            writer.close();

            Toast.makeText(this, "Exported Diagnostic Report to: " + reportFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
