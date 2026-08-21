package com.autosentry.app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "agent_logs")
public class AgentLog {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long timestamp;
    public String type; // e.g., "RPM_INSTABILITY", "MAINTENANCE_DUE"
    public String message;
    public String metadata; // JSON or freeform
}
