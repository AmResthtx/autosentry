package com.autosentry.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.autosentry.app.R;
import com.autosentry.app.data.AppDatabase;
import com.autosentry.app.data.PIDRecord;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView rpmView;
    private Button pidEditorBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rpmView = findViewById(R.id.textView);
        pidEditorBtn = findViewById(R.id.button_pid_editor);

        pidEditorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PIDEditorActivity.class));
            }
        });

        refreshLatestRPM();
    }

    private void refreshLatestRPM() {
        // Load latest RPM sample from DB (performed on background thread in real code; simplified here)
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
