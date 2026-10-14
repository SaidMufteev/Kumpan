package com.example.kumpan;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "KumpanPrefs";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";

    private Switch switchNotifications;
    private LinearLayout rowLanguage;
    private TextView tvLanguage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Находим элементы
        switchNotifications = view.findViewById(R.id.switchNotifications);
        rowLanguage = view.findViewById(R.id.rowLanguage);
        tvLanguage = view.findViewById(R.id.tvLanguage);

        // Загружаем сохранённое значение переключателя
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true);
        switchNotifications.setChecked(notificationsEnabled);

        // Обработчик переключателя
        switchNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Сохраняем значение
                prefs.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();

                // Показываем сообщение
                String msg = isChecked ? "Уведомления включены" : "Уведомления выключены";
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Обработчик клика на "Язык приложения"
        rowLanguage.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Выбор языка (в разработке)", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}