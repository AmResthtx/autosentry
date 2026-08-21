package com.autosentry.app;

import com.autosentry.app.util.RPMDetector;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

public class RPMDetectorTest {
    @Test
    public void testStableRPM() {
        List<Integer> samples = Arrays.asList(800, 802, 798, 801, 799, 800, 800);
        assertFalse(RPMDetector.isRPMUnstable(samples, 50, 5));
    }

    @Test
    public void testUnstableRPM() {
        List<Integer> samples = Arrays.asList(800, 1000, 760, 990, 740, 1010, 770);
        assertTrue(RPMDetector.isRPMUnstable(samples, 150, 5));
    }
}
