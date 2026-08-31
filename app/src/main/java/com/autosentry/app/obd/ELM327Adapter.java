package com.autosentry.app.obd;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Real ELM327 / OBDLink Bluetooth adapter layer.
 * Replaces OBDSimulator for production use.
 */
public class ELM327Adapter {
    private static final String TAG = "ELM327Adapter";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket socket = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private String adapterAddress = null;
    private boolean connected = false;

    public ELM327Adapter() {}

    // Bluetooth permissions (BLUETOOTH_CONNECT/SCAN) are declared in the manifest and
    // requested at runtime by PermissionFlow before any adapter connection is attempted.
    @SuppressLint("MissingPermission")
    public boolean connect() throws IOException, InterruptedException {
        if (connected) disconnect();
        this.adapterAddress = adapterAddress;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            throw new IOException("Bluetooth adapter not available or disabled");
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(adapterAddress);
        socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
        socket.connect();

        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        connected = true;

        // Initialize adapter: reset, echo off, headers off, spaces off, linefeeds off
        sendCommand("AT Z");
        readResponse(); // reset response
        Thread.sleep(100);
        sendCommand("AT E0");
        sendCommand("AT L0");
        sendCommand("AT S0");
        sendCommand("AT H0");
        sendCommand("AT CAF0");
        sendCommand("AT CFC0");
        sendCommand("AT CRA 7E8");
        sendCommand("AT CSM 1");
        sendCommand("AT SH 7E0");
        sendCommand("AT SP 6");
        sendCommand("AT DP");

        return true;
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Disconnect error", e);
        }
        connected = false;
        socket = null;
        inputStream = null;
        outputStream = null;
    }

    public boolean isConnected() {
        return connected && socket != null && socket.isConnected();
    }

    public int readRPM() throws IOException {
        sendCommand("01 0C");
        String response = readResponse();
        return parseRPMResponse(response);
    }

    public int readCoolantTemp() throws IOException {
        sendCommand("01 05");
        String response = readResponse();
        return parseTempResponse(response);
    }

    public int readICPFahrenheit() throws IOException {
        // Enhanced PID for 7.3L Powerstroke: ICP in MPa, then converted
        sendCommand("22 11 93");
        String response = readResponse();
        float mpa = parseFloatResponse(response);
        // Conversion approximation: MPa to PSI roughly, but keep in native unit
        // For display we'll handle conversion in MainActivity / dashboard
        int value = (int)(mpa * 100); // store scaled value
        return value;
    }

    public void sendCommand(String cmd) throws IOException {
        if (outputStream == null) throw new IOException("Not connected to adapter");
        String fullCmd = cmd + "\r";
        outputStream.write(fullCmd.getBytes());
        outputStream.flush();
    }

    public String readResponse() throws IOException {
        if (inputStream == null) throw new IOException("Not connected");
        StringBuilder response = new StringBuilder();
        byte[] buffer = new byte[1024];
        int read = inputStream.read(buffer);
        while (read > 0) {
            response.append(new String(buffer, 0, read));
            // Break when we see prompt (>)
            if (response.toString().contains(">")) {
                break;
            }
            read = inputStream.read(buffer);
        }
        return response.toString();
    }

    private int parseRPMResponse(String response) {
        // Response format: "41 0C XX XX >"
        // Formula: ((A*256)+B)/4
        try {
            String clean = response.replaceAll(">", "").trim();
            String[] lines = clean.split("\\r?\\n");
            for (String line : lines) {
                if (line.contains("41 0C")) {
                    String[] parts = line.trim().split("\\s+");
                    // Find bytes after "41 0C"
                    boolean foundHeader = false;
                    int aVal = 0, bVal = 0;
                    for (String part : parts) {
                        if (part.equals("41")) { foundHeader = true; continue; }
                        if (foundHeader && part.equals("0C")) { continue; }
                        if (foundHeader && aVal == 0 && !part.equals("0C")) {
                            aVal = Integer.parseInt(part, 16);
                        } else if (foundHeader && aVal > 0 && bVal == 0 && !part.equals("0C")) {
                            bVal = Integer.parseInt(part, 16);
                        }
                    }
                    return ((aVal * 256) + bVal) / 4;
                }
            }
            // Default: try to extract any hex pair
            String hexPart = clean.replaceAll("[^0-9A-Fa-f\\s]", "").trim();
            String[] hexVals = hexPart.split("\\s+");
            if (hexVals.length >= 2) {
                int a = Integer.parseInt(hexVals[0], 16);
                int b = Integer.parseInt(hexVals[1], 16);
                return ((a * 256) + b) / 4;
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse RPM error", e);
        }
        return 0; // Fallback
    }

    private int parseTempResponse(String response) {
        // Formula: A - 40 (Celsius); converted to F in dashboard layer
        try {
            String hexPart = response.replaceAll("[^0-9A-Fa-f\\s]", "").trim();
            String[] hexVals = hexPart.split("\\s+");
            if (hexVals.length >= 1) {
                int a = Integer.parseInt(hexVals[0], 16);
                return a - 40; // Celsius value returned; MainActivity converts to F
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse temp error", e);
        }
        return -1;
    }

    private float parseFloatResponse(String response) {
        try {
            String hexPart = response.replaceAll("[^0-9A-Fa-f\\s]", "").trim();
            String[] hexVals = hexPart.split("\\s+");
            if (hexVals.length >= 2) {
                int a = Integer.parseInt(hexVals[0], 16);
                int b = Integer.parseInt(hexVals[1], 16);
                return ((a * 256) + b) * 0.001f;
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse float error", e);
        }
        return 0f;
    }
}
