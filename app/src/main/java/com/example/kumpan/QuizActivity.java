package com.example.kumpan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class QuizActivity extends AppCompatActivity {

    private String category;
    private ArrayList<Question> questions;
    private int currentIndex = 0;
    private int correctCount = 0;
    private int selectedOption = -1;

    private TextView tvQuestionCounter, tvQuestionText, tvQuizTitle;
    private Button btnOption1, btnOption2, btnOption3, btnOption4, btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Получаем категорию
        category = getIntent().getStringExtra("category");
        if (category == null) category = "alco";

        // Загружаем вопросы
        questions = QuizData.getQuestions(category);

        // Находим элементы
        tvQuizTitle = findViewById(R.id.tvQuizTitle);
        tvQuestionCounter = findViewById(R.id.tvQuestionCounter);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        btnNext = findViewById(R.id.btnNext);

        // Заголовок
        tvQuizTitle.setText(getTitleForCategory(category));

        // Кнопка "Назад"
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Обработчики вариантов
        btnOption1.setOnClickListener(v -> selectOption(0));
        btnOption2.setOnClickListener(v -> selectOption(1));
        btnOption3.setOnClickListener(v -> selectOption(2));
        btnOption4.setOnClickListener(v -> selectOption(3));

        // Кнопка "Далее"
        btnNext.setOnClickListener(v -> onNextClick());

        // Показываем первый вопрос
        showQuestion();
    }

    /**
     * Показывает текущий вопрос и варианты
     */
    private void showQuestion() {
        Question q = questions.get(currentIndex);

        // Счётчик
        tvQuestionCounter.setText("Вопрос " + (currentIndex + 1) + " из " + questions.size());

        // Текст вопроса
        tvQuestionText.setText(q.text);

        // Варианты
        btnOption1.setText(q.options[0]);
        btnOption2.setText(q.options[1]);
        btnOption3.setText(q.options[2]);
        btnOption4.setText(q.options[3]);

        // Сброс выделения
        selectedOption = -1;
        resetOptionColors();

        // Кнопка "Далее" неактивна
        btnNext.setEnabled(false);

        // Текст кнопки (Последний вопрос → "Завершить")
        if (currentIndex == questions.size() - 1) {
            btnNext.setText("Завершить");
        } else {
            btnNext.setText("Далее");
        }
    }

    /**
     * Пользователь выбрал вариант
     */
    private void selectOption(int index) {
        selectedOption = index;
        resetOptionColors();

        // Подсветить выбранный
        int selectedColor = 0xFFBCFF8D; // color_light_green
        switch (index) {
            case 0: btnOption1.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedColor)); break;
            case 1: btnOption2.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedColor)); break;
            case 2: btnOption3.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedColor)); break;
            case 3: btnOption4.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedColor)); break;
        }

        // Активировать "Далее"
        btnNext.setEnabled(true);
    }

    /**
     * Сбрасывает цвет всех кнопок в белый
     */
    private void resetOptionColors() {
        int white = 0xFFFFFFFF;
        btnOption1.setBackgroundTintList(android.content.res.ColorStateList.valueOf(white));
        btnOption2.setBackgroundTintList(android.content.res.ColorStateList.valueOf(white));
        btnOption3.setBackgroundTintList(android.content.res.ColorStateList.valueOf(white));
        btnOption4.setBackgroundTintList(android.content.res.ColorStateList.valueOf(white));
    }

    /**
     * Нажатие на "Далее"
     */
    private void onNextClick() {
        // Проверяем правильность ответа
        Question q = questions.get(currentIndex);
        if (selectedOption == q.correctIndex) {
            correctCount++;
        }

        // Переход к следующему вопросу
        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            showQuestion();
        } else {
            // Это был последний вопрос — показываем результат
            Intent intent = new Intent(QuizActivity.this, QuizResultActivity.class);
            intent.putExtra("category", category);
            intent.putExtra("correct", correctCount);
            intent.putExtra("total", questions.size());
            startActivity(intent);
            finish();
        }
    }

    /**
     * Заголовок по категории
     */
    private String getTitleForCategory(String category) {
        switch (category) {
            case "alco":     return "Алко";
            case "desserts": return "Десерты";
            case "coffee":   return "Кофе/Чай";
            case "prep":     return "Заготовки";
            case "season":   return "Сезонка";
            case "grain":    return "Зерно";
            default:         return "Тест";
        }
    }
}