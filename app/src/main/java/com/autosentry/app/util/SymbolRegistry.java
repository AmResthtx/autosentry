package com.autosentry.app.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Global Symbol & Icon Registry.
 * Ensures EVERY parameter, PID, gauge, vehicle profile, sensor, maintenance item,
 * and diagnostic test has an intuitive, crisp visual symbol.
 * If something doesn't have a pre-assigned symbol, assigns one dynamically based on
 * units, keywords, or intelligent categorized fallbacks.
 */
public class SymbolRegistry {

    private static final Map<String, String> EXPLICIT_SYMBOLS = new HashMap<>();

    static {
        // Powertrain & Engine Core
        EXPLICIT_SYMBOLS.put("rpm", "⏱️");
        EXPLICIT_SYMBOLS.put("engine revolutions per minute", "⏱️");
        EXPLICIT_SYMBOLS.put("vss", "🚗");
        EXPLICIT_SYMBOLS.put("speed", "🚗");
        EXPLICIT_SYMBOLS.put("vehicle speed sensor", "🚗");
        EXPLICIT_SYMBOLS.put("icp", "⚡");
        EXPLICIT_SYMBOLS.put("injector control pressure", "⚡");
        EXPLICIT_SYMBOLS.put("ipr", "⚙️");
        EXPLICIT_SYMBOLS.put("ipr_dc", "⚙️");
        EXPLICIT_SYMBOLS.put("injector pressure regulator", "⚙️");
        EXPLICIT_SYMBOLS.put("eot", "🛢️");
        EXPLICIT_SYMBOLS.put("engine oil temperature", "🛢️");
        EXPLICIT_SYMBOLS.put("ect", "🌡️");
        EXPLICIT_SYMBOLS.put("engine coolant temperature", "🌡️");
        EXPLICIT_SYMBOLS.put("coolant", "🌡️");
        EXPLICIT_SYMBOLS.put("bst_psi", "🌀");
        EXPLICIT_SYMBOLS.put("boost", "🌀");
        EXPLICIT_SYMBOLS.put("turbocharger boost pressure", "🌀");
        EXPLICIT_SYMBOLS.put("map", "💨");
        EXPLICIT_SYMBOLS.put("manifold absolute pressure", "💨");
        EXPLICIT_SYMBOLS.put("baro", "🧭");
        EXPLICIT_SYMBOLS.put("barometric pressure sensor", "🧭");
        EXPLICIT_SYMBOLS.put("tft", "⚙️");
        EXPLICIT_SYMBOLS.put("transmission fluid temperature", "⚙️");
        EXPLICIT_SYMBOLS.put("trans temp", "⚙️");
        EXPLICIT_SYMBOLS.put("ebp", "🔥");
        EXPLICIT_SYMBOLS.put("exhaust back pressure", "🔥");
        EXPLICIT_SYMBOLS.put("egt", "🔥");
        EXPLICIT_SYMBOLS.put("exhaust gas temperature", "🔥");
        EXPLICIT_SYMBOLS.put("app", "🦶");
        EXPLICIT_SYMBOLS.put("accelerator pedal position", "🦶");
        EXPLICIT_SYMBOLS.put("pedal", "🦶");
        EXPLICIT_SYMBOLS.put("tp", "🎚️");
        EXPLICIT_SYMBOLS.put("throttle position sensor", "🎚️");
        EXPLICIT_SYMBOLS.put("throttle", "🎚️");
        EXPLICIT_SYMBOLS.put("fli", "⛽");
        EXPLICIT_SYMBOLS.put("fuel level indicator", "⛽");
        EXPLICIT_SYMBOLS.put("fp", "⛽");
        EXPLICIT_SYMBOLS.put("fuel pressure", "⛽");
        EXPLICIT_SYMBOLS.put("frp", "⛽");
        EXPLICIT_SYMBOLS.put("fuel rail pressure", "⛽");
        EXPLICIT_SYMBOLS.put("vpwr", "🔋");
        EXPLICIT_SYMBOLS.put("battery", "🔋");
        EXPLICIT_SYMBOLS.put("voltage", "🔋");
        EXPLICIT_SYMBOLS.put("alternator", "🔋");
        EXPLICIT_SYMBOLS.put("iat", "❄️");
        EXPLICIT_SYMBOLS.put("intake air temperature", "❄️");
        EXPLICIT_SYMBOLS.put("iat2", "❄️");
        EXPLICIT_SYMBOLS.put("maf", "💨");
        EXPLICIT_SYMBOLS.put("mass air flow sensor", "💨");
        EXPLICIT_SYMBOLS.put("inj_pw", "⏱️");
        EXPLICIT_SYMBOLS.put("fuel injector pulse width", "⏱️");
        EXPLICIT_SYMBOLS.put("cmp", "📡");
        EXPLICIT_SYMBOLS.put("camshaft position sensor sync", "📡");
        EXPLICIT_SYMBOLS.put("ckp", "📡");
        EXPLICIT_SYMBOLS.put("crankshaft position sensor sync", "📡");
        EXPLICIT_SYMBOLS.put("fip", "⏱️");
        EXPLICIT_SYMBOLS.put("spark", "⚡");
        EXPLICIT_SYMBOLS.put("spark advance", "⚡");
        EXPLICIT_SYMBOLS.put("stft", "⚖️");
        EXPLICIT_SYMBOLS.put("short term fuel trim", "⚖️");
        EXPLICIT_SYMBOLS.put("ltft", "⚖️");
        EXPLICIT_SYMBOLS.put("long term fuel trim", "⚖️");
        EXPLICIT_SYMBOLS.put("o2", "🧪");
        EXPLICIT_SYMBOLS.put("oxygen sensor", "🧪");
        EXPLICIT_SYMBOLS.put("catalyst", "🔥");
        EXPLICIT_SYMBOLS.put("cat temp", "🔥");
        EXPLICIT_SYMBOLS.put("dpf", "🌫️");
        EXPLICIT_SYMBOLS.put("dpf soot load", "🌫️");
        EXPLICIT_SYMBOLS.put("def", "💧");
        EXPLICIT_SYMBOLS.put("def fluid level", "💧");
        EXPLICIT_SYMBOLS.put("vgt", "🌀");
        EXPLICIT_SYMBOLS.put("vgt duty cycle", "🌀");
        EXPLICIT_SYMBOLS.put("ficm", "⚡");
        EXPLICIT_SYMBOLS.put("ficm main power", "⚡");
        EXPLICIT_SYMBOLS.put("egr_dc", "♻️");
        EXPLICIT_SYMBOLS.put("egr", "♻️");
        EXPLICIT_SYMBOLS.put("exhaust gas recirculation", "♻️");
        EXPLICIT_SYMBOLS.put("tc_slip", "⚙️");
        EXPLICIT_SYMBOLS.put("tcc", "🔒");
        EXPLICIT_SYMBOLS.put("gear", "🔢");
        EXPLICIT_SYMBOLS.put("tr", "🕹️");
        EXPLICIT_SYMBOLS.put("eot_ect_diff", "📐");
        EXPLICIT_SYMBOLS.put("oil cooler delta", "📐");
        EXPLICIT_SYMBOLS.put("wg_dc", "🌀");
        EXPLICIT_SYMBOLS.put("gp_dc", "🔌");
        EXPLICIT_SYMBOLS.put("glow plug", "🔌");
        EXPLICIT_SYMBOLS.put("gpc", "🔌");
        EXPLICIT_SYMBOLS.put("ac_clutch", "❄️");
        EXPLICIT_SYMBOLS.put("brake_sw", "🛑");
        EXPLICIT_SYMBOLS.put("cruise_sw", "🚀");
        EXPLICIT_SYMBOLS.put("load_pct", "📊");
        EXPLICIT_SYMBOLS.put("dtc_count", "⚠️");
        EXPLICIT_SYMBOLS.put("mil_dist", "🛣️");
        EXPLICIT_SYMBOLS.put("warm_ups", "🔥");
        EXPLICIT_SYMBOLS.put("run_time", "⏳");

        // Vehicles & Brands
        EXPLICIT_SYMBOLS.put("ford", "🛻");
        EXPLICIT_SYMBOLS.put("powerstroke", "🛻");
        EXPLICIT_SYMBOLS.put("chevy", "🚚");
        EXPLICIT_SYMBOLS.put("chevrolet", "🚚");
        EXPLICIT_SYMBOLS.put("duramax", "🚚");
        EXPLICIT_SYMBOLS.put("gmc", "🚚");
        EXPLICIT_SYMBOLS.put("ram", "🐏");
        EXPLICIT_SYMBOLS.put("cummins", "🐏");
        EXPLICIT_SYMBOLS.put("dodge", "🐏");
        EXPLICIT_SYMBOLS.put("toyota", "🚙");
        EXPLICIT_SYMBOLS.put("generic", "🚗");
        EXPLICIT_SYMBOLS.put("gasoline", "⛽");
        EXPLICIT_SYMBOLS.put("diesel", "🛢️");

        // Maintenance & Part Categories
        EXPLICIT_SYMBOLS.put("oil filter", "🛢️");
        EXPLICIT_SYMBOLS.put("fuel filter", "⛽");
        EXPLICIT_SYMBOLS.put("air filter", "💨");
        EXPLICIT_SYMBOLS.put("transmission", "⚙️");
        EXPLICIT_SYMBOLS.put("differential", "🔩");
        EXPLICIT_SYMBOLS.put("brakes", "🛑");
        EXPLICIT_SYMBOLS.put("glow plugs", "🔌");
        EXPLICIT_SYMBOLS.put("spark plugs", "⚡");
        EXPLICIT_SYMBOLS.put("turbo", "🌀");
        EXPLICIT_SYMBOLS.put("injector", "💉");
        EXPLICIT_SYMBOLS.put("hpop", "⚡");
        EXPLICIT_SYMBOLS.put("coolant flush", "🧪");
    }

