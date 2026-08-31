package com.autosentry.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.autosentry.app.R;
import com.autosentry.app.data.AgentLog;
import com.autosentry.app.data.AppDatabase;
import com.autosentry.app.data.MaintenanceEvent;
import com.autosentry.app.notifications.NotificationUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MaintenanceActivity extends AppCompatActivity {
    private static final int REQUEST_PICK_RECEIPT = 101;
    private static final int CURRENT_TRUCK_MILEAGE = 164250;
    private static final double BASE_TRUCK_MARKET_VALUE = 17500.0;

    private ListView listView;
    private EditText editSearch;
    private TextView carfaxTruckInfo;
    private TextView carfaxHealthScore;
    private TextView valBaseMarket;
    private TextView valUpgradesAdded;
    private TextView valResaleAppraised;
    private Button btnAddCustom;
    private Button btnReloadOem;
    private Button btnExportReport;
    private Button tabAll;
    private Button tabOem;
    private Button tabUpgrades;

    private int activeFilterTab = 0; // 0: All, 1: OEM only, 2: Upgrades only

    private final List<MaintenanceEvent> masterList = new ArrayList<>();
    private final List<MaintenanceEvent> displayedList = new ArrayList<>();
    private MaintenanceAdapter adapter;

    // Temporary storage for pending receipt attachment
    private ImageView pendingDialogImageView = null;
    private TextView pendingDialogStatusView = null;
    private String lastSelectedPhotoPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        listView = findViewById(R.id.maintenance_list_view);
        editSearch = findViewById(R.id.edit_search_maint);
        carfaxTruckInfo = findViewById(R.id.carfax_truck_info);
        carfaxHealthScore = findViewById(R.id.carfax_health_score);
        valBaseMarket = findViewById(R.id.val_base_market);
        valUpgradesAdded = findViewById(R.id.val_upgrades_added);
        valResaleAppraised = findViewById(R.id.val_resale_appraised);

        btnAddCustom = findViewById(R.id.btn_add_custom_service);
        btnReloadOem = findViewById(R.id.btn_restore_oem_intervals);
        btnExportReport = findViewById(R.id.btn_export_history);

        tabAll = findViewById(R.id.tab_filter_all);
        tabOem = findViewById(R.id.tab_filter_oem);
        tabUpgrades = findViewById(R.id.tab_filter_upgrades);

        adapter = new MaintenanceAdapter();
        listView.setAdapter(adapter);

        btnAddCustom.setOnClickListener(v -> showAddCustomServiceDialog());
        btnReloadOem.setOnClickListener(v -> reloadOEMIntervalsAndUpgrades(true));
        btnExportReport.setOnClickListener(v -> exportCarfaxReport());

        tabAll.setOnClickListener(v -> {
            activeFilterTab = 0;
            updateTabStyles();
            filterList(editSearch.getText() != null ? editSearch.getText().toString() : "");
        });

        tabOem.setOnClickListener(v -> {
            activeFilterTab = 1;
            updateTabStyles();
            filterList(editSearch.getText() != null ? editSearch.getText().toString() : "");
        });

        tabUpgrades.setOnClickListener(v -> {
            activeFilterTab = 2;
            updateTabStyles();
            filterList(editSearch.getText() != null ? editSearch.getText().toString() : "");
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s != null ? s.toString() : "");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadMaintenanceData();
    }

    private void updateTabStyles() {
        tabAll.setBackgroundColor(activeFilterTab == 0 ? 0xFF0284C7 : 0xFF334155);
        tabOem.setBackgroundColor(activeFilterTab == 1 ? 0xFF0284C7 : 0xFF334155);
        tabUpgrades.setBackgroundColor(activeFilterTab == 2 ? 0xFF0284C7 : 0xFF334155);
    }

    private void loadMaintenanceData() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<MaintenanceEvent> current = db.maintenanceDao().getAll();
            if (current.isEmpty()) {
                reloadOEMIntervalsAndUpgrades(false);
            } else {
                updateList(current);
            }
        }).start();
    }

    private void updateList(List<MaintenanceEvent> items) {
        runOnUiThread(() -> {
            masterList.clear();
            masterList.addAll(items);

            int totalRecords = masterList.size();
            int oemCount = 0;
            int upgradeCount = 0;
            int servicedCount = 0;
            double totalInvested = 0.0;
            double totalUpgradesAddedVal = 0.0;
            double totalMaintProtectionVal = 0.0;

            for (MaintenanceEvent m : masterList) {
                totalInvested += m.cost;
                if (m.isUpgrade) {
                    upgradeCount++;
                    totalUpgradesAddedVal += (m.estimatedValueAdded > 0 ? m.estimatedValueAdded : (m.cost * 0.75));
                } else {
                    oemCount++;
                    if (m.serviceCount > 0) {
                        servicedCount++;
                        totalMaintProtectionVal += 200.0; // Maintenance proof retains approx $200 per documented routine item
                    }
                }
            }

            tabAll.setText(String.format(Locale.US, "All (%d)", totalRecords));
            tabOem.setText(String.format(Locale.US, "OEM Service (%d)", oemCount));
            tabUpgrades.setText(String.format(Locale.US, "Upgrades & Mods (%d)", upgradeCount));

            double totalAppraised = BASE_TRUCK_MARKET_VALUE + totalUpgradesAddedVal + Math.min(3500.0, totalMaintProtectionVal);

            valBaseMarket.setText(String.format(Locale.US, "$%,.0f", BASE_TRUCK_MARKET_VALUE));
            valUpgradesAdded.setText(String.format(Locale.US, "+$%,.0f", totalUpgradesAddedVal + totalMaintProtectionVal));
            valResaleAppraised.setText(String.format(Locale.US, "$%,.0f", totalAppraised));

            com.autosentry.app.data.VehicleProfile vehicle = com.autosentry.app.util.VehicleManager.getActiveVehicle(MaintenanceActivity.this);
            carfaxTruckInfo.setText(String.format(Locale.US,
                    "%s %s • Current Odometer: %,d mi", vehicle.symbol, vehicle.getFullVehicleTitle(), CURRENT_TRUCK_MILEAGE));
            carfaxHealthScore.setText(String.format(Locale.US,
                    "Service Dossier: %d Upgrades + %d/%d Routine Services • Total Documented: $%,.2f",
                    upgradeCount, servicedCount, oemCount, totalInvested));

            filterList(editSearch.getText() != null ? editSearch.getText().toString() : "");
        });
    }

    private void filterList(String query) {
        displayedList.clear();
        String q = query != null ? query.trim().toLowerCase(Locale.US) : "";
        for (MaintenanceEvent m : masterList) {
            if (activeFilterTab == 1 && m.isUpgrade) continue;
            if (activeFilterTab == 2 && !m.isUpgrade) continue;

            if (q.isEmpty()) {
                displayedList.add(m);
            } else {
                boolean matchTitle = (m.title != null && m.title.toLowerCase(Locale.US).contains(q));
                boolean matchNotes = (m.notes != null && m.notes.toLowerCase(Locale.US).contains(q));
                boolean matchCategory = (m.category != null && m.category.toLowerCase(Locale.US).contains(q));
                boolean matchShop = (m.serviceShop != null && m.serviceShop.toLowerCase(Locale.US).contains(q));
                boolean matchBrand = (m.brandOrPartNumber != null && m.brandOrPartNumber.toLowerCase(Locale.US).contains(q));
                if (matchTitle || matchNotes || matchCategory || matchShop || matchBrand) {
                    displayedList.add(m);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showDatePicker(EditText targetDateEdit, Calendar calendar) {
        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            targetDateEdit.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void showResetDialog(MaintenanceEvent event) {
        lastSelectedPhotoPath = event.receiptImagePath;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reset_maintenance, null);
        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        TextView serviceName = dialogView.findViewById(R.id.dialog_service_name);
        EditText editDate = dialogView.findViewById(R.id.dialog_edit_date);
        Button btnPickDate = dialogView.findViewById(R.id.dialog_btn_pick_date);
        EditText editMileage = dialogView.findViewById(R.id.dialog_edit_mileage);
        EditText editCost = dialogView.findViewById(R.id.dialog_edit_cost);
        EditText editDeprecVal = dialogView.findViewById(R.id.dialog_edit_deprec_val);
        EditText editBrand = dialogView.findViewById(R.id.dialog_edit_brand);
        EditText editShop = dialogView.findViewById(R.id.dialog_edit_shop);
        EditText editNotes = dialogView.findViewById(R.id.dialog_edit_notes);
        ImageView previewImage = dialogView.findViewById(R.id.dialog_receipt_preview);
        Button btnPhoto = dialogView.findViewById(R.id.dialog_btn_choose_photo);
        TextView photoStatus = dialogView.findViewById(R.id.dialog_photo_status);

        pendingDialogImageView = previewImage;
        pendingDialogStatusView = photoStatus;

        titleView.setText(event.isUpgrade ? "Log Installed Upgrade & Value" : "Log Service & Reset Interval");
        serviceName.setText(event.title);

        Calendar cal = Calendar.getInstance();
        if (event.lastServiceAt > 0) {
            cal.setTimeInMillis(event.lastServiceAt);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        editDate.setText(sdf.format(cal.getTime()));

        btnPickDate.setOnClickListener(v -> showDatePicker(editDate, cal));

        editMileage.setText(String.valueOf(CURRENT_TRUCK_MILEAGE));
        if (event.cost > 0) {
            editCost.setText(String.format(Locale.US, "%.2f", event.cost));
        }
        if (event.estimatedValueAdded > 0) {
            editDeprecVal.setText(String.format(Locale.US, "%.2f", event.estimatedValueAdded));
        } else if (event.cost > 0) {
            editDeprecVal.setText(String.format(Locale.US, "%.2f", event.isUpgrade ? event.cost * 0.8 : 150.0));
        }

        if (event.brandOrPartNumber != null) {
            editBrand.setText(event.brandOrPartNumber);
        }
        if (event.serviceShop != null) {
            editShop.setText(event.serviceShop);
        }
        if (event.notes != null) {
            editNotes.setText(event.notes);
        }

        if (event.receiptImagePath != null) {
            File f = new File(event.receiptImagePath);
            if (f.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
                if (bmp != null) previewImage.setImageBitmap(bmp);
                photoStatus.setText("Receipt attached: " + f.getName());
            }
        }

        btnPhoto.setOnClickListener(v -> openPhotoPicker());

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Save to CarFax & Valuation", (dialog, which) -> {
                    String dateStr = editDate.getText().toString().trim();
                    long chosenEpoch = System.currentTimeMillis();
                    try {
                        Date parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr);
                        if (parsed != null) chosenEpoch = parsed.getTime();
                    } catch (Exception ignored) {}

                    String miStr = editMileage.getText().toString().trim();
                    int mileage = miStr.isEmpty() ? CURRENT_TRUCK_MILEAGE : Integer.parseInt(miStr);

                    String costStr = editCost.getText().toString().trim();
                    double cost = costStr.isEmpty() ? 0.0 : Double.parseDouble(costStr);

                    String valStr = editDeprecVal.getText().toString().trim();
                    double valueAdded = valStr.isEmpty() ? (event.isUpgrade ? cost * 0.8 : 100.0) : Double.parseDouble(valStr);

                    String brand = editBrand.getText().toString().trim();
                    String shop = editShop.getText().toString().trim();
                    String notes = editNotes.getText().toString().trim();

                    // Apply update
                    event.lastServiceAt = chosenEpoch;
                    event.eventTime = chosenEpoch;
                    event.currentMileage = mileage;
                    event.odometerAtLastService = mileage;
                    if (!event.isUpgrade && event.intervalKm > 0) {
                        event.nextServiceMileage = mileage + event.intervalKm;
                    } else if (event.isUpgrade) {
                        event.nextServiceMileage = mileage;
                    } else {
                        event.nextServiceMileage = mileage + 5000;
                    }

                    if (event.intervalMonths > 0) {
                        event.nextServiceAt = event.lastServiceAt + ((long) event.intervalMonths * 30L * 24L * 3600L * 1000L);
                    }
                    event.cost = cost;
                    event.estimatedValueAdded = valueAdded;
                    if (!brand.isEmpty()) event.brandOrPartNumber = brand;
                    event.serviceShop = shop.isEmpty() ? "Verified Installation" : shop;
                    event.notes = notes;
                    event.serviceCount++;
                    if (lastSelectedPhotoPath != null) {
                        event.receiptImagePath = lastSelectedPhotoPath;
                    }

                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                        db.maintenanceDao().update(event);
                        List<MaintenanceEvent> all = db.maintenanceDao().getAll();
                        updateList(all);
                    }).start();

                    Toast.makeText(this, "Logged " + event.title + " & updated valuation!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_RECEIPT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_RECEIPT && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                File receiptsDir = new File(getFilesDir(), "receipts");
                if (!receiptsDir.exists()) receiptsDir.mkdirs();

                String filename = "receipt_" + System.currentTimeMillis() + ".jpg";
                File destFile = new File(receiptsDir, filename);

                try (InputStream in = getContentResolver().openInputStream(imageUri);
                     OutputStream out = new FileOutputStream(destFile)) {
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }

                lastSelectedPhotoPath = destFile.getAbsolutePath();
                if (pendingDialogImageView != null) {
                    Bitmap bmp = BitmapFactory.decodeFile(destFile.getAbsolutePath());
                    if (bmp != null) pendingDialogImageView.setImageBitmap(bmp);
                }
                if (pendingDialogStatusView != null) {
                    pendingDialogStatusView.setText("Attached: " + filename);
                }
                Toast.makeText(this, "Receipt attached successfully!", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Toast.makeText(this, "Failed to load receipt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAddCustomServiceDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_maintenance, null);
        RadioButton radioUpgrade = dialogView.findViewById(R.id.radio_type_upgrade);
        EditText titleEdit = dialogView.findViewById(R.id.edit_maint_title);
        EditText brandEdit = dialogView.findViewById(R.id.edit_brand_part);
        EditText dateEdit = dialogView.findViewById(R.id.edit_service_date);
        Button btnPickDate = dialogView.findViewById(R.id.btn_pick_date);
        EditText mileageEdit = dialogView.findViewById(R.id.edit_install_mileage);
        EditText costEdit = dialogView.findViewById(R.id.edit_cost_val);
        EditText deprecValueEdit = dialogView.findViewById(R.id.edit_deprec_value_added);
        EditText warrantyEdit = dialogView.findViewById(R.id.edit_warranty);
        EditText shopEdit = dialogView.findViewById(R.id.edit_maint_shop);
        EditText intervalKmEdit = dialogView.findViewById(R.id.edit_interval_km);
        EditText intervalMonthsEdit = dialogView.findViewById(R.id.edit_interval_months);
        EditText notesEdit = dialogView.findViewById(R.id.edit_maint_notes);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateEdit.setText(sdf.format(cal.getTime()));
        mileageEdit.setText(String.valueOf(CURRENT_TRUCK_MILEAGE));

        btnPickDate.setOnClickListener(v -> showDatePicker(dateEdit, cal));

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Add & Protect Resale Value", (dialog, which) -> {
                    String title = titleEdit.getText().toString().trim();
                    if (title.isEmpty()) return;

                    boolean isUpgrade = radioUpgrade.isChecked();
                    MaintenanceEvent evt = new MaintenanceEvent();
                    evt.title = title;
                    evt.isUpgrade = isUpgrade;
                    evt.category = isUpgrade ? "PERFORMANCE / UPGRADE" : "ROUTINE SERVICE";
                    evt.eventType = isUpgrade ? "UPGRADE_INSTALLED" : "CUSTOM_SERVICE";
                    evt.brandOrPartNumber = brandEdit.getText().toString().trim();
                    evt.warrantyInfo = warrantyEdit.getText().toString().trim();

                    String dateStr = dateEdit.getText().toString().trim();
                    long chosenEpoch = System.currentTimeMillis();
                    try {
                        Date parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr);
                        if (parsed != null) chosenEpoch = parsed.getTime();
                    } catch (Exception ignored) {}
                    evt.lastServiceAt = chosenEpoch;
                    evt.eventTime = chosenEpoch;

                    String miStr = mileageEdit.getText().toString().trim();
                    evt.currentMileage = miStr.isEmpty() ? CURRENT_TRUCK_MILEAGE : Integer.parseInt(miStr);
                    evt.odometerAtLastService = evt.currentMileage;

                    String costStr = costEdit.getText().toString().trim();
                    evt.cost = costStr.isEmpty() ? 0.0 : Double.parseDouble(costStr);

                    String valStr = deprecValueEdit.getText().toString().trim();
                    evt.estimatedValueAdded = valStr.isEmpty() ? (isUpgrade ? evt.cost * 0.8 : 100.0) : Double.parseDouble(valStr);

                    evt.serviceShop = shopEdit.getText().toString().trim().isEmpty() ? "Owner Logged" : shopEdit.getText().toString().trim();
                    evt.notes = notesEdit.getText().toString().trim();

                    String kmStr = intervalKmEdit.getText().toString().trim();
                    evt.intervalKm = kmStr.isEmpty() ? (isUpgrade ? 0 : 5000) : Integer.parseInt(kmStr);
                    evt.nextServiceMileage = evt.currentMileage + evt.intervalKm;

                    String moStr = intervalMonthsEdit.getText().toString().trim();
                    evt.intervalMonths = moStr.isEmpty() ? (isUpgrade ? 0 : 6) : Integer.parseInt(moStr);
                    if (evt.intervalMonths > 0) {
                        evt.nextServiceAt = evt.lastServiceAt + (long) evt.intervalMonths * 30L * 24L * 3600L * 1000L;
                    }
                    evt.serviceCount = 1;

                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                        db.maintenanceDao().insert(evt);
                        List<MaintenanceEvent> all = db.maintenanceDao().getAll();
                        updateList(all);
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showReceiptDetailsDialog(MaintenanceEvent item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle((item.isUpgrade ? "★ Upgrade Dossier: " : "🔧 Service Record: ") + item.title);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        String lastDate = item.lastServiceAt > 0 ? sdf.format(new Date(item.lastServiceAt)) : "Initial Factory Spec";

        StringBuilder sb = new StringBuilder();
        sb.append("• Type: ").append(item.isUpgrade ? "Installed Capital Upgrade / Performance Mod" : "Scheduled OEM Maintenance").append("\n");
        sb.append("• Date Serviced / Installed: ").append(lastDate).append("\n");
        sb.append("• Odometer at Service: ").append(String.format(Locale.US, "%,d mi", item.currentMileage)).append("\n");
        if (item.brandOrPartNumber != null && !item.brandOrPartNumber.isEmpty()) {
            sb.append("• Brand / Part #: ").append(item.brandOrPartNumber).append("\n");
        }
        if (item.warrantyInfo != null && !item.warrantyInfo.isEmpty()) {
            sb.append("• Warranty Coverage: ").append(item.warrantyInfo).append("\n");
        }
        sb.append("• Installed / Service Facility: ").append(item.serviceShop != null ? item.serviceShop : "N/A").append("\n");
        sb.append("• Total Cost: $").append(String.format(Locale.US, "%.2f", item.cost)).append("\n");
        sb.append("• Resale / Depreciation Added Value: +$").append(String.format(Locale.US, "%.2f", item.estimatedValueAdded > 0 ? item.estimatedValueAdded : (item.cost * 0.75))).append("\n");
        if (!item.isUpgrade && item.intervalKm > 0) {
            sb.append("• Next Due: ").append(String.format(Locale.US, "%,d mi (Interval: %,d mi / %d mo)", item.nextServiceMileage, item.intervalKm, item.intervalMonths)).append("\n");
        }
        sb.append("• Specs / Service Notes: ").append(item.notes != null ? item.notes : "Standard Specifications").append("\n");
        sb.append("• Receipt File Status: ").append(item.receiptImagePath != null ? "✓ Photo Proof Verified & Attached" : "Digital Entry Only");

        builder.setMessage(sb.toString());
        builder.setPositiveButton("OK", null);
        builder.setNeutralButton("Delete Item", (d, w) -> {
            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                db.maintenanceDao().delete(item);
                List<MaintenanceEvent> all = db.maintenanceDao().getAll();
                updateList(all);
            }).start();
        });
        builder.show();
    }

    private void reloadOEMIntervalsAndUpgrades(boolean showToast) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.maintenanceDao().deleteAll();

            long now = System.currentTimeMillis();
            long dayMs = 1000L * 60 * 60 * 24;

            // 1. FACTORY OEM ROUTINE MAINTENANCE SCHEDULES
            addRecord(db, "Engine Oil & Filter (FL-1995)", "ENGINE", "OIL_CHANGE", false, 5000, 6, 160000, 118.50, 150.00,
                    "Motorcraft FL-1995", "OEM Spec", "15 Quarts Motorcraft 15W-40 Diesel Spec + FL-1995 filter", "Ford Dealership Quick Lane", now - (45 * dayMs));

            addRecord(db, "Fuel Filter (Underhood FD-4596 Element)", "FUEL", "FUEL_FILTER", false, 15000, 12, 150000, 48.00, 75.00,
                    "Motorcraft FD-4596", "OEM Spec", "Cleaned fuel bowl and replaced fuel element with OEM Motorcraft FD-4596", "Self / DIY", now - (120 * dayMs));

            addRecord(db, "Automatic Transmission Fluid & Filter (4R100 Mercon V)", "TRANSMISSION", "TRANS_FLUID", false, 30000, 24, 140000, 185.00, 300.00,
                    "Motorcraft Mercon V", "1-Year Parts", "Drain pan, replace internal transmission filter & refill with Mercon V", "Diesel Specialty Trans Shop", now - (280 * dayMs));

            addRecord(db, "Engine Coolant Flush & SCA Cavitation Check", "COOLING", "COOLANT_FLUSH", false, 50000, 36, 130000, 145.00, 250.00,
                    "Ford Gold / ELC", "OEM Spec", "Flushed cooling system, verified SCA nitrite levels to protect against cavitation", "Ford Service Center", now - (360 * dayMs));

            addRecord(db, "Rear Differential Gear Oil (75W-140 Synthetic)", "DIFFERENTIAL", "DIFF_FLUID", false, 50000, 36, 135000, 89.00, 120.00,
                    "Motorcraft 75W-140 Synthetic", "OEM Spec", "Dana 80 / Ford 10.5 rear axle lube with 4oz friction modifier additive", "Self / DIY", now - (300 * dayMs));

            addRecord(db, "Front Differential Gear Oil (75W-90 Synthetic)", "DIFFERENTIAL", "DIFF_FLUID", false, 50000, 36, 135000, 75.00, 100.00,
                    "Valvoline 75W-90 Synthetic", "OEM Spec", "Dana 60 front monobeam axle fluid replacement", "Self / DIY", now - (300 * dayMs));

            addRecord(db, "Transfer Case Fluid Drain & Fill (Mercon)", "TRANSMISSION", "TCASE_FLUID", false, 30000, 24, 140000, 55.00, 80.00,
                    "Motorcraft Mercon ATF", "OEM Spec", "NV273 Transfer Case 2 Quarts Mercon ATF", "Diesel Shop", now - (280 * dayMs));

            addRecord(db, "Serpentine Drive Belt & Heavy Duty Tensioner", "BELTS", "BELT_INSPECT", false, 30000, 24, 155000, 65.00, 90.00,
                    "Gates Heavy Duty Green Fleet", "2-Year Warranty", "Dual-alternator heavy duty serpentine belt installed and tensioner inspected", "Self / DIY", now - (90 * dayMs));

            addRecord(db, "High-Pressure Oil Pump (HPOP) O-Rings & ICP", "ENGINE", "HPOP_INSPECT", false, 50000, 36, 150000, 95.00, 180.00,
                    "DieselOrings OEM Viton", "Lifetime O-Rings", "Replaced HPOP fittings with Viton O-rings, inspected ICP pigtail", "PowerStroke Specialist", now - (150 * dayMs));

            addRecord(db, "Brake Fluid Complete Flush (DOT 4 High Temp)", "BRAKES", "BRAKE_FLUID", false, 30000, 24, 145000, 85.00, 120.00,
                    "Prestone DOT 4 High Temp", "OEM Spec", "Complete master cylinder and 4-corner caliper brake hydraulic bleed", "Brake & Tire Center", now - (200 * dayMs));

            addRecord(db, "Front Wheel Bearings & Greasable Ball Joints", "CHASSIS", "CHASSIS_LUBE", false, 10000, 12, 160000, 45.00, 80.00,
                    "Moog Greasable HD", "Lifetime Replacement", "Greased all zerk fittings, tie rods, drag links, and ball joints", "Quick Lube Service", now - (45 * dayMs));

            addRecord(db, "Air Filter Element (FA-1750 High Capacity)", "AIR", "AIR_FILTER", false, 15000, 12, 155000, 35.00, 50.00,
                    "Motorcraft FA-1750", "OEM Spec", "Filter minder inspected at 0% restriction, clean air box intake", "Self / DIY", now - (90 * dayMs));

            // 2. DOCUMENTED INSTALLED CAPITAL UPGRADES & PERFORMANCE MODS (Protects Depreciation / Raises Resale Value)
            addRecord(db, "KC300x Stage 1 Turbo Upgrade (63/68 .84 A/R)", "ENGINE", "UPGRADE_TURBO", true, 0, 0, 158000, 1450.00, 1250.00,
                    "KC Turbos KC300x", "3-Year Limited Warranty", "Installed Billet Compressor Wheel & 360-degree thrust bearing for cooler EGTs and +65 HP", "Diesel Performance Unlimited", now - (75 * dayMs));

            addRecord(db, "PHP Hydra Chip 15-Position Multi-Tuner", "TUNING", "UPGRADE_TUNER", true, 0, 0, 156000, 425.00, 380.00,
                    "Power Hungry Performance", "Lifetime Hardware Warranty", "Includes Daily, Heavy Tow, High Idle, and Eco Fuel 80HP Tunes with digital switch", "Self / DIY Installed", now - (85 * dayMs));

            addRecord(db, "Mishimoto All-Aluminum Heavy-Duty Radiator", "COOLING", "UPGRADE_COOLING", true, 0, 0, 152000, 750.00, 680.00,
                    "Mishimoto Performance", "Mishimoto Lifetime Warranty", "Full TIG-welded aluminum end tanks preventing factory plastic tank burst under heavy towing", "Diesel Specialty Shop", now - (140 * dayMs));

            addRecord(db, "S&B Cold Air Intake Kit with Cleanable Filter", "AIR", "UPGRADE_AIR", true, 0, 0, 157000, 349.00, 290.00,
                    "S&B Filters 75-5062", "Million Mile Warranty", "Enclosed airbox, silicone couplers, high-flow cleanable 8-ply cotton filter", "Self / DIY", now - (80 * dayMs));

            addRecord(db, "AirDog II-4G 165 GPH Demand-Flow Fuel Lift Pump", "FUEL", "UPGRADE_FUEL", true, 0, 0, 154000, 789.00, 700.00,
                    "PureFlow AirDog", "Lifetime Limited Warranty", "Removes air/vapor from diesel fuel, supplies constant 65 PSI fuel pressure to protect 7.3L injectors", "Diesel Dynamics Center", now - (110 * dayMs));

            addRecord(db, "MBRP 4-Inch Turbo-Back Armor Plus Stainless Exhaust", "EXHAUST", "UPGRADE_EXHAUST", true, 0, 0, 155000, 620.00, 520.00,
                    "MBRP T304 Stainless", "Lifetime Warranty", "Direct fit 4\" mandrel-bent stainless downpipe and exhaust, lowers EGTs by 150°F during steep grade towing", "Custom Exhaust & 4x4", now - (95 * dayMs));

            List<MaintenanceEvent> all = db.maintenanceDao().getAll();
            updateList(all);
            if (showToast) {
                runOnUiThread(() -> Toast.makeText(MaintenanceActivity.this, "Reloaded 12 OEM Schedules & 6 Performance Upgrades!", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void addRecord(AppDatabase db, String title, String category, String type, boolean isUpgrade,
                           int intKm, int intMo, int lastMi, double cost, double valAdded,
                           String brand, String warranty, String notes, String shop, long dateEpoch) {
        MaintenanceEvent m = new MaintenanceEvent();
        m.title = title;
        m.category = category;
        m.eventType = type;
        m.isUpgrade = isUpgrade;
        m.intervalKm = intKm;
        m.intervalMonths = intMo;
        m.currentMileage = lastMi;
        m.odometerAtLastService = lastMi;
        m.nextServiceMileage = isUpgrade ? lastMi : (lastMi + intKm);
        m.lastServiceAt = dateEpoch;
        m.eventTime = dateEpoch;
        m.nextServiceAt = isUpgrade ? 0 : (m.lastServiceAt + ((long) intMo * 30L * 24L * 3600L * 1000L));
        m.cost = cost;
        m.estimatedValueAdded = valAdded;
        m.brandOrPartNumber = brand;
        m.warrantyInfo = warranty;
        m.notes = notes;
        m.serviceShop = shop;
        m.serviceCount = 1;
        db.maintenanceDao().insert(m);
    }

    private void exportCarfaxReport() {
        new Thread(() -> {
            try {
                File exportsDir = new File(Environment.getExternalStorageDirectory(), "AutoSentry/Maintenance_Reports");
                if (!exportsDir.exists()) exportsDir.mkdirs();

                String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date());
                File reportFile = new File(exportsDir, "Ford_F350_Service_Valuation_Dossier_" + timestamp + ".csv");

                try (FileWriter writer = new FileWriter(reportFile)) {
                    writer.append("========================================================================================\n");
                    writer.append("AUTOSENTRY CERTIFIED VEHICLE MAINTENANCE & CAPITAL UPGRADES VALUATION DOSSIER\n");
                    writer.append("Vehicle: 2002 Ford F-350 Super Duty 7.3L PowerStroke Turbo Diesel\n");
                    writer.append("VIN: 1FTNW21F82EB74901 | Current Odometer: 164,250 Miles\n");
                    writer.append("Base Market BlueBook Value: $17,500.00\n");
                    writer.append("========================================================================================\n\n");
                    writer.append("Item_Title,Record_Type,Category,Date_Serviced_Installed,Odometer_Miles,Next_Due_Miles,Cost_USD,Resale_Depreciation_Value_Added,Brand_Part_Num,Warranty_Coverage,Shop_Mechanic,Receipt_File,Notes_Specs\n");

                    AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                    List<MaintenanceEvent> all = db.maintenanceDao().getAll();
                    double grandTotalSpent = 0;
                    double grandTotalValuationAdded = 0;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                    for (MaintenanceEvent m : all) {
                        grandTotalSpent += m.cost;
                        grandTotalValuationAdded += (m.estimatedValueAdded > 0 ? m.estimatedValueAdded : (m.cost * 0.75));
                        String dateStr = m.lastServiceAt > 0 ? sdf.format(new Date(m.lastServiceAt)) : "OEM";

                        writer.append(String.format(Locale.US,
                                "\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%.2f,%.2f,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                                m.title != null ? m.title.replace("\"", "'") : "",
                                m.isUpgrade ? "Installed Capital Upgrade" : "Routine Maintenance",
                                m.category != null ? m.category : "",
                                dateStr,
                                m.currentMileage,
                                m.nextServiceMileage,
                                m.cost,
                                m.estimatedValueAdded > 0 ? m.estimatedValueAdded : (m.cost * 0.75),
                                m.brandOrPartNumber != null ? m.brandOrPartNumber.replace("\"", "'") : "",
                                m.warrantyInfo != null ? m.warrantyInfo.replace("\"", "'") : "",
                                m.serviceShop != null ? m.serviceShop.replace("\"", "'") : "",
                                m.receiptImagePath != null ? m.receiptImagePath : "None",
                                m.notes != null ? m.notes.replace("\"", "'") : ""));
                    }

                    double appraisedTotal = BASE_TRUCK_MARKET_VALUE + grandTotalValuationAdded;
                    writer.append(String.format(Locale.US, "\nTOTAL DOCUMENTED CAPITAL INVESTMENT: $%.2f\n", grandTotalSpent));
                    writer.append(String.format(Locale.US, "TOTAL RESALE & DEPRECIATION PROTECTION VALUE ADDED: +$%.2f\n", grandTotalValuationAdded));
                    writer.append(String.format(Locale.US, "FINAL APPRAISED VEHICLE VALUE: $%.2f\n", appraisedTotal));
                }

                runOnUiThread(() -> Toast.makeText(MaintenanceActivity.this,
                        "Exported Buyer CarFax Dossier to: " + reportFile.getAbsolutePath(), Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MaintenanceActivity.this,
                        "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private class MaintenanceAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return displayedList.size();
        }

        @Override
        public Object getItem(int position) {
            return displayedList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return displayedList.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MaintenanceActivity.this).inflate(R.layout.item_maintenance_record, parent, false);
            }

            MaintenanceEvent item = displayedList.get(position);
            TextView titleView = convertView.findViewById(R.id.item_maint_title);
            TextView symbolView = convertView.findViewById(R.id.item_maint_symbol);
            TextView typeTagView = convertView.findViewById(R.id.item_type_tag);
            TextView brandWarrantyView = convertView.findViewById(R.id.item_brand_warranty);
            TextView badgeView = convertView.findViewById(R.id.item_maint_badge);
            TextView labelDateView = convertView.findViewById(R.id.item_label_date_serviced);
            TextView lastServiceView = convertView.findViewById(R.id.item_last_service);
            TextView labelNextDueView = convertView.findViewById(R.id.item_label_next_due);
            TextView nextDueView = convertView.findViewById(R.id.item_next_due);
            TextView costSpentView = convertView.findViewById(R.id.item_cost_spent);
            TextView deprecValView = convertView.findViewById(R.id.item_deprec_value);
            TextView shopCostView = convertView.findViewById(R.id.item_shop_cost);
            TextView notesView = convertView.findViewById(R.id.item_notes);
            ImageView thumbView = convertView.findViewById(R.id.item_receipt_thumb);
            Button btnViewDoc = convertView.findViewById(R.id.btn_view_receipt);
            Button btnReset = convertView.findViewById(R.id.btn_item_reset);
            Button btnUpload = convertView.findViewById(R.id.btn_item_upload_photo);
            Button btnAiDeals = convertView.findViewById(R.id.btn_item_ai_deals);

            View banner80Pct = convertView.findViewById(R.id.banner_80pct_wear);
            TextView text80Alert = convertView.findViewById(R.id.text_80pct_alert);
            TextView badge80Pct = convertView.findViewById(R.id.badge_80pct_pct);
            View wearBarContainer = convertView.findViewById(R.id.wear_bar_container);
            ProgressBar wearProgress = convertView.findViewById(R.id.item_wear_progress);
            TextView wearLabel = convertView.findViewById(R.id.item_wear_label);
            TextView remainingLabel = convertView.findViewById(R.id.item_remaining_label);

            titleView.setText(item.title);
            if (symbolView != null) {
                symbolView.setText(com.autosentry.app.util.SymbolRegistry.getSymbol(item.title, item.category));
            }

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            String dateServicedStr = item.lastServiceAt > 0 ? sdf.format(new Date(item.lastServiceAt)) : "Initial Factory Spec";

            if (item.isUpgrade) {
                if (banner80Pct != null) banner80Pct.setVisibility(View.GONE);
                if (wearBarContainer != null) wearBarContainer.setVisibility(View.GONE);

                typeTagView.setText("★ UPGRADE");
                typeTagView.setTextColor(0xFF38BDF8);
                typeTagView.setBackgroundColor(0xFF0C4A6E);

                badgeView.setText("INSTALLED");
                badgeView.setTextColor(0xFF38BDF8);
                badgeView.setBackgroundColor(0xFF0C4A6E);

                labelDateView.setText("Date Installed");
                lastServiceView.setText(String.format(Locale.US, "%,d mi (%s)", item.currentMileage, dateServicedStr));

                labelNextDueView.setText("Performance Status");
                nextDueView.setText("Permanent Asset");
                nextDueView.setTextColor(0xFF38BDF8);

                String brand = (item.brandOrPartNumber != null && !item.brandOrPartNumber.isEmpty()) ? item.brandOrPartNumber : "Custom Upgrade";
                String warranty = (item.warrantyInfo != null && !item.warrantyInfo.isEmpty()) ? item.warrantyInfo : "Verified";
                brandWarrantyView.setText(String.format(Locale.US, "Brand: %s • Warranty: %s • %s", brand, warranty, item.category != null ? item.category : "UPGRADE"));

                btnReset.setText("Log / Update");
            } else {
                typeTagView.setText("🔧 SERVICE");
                typeTagView.setTextColor(0xFF4ADE80);
                typeTagView.setBackgroundColor(0xFF064E3B);

                int milesDriven = Math.max(0, CURRENT_TRUCK_MILEAGE - (item.odometerAtLastService > 0 ? item.odometerAtLastService : (CURRENT_TRUCK_MILEAGE - 4250)));
                int interval = item.intervalKm > 0 ? item.intervalKm : 5000;
                int wearPct = (int) Math.min(100, Math.max(0, (milesDriven / (double) interval) * 100));
                int milesRemaining = item.nextServiceMileage - CURRENT_TRUCK_MILEAGE;

                if (wearBarContainer != null) {
                    wearBarContainer.setVisibility(View.VISIBLE);
                    if (wearProgress != null) {
                        wearProgress.setProgress(wearPct);
                        if (wearPct >= 100) {
                            wearProgress.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFEF4444));
                        } else if (wearPct >= 80) {
                            wearProgress.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFF59E0B));
                        } else {
                            wearProgress.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF22C55E));
                        }
                    }
                    if (wearLabel != null) wearLabel.setText(String.format(Locale.US, "Service Life Consumed: %d%%", wearPct));
                    if (remainingLabel != null) remainingLabel.setText(String.format(Locale.US, "%s mi remaining", milesRemaining > 0 ? String.format(Locale.US, "%,d", milesRemaining) : "0"));
                }

                // 80% Wear Alerting rule
                if (wearPct >= 80 || milesRemaining <= (interval * 0.20)) {
                    if (banner80Pct != null) {
                        banner80Pct.setVisibility(View.VISIBLE);
                        if (wearPct >= 100) {
                            banner80Pct.setBackgroundColor(0xFF7F1D1D);
                            if (text80Alert != null) text80Alert.setText("🚨 100% EXPIRED - IMMEDIATE SERVICE REQUIRED");
                            if (badge80Pct != null) {
                                badge80Pct.setText("OVERDUE");
                                badge80Pct.setBackgroundColor(0xFF991B1B);
                            }
                        } else {
                            banner80Pct.setBackgroundColor(0xFF78350F);
                            if (text80Alert != null) text80Alert.setText("⚠️ 80% WEAR REACHED - SERVICE PLANNING REQUIRED");
                            if (badge80Pct != null) {
                                badge80Pct.setText(wearPct + "% Consumed");
                                badge80Pct.setBackgroundColor(0xFF92400E);
                            }
                        }
                    }
                } else {
                    if (banner80Pct != null) banner80Pct.setVisibility(View.GONE);
                }

                if (milesRemaining <= 0) {
                    badgeView.setText("SERVICE DUE");
                    badgeView.setTextColor(0xFFEF4444);
                    badgeView.setBackgroundColor(0xFF7F1D1D);
                    nextDueView.setText(String.format(Locale.US, "%,d mi (%d mi OVERDUE)", item.nextServiceMileage, Math.abs(milesRemaining)));
                    nextDueView.setTextColor(0xFFEF4444);
                } else if (milesRemaining < 1500 || wearPct >= 80) {
                    badgeView.setText("80%+ DUE");
                    badgeView.setTextColor(0xFFFBBF24);
                    badgeView.setBackgroundColor(0xFF78350F);
                    nextDueView.setText(String.format(Locale.US, "%,d mi (in %,d mi)", item.nextServiceMileage, milesRemaining));
                    nextDueView.setTextColor(0xFFFBBF24);
                } else {
                    badgeView.setText("HEALTHY");
                    badgeView.setTextColor(0xFF4ADE80);
                    badgeView.setBackgroundColor(0xFF064E3B);
                    nextDueView.setText(String.format(Locale.US, "%,d mi (in %,d mi)", item.nextServiceMileage, milesRemaining));
                    nextDueView.setTextColor(0xFF38BDF8);
                }

                labelDateView.setText("Date Serviced");
                lastServiceView.setText(String.format(Locale.US, "%,d mi (%s)", item.currentMileage, dateServicedStr));
                labelNextDueView.setText("Next Service Due");

                brandWarrantyView.setText(String.format(Locale.US, "OEM Interval: %,d mi / %d mo • %s",
                        item.intervalKm, item.intervalMonths, item.brandOrPartNumber != null ? item.brandOrPartNumber : "Motorcraft Spec"));

                btnReset.setText("Reset Interval");
            }

            costSpentView.setText(String.format(Locale.US, "Installed Cost: $%.2f", item.cost));
            double addedVal = item.estimatedValueAdded > 0 ? item.estimatedValueAdded : (item.cost * (item.isUpgrade ? 0.8 : 0.5));
            deprecValView.setText(String.format(Locale.US, "Resale Protection: +$%.2f", addedVal));

            shopCostView.setText(String.format(Locale.US, "Facility: %s • Work Documented",
                    item.serviceShop != null ? item.serviceShop : "DIY / Self"));
            notesView.setText(item.notes != null ? item.notes : "Standard specifications and parts used");

            // Thumbnail / receipt image
            if (item.receiptImagePath != null) {
                File f = new File(item.receiptImagePath);
                if (f.exists()) {
                    Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
                    if (bmp != null) thumbView.setImageBitmap(bmp);
                } else {
                    thumbView.setImageResource(android.R.drawable.ic_menu_agenda);
                }
                if (btnViewDoc != null) btnViewDoc.setText("View Doc");
            } else {
                thumbView.setImageResource(android.R.drawable.ic_menu_camera);
                if (btnViewDoc != null) btnViewDoc.setText("Details");
            }

            // Click actions
            btnReset.setOnClickListener(v -> showResetDialog(item));
            btnUpload.setOnClickListener(v -> showResetDialog(item));
            if (btnViewDoc != null) btnViewDoc.setOnClickListener(v -> showReceiptDetailsDialog(item));

            if (btnAiDeals != null) {
                btnAiDeals.setOnClickListener(v -> {
                    Intent intent = new Intent(MaintenanceActivity.this, AIPartsActivity.class);
                    intent.putExtra(AIPartsActivity.EXTRA_SERVICE_TITLE, item.title);
                    intent.putExtra(AIPartsActivity.EXTRA_SERVICE_CATEGORY, item.category);
                    startActivity(intent);
                });
            }

            return convertView;
        }
    }
}
