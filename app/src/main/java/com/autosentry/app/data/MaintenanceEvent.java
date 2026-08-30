package com.autosentry.app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "maintenance_events")
public class MaintenanceEvent {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title; // e.g., "Oil change"
    public String eventType; // e.g., "OIL_CHANGE"
    public long eventTime; // epoch ms (date of service)
    public long lastServiceAt; // epoch ms (last service time, same as eventTime)
    public long nextServiceAt; // epoch ms (optional)
    public int intervalKm; // 0 if not distance-based
    public int intervalMonths; // 0 if not time-based
    public int odometerAtLastService; // odometer reading at last service (simulated)
    public String notes;
}
