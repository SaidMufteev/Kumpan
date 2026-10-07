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

public class RegisterActivity extends AppCompatActivity {

    private EditText etLogin, etPassword, etConfirmPassword;
    private TextView tvConfirmError;
    private Button btnRegister, btnLoginLink;
    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvConfirmError = findViewById(R.id.tvConfirmError);
        btnRegister = findViewById(R.id.btnRegister);
        btnLoginLink = findViewById(R.id.btnLoginLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvConfirmError.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        etPassword.addTextChangedListener(watcher);
        etConfirmPassword.addTextChangedListener(watcher);

        btnRegister.setOnClickListener(v -> {
            String login = etLogin.getText().toString().trim();
            String password = etPassword.getText().toString();
            String confirm = etConfirmPassword.getText().toString();

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                tvConfirmError.setVisibility(View.VISIBLE);
                return;
            }

            SharedPreferences prefs = getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
            prefs.edit().putString("user_login", login).apply();

            Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Обратитесь к администратору", Toast.LENGTH_SHORT).show();
        });
    }
}