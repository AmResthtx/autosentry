package com.autosentry.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.autosentry.app.data.VehicleProfile;
import com.autosentry.app.data.VehicleProfile.EngineArchitecture;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Vehicle Profile & Identification Manager.
 * Handles OBD-II vehicle interrogation, VIN decoding, and broadcasting
 * the identified vehicle state to all subsystems (PID library, live cluster,
 * diagnostics, maintenance, and AI parts).
 */
public class VehicleManager {

    private static final String PREF_NAME = "autosentry_vehicle_prefs";
    private static final String KEY_CURRENT_VIN = "current_vehicle_vin";

    public interface OnVehicleChangeListener {
        void onVehicleChanged(VehicleProfile newProfile);
    }

    private static final List<OnVehicleChangeListener> listeners = new ArrayList<>();
    private static VehicleProfile currentProfile = null;
    private static final List<VehicleProfile> PRESET_VEHICLES = new ArrayList<>();

    static {
        // 1. Ford 7.3L PowerStroke Turbodiesel (Classic HEUI)
        PRESET_VEHICLES.add(new VehicleProfile(
                "1FTNW21F82EB74901",
                "Ford",
                "F-350 Super Duty",
                2002,
                "Lariat 4x4 Dually",
                EngineArchitecture.FORD_73L_POWERSTROKE,
                "DPC-422 / VDH5 PCM",
                "SAE J1850 PWM (Ford Standard)",
                "🛻"
        ));

        // 2. Ford 6.0L PowerStroke Turbodiesel (VGT + 48V FICM)
        PRESET_VEHICLES.add(new VehicleProfile(
                "1FTWW21P46EB12894",
                "Ford",
                "F-250 Super Duty",
                2006,
                "King Ranch 4x4 FX4",
                EngineArchitecture.FORD_60L_POWERSTROKE,
                "VXC-301 / TQ2 FICM (48V)",
                "SAE J1850 PWM / CAN",
                "🛻"
        ));

        // 3. Ford 6.7L PowerStroke Scorpion Diesel (Common Rail + DPF)
        PRESET_VEHICLES.add(new VehicleProfile(
                "1FT8W3BT4LEC55102",
                "Ford",
                "F-350 Super Duty",
                2020,
                "Platinum 4x4 PowerStroke",
                EngineArchitecture.FORD_67L_POWERSTROKE,
                "Continental SID902 CAN-ECM",
                "ISO 15765-4 CAN (11bit/500k)",
                "🛻"
        ));

        // 4. Chevrolet 6.6L Duramax Turbo-Diesel (L5P Common Rail + Allison)
        PRESET_VEHICLES.add(new VehicleProfile(
                "1GC4K0EY5JF189342",
                "Chevrolet",
                "Silverado 2500HD",
                2018,
                "High Country Duramax",
                EngineArchitecture.GM_66L_DURAMAX,
                "Delphi E41 ECM / Allison A10",
                "ISO 15765-4 CAN (11bit/500k)",
                "🚚"
        ));

        // 5. Ram 2500 6.7L Cummins Turbo-Diesel (Common Rail High Output)
        PRESET_VEHICLES.add(new VehicleProfile(
                "3C6UR5DL7KG582103",
                "Ram",
                "2500 Heavy Duty",
                2019,
                "Laramie Longhorn 4x4",
                EngineArchitecture.RAM_67L_CUMMINS,
                "Cummins CM2350B CAN-ECM",
                "ISO 15765-4 CAN (11bit/500k)",
                "🐏"
        ));

        // 6. Ford F-150 5.0L Coyote Gas V8
        PRESET_VEHICLES.add(new VehicleProfile(
                "1FTFW1E57MFB22910",
                "Ford",
                "F-150",
                2021,
                "XLT FX4 5.0L Coyote",
                EngineArchitecture.FORD_GAS_COYOTE,
                "Ford Copperhead Dual-PCM",
                "ISO 15765-4 CAN (11bit/500k)",
                "🛻"
        ));

        // 7. Generic OBD-II Spark Gasoline Vehicle
        PRESET_VEHICLES.add(new VehicleProfile(
                "4T1BF1FK3MU781290",
                "Toyota",
                "Camry SE / Generic OBD2",
                2021,
                "2.5L Dynamic Force VVT-iE",
                EngineArchitecture.GENERIC_GAS_OBD2,
                "Denso Generic ISO-15765",
                "ISO 15765-4 CAN (11bit/500k)",
                "🚗"
        ));
    }

    public static synchronized VehicleProfile getActiveVehicle(Context context) {
        if (currentProfile == null) {
            if (context != null) {
                SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String savedVin = prefs.getString(KEY_CURRENT_VIN, PRESET_VEHICLES.get(0).vin);
                for (VehicleProfile p : PRESET_VEHICLES) {
                    if (p.vin.equalsIgnoreCase(savedVin)) {
                        currentProfile = p;
                        break;
                    }
                }
            }
            if (currentProfile == null) {
                currentProfile = PRESET_VEHICLES.get(0); // Default to 7.3L PowerStroke
            }
        }
        return currentProfile;
    }

