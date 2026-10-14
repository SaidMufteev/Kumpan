package com.example.kumpan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SeasonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_season);

        // Кнопка "Назад"
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 4 кнопки сезонов
        LinearLayout btnSummer = findViewById(R.id.btnSummer);
        LinearLayout btnAutumn = findViewById(R.id.btnAutumn);
        LinearLayout btnWinter = findViewById(R.id.btnWinter);
        LinearLayout btnSpring = findViewById(R.id.btnSpring);

        btnSummer.setOnClickListener(v -> openSeasonList("season_summer"));
        btnAutumn.setOnClickListener(v -> openSeasonList("season_autumn"));
        btnWinter.setOnClickListener(v -> openSeasonList("season_winter"));
        btnSpring.setOnClickListener(v -> openSeasonList("season_spring"));
    }

    /**
     * Открывает список напитков для выбранного сезона
     */
    private void openSeasonList(String seasonCategory) {
        Intent intent = new Intent(SeasonActivity.this, TtkListActivity.class);
        intent.putExtra("category", seasonCategory);
        startActivity(intent);
    }
}