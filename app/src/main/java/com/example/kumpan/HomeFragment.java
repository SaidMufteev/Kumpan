package com.example.kumpan;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    private Button btnStartShift, btnEndShift;
    private TextView tvTodayTime, tvTodayDuration, tvTodayPosition;
    private LinearLayout cardOtherDay;
    private TextView tvOtherTitle, tvOtherTime, tvOtherDuration, tvOtherPosition, tvOtherDayOff;

    private LinearLayout dayMon, dayTue, dayWed, dayThu, dayFri, daySat, daySun;
    private TextView[] dateTexts = new TextView[7];
    private TextView[] iconTexts = new TextView[7];

    private Map<String, Shift> shiftsMap = new HashMap<>();
    private String[] weekDates = new String[7];
    private Integer selectedDayIndex = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Инициализация элементов
        btnStartShift = view.findViewById(R.id.btnStartShift);
        btnEndShift = view.findViewById(R.id.btnEndShift);

        tvTodayTime = view.findViewById(R.id.tvTodayTime);
        tvTodayDuration = view.findViewById(R.id.tvTodayDuration);
        tvTodayPosition = view.findViewById(R.id.tvTodayPosition);

        cardOtherDay = view.findViewById(R.id.cardOtherDay);
        tvOtherTitle = view.findViewById(R.id.tvOtherTitle);
        tvOtherTime = view.findViewById(R.id.tvOtherTime);
        tvOtherDuration = view.findViewById(R.id.tvOtherDuration);
        tvOtherPosition = view.findViewById(R.id.tvOtherPosition);
        tvOtherDayOff = view.findViewById(R.id.tvOtherDayOff);

        dayMon = view.findViewById(R.id.dayMon);
        dayTue = view.findViewById(R.id.dayTue);
        dayWed = view.findViewById(R.id.dayWed);
        dayThu = view.findViewById(R.id.dayThu);
        dayFri = view.findViewById(R.id.dayFri);
        daySat = view.findViewById(R.id.daySat);
        daySun = view.findViewById(R.id.daySun);

        // Массивы для быстрого доступа к датам и иконкам
        dateTexts[0] = view.findViewById(R.id.tvDate1); dateTexts[1] = view.findViewById(R.id.tvDate2);
        dateTexts[2] = view.findViewById(R.id.tvDate3); dateTexts[3] = view.findViewById(R.id.tvDate4);
        dateTexts[4] = view.findViewById(R.id.tvDate5); dateTexts[5] = view.findViewById(R.id.tvDate6);
        dateTexts[6] = view.findViewById(R.id.tvDate7);

        iconTexts[0] = view.findViewById(R.id.tvIcon1); iconTexts[1] = view.findViewById(R.id.tvIcon2);
        iconTexts[2] = view.findViewById(R.id.tvIcon3); iconTexts[3] = view.findViewById(R.id.tvIcon4);
        iconTexts[4] = view.findViewById(R.id.tvIcon5); iconTexts[5] = view.findViewById(R.id.tvIcon6);
        iconTexts[6] = view.findViewById(R.id.tvIcon7);

        // Обработчики кнопок смен
        btnStartShift.setOnClickListener(v -> Toast.makeText(getContext(), "Смена началась!", Toast.LENGTH_SHORT).show());
        btnEndShift.setOnClickListener(v -> Toast.makeText(getContext(), "Смена завершена!", Toast.LENGTH_SHORT).show());

        // Клики по дням
        dayMon.setOnClickListener(v -> onDayClick(0));
        dayTue.setOnClickListener(v -> onDayClick(1));
        dayWed.setOnClickListener(v -> onDayClick(2));
        dayThu.setOnClickListener(v -> onDayClick(3));
        dayFri.setOnClickListener(v -> onDayClick(4));
        daySat.setOnClickListener(v -> onDayClick(5));
        daySun.setOnClickListener(v -> onDayClick(6));

        // 1. СНАЧАЛА рисуем даты текущей недели (мгновенно)
        setupCurrentWeek();

        // 2. ПОТОМ идем в базу за сменами
        loadMySchedule();

        return view;
    }

    private void setupCurrentWeek() {
        Calendar calendar = Calendar.getInstance();
        // Устанавливаем на понедельник текущей недели
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            weekDates[i] = sdf.format(calendar.getTime());
            // Ставим реальное число (например, 26, 27, 28...)
            dateTexts[i].setText(dayFormat.format(calendar.getTime()));
            // По умолчанию ставим прочерк
            iconTexts[i].setText("—");

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void loadMySchedule() {
        SharedPreferences prefs = requireContext().getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        String myLogin = prefs.getString("user_login", "");
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        FirebaseHelper.loadSchedules()
                .addOnSuccessListener(querySnapshot -> {
                    shiftsMap.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String userId = doc.getString("userId");
                        String date = doc.getString("date");

                        if (myLogin.equals(userId)) {
                            String timeStart = doc.getString("timeStart");
                            String timeEnd = doc.getString("timeEnd");
                            String position = doc.getString("position");

                            int hours = 0;
                            try {
                                int startH = Integer.parseInt(timeStart.split(":")[0]);
                                int endH = Integer.parseInt(timeEnd.split(":")[0]);
                                hours = endH - startH;
                                if (hours < 0) hours += 24;
                            } catch (Exception ignored) {}

                            shiftsMap.put(date, new Shift(timeStart, timeEnd, position, hours));
                        }
                    }

                    // Обновляем карточку "Сегодня"
                    if (shiftsMap.containsKey(todayDate)) {
                        Shift s = shiftsMap.get(todayDate);
                        tvTodayTime.setText(s.timeStart + " - " + s.timeEnd);
                        tvTodayDuration.setText("(" + s.hours + " часов)");
                        tvTodayPosition.setText("Позиция: " + s.position);
                    } else {
                        tvTodayTime.setText("Нет смены");
                        tvTodayDuration.setText("Отдыхай :)");
                        tvTodayPosition.setText("");
                    }

                    // Обновляем иконки на днях недели (ставим часики)
                    for (int i = 0; i < 7; i++) {
                        if (shiftsMap.containsKey(weekDates[i])) {
                            iconTexts[i].setText("🕐");
                        } else {
                            iconTexts[i].setText("—");
                        }
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка загрузки графика", Toast.LENGTH_SHORT).show();
                });
    }

    private void onDayClick(int dayIndex) {
        if (selectedDayIndex != null && selectedDayIndex == dayIndex) {
            cardOtherDay.setVisibility(View.GONE);
            selectedDayIndex = null;
            return;
        }

        selectedDayIndex = dayIndex;
        String dateStr = weekDates[dayIndex];

        String[] dayNames = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};
        tvOtherTitle.setText(dayNames[dayIndex]);

        if (shiftsMap.containsKey(dateStr)) {
            Shift s = shiftsMap.get(dateStr);
            tvOtherTime.setText(s.timeStart + " - " + s.timeEnd);
            tvOtherDuration.setText("(" + s.hours + " часов)");
            tvOtherPosition.setText("Позиция: " + s.position);

            tvOtherTime.setVisibility(View.VISIBLE);
            tvOtherDuration.setVisibility(View.VISIBLE);
            tvOtherPosition.setVisibility(View.VISIBLE);
            tvOtherDayOff.setVisibility(View.GONE);
        } else {
            tvOtherTime.setVisibility(View.GONE);
            tvOtherDuration.setVisibility(View.GONE);
            tvOtherPosition.setVisibility(View.GONE);
            tvOtherDayOff.setVisibility(View.VISIBLE);
        }

        cardOtherDay.setVisibility(View.VISIBLE);
    }

    private static class Shift {
        String timeStart, timeEnd, position;
        int hours;
        Shift(String timeStart, String timeEnd, String position, int hours) {
            this.timeStart = timeStart;
            this.timeEnd = timeEnd;
            this.position = position;
            this.hours = hours;
        }
    }
}