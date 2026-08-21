package com.autosentry.app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sessions")
public class Session {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long startTime;
    public long endTime;
    public String adapterType;
    public String vehicleVin;
    public String notes;
}
