package com.example.kumpan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private String category;
    private ArrayList<Question> questions = new ArrayList<>();
    private ArrayList<Integer> userAnswers = new ArrayList<>(); // Храним ответы пользователя
    private int currentIndex = 0;
    private int correctCount = 0;
    private int selectedOption = -1;
    private boolean isFinished = false;

    private TextView tvQuestionCounter, tvQuestionText;
    private Button btnOption1, btnOption2, btnOption3, btnOption4, btnNext;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        category = getIntent().getStringExtra("category");
        if (category == null) category = "alco";

        tvQuestionCounter = findViewById(R.id.tvQuestionCounter);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        btnNext = findViewById(R.id.btnNext);

        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.VISIBLE);
        ((android.view.ViewGroup)findViewById(R.id.btnOption1).getParent()).addView(progressBar);

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnOption1.setOnClickListener(v -> selectOption(0));
        btnOption2.setOnClickListener(v -> selectOption(1));
        btnOption3.setOnClickListener(v -> selectOption(2));
        btnOption4.setOnClickListener(v -> selectOption(3));
        btnNext.setOnClickListener(v -> onNextClick());

        loadQuestionsFromDB();
    }

    private void loadQuestionsFromDB() {
        FirebaseHelper.loadQuestions(category)
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    questions.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String text = doc.getString("text");
                        List<String> optionsList = (List<String>) doc.get("options");
                        Long correctLong = doc.getLong("correctIndex");

                        if (text != null && optionsList != null && correctLong != null) {
                            String[] options = optionsList.toArray(new String[0]);
                            questions.add(new Question(text, options, correctLong.intValue()));
                        }
                    }

                    if (questions.isEmpty()) {
                        Toast.makeText(this, "Вопросы для этой темы еще не добавлены", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    TextView tvQuizTitle = findViewById(R.id.tvQuizTitle);
                    tvQuizTitle.setText(getTitleForCategory(category));
                    showQuestion();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ошибка загрузки вопросов", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void showQuestion() {
        Question q = questions.get(currentIndex);
        tvQuestionCounter.setText("Вопрос " + (currentIndex + 1) + " из " + questions.size());
        tvQuestionText.setText(q.text);

        btnOption1.setText(q.options[0]);
        btnOption2.setText(q.options[1]);
        btnOption3.setText(q.options[2]);
        btnOption4.setText(q.options[3]);

        selectedOption = -1;
        resetOptionColors();
        btnNext.setEnabled(false);
        btnNext.setText(currentIndex == questions.size() - 1 ? "Завершить" : "Далее");
    }

    private void selectOption(int index) {
        selectedOption = index;
        resetOptionColors();
        int selectedColor = 0xFFBCFF8D;
        switch (index) {
            case 0: btnOption1.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedColor)); break;
            case 1: btnOption2.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedColor)); break;
            case 2: btnOption3.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedColor)); break;
            case 3: btnOption4.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedColor)); break;
        }
        btnNext.setEnabled(true);
    }

    private void resetOptionColors() {
        int white = 0xFFFFFFFF;
        btnOption1.setBackgroundTintList(android.content.res.ColorStateList.valueOf(white));
        btnOption2.setBackgroundTintList(android.content.res.ColorStateList.valueOf(white));
        btnOption3.setBackgroundTintList(android.content.res.ColorStateList.valueOf(white));
        btnOption4.setBackgroundTintList(android.content.res.ColorStateList.valueOf(white));
    }

    private void onNextClick() {
        // Сохраняем ответ пользователя
        userAnswers.add(selectedOption);

        Question q = questions.get(currentIndex);
        if (selectedOption == q.correctIndex) correctCount++;

        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            showQuestion();
        } else {
            isFinished = true;

            SharedPreferences prefs = getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
            String userId = prefs.getString("user_login", "");
            prefs.edit().putLong("last_attempt_" + category, System.currentTimeMillis()).apply();

            FirebaseHelper.saveTestResult(userId, category, correctCount, questions.size())
                    .addOnSuccessListener(aVoid -> {
                        Intent intent = new Intent(QuizActivity.this, QuizResultActivity.class);
                        intent.putExtra("category", category);
                        intent.putExtra("correct", correctCount);
                        intent.putExtra("total", questions.size());
                        // Передаем вопросы и ответы для просмотра
                        intent.putExtra("questions", questions);
                        intent.putExtra("userAnswers", userAnswers);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка сохранения результата", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isFinished && !isChangingConfigurations() && !questions.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
            String userId = prefs.getString("user_login", "");

            FirebaseHelper.saveTestResult(userId, category, 0, questions.size());
            prefs.edit().putLong("last_attempt_" + category, System.currentTimeMillis()).apply();

            Toast.makeText(this, "Вы вышли из теста. Попытка списана (0 баллов)", Toast.LENGTH_LONG).show();
        }
    }

    private String getTitleForCategory(String category) {
        switch (category) {
            case "alco": return "Алко";
            case "desserts": return "Десерты";
            case "coffee": return "Кофе/Чай";
            case "prep": return "Заготовки";
            case "season": return "Сезонка";
            case "grain": return "Зерно";
            case "dishes": return "Блюда";
            case "wines": return "Вина";
            default: return "Тест";
        }
    }
}