    public static synchronized void setActiveVehicle(Context context, VehicleProfile profile) {
        currentProfile = profile;
        if (context != null && profile != null) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_CURRENT_VIN, profile.vin).apply();
        }
        notifyVehicleChanged(profile);
    }

    public static List<VehicleProfile> getPresetVehicles() {
        return new ArrayList<>(PRESET_VEHICLES);
    }

    public static synchronized void addListener(OnVehicleChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static synchronized void removeListener(OnVehicleChangeListener listener) {
        listeners.remove(listener);
    }

    private static synchronized void notifyVehicleChanged(VehicleProfile profile) {
        for (OnVehicleChangeListener listener : listeners) {
            try {
                listener.onVehicleChanged(profile);
            } catch (Exception e) {
                // Ignore listener exceptions
            }
        }
    }

    /**
     * Decodes an arbitrary VIN into a functional vehicle profile.
     */
    public static VehicleProfile decodeVIN(String vin) {
        if (vin == null || vin.trim().isEmpty()) {
            return PRESET_VEHICLES.get(0);
        }
        String clean = vin.trim().toUpperCase(Locale.US);

        // Check if matches known preset
        for (VehicleProfile p : PRESET_VEHICLES) {
            if (p.vin.equalsIgnoreCase(clean)) {
                return p;
            }
        }

        // Standard VIN decoding heuristics
        String make = "Universal";
        String model = "OBD-II Vehicle";
        int year = 2015;
        EngineArchitecture arch = EngineArchitecture.GENERIC_GAS_OBD2;
        String protocol = "ISO 15765-4 CAN";
        String symbol = "🚗";

        if (clean.startsWith("1FT") || clean.startsWith("2FT") || clean.startsWith("3FT")) {
            make = "Ford";
            symbol = "🛻";
            if (clean.length() >= 8) {
                char engineChar = clean.charAt(7);
                if (engineChar == 'F') {
                    model = "F-250/F-350 Super Duty";
                    arch = EngineArchitecture.FORD_73L_POWERSTROKE;
                    protocol = "SAE J1850 PWM (Ford)";
                } else if (engineChar == 'P') {
                    model = "F-250/F-350 Super Duty";
                    arch = EngineArchitecture.FORD_60L_POWERSTROKE;
                    protocol = "SAE J1850 PWM / CAN";
                } else if (engineChar == 'T' || engineChar == 'Y') {
                    model = "F-250/F-350 Super Duty";
                    arch = EngineArchitecture.FORD_67L_POWERSTROKE;
                    protocol = "ISO 15765-4 CAN (11bit/500k)";
                } else if (engineChar == '5' || engineChar == 'F' || engineChar == 'N') {
                    model = "F-150 V8";
                    arch = EngineArchitecture.FORD_GAS_COYOTE;
                    protocol = "ISO 15765-4 CAN";
                }
            }
        } else if (clean.startsWith("1GC") || clean.startsWith("1GT") || clean.startsWith("3GC")) {
            make = "Chevrolet / GMC";
            model = "Silverado / Sierra HD";
            symbol = "🚚";
            if (clean.length() >= 8 && (clean.charAt(7) == '1' || clean.charAt(7) == 'D' || clean.charAt(7) == '8')) {
                arch = EngineArchitecture.GM_66L_DURAMAX;
                protocol = "ISO 15765-4 CAN";
            }
        } else if (clean.startsWith("1C4") || clean.startsWith("1C6") || clean.startsWith("3C6")) {
            make = "Ram";
            model = "2500 / 3500 HD";
            symbol = "🐏";
            if (clean.length() >= 8 && (clean.charAt(7) == 'L' || clean.charAt(7) == 'A' || clean.charAt(7) == 'C')) {
                arch = EngineArchitecture.RAM_67L_CUMMINS;
                protocol = "ISO 15765-4 CAN";
            }
        }

        // Year decoding (10th digit)
        if (clean.length() >= 10) {
            char yChar = clean.charAt(9);
            if (yChar == '2') year = 2002;
            else if (yChar == '3') year = 2003;
            else if (yChar == '4') year = 2004;
            else if (yChar == '5') year = 2005;
            else if (yChar == '6') year = 2006;
            else if (yChar == '7') year = 2007;
            else if (yChar == '8') year = 2008;
            else if (yChar == '9') year = 2009;
            else if (yChar == 'A') year = 2010;
            else if (yChar == 'B') year = 2011;
            else if (yChar == 'C') year = 2012;
            else if (yChar == 'D') year = 2013;
            else if (yChar == 'E') year = 2014;
            else if (yChar == 'F') year = 2015;
            else if (yChar == 'G') year = 2016;
            else if (yChar == 'H') year = 2017;
            else if (yChar == 'J') year = 2018;
            else if (yChar == 'K') year = 2019;
            else if (yChar == 'L') year = 2020;
            else if (yChar == 'M') year = 2021;
            else if (yChar == 'N') year = 2022;
            else if (yChar == 'P') year = 2023;
        }

        return new VehicleProfile(
                clean,
                make,
                model,
                year,
                "Identified ECU",
                arch,
                "OBD-II Interrogated PCM",
                protocol,
                symbol
        );
    }
}
