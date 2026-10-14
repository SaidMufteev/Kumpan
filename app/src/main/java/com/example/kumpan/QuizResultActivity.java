package com.example.kumpan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class QuizResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        // Получаем данные
        String category = getIntent().getStringExtra("category");
        int correct = getIntent().getIntExtra("correct", 0);
        int total = getIntent().getIntExtra("total", 9);

        // Считаем процент
        int percent = (int) Math.round(100.0 * correct / total);

        // Показываем
        TextView tvScore = findViewById(R.id.tvScore);
        TextView tvPercent = findViewById(R.id.tvPercent);

        tvScore.setText(correct + " из " + total);
        tvPercent.setText(percent + "%");

        // Кнопка "Пройти заново"
        Button btnRetry = findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(v -> {
            Intent intent = new Intent(QuizResultActivity.this, QuizActivity.class);
            intent.putExtra("category", category);
            startActivity(intent);
            finish();
        });

        // Кнопка "Выйти"
        Button btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(v -> finish());
    }
}