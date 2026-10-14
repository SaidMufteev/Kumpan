package com.example.kumpan;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class CocktailDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cocktail_detail);

        // Получаем данные из Intent
        String category = getIntent().getStringExtra("category");
        int position = getIntent().getIntExtra("position", 0);

        if (category == null) category = "alco";

        // Получаем коктейль
        ArrayList<TtkItem> items = TtkData.getItems(category);
        TtkItem item = items.get(position);

        // Кнопка "Назад"
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Заголовок
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        tvTitle.setText(item.name);

        // Состав — создаём TextView для каждого ингредиента
        LinearLayout layoutIngredients = findViewById(R.id.layoutIngredients);
        for (String ingredient : item.ingredients) {
            TextView tv = new TextView(this);
            tv.setText(ingredient);
            tv.setTextSize(15);
            tv.setTextColor(0xFF2D3436); // color_text_dark

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = 8;
            tv.setLayoutParams(params);

            layoutIngredients.addView(tv);
        }

        // Примечание
        TextView tvNote = findViewById(R.id.tvNote);
        tvNote.setText(item.note);
    }
}