package com.autosentry.app.util;

import java.util.List;

// RPM instability detector: given a sample window, detect oscillations
public class RPMDetector {
    // Detects instability: returns true if there is sustained oscillation
    // Algorithm: compute max-min over window and variance; if max-min exceeds swingThreshold
    // and variance over threshold for at least minSamples, flag unstable.
    public static boolean isRPMUnstable(List<Integer> samples, int swingThreshold, int minSamples) {
        if (samples == null || samples.size() < minSamples) return false;
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        double sum = 0;
        for (int v : samples) {
            max = Math.max(max, v);
            min = Math.min(min, v);
            sum += v;
        }
        double mean = sum / samples.size();
        double var = 0;
        for (int v : samples) var += (v - mean) * (v - mean);
        var /= samples.size();

        int swing = max - min;
        // criteria: swing large enough and variance reasonably high
        return swing >= swingThreshold && var > 100.0;
    }
}
