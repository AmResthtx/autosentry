package com.autosentry.app.ui;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.autosentry.app.R;
import com.autosentry.app.data.VehicleProfile;
import com.autosentry.app.util.AIPartsFinder;
import com.autosentry.app.util.AIPartsFinder.PartDeal;
import com.autosentry.app.util.SymbolRegistry;
import com.autosentry.app.util.VehicleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AIPartsActivity extends AppCompatActivity {

    public static final String EXTRA_SERVICE_TITLE = "extra_service_title";
    public static final String EXTRA_SERVICE_CATEGORY = "extra_service_category";

    private EditText editSearchParts;
    private Button btnAiSearch;
    private ListView partsListView;
    private LinearLayout aiAdvisorCard;
    private TextView aiAdvisorText;

    private Button chipAll;
    private Button chipOil;
    private Button chipFuel;
    private Button chipTrans;
    private Button chipSensors;

    private VehicleProfile currentVehicle;
    private final List<PartDeal> masterDeals = new ArrayList<>();
    private final List<PartDeal> displayedDeals = new ArrayList<>();
    private PartsDealAdapter adapter;
    private int selectedCategory = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_parts);

        currentVehicle = VehicleManager.getActiveVehicle(this);

        editSearchParts = findViewById(R.id.edit_search_parts);
        btnAiSearch = findViewById(R.id.btn_ai_search_parts);
        partsListView = findViewById(R.id.parts_list_view);
        aiAdvisorCard = findViewById(R.id.ai_advisor_card);
        aiAdvisorText = findViewById(R.id.ai_advisor_text);

        chipAll = findViewById(R.id.chip_all_parts);
        chipOil = findViewById(R.id.chip_oil_filters);
        chipFuel = findViewById(R.id.chip_fuel_system);
        chipTrans = findViewById(R.id.chip_transmission);
        chipSensors = findViewById(R.id.chip_sensors);

        adapter = new PartsDealAdapter();
        partsListView.setAdapter(adapter);

        chipAll.setOnClickListener(v -> setCategory(0));
        chipOil.setOnClickListener(v -> setCategory(1));
        chipFuel.setOnClickListener(v -> setCategory(2));
        chipTrans.setOnClickListener(v -> setCategory(3));
        chipSensors.setOnClickListener(v -> setCategory(4));

        btnAiSearch.setOnClickListener(v -> performAiSearch());

        editSearchParts.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDeals();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        String initTitle = getIntent().getStringExtra(EXTRA_SERVICE_TITLE);
        String initCategory = getIntent().getStringExtra(EXTRA_SERVICE_CATEGORY);

        loadInitialDeals(initTitle, initCategory);
    }

    private void setCategory(int cat) {
        selectedCategory = cat;
        chipAll.setBackgroundColor(cat == 0 ? 0xFF0284C7 : 0xFF334155);
        chipOil.setBackgroundColor(cat == 1 ? 0xFF0284C7 : 0xFF334155);
        chipFuel.setBackgroundColor(cat == 2 ? 0xFF0284C7 : 0xFF334155);
        chipTrans.setBackgroundColor(cat == 3 ? 0xFF0284C7 : 0xFF334155);
        chipSensors.setBackgroundColor(cat == 4 ? 0xFF0284C7 : 0xFF334155);
        filterDeals();
    }

    private void loadInitialDeals(String title, String category) {
        new Thread(() -> {
            List<PartDeal> all = AIPartsFinder.searchDealsForVehicle(currentVehicle, title, category);

            runOnUiThread(() -> {
                masterDeals.clear();
                masterDeals.addAll(all);

                if (title != null && !title.isEmpty()) {
                    editSearchParts.setText(title);
                }
                filterDeals();
            });
        }).start();
    }

    private void filterDeals() {
        displayedDeals.clear();
        String query = editSearchParts.getText() != null ? editSearchParts.getText().toString().toLowerCase(Locale.US).trim() : "";

        for (PartDeal d : masterDeals) {
            String cat = d.category.toLowerCase(Locale.US);
            if (selectedCategory == 1 && !cat.contains("oil") && !cat.contains("engine")) continue;
            if (selectedCategory == 2 && !cat.contains("fuel")) continue;
            if (selectedCategory == 3 && !cat.contains("trans") && !cat.contains("drivetrain")) continue;
            if (selectedCategory == 4 && !cat.contains("sensor") && !cat.contains("electrical") && !cat.contains("ignition")) continue;

            if (query.isEmpty()) {
                displayedDeals.add(d);
            } else {
                boolean matchName = d.partName.toLowerCase(Locale.US).contains(query);
                boolean matchNum = d.oemPartNumber.toLowerCase(Locale.US).contains(query);
                boolean matchSeller = d.partnerSeller.toLowerCase(Locale.US).contains(query);
                boolean matchWhy = d.whyQualityMatters.toLowerCase(Locale.US).contains(query);
                if (matchName || matchNum || matchSeller || matchWhy) {
                    displayedDeals.add(d);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void performAiSearch() {
        String query = editSearchParts.getText() != null ? editSearchParts.getText().toString().trim() : "";
        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a part name or symptom to search", Toast.LENGTH_SHORT).show();
            return;
        }

        aiAdvisorCard.setVisibility(View.VISIBLE);
        aiAdvisorText.setText("⚡ Contacting Gemini AI & searching partner distributor inventory for '" + query + "' (" + currentVehicle.getFullVehicleTitle() + ")...");

        new Thread(() -> {
            String result = AIPartsFinder.queryGeminiPartsAdvisor(query, currentVehicle.getFullVehicleTitle());
            runOnUiThread(() -> {
                aiAdvisorText.setText(result);
                filterDeals();
            });
        }).start();
    }

    private class PartsDealAdapter extends ArrayAdapter<PartDeal> {
        PartsDealAdapter() {
            super(AIPartsActivity.this, 0, displayedDeals);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_part_deal, parent, false);
            }

            PartDeal deal = getItem(position);
            if (deal == null) return convertView;

            TextView symbolView = convertView.findViewById(R.id.deal_symbol);
            TextView qualityBadge = convertView.findViewById(R.id.deal_quality_badge);
            TextView partnerTag = convertView.findViewById(R.id.deal_partner_tag);
            TextView partName = convertView.findViewById(R.id.deal_part_name);
            TextView partNumber = convertView.findViewById(R.id.deal_part_number);
            TextView bestPrice = convertView.findViewById(R.id.deal_best_price);
            TextView regularPrice = convertView.findViewById(R.id.deal_regular_price);
            TextView savingsBadge = convertView.findViewById(R.id.deal_savings_badge);
            TextView whyQuality = convertView.findViewById(R.id.deal_why_quality);
            TextView partnerSeller = convertView.findViewById(R.id.deal_partner_seller);
            TextView couponCode = convertView.findViewById(R.id.deal_coupon_code);
            Button btnCopyCode = convertView.findViewById(R.id.btn_copy_code);
            Button btnClaim = convertView.findViewById(R.id.btn_claim_deal);

            String sym = (deal.symbol != null && !deal.symbol.isEmpty()) ? deal.symbol : SymbolRegistry.getSymbol(deal.partName, deal.category);
            if (symbolView != null) {
                symbolView.setText(sym);
            }

            qualityBadge.setText(deal.qualityTier);
            partnerTag.setVisibility(deal.isPartnerExclusive ? View.VISIBLE : View.GONE);
            partName.setText(deal.partName);
            partNumber.setText(deal.oemPartNumber + " • " + deal.fitmentVerification);
            bestPrice.setText(String.format(Locale.US, "$%.2f", deal.bestPrice));
            regularPrice.setText(String.format(Locale.US, " $%.2f", deal.regularPrice));
            savingsBadge.setText(deal.savingsText);
            whyQuality.setText(deal.whyQualityMatters);
            partnerSeller.setText(deal.partnerSeller + " • " + deal.stockStatus);
            couponCode.setText(deal.couponCode);

            btnCopyCode.setOnClickListener(v -> {
                String codeOnly = deal.couponCode.split(" ")[0];
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("AutoSentry Partner Promo Code", codeOnly);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(AIPartsActivity.this, "Copied partner coupon '" + codeOnly + "' to clipboard!", Toast.LENGTH_SHORT).show();
            });

            btnClaim.setOnClickListener(v -> {
                new AlertDialog.Builder(AIPartsActivity.this)
                        .setTitle("AutoSentry Partner Deal Claimed!")
                        .setMessage(String.format(Locale.US,
                                "Part: %s\n\n" +
                                "• Partner Seller: %s\n" +
                                "• Exclusive Price: $%.2f (Reg: $%.2f)\n" +
                                "• Discount Code: %s\n\n" +
                                "Quality Protection Guarantee:\n%s\n\n" +
                                "Proceed to seller store with partner discount automatically applied?",
                                deal.partName, deal.partnerSeller, deal.bestPrice, deal.regularPrice, deal.couponCode, deal.whyQualityMatters))
                        .setPositiveButton("Open Seller Store", (dialog, which) -> {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deal.buyUrl));
                                startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(AIPartsActivity.this, "Opening partner store with promo applied...", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            return convertView;
        }
    }
}
