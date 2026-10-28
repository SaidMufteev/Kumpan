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

        // Скрываем ошибки при вводе
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

        // Кнопка "Войти"
        btnLogin.setOnClickListener(v -> onLoginClick());

        // Кнопка "Зарегистрироваться"
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // "Возникли трудности?"
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Обратитесь к администратору", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Обработка нажатия на кнопку "Войти"
     */
    private void onLoginClick() {
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Проверки на пустоту
        if (login.isEmpty()) {
            tvLoginError.setVisibility(View.VISIBLE);
            return;
        }
        if (password.isEmpty()) {
            tvPasswordError.setVisibility(View.VISIBLE);
            return;
        }

        // Проверка: 4 цифры
        if (login.length() != 4 || password.length() != 4) {
            tvLoginError.setText("Код и пароль — 4 цифры");
            tvLoginError.setVisibility(View.VISIBLE);
            return;
        }

        // Проверка: логин = пароль
        if (!login.equals(password)) {
            tvLoginError.setText("Код и пароль должны совпадать");
            tvLoginError.setVisibility(View.VISIBLE);
            return;
        }

        // Блокируем кнопку и показываем, что идет загрузка
        btnLogin.setEnabled(false);
        btnLogin.setText("Загрузка...");

        // Запрос к Firestore
        FirebaseHelper.loadUser(login)
                .addOnSuccessListener(documentSnapshot -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Войти");
                    handleUserResponse(documentSnapshot, login);
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Войти");
                    tvLoginError.setText("Ошибка подключения. Проверьте интернет");
                    tvLoginError.setVisibility(View.VISIBLE);
                });
    }

    /**
     * Обработка ответа от Firestore
     */
    private void handleUserResponse(DocumentSnapshot doc, String login) {
        // Пользователь не найден
        if (!doc.exists()) {
            tvLoginError.setText("Код " + login + " не найден. Зарегистрируйтесь");
            tvLoginError.setVisibility(View.VISIBLE);
            return;
        }

        // Проверка роли — pending не пускаем
        String role = doc.getString("role");
        if (role == null || role.equals("pending")) {
            tvLoginError.setText("Роль ещё не назначена. Обратитесь к менеджеру");
            tvLoginError.setVisibility(View.VISIBLE);
            return;
        }

        // Проверка блокировки (на будущее)
        Long lockedUntil = doc.getLong("lockedUntil");
        if (lockedUntil != null && lockedUntil > System.currentTimeMillis()) {
            long minutesLeft = (lockedUntil - System.currentTimeMillis()) / 60000 + 1;
            tvLoginError.setText("Аккаунт заблокирован на " + minutesLeft + " мин");
            tvLoginError.setVisibility(View.VISIBLE);
            return;
        }

        // Всё ок — сохраняем данные в SharedPreferences
        String fullName = doc.getString("fullName");
        String photoUrl = doc.getString("photoUrl");
        String positionTitle = doc.getString("positionTitle"); // Получаем должность

        SharedPreferences prefs = getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("user_login", login)
                .putString("user_name", fullName != null ? fullName : "")
                .putString("user_role", role)
                .putString("user_position", positionTitle != null ? positionTitle : "Сотрудник")
                .putString("user_photo", photoUrl != null ? photoUrl : "")
                .apply();

        Toast.makeText(this, "Добро пожаловать, " + fullName + "!", Toast.LENGTH_SHORT).show();

        // Переход на главный экран
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}