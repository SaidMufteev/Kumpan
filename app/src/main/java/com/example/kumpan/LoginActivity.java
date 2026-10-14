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

import com.google.firebase.firestore.DocumentSnapshot;

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

        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvLoginError.setVisibility(View.GONE);
                tvPasswordError.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        etLogin.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);

        btnLogin.setOnClickListener(v -> onLoginClick());

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Обратитесь к администратору", Toast.LENGTH_SHORT).show();
        });
    }

    private void onLoginClick() {
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (login.isEmpty()) {
            tvLoginError.setVisibility(View.VISIBLE);
            return;
        }
        if (password.isEmpty()) {
            tvPasswordError.setVisibility(View.VISIBLE);
            return;
        }

        if (login.length() != 4 || password.length() != 4) {
            tvLoginError.setText("Код и пароль — 4 цифры");
            tvLoginError.setVisibility(View.VISIBLE);
            return;
        }

        if (!login.equals(password)) {
            tvLoginError.setText("Код и пароль должны совпадать");
            tvLoginError.setVisibility(View.VISIBLE);
            return;
        }

        btnLogin.setEnabled(false);
        Toast.makeText(this, "Проверка...", Toast.LENGTH_SHORT).show();

        FirebaseHelper.loadUser(login)
                .addOnSuccessListener(documentSnapshot -> {
                    btnLogin.setEnabled(true);
                    handleUserResponse(documentSnapshot, login);
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    tvLoginError.setText("Ошибка подключения. Проверьте интернет");
                    tvLoginError.setVisibility(View.VISIBLE);
                });
    }

    private void handleUserResponse(DocumentSnapshot doc, String login) {
        if (!doc.exists()) {
            tvLoginError.setText("Код " + login + " не найден. Зарегистрируйтесь");
            tvLoginError.setVisibility(View.VISIBLE);
            return;
        }

        String role = doc.getString("role");
        if (role == null || role.equals("pending")) {
            tvLoginError.setText("Роль ещё не назначена. Обратитесь к менеджеру");
            tvLoginError.setVisibility(View.VISIBLE);
            return;
        }

        Long lockedUntil = doc.getLong("lockedUntil");
        if (lockedUntil != null && lockedUntil > System.currentTimeMillis()) {
            long minutesLeft = (lockedUntil - System.currentTimeMillis()) / 60000 + 1;
            tvLoginError.setText("Аккаунт заблокирован на " + minutesLeft + " мин");
            tvLoginError.setVisibility(View.VISIBLE);
            return;
        }

        String fullName = doc.getString("fullName");
        String photoUrl = doc.getString("photoUrl");

        // СОХРАНЯЕМ ДАННЫЕ ПРИ ВХОДЕ
        SharedPreferences prefs = getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("user_login", login)
                .putString("user_name", fullName != null ? fullName : "")
                .putString("user_role", role)
                .putString("user_photo", photoUrl != null ? photoUrl : "")
                .apply();

        Toast.makeText(this, "Добро пожаловать, " + fullName + "!", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}