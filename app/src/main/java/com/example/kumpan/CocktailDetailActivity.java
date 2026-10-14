package com.example.kumpan;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class CocktailDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cocktail_detail);

        String collectionName = getIntent().getStringExtra("collectionName");
        String docId = getIntent().getStringExtra("docId");
        String category = getIntent().getStringExtra("category");

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        LinearLayout layoutIngredients = findViewById(R.id.layoutIngredients);
        TextView tvNote = findViewById(R.id.tvNote);
        TextView tvTitle = findViewById(R.id.tvDetailTitle);

        if (docId == null) { finish(); return; }

        SharedPreferences prefs = getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        String userId = prefs.getString("user_login", "");

        // ОТМЕЧАЕМ ПРОЧТЕНИЕ ДЛЯ ПРОГРЕСС-БАРА
        if (category != null && !userId.isEmpty()) {
            FirebaseHelper.markAsRead(userId, category, docId);
        }

        FirebaseHelper.loadTtkDetail(collectionName, docId)
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    tvTitle.setText(doc.getString("name"));

                    List<String> ingredients = (List<String>) doc.get("ingredients");
                    if (ingredients != null) {
                        for (String ing : ingredients) {
                            TextView tv = new TextView(this);
                            tv.setText(ing);
                            tv.setTextSize(15);
                            tv.setTextColor(0xFF2D3436);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.bottomMargin = 8;
                            tv.setLayoutParams(params);
                            layoutIngredients.addView(tv);
                        }
                    }
                    String note = doc.getString("note");
                    if (note == null) note = doc.getString("technology");
                    if (note == null) note = "Нет описания";
                    tvNote.setText(note);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show());
    }
}