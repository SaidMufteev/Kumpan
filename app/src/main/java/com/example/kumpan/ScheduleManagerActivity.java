package com.example.kumpan;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class ScheduleManagerActivity extends AppCompatActivity {

    private LinearLayout layoutDays;
    private Button btnDeptBar, btnDeptHall, btnDeptKitchen;
    private EditText etStartTime, etEndTime;
    private ListView listEmployees;

    private Calendar currentWeekStart;
    private String selectedDate = "";
    private String currentDepartment = "bar";

    private List<Employee> allEmployees = new ArrayList<>();
    private List<Employee> filteredEmployees = new ArrayList<>();
    private EmployeeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_manager);

        // 1. СНАЧАЛА инициализируем все View элементы
        layoutDays = findViewById(R.id.layoutDays);
        btnDeptBar = findViewById(R.id.btnDeptBar);
        btnDeptHall = findViewById(R.id.btnDeptHall);
        btnDeptKitchen = findViewById(R.id.btnDeptKitchen);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        listEmployees = findViewById(R.id.listEmployees);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 2. ТЕПЕРЬ создаем адаптер (чтобы программа не падала при обновлении списка)
        adapter = new EmployeeAdapter();
        listEmployees.setAdapter(adapter);

        // 3. Получаем роль
        SharedPreferences prefs = getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        String myRole = prefs.getString("user_role", "");

        // 4. Настраиваем видимость кнопок (теперь безопасно, адаптер уже есть)
        if ("manager".equals(myRole)) {
            btnDeptKitchen.setVisibility(View.GONE);
            switchDepartment("bar");
        } else if ("chef".equals(myRole)) {
            btnDeptBar.setVisibility(View.GONE);
            btnDeptHall.setVisibility(View.GONE);
            switchDepartment("kitchen");
        } else {
            switchDepartment("bar");
        }

        // 5. Настройка календаря и кликов
        currentWeekStart = Calendar.getInstance();
        currentWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        findViewById(R.id.btnPrevWeek).setOnClickListener(v -> {
            currentWeekStart.add(Calendar.DAY_OF_MONTH, -7);
            drawCalendar();
        });
        findViewById(R.id.btnNextWeek).setOnClickListener(v -> {
            currentWeekStart.add(Calendar.DAY_OF_MONTH, 7);
            drawCalendar();
        });

        btnDeptBar.setOnClickListener(v -> switchDepartment("bar"));
        btnDeptHall.setOnClickListener(v -> switchDepartment("hall"));
        btnDeptKitchen.setOnClickListener(v -> switchDepartment("kitchen"));

        findViewById(R.id.btnSaveSchedule).setOnClickListener(v -> saveSchedules());

        // 6. Загружаем данные
        loadEmployees();
        drawCalendar();
    }

    private void switchDepartment(String dept) {
        currentDepartment = dept;

        int activeColor = Color.parseColor("#BCFF8D");
        int inactiveColor = Color.WHITE;
        int activeText = Color.parseColor("#1F4025");
        int inactiveText = Color.parseColor("#2D3436");

        if (btnDeptBar.getVisibility() == View.VISIBLE) {
            btnDeptBar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(dept.equals("bar") ? activeColor : inactiveColor));
            btnDeptBar.setTextColor(dept.equals("bar") ? activeText : inactiveText);
        }

        if (btnDeptHall.getVisibility() == View.VISIBLE) {
            btnDeptHall.setBackgroundTintList(android.content.res.ColorStateList.valueOf(dept.equals("hall") ? activeColor : inactiveColor));
            btnDeptHall.setTextColor(dept.equals("hall") ? activeText : inactiveText);
        }

        if (btnDeptKitchen.getVisibility() == View.VISIBLE) {
            btnDeptKitchen.setBackgroundTintList(android.content.res.ColorStateList.valueOf(dept.equals("kitchen") ? activeColor : inactiveColor));
            btnDeptKitchen.setTextColor(dept.equals("kitchen") ? activeText : inactiveText);
        }

        filterEmployees();
    }

    private void drawCalendar() {
        layoutDays.removeAllViews();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat daySdf = new SimpleDateFormat("EE\nd", new Locale("ru"));

        Calendar day = (Calendar) currentWeekStart.clone();
        for (int i = 0; i < 7; i++) {
            final String dateStr = sdf.format(day.getTime());
            String displayDay = daySdf.format(day.getTime()).toUpperCase();

            TextView tvDay = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tvDay.setLayoutParams(params);
            tvDay.setText(displayDay);
            tvDay.setGravity(android.view.Gravity.CENTER);
            tvDay.setTextSize(12);
            tvDay.setTextColor(Color.parseColor("#2D3436"));
            tvDay.setPadding(4, 8, 4, 8);

            if (dateStr.equals(selectedDate)) {
                tvDay.setBackgroundResource(R.drawable.card_green);
                tvDay.setTextColor(Color.parseColor("#1F4025"));
                tvDay.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tvDay.setBackgroundResource(R.drawable.card_white);
            }

            tvDay.setOnClickListener(v -> {
                selectedDate = dateStr;
                drawCalendar();
            });

            layoutDays.addView(tvDay);
            day.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void loadEmployees() {
        FirebaseHelper.loadAllUsers().addOnSuccessListener(querySnapshot -> {
            allEmployees.clear();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String login = doc.getId();
                String name = doc.getString("fullName");
                String role = doc.getString("role");

                if ("pending".equals(role)) continue;

                allEmployees.add(new Employee(login, name, role));
            }
            filterEmployees();
        });
    }

    private void filterEmployees() {
        filteredEmployees.clear();
        for (Employee emp : allEmployees) {
            if (currentDepartment.equals("bar") && "bartender".equals(emp.role)) {
                filteredEmployees.add(emp);
            } else if (currentDepartment.equals("hall") && "waiter".equals(emp.role)) {
                filteredEmployees.add(emp);
            } else if (currentDepartment.equals("kitchen") && ("cook".equals(emp.role) || "chef".equals(emp.role))) {
                filteredEmployees.add(emp);
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void saveSchedules() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Выберите дату в календаре", Toast.LENGTH_SHORT).show();
            return;
        }
        String start = etStartTime.getText().toString().trim();
        String end = etEndTime.getText().toString().trim();
        if (start.isEmpty() || end.isEmpty()) {
            Toast.makeText(this, "Введите время", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        String myLogin = prefs.getString("user_login", "");

        int savedCount = 0;
        for (Employee emp : filteredEmployees) {
            if (emp.isSelected) {
                String position = "Бар";
                if (currentDepartment.equals("hall")) position = "Офик";
                if (currentDepartment.equals("kitchen")) position = "Кухня";

                FirebaseHelper.createSchedule(emp.login, selectedDate, start, end, position, currentDepartment, myLogin);
                savedCount++;
            }
        }

        if (savedCount > 0) {
            Toast.makeText(this, "Смены сохранены для " + savedCount + " чел.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Отметьте сотрудников", Toast.LENGTH_SHORT).show();
        }
    }

    private static class Employee {
        String login, name, role;
        boolean isSelected = false;
        Employee(String login, String name, String role) {
            this.login = login; this.name = name; this.role = role;
        }
    }

    private class EmployeeAdapter extends ArrayAdapter<Employee> {
        EmployeeAdapter() {
            super(ScheduleManagerActivity.this, 0, filteredEmployees);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(ScheduleManagerActivity.this)
                        .inflate(R.layout.item_employee_checkbox, parent, false);
            }
            Employee emp = filteredEmployees.get(position);

            CheckBox cb = convertView.findViewById(R.id.cbEmployee);
            TextView tvName = convertView.findViewById(R.id.tvEmpName);
            TextView tvRole = convertView.findViewById(R.id.tvEmpRole);

            tvName.setText(emp.name);
            tvRole.setText(emp.role + " (" + emp.login + ")");

            cb.setOnCheckedChangeListener(null);
            cb.setChecked(emp.isSelected);
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> emp.isSelected = isChecked);

            return convertView;
        }
    }
}