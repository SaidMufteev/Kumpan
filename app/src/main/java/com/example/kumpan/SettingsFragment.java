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
    private LinearLayout rowLanguage, rowEmployees, rowAddSchedule, rowSalary, rowActiveShifts;
    private TextView tvLanguage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        switchNotifications = view.findViewById(R.id.switchNotifications);
        rowLanguage = view.findViewById(R.id.rowLanguage);
        rowEmployees = view.findViewById(R.id.rowEmployees);
        rowAddSchedule = view.findViewById(R.id.rowAddSchedule);
        rowSalary = view.findViewById(R.id.rowSalary);
        rowActiveShifts = view.findViewById(R.id.rowActiveShifts);
        tvLanguage = view.findViewById(R.id.tvLanguage);

        TextView tvProfileName = view.findViewById(R.id.tvProfileName);
        TextView tvProfilePosition = view.findViewById(R.id.tvProfilePosition);

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String userName = prefs.getString("user_name", "Неизвестно");
        String userRole = prefs.getString("user_role", "guest");
        String userPosition = prefs.getString("user_position", "Сотрудник");

        tvProfileName.setText(userName);
        tvProfilePosition.setText(userPosition);

        boolean notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true);
        switchNotifications.setChecked(notificationsEnabled);
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();
        });

        rowLanguage.setOnClickListener(v -> Toast.makeText(getContext(), "Выбор языка (в разработке)", Toast.LENGTH_SHORT).show());

        // Зарплата
        rowSalary.setOnClickListener(v -> startActivity(new Intent(getContext(), SalaryActivity.class)));

        // Кто на смене (только менеджер/шеф)
        if ("manager".equals(userRole) || "chef".equals(userRole)) {
            rowActiveShifts.setVisibility(View.VISIBLE);
        }
        rowActiveShifts.setOnClickListener(v -> startActivity(new Intent(getContext(), ActiveShiftsActivity.class)));

        // Управление
        if ("manager".equals(userRole)) {
            rowEmployees.setVisibility(View.VISIBLE);
            rowAddSchedule.setVisibility(View.VISIBLE);
        } else if ("chef".equals(userRole)) {
            rowAddSchedule.setVisibility(View.VISIBLE);
        }

        rowEmployees.setOnClickListener(v -> startActivity(new Intent(getContext(), EmployeeListActivity.class)));
        rowAddSchedule.setOnClickListener(v -> startActivity(new Intent(getContext(), ScheduleManagerActivity.class)));

        return view;
    }
}