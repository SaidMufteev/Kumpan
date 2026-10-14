package com.example.kumpan;

import android.content.Intent;
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
    private LinearLayout rowLanguage, rowEmployees, rowAddSchedule;
    private TextView tvLanguage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        switchNotifications = view.findViewById(R.id.switchNotifications);
        rowLanguage = view.findViewById(R.id.rowLanguage);
        rowEmployees = view.findViewById(R.id.rowEmployees);
        rowAddSchedule = view.findViewById(R.id.rowAddSchedule);
        tvLanguage = view.findViewById(R.id.tvLanguage);

        TextView tvProfileName = view.findViewById(R.id.tvProfileName);
        TextView tvProfilePosition = view.findViewById(R.id.tvProfilePosition);

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String userName = prefs.getString("user_name", "Неизвестно");
        String userRole = prefs.getString("user_role", "guest");

        tvProfileName.setText(userName);
        tvProfilePosition.setText(getPositionByRole(userRole));

        boolean notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true);
        switchNotifications.setChecked(notificationsEnabled);
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();
        });

        rowLanguage.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Выбор языка (в разработке)", Toast.LENGTH_SHORT).show();
        });

        // Логика для менеджера и шефа
        if ("manager".equals(userRole)) {
            rowEmployees.setVisibility(View.VISIBLE);
            rowAddSchedule.setVisibility(View.VISIBLE);
        } else if ("chef".equals(userRole)) {
            rowEmployees.setVisibility(View.GONE); // Шеф не управляет сотрудниками
            rowAddSchedule.setVisibility(View.VISIBLE); // Но составляет график кухни
        } else {
            rowEmployees.setVisibility(View.GONE);
            rowAddSchedule.setVisibility(View.GONE);
        }

        rowEmployees.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), EmployeeListActivity.class));
        });

        rowAddSchedule.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), ScheduleManagerActivity.class));
        });

        return view;
    }

    private String getPositionByRole(String role) {
        switch (role) {
            case "manager":   return "Менеджер";
            case "waiter":    return "Официант";
            case "bartender": return "Бармен";
            case "cook":      return "Повар";
            case "chef":      return "Шеф-повар";
            case "pending":   return "Ожидает назначения";
            default:          return "Сотрудник";
        }
    }
}