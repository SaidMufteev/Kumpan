package com.example.kumpan;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class DessertDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dessert_detail);

        // Получаем позицию десерта
        int position = getIntent().getIntExtra("position", 0);

        // Получаем десерт
        ArrayList<DessertItem> items = DessertData.getItems();
        DessertItem item = items.get(position);

        // Кнопка "Назад"
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Название
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        tvTitle.setText(item.name);

        // Состав
        LinearLayout layoutIngredients = findViewById(R.id.layoutIngredients);
        for (String ingredient : item.ingredients) {
            TextView tv = new TextView(this);
            tv.setText(ingredient);
            tv.setTextSize(15);
            tv.setTextColor(0xFF2D3436);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = 8;
            tv.setLayoutParams(params);

            layoutIngredients.addView(tv);
        }

        // Срок хранения
        TextView tvStorage = findViewById(R.id.tvStorage);
        tvStorage.setText(item.storage);

        // Аллергены
        TextView tvAllergens = findViewById(R.id.tvAllergens);
        tvAllergens.setText(item.allergens);

        // Описание для гостя
        TextView tvGuest = findViewById(R.id.tvGuestDescription);
        tvGuest.setText(item.guestDescription);
    }
}