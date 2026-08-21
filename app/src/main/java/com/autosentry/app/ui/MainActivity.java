package com.autosentry.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.autosentry.app.R;
import com.autosentry.app.data.AppDatabase;
import com.autosentry.app.data.PIDRecord;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView rpmView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rpmView = findViewById(R.id.rpm_value);

        Button alertsBtn = findViewById(R.id.button_alerts);
        alertsBtn.setOnClickListener(v ->
                startActivity(new Intent(this, AlertsActivity.class)));

        Button maintBtn = findViewById(R.id.button_maintenance);
        maintBtn.setOnClickListener(v ->
                startActivity(new Intent(this, MaintenanceActivity.class)));

        Button pidBtn = findViewById(R.id.button_pid_editor);
        pidBtn.setOnClickListener(v ->
                startActivity(new Intent(this, PIDEditorActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLatestRPM();
    }

    private void refreshLatestRPM() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<PIDRecord> latest = db.pidRecordDao().latest(1);
            runOnUiThread(() -> {
                if (latest != null && !latest.isEmpty()) {
                    rpmView.setText("RPM: " + latest.get(0).pidValue);
                } else {
                    rpmView.setText("RPM: --");
                }
            });
        }).start();
    }
}