    /**
     * Retrieves the visual symbol for a given parameter, PID, or component name.
     * If the exact name is not mapped, dynamically inspects tokens, units, or context
     * to assign an appropriate symbol so NOTHING is ever without a symbol.
     */
    public static String getSymbol(String name) {
        return getSymbol(name, "");
    }

    /**
     * Resolves a visual symbol for a name and optional unit/command.
     */
    public static String getSymbol(String name, String unitOrExtra) {
        if (name == null || name.trim().isEmpty()) {
            return "🔹";
        }

        String lower = name.trim().toLowerCase(Locale.US);

        // 1. Direct match in dictionary
        if (EXPLICIT_SYMBOLS.containsKey(lower)) {
            return EXPLICIT_SYMBOLS.get(lower);
        }

        // 2. Prefix / Substring matching
        for (Map.Entry<String, String> entry : EXPLICIT_SYMBOLS.entrySet()) {
            if (lower.contains(entry.getKey()) || entry.getKey().contains(lower)) {
                return entry.getValue();
            }
        }

        // 3. Inspect by Units & Measurements
        String combined = (lower + " " + (unitOrExtra != null ? unitOrExtra.toLowerCase(Locale.US) : "")).trim();
        if (combined.contains("°f") || combined.contains("°c") || combined.contains("deg") || combined.contains("temp") || combined.contains("cool")) {
            return "🌡️";
        }
        if (combined.contains("psi") || combined.contains("kpa") || combined.contains("bar") || combined.contains("press")) {
            return "⚡";
        }
        if (combined.contains("%") || combined.contains("duty") || combined.contains("ratio") || combined.contains("pct") || combined.contains("wear")) {
            return "⚙️";
        }
        if (combined.contains("rpm") || combined.contains("speed") || combined.contains("mph") || combined.contains("kph")) {
            return "⏱️";
        }
        if (combined.contains("volt") || combined.contains(" v ") || combined.endsWith("v") || combined.contains("amp") || combined.contains("batt")) {
            return "🔋";
        }
        if (combined.contains("ms") || combined.contains("sec") || combined.contains("time") || combined.contains("interval")) {
            return "⏳";
        }
        if (combined.contains("hz") || combined.contains("freq") || combined.contains("wave")) {
            return "〰️";
        }
        if (combined.contains("oil") || combined.contains("lube") || combined.contains("fluid")) {
            return "🛢️";
        }
        if (combined.contains("fuel") || combined.contains("gas") || combined.contains("diesel")) {
            return "⛽";
        }
        if (combined.contains("air") || combined.contains("flow") || combined.contains("boost") || combined.contains("exhaust")) {
            return "💨";
        }
        if (combined.contains("brake") || combined.contains("stop") || combined.contains("lock")) {
            return "🛑";
        }
        if (combined.contains("sync") || combined.contains("sensor") || combined.contains("signal") || combined.contains("obd")) {
            return "📡";
        }
        if (combined.contains("mod") || combined.contains("tune") || combined.contains("upgrade") || combined.contains("turbo")) {
            return "★";
        }
        if (combined.contains("trip") || combined.contains("log") || combined.contains("file")) {
            return "📁";
        }
        if (combined.contains("alert") || combined.contains("warn") || combined.contains("fault") || combined.contains("dtc")) {
            return "⚠️";
        }

        // 4. Deterministic Categorical Fallback based on hash
        int hash = Math.abs(name.hashCode());
        String[] fallbackGlyphs = new String[]{"🔹", "⚙️", "📊", "🏷️", "📌", "⚡", "🔧", "🛡️"};
        return fallbackGlyphs[hash % fallbackGlyphs.length];
    }
}
