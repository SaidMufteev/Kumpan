package com.example.kumpan;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GrainDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grain_detail);

        String collectionName = getIntent().getStringExtra("collectionName");
        String docId = getIntent().getStringExtra("docId");
        String category = getIntent().getStringExtra("category");

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        LinearLayout sectionComposition = findViewById(R.id.sectionComposition);
        TextView tvComposition = findViewById(R.id.tvComposition);
        TextView tvOrigin = findViewById(R.id.tvOrigin);
        TextView tvProcessing = findViewById(R.id.tvProcessing);
        TextView tvBody = findViewById(R.id.tvBody);
        TextView tvAcidity = findViewById(R.id.tvAcidity);
        TextView tvSweetness = findViewById(R.id.tvSweetness);
        TextView tvBitterness = findViewById(R.id.tvBitterness);
        TextView tvAftertaste = findViewById(R.id.tvAftertaste);
        TextView tvBouquet = findViewById(R.id.tvBouquet);
        TextView tvBrewMethods = findViewById(R.id.tvBrewMethods);
        TextView tvTitle = findViewById(R.id.tvDetailTitle);

        if (docId == null) { finish(); return; }

        SharedPreferences prefs = getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        String userId = prefs.getString("user_login", "");

        if (category != null && !userId.isEmpty()) {
            FirebaseHelper.markAsRead(userId, category, docId);
        }

        FirebaseHelper.loadTtkDetail(collectionName, docId)
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    String name = doc.getString("name");
                    tvTitle.setText(name);

                    String composition = doc.getString("composition");
                    if (composition != null && !composition.isEmpty()) {
                        sectionComposition.setVisibility(View.VISIBLE);
                        tvComposition.setText(composition);
                        tvOrigin.setText("Бленд");
                    } else {
                        sectionComposition.setVisibility(View.GONE);
                        tvOrigin.setText(name);
                    }

                    tvProcessing.setText(getStr(doc.getString("processing")));
                    tvBody.setText(getStr(doc.getString("body")));
                    tvAcidity.setText(getStr(doc.getString("acidity")));
                    tvSweetness.setText(getStr(doc.getString("sweetness")));
                    tvBitterness.setText(getStr(doc.getString("bitterness")));
                    tvAftertaste.setText(getStr(doc.getString("aftertaste")));
                    tvBouquet.setText(getStr(doc.getString("bouquet")));
                    tvBrewMethods.setText(getStr(doc.getString("brewMethods")));
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show());
    }

    private String getStr(String s) {
        return s != null ? s : "—";
    }
}