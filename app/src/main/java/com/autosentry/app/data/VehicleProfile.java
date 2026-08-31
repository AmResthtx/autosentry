package com.autosentry.app.data;

import java.util.Locale;

/**
 * Data Model and Rules Engine for Connected Vehicle Identification.
 * Governs which PIDs, live gauges, diagnostic tests, and parts are applicable
 * strictly to the currently identified vehicle.
 */
public class VehicleProfile {

    public enum EngineArchitecture {
        FORD_73L_POWERSTROKE("7.3L PowerStroke Diesel", "HEUI (Hydraulic Electronic Unit Injector)", "SAE J1850 PWM", "🛻"),
        FORD_60L_POWERSTROKE("6.0L PowerStroke Diesel", "HEUI + VGT Turbo + 48V FICM", "SAE J1850 PWM / CAN", "🛻"),
        FORD_67L_POWERSTROKE("6.7L PowerStroke Scorpion", "Bosch Common Rail (29k PSI) + DPF/SCR", "ISO 15765-4 CAN", "🛻"),
        GM_66L_DURAMAX("6.6L Duramax Turbo-Diesel", "Bosch CP3/CP4 Common Rail + Allison 1000/10L1000", "SAE J1850 VPW / CAN", "🚚"),
        RAM_67L_CUMMINS("6.7L Cummins Turbo-Diesel", "Bosch High-Pressure Common Rail + Grid Heater", "ISO 15765-4 CAN", "🐏"),
        FORD_GAS_COYOTE("5.0L / 6.2L / 7.3L Gas V8", "Multi-Port / Direct Fuel Injection + Spark", "ISO 15765-4 CAN", "🛻"),
        GENERIC_GAS_OBD2("Standard OBD-II Gasoline", "Standard Spark Ignition Engine", "ISO 15765-4 CAN / J1850", "🚗");

        public final String displayName;
        public final String techDescription;
        public final String defaultProtocol;
        public final String defaultSymbol;

        EngineArchitecture(String displayName, String techDescription, String defaultProtocol, String defaultSymbol) {
            this.displayName = displayName;
            this.techDescription = techDescription;
            this.defaultProtocol = defaultProtocol;
            this.defaultSymbol = defaultSymbol;
        }
    }

    public String vin;
    public String make;
    public String model;
    public int year;
    public String trim;
    public EngineArchitecture architecture;
    public String pcmStrategy;
    public String protocol;
    public String connectionStatus;
    public String symbol;

    public VehicleProfile() {
        // Default initialized to 7.3L PowerStroke
        this.vin = "1FTNW21F82EB74901";
        this.make = "Ford";
        this.model = "F-350 Super Duty";
        this.year = 2002;
        this.trim = "Lariat 4x4 Dually";
        this.architecture = EngineArchitecture.FORD_73L_POWERSTROKE;
        this.pcmStrategy = "DPC-422 / VDH5";
        this.protocol = "SAE J1850 PWM (Ford Standard)";
        this.connectionStatus = "● CONNECTED (OBDLink EX via USB-UART)";
        this.symbol = "🛻";
    }

    public VehicleProfile(String vin, String make, String model, int year, String trim,
                          EngineArchitecture architecture, String pcmStrategy, String protocol, String symbol) {
        this.vin = vin;
        this.make = make;
        this.model = model;
        this.year = year;
        this.trim = trim;
        this.architecture = architecture;
        this.pcmStrategy = pcmStrategy;
        this.protocol = protocol;
        this.connectionStatus = "● IDENTIFIED & CONNECTED";
        this.symbol = symbol != null ? symbol : architecture.defaultSymbol;
    }

    public boolean isDiesel() {
        return architecture == EngineArchitecture.FORD_73L_POWERSTROKE ||
               architecture == EngineArchitecture.FORD_60L_POWERSTROKE ||
               architecture == EngineArchitecture.FORD_67L_POWERSTROKE ||
               architecture == EngineArchitecture.GM_66L_DURAMAX ||
               architecture == EngineArchitecture.RAM_67L_CUMMINS;
    }

