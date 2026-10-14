package com.example.kumpan;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class GrainDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grain_detail);

        // Получаем позицию зерна
        int position = getIntent().getIntExtra("position", 0);

        // Получаем зерно
        ArrayList<GrainItem> items = GrainData.getItems();
        GrainItem item = items.get(position);

        // Кнопка "Назад"
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Название
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        tvTitle.setText(item.name);

        // Секция "Состав" — показываем ТОЛЬКО для блендов
        LinearLayout sectionComposition = findViewById(R.id.sectionComposition);
        TextView tvComposition = findViewById(R.id.tvComposition);
        if (item.composition != null) {
            sectionComposition.setVisibility(View.VISIBLE);
            tvComposition.setText(item.composition);
        } else {
            sectionComposition.setVisibility(View.GONE);
        }

        // Происхождение (для сортов — название, для блендов "Бленд")
        TextView tvOrigin = findViewById(R.id.tvOrigin);
        if (item.composition != null) {
            tvOrigin.setText("Бленд");
        } else {
            tvOrigin.setText(item.name);
        }

        // Обработка
        TextView tvProcessing = findViewById(R.id.tvProcessing);
        tvProcessing.setText(item.processing);

        // Профиль вкуса
        TextView tvBody = findViewById(R.id.tvBody);
        TextView tvAcidity = findViewById(R.id.tvAcidity);
        TextView tvSweetness = findViewById(R.id.tvSweetness);
        TextView tvBitterness = findViewById(R.id.tvBitterness);
        TextView tvAftertaste = findViewById(R.id.tvAftertaste);

        tvBody.setText(item.body);
        tvAcidity.setText(item.acidity);
        tvSweetness.setText(item.sweetness);
        tvBitterness.setText(item.bitterness);
        tvAftertaste.setText(item.aftertaste);

        // Букет
        TextView tvBouquet = findViewById(R.id.tvBouquet);
        tvBouquet.setText(item.bouquet);

        // Способы заваривания
        TextView tvBrewMethods = findViewById(R.id.tvBrewMethods);
        tvBrewMethods.setText(item.brewMethods);
    }
}