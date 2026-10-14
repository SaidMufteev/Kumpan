package com.example.kumpan;

public class Question {

    public String text;             // Текст вопроса
    public String[] options;        // 4 варианта ответа
    public int correctIndex;        // Номер правильного ответа (0-3)

    public Question(String text, String[] options, int correctIndex) {
        this.text = text;
        this.options = options;
        this.correctIndex = correctIndex;
    }
}