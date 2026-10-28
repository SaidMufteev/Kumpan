package com.example.kumpan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class QuizReviewActivity extends AppCompatActivity {

    private ArrayList<Question> questions;
    private ArrayList<Integer> userAnswers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_review);

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        questions = (ArrayList<Question>) getIntent().getSerializableExtra("questions");
        userAnswers = getIntent().getIntegerArrayListExtra("userAnswers");

        if (questions == null || userAnswers == null) {
            finish();
            return;
        }

        ListView listReview = findViewById(R.id.listReview);
        ReviewAdapter adapter = new ReviewAdapter();
        listReview.setAdapter(adapter);
    }

    private class ReviewAdapter extends ArrayAdapter<Question> {
        ReviewAdapter() {
            super(QuizReviewActivity.this, 0, questions);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(QuizReviewActivity.this)
                        .inflate(R.layout.item_quiz_review, parent, false);
            }

            Question q = questions.get(position);
            int userAnswerIndex = userAnswers.get(position);

            TextView tvQuestion = convertView.findViewById(R.id.tvReviewQuestion);
            TextView tvUserAnswer = convertView.findViewById(R.id.tvUserAnswer);
            TextView tvCorrectAnswer = convertView.findViewById(R.id.tvCorrectAnswer);

            tvQuestion.setText("Вопрос " + (position + 1) + ": " + q.text);

            // Вывод ответа пользователя
            if (userAnswerIndex >= 0 && userAnswerIndex < q.options.length) {
                tvUserAnswer.setText("Ваш ответ: " + q.options[userAnswerIndex]);
            } else {
                tvUserAnswer.setText("Ваш ответ: Нет ответа");
            }

            // Проверка: правильно или нет
            if (userAnswerIndex == q.correctIndex) {
                // Правильно
                tvUserAnswer.setTextColor(0xFF1F4025); // Зеленый
                tvCorrectAnswer.setVisibility(View.GONE);
            } else {
                // Неправильно
                tvUserAnswer.setTextColor(0xFFFF0000); // Красный
                tvCorrectAnswer.setVisibility(View.VISIBLE);
                tvCorrectAnswer.setText("Правильный ответ: " + q.options[q.correctIndex]);
            }

            return convertView;
        }
    }
}