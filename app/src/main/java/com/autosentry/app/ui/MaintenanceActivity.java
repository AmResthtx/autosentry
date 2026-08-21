package com.autosentry.app.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.autosentry.app.R;
import com.autosentry.app.data.AppDatabase;
import com.autosentry.app.data.MaintenanceEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MaintenanceActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<MaintenanceEvent> events = new ArrayList<>();
    private Button addBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        listView = findViewById(R.id.maintenance_list_view);
        addBtn = findViewById(R.id.btn_add_maintenance);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);

        loadEvents();

        addBtn.setOnClickListener(v -> showAddDialog());
    }

    private void loadEvents() {
        new Thread(() -> {
            events = AppDatabase.getInstance(getApplicationContext()).maintenanceDao().dueSoon();
            List<String> items = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            for (MaintenanceEvent e : events) {
                String next = e.nextServiceAt > 0 ? sdf.format(new Date(e.nextServiceAt)) : "Not set";
                items.add(e.title + " — Next: " + next);
            }
            runOnUiThread(() -> {
                adapter.clear();
                adapter.addAll(items);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_maintenance, null);
        EditText titleEdit = dialogView.findViewById(R.id.edit_maint_title);
        EditText intervalKmEdit = dialogView.findViewById(R.id.edit_interval_km);
        EditText intervalMonthsEdit = dialogView.findViewById(R.id.edit_interval_months);
        EditText notesEdit = dialogView.findViewById(R.id.edit_maint_notes);

        new AlertDialog.Builder(this)
                .setTitle("Add Maintenance Item")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = titleEdit.getText().toString().trim();
                    if (title.isEmpty()) return;

                    MaintenanceEvent evt = new MaintenanceEvent();
                    evt.title = title;
                    evt.lastServiceAt = System.currentTimeMillis();
                    evt.notes = notesEdit.getText().toString().trim();

                    String kmStr = intervalKmEdit.getText().toString().trim();
                    evt.intervalKm = kmStr.isEmpty() ? 0 : Integer.parseInt(kmStr);

                    String moStr = intervalMonthsEdit.getText().toString().trim();
                    evt.intervalMonths = moStr.isEmpty() ? 0 : Integer.parseInt(moStr);

                    if (evt.intervalMonths > 0) {
                        evt.nextServiceAt = evt.lastServiceAt + (long) evt.intervalMonths * 30L * 24L * 3600L * 1000L;
                    }

                    new Thread(() -> {
                        AppDatabase.getInstance(getApplicationContext()).maintenanceDao().insert(evt);
                        runOnUiThread(this::loadEvents);
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
