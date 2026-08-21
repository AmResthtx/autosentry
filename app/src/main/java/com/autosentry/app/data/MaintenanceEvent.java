package com.autosentry.app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "maintenance_events")
public class MaintenanceEvent {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title; // e.g., "Oil change"
    public long lastServiceAt; // epoch ms (date of last service)
    public long nextServiceAt; // epoch ms (optional)
    public int intervalKm; // 0 if not distance-based
    public int intervalMonths; // 0 if not time-based
    public int odometerAtLastService; // odometer reading at last service (simulated)
    public String notes;
}
