package com.example.kumpan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class QuizResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        String category = getIntent().getStringExtra("category");
        int correct = getIntent().getIntExtra("correct", 0);
        int total = getIntent().getIntExtra("total", 9);

        // Получаем данные для просмотра
        ArrayList<Question> questions = (ArrayList<Question>) getIntent().getSerializableExtra("questions");
        ArrayList<Integer> userAnswers = getIntent().getIntegerArrayListExtra("userAnswers");

        int percent = (int) Math.round(100.0 * correct / total);

        TextView tvScore = findViewById(R.id.tvScore);
        TextView tvPercent = findViewById(R.id.tvPercent);
        tvScore.setText(correct + " из " + total);
        tvPercent.setText(percent + "%");

        // Кнопка "Посмотреть ответы"
        Button btnReview = findViewById(R.id.btnReview);
        btnReview.setOnClickListener(v -> {
            Intent intent = new Intent(QuizResultActivity.this, QuizReviewActivity.class);
            intent.putExtra("questions", questions);
            intent.putExtra("userAnswers", userAnswers);
            startActivity(intent);
        });

        Button btnRetry = findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(v -> {
            Intent intent = new Intent(QuizResultActivity.this, QuizActivity.class);
            intent.putExtra("category", category);
            startActivity(intent);
            finish();
        });

        Button btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(v -> finish());
    }
}