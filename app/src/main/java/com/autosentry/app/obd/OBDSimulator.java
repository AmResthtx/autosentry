package com.autosentry.app.obd;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

// Simple OBD simulator that produces RPM and a few other PIDs
public class OBDSimulator {
    private final Random rand = new Random();
    private final AtomicInteger rpm = new AtomicInteger(800);

    public OBDSimulator() {
    }

    // Simulate reading RPM with some noise and occasional instability
    public int readRPM() {
        int base = rpm.get();
        // small random walk
        int delta = rand.nextInt(11) - 5;
        int next = Math.max(0, base + delta);
        // occasionally inject oscillation
        if (rand.nextDouble() < 0.02) {
            next += (rand.nextBoolean() ? 200 : -200);
        }
        rpm.set(next);
        return next;
    }

    // Simulate other PIDs (e.g., speed)
    public int readSpeed() {
        return Math.max(0, rand.nextInt(120));
    }
}
