package com.example.kumpan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import static android.content.Context.MODE_PRIVATE;

public class SeasonActivity extends AppCompatActivity {

    private String currentDepartment = "bar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_season);

        SharedPreferences prefs = getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        String role = prefs.getString("user_role", "guest");
        if ("cook".equals(role) || "chef".equals(role)) {
            currentDepartment = "kitchen";
        }

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        LinearLayout btnSummer = findViewById(R.id.btnSummer);
        LinearLayout btnAutumn = findViewById(R.id.btnAutumn);
        LinearLayout btnWinter = findViewById(R.id.btnWinter);
        LinearLayout btnSpring = findViewById(R.id.btnSpring);

        btnSummer.setOnClickListener(v -> openSeasonList("season_summer"));
        btnAutumn.setOnClickListener(v -> openSeasonList("season_autumn"));
        btnWinter.setOnClickListener(v -> openSeasonList("season_winter"));
        btnSpring.setOnClickListener(v -> openSeasonList("season_spring"));
    }

    private void openSeasonList(String seasonCategory) {
        Intent intent = new Intent(SeasonActivity.this, TtkListActivity.class);
        intent.putExtra("category", seasonCategory);
        intent.putExtra("department", currentDepartment); // Передаем отдел
        startActivity(intent);
    }
}