package com.example.kumpan;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etLogin, etPassword, etConfirmPassword;
    private TextView tvError;
    private Button btnRegister, btnLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvError = findViewById(R.id.tvError);
        btnRegister = findViewById(R.id.btnRegister);
        btnLoginLink = findViewById(R.id.btnLoginLink);

        // Скрываем ошибку при вводе
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvError.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        etFullName.addTextChangedListener(textWatcher);
        etLogin.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
        etConfirmPassword.addTextChangedListener(textWatcher);

        // Кнопка "Зарегистрироваться"
        btnRegister.setOnClickListener(v -> onRegisterClick());

        // Кнопка "Войти"
        btnLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    /**
     * Обработка нажатия на "Зарегистрироваться"
     */
    private void onRegisterClick() {
        String fullName = etFullName.getText().toString().trim();
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        // 1. Проверка на пустоту
        if (fullName.isEmpty()) {
            showError("Введите ФИО");
            return;
        }
        if (login.isEmpty() || password.isEmpty()) {
            showError("Введите код и пароль");
            return;
        }

        // 2. Проверка: ровно 4 цифры
        if (login.length() != 4 || password.length() != 4) {
            showError("Код и пароль — 4 цифры");
            return;
        }

        // 3. Проверка: логин = пароль
        if (!login.equals(password)) {
            showError("Код и пароль должны совпадать");
            return;
        }

        // 4. Проверка: пароли совпадают
        if (!password.equals(confirm)) {
            showError("Пароли не совпадают");
            return;
        }

        // 5. Проверяем в Firestore — занят ли код
        btnRegister.setEnabled(false);
        Toast.makeText(this, "Проверка кода...", Toast.LENGTH_SHORT).show();

        FirebaseHelper.loadUser(login)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Код уже занят
                        btnRegister.setEnabled(true);
                        showError("Код " + login + " уже используется");
                    } else {
                        // Код свободен — создаём пользователя
                        createNewUser(login, fullName);
                    }
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    showError("Ошибка подключения. Проверьте интернет");
                });
    }

    /**
     * Создаёт нового пользователя в Firestore
     */
    private void createNewUser(String login, String fullName) {
        FirebaseHelper.createUser(login, fullName)
                .addOnSuccessListener(aVoid -> {
                    btnRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this,
                            "Регистрация успешна! Ожидайте назначения роли",
                            Toast.LENGTH_LONG).show();

                    // Переход на экран входа
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    showError("Не удалось создать пользователя. Попробуйте ещё раз");
                });
    }

    /**
     * Показывает текст ошибки
     */
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}