package com.example.kumpan;

import java.io.Serializable;

public class Question implements Serializable {
    public String text;
    public String[] options;
    public int correctIndex;

    public Question(String text, String[] options, int correctIndex) {
        this.text = text;
        this.options = options;
        this.correctIndex = correctIndex;
    }
}