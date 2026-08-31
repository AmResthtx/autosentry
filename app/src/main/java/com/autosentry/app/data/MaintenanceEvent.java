package com.autosentry.app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "maintenance_events")
public class MaintenanceEvent {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title; // e.g. "Engine Oil & Filter (Motorcraft FL-1995)" or "KC300x Stage 1 Turbo Upgrade"
    public String category; // "ENGINE", "FUEL", "TRANSMISSION", "COOLING", "DIFFERENTIAL", "BRAKES", "EXHAUST", "SUSPENSION", "TUNING", "ELECTRICAL"
    public String eventType; // "OIL_CHANGE", "FUEL_FILTER", "TRANS_FLUID", "UPGRADE_TURBO", "UPGRADE_TUNER", etc.
    public boolean isUpgrade; // true if aftermarket upgrade / capital improvement, false for routine maintenance
    public long eventTime; // epoch ms (date of service or installation)
    public long lastServiceAt; // epoch ms
    public long nextServiceAt; // epoch ms
    public int intervalKm; // interval in miles (0 if one-time upgrade)
    public int intervalMonths; // interval in months (0 if one-time upgrade)
    public int currentMileage; // odometer reading at service/install
    public int odometerAtLastService; // odometer reading at last service
    public int nextServiceMileage; // next due odometer
    public double cost; // dollar amount spent on parts/labor
    public double estimatedValueAdded; // estimated residual resale / depreciation protection value
    public String brandOrPartNumber; // e.g. "KC Turbos", "PHP Hydra", "Motorcraft FD-4596"
    public String warrantyInfo; // e.g. "Lifetime Limited", "3 Year / 36k Mi", "1 Year Manufacturer"
    public String serviceShop; // e.g. "Ford Dealership", "Self / DIY", "Diesel Performance Shop"
    public String receiptImagePath; // local file path to uploaded photo / receipt
    public String notes; // parts used, fluid specs, dyno numbers, install notes
    public int serviceCount; // number of times reset/serviced
}
