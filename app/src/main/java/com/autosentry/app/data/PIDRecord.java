package com.autosentry.app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pid_records")
public class PIDRecord {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String pidName;
    public int pidValue;
    public long timestamp;
}
