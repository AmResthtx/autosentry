package com.autosentry.app.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionFlow {
    private static final int PERMISSION_REQ = 1001;
    private static final String[] REQUIRED_PERMS = {
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.POST_NOTIFICATIONS
    };

    public static boolean hasAllPermissions(MainActivity activity) {
        for (String perm : REQUIRED_PERMS) {
            if (ContextCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // adapter.isEnabled() is guarded by the runtime permission requests issued just above.
    @SuppressLint("MissingPermission")
    public static void requestAllPermissions(MainActivity activity) {
        for (String perm : REQUIRED_PERMS) {
            if (ContextCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{perm}, PERMISSION_REQ);
            }
        }
        // Check Bluetooth adapter enabled
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            Toast.makeText(activity, "Please pair your OBD adapter in Settings > Bluetooth", Toast.LENGTH_LONG).show();
        }
    }
}