    public boolean hasHeui() {
        return architecture == EngineArchitecture.FORD_73L_POWERSTROKE ||
               architecture == EngineArchitecture.FORD_60L_POWERSTROKE;
    }

    public boolean hasCommonRail() {
        return architecture == EngineArchitecture.FORD_67L_POWERSTROKE ||
               architecture == EngineArchitecture.GM_66L_DURAMAX ||
               architecture == EngineArchitecture.RAM_67L_CUMMINS;
    }

    public boolean hasDpf() {
        return architecture == EngineArchitecture.FORD_67L_POWERSTROKE ||
               architecture == EngineArchitecture.GM_66L_DURAMAX ||
               architecture == EngineArchitecture.RAM_67L_CUMMINS;
    }

    public boolean hasFicm() {
        return architecture == EngineArchitecture.FORD_60L_POWERSTROKE;
    }

    public boolean hasVgt() {
        return architecture == EngineArchitecture.FORD_60L_POWERSTROKE ||
               architecture == EngineArchitecture.FORD_67L_POWERSTROKE ||
               architecture == EngineArchitecture.GM_66L_DURAMAX ||
               architecture == EngineArchitecture.RAM_67L_CUMMINS;
    }

    public String getFullVehicleTitle() {
        return year + " " + make + " " + model + " (" + architecture.displayName + ")";
    }

    /**
     * Determines whether a given PID command / name is strictly applicable
     * to this vehicle profile.
     */
    public boolean isPidApplicable(String command, String name) {
        if (command == null || name == null) return true;
        String n = name.toUpperCase(Locale.US);
        String c = command.toUpperCase(Locale.US);

        // Universal PIDs applicable to all vehicles:
        if (n.contains("RPM") || n.contains("VSS") || n.contains("SPEED") ||
            n.contains("COOLANT") || n.contains("ECT") || n.contains("BATTERY") ||
            n.contains("VPWR") || n.contains("VOLTAGE") || n.contains("RUN_TIME") ||
            n.contains("DTC") || n.contains("WARM_UPS") || n.contains("MIL") ||
            n.contains("APP") || n.contains("THROTTLE") || n.contains("LOAD") ||
            c.equals("010C") || c.equals("010D") || c.equals("0105") || c.equals("0101") ||
            c.equals("011F") || c.equals("0130") || c.equals("0104")) {
            return true;
        }

        // HEUI specific PIDs (7.3L / 6.0L):
        if (n.contains("ICP") || n.contains("IPR") || n.contains("HPOP") ||
            n.contains("INJ_PW") || n.contains("EOT_ECT_DIFF") || c.equals("221446") ||
            c.equals("221434") || c.equals("22C001") || c.equals("221410")) {
            return hasHeui();
        }

        // 6.0L FICM specific:
        if (n.contains("FICM") || n.contains("MAIN POWER") || c.contains("FICM")) {
            return hasFicm();
        }

        // Common Rail Specific:
        if (n.contains("RAIL PRESSURE") || n.contains("FRP") || n.contains("CP3") || n.contains("CP4")) {
            return hasCommonRail();
        }

        // DPF / DEF / Aftertreatment:
        if (n.contains("DPF") || n.contains("SOOT") || n.contains("DEF") || n.contains("SCR")) {
            return hasDpf();
        }

        // VGT Turbo:
        if (n.contains("VGT") || n.contains("VANE")) {
            return hasVgt();
        }

        // Glow Plugs:
        if (n.contains("GLOW PLUG") || n.contains("GP_DC") || n.contains("GPC") || c.equals("221430") || c.equals("221431")) {
            return isDiesel();
        }

        // Gasoline Specific: Spark Advance, O2 Sensors, Fuel Trims, Catalytic Converter
        if (n.contains("SPARK") || n.contains("STFT") || n.contains("LTFT") ||
            n.contains("O2_") || n.contains("O2S") || n.contains("CATALYST") ||
            n.contains("CAT_TEMP") || n.contains("EVAP") || c.equals("010E") ||
            c.equals("0106") || c.equals("0107") || c.equals("0114") || c.equals("013C")) {
            return !isDiesel();
        }

        return true;
    }
}
