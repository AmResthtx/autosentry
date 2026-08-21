package com.autosentry.app.util;

import java.util.List;

/**
 * Detects RPM instability using a sliding window variance algorithm.
 * If the max swing (max - min) exceeds swingThreshold AND the sample
 * count meets minSamples, the RPM is considered unstable.
 */
public class RPMDetector {

    /**
     * @param samples       recent RPM readings (oldest to newest)
     * @param swingThreshold  max allowed difference between highest and lowest RPM in window
     * @param minSamples    minimum number of samples required before detection triggers
     * @return true if RPM instability is detected
     */
    public static boolean isRPMUnstable(List<Integer> samples, int swingThreshold, int minSamples) {
        if (samples == null || samples.size() < minSamples) return false;

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int v : samples) {
            if (v < min) min = v;
            if (v > max) max = v;
        }
        return (max - min) >= swingThreshold;
    }

    /**
     * Computes the variance of the sample set.
     */
    public static double variance(List<Integer> samples) {
        if (samples == null || samples.isEmpty()) return 0;
        double mean = 0;
        for (int v : samples) mean += v;
        mean /= samples.size();
        double sumSq = 0;
        for (int v : samples) sumSq += (v - mean) * (v - mean);
        return sumSq / samples.size();
    }
}
