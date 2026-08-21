package com.autosentry.app.ui;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.autosentry.app.R;
import com.autosentry.app.data.AppDatabase;
import com.autosentry.app.data.AgentLog;

import java.util.ArrayList;
import java.util.List;

public class AlertsActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);

        listView = findViewById(R.id.alerts_list_view);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);

        loadAlerts();
    }

    private void loadAlerts() {
        new Thread(() -> {
            List<AgentLog> logs = AppDatabase.getInstance(getApplicationContext()).agentLogDao().latest(100);
            List<String> items = new ArrayList<>();
            for (AgentLog l : logs) {
                items.add(l.type + ": " + l.message);
            }
            runOnUiThread(() -> {
                adapter.clear();
                adapter.addAll(items);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}
