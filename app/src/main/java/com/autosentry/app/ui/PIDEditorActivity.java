package com.autosentry.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.autosentry.app.R;
import com.autosentry.app.data.AppDatabase;
import com.autosentry.app.data.PIDDefinition;

import java.util.ArrayList;
import java.util.List;

public class PIDEditorActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<PIDDefinition> defs = new ArrayList<>();

    private EditText nameEdit;
    private EditText commandEdit;
    private Button addBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pid_editor);

        listView = findViewById(R.id.pid_list_view);
        nameEdit = findViewById(R.id.edit_name);
        commandEdit = findViewById(R.id.edit_command);
        addBtn = findViewById(R.id.btn_add_pid);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);

        loadDefs();

        addBtn.setOnClickListener(v -> {
            String name = nameEdit.getText().toString().trim();
            String cmd = commandEdit.getText().toString().trim();
            if (!name.isEmpty() && !cmd.isEmpty()) {
                PIDDefinition d = new PIDDefinition();
                d.name = name;
                d.command = cmd;
                d.pollIntervalSeconds = 15; // default
                new Thread(() -> {
                    long id = AppDatabase.getInstance(getApplicationContext()).pidDefinitionDao().insert(d);
                    d.id = id;
                    runOnUiThread(() -> {
                        defs.add(d);
                        adapter.add(d.name + " → " + d.command);
                        adapter.notifyDataSetChanged();
                        nameEdit.setText("");
                        commandEdit.setText("");
                    });
                }).start();
            }
        });
    }

    private void loadDefs() {
        new Thread(() -> {
            defs = AppDatabase.getInstance(getApplicationContext()).pidDefinitionDao().all();
            runOnUiThread(() -> {
                adapter.clear();
                for (PIDDefinition d : defs) adapter.add(d.name + " → " + d.command);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}
