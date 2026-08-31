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

    // Rich Trip Telemetry & Tracking Fields
    public int startMileage;
    public int endMileage;
    public double distanceMiles;
    public int avgRpm;
    public int maxRpm;
    public double avgSpeedMph;
    public int maxSpeedMph;
    public double avgMpg;
    public double fuelConsumedGal;
    public double maxBoostPsi;
    public int maxEotF;
    public int maxTransTempF;
    public int avgIcpPsi;
    public double maxIprPct;
    public String tripPurpose; // "Heavy Towing", "Highway Haul", "City Delivery", "Off-Road"
    public int healthAlertsCount;
    public String startLocation;
    public String endLocation;
}
