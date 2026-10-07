package com.example.kumpan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etLogin, etPassword;
    private TextView tvLoginError, tvPasswordError;
    private Button btnLogin, btnRegister;
    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        tvLoginError = findViewById(R.id.tvLoginError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Скрываем ошибку логина при вводе
        etLogin.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvLoginError.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Скрываем ошибку пароля при вводе
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvPasswordError.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Кнопка "Войти"
        btnLogin.setOnClickListener(v -> {
            String login = etLogin.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            boolean hasError = false;

            if (login.isEmpty()) {
                tvLoginError.setVisibility(View.VISIBLE);
                hasError = true;
            }
            if (password.isEmpty()) {
                tvPasswordError.setVisibility(View.VISIBLE);
                hasError = true;
            }
            if (hasError) return;

            SharedPreferences prefs = getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
            prefs.edit().putString("user_login", login).apply();

            Toast.makeText(this, "Вход выполнен!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // Кнопка "Зарегистрироваться"
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        // "Возникли трудности?"
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Обратитесь к администратору", Toast.LENGTH_SHORT).show();
        });
    }
}