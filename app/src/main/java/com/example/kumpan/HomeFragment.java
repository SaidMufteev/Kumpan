package com.example.kumpan;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
    private TextView tvShiftTimer, tvTodayTime, tvTodayDuration, tvTodayPosition;
    private LinearLayout cardOtherDay;
    private TextView tvOtherTitle, tvOtherTime, tvOtherDuration, tvOtherPosition, tvOtherDayOff;
    private LinearLayout dayMon, dayTue, dayWed, dayThu, dayFri, daySat, daySun;
    private TextView[] dateTexts = new TextView[7];
    private TextView[] iconTexts = new TextView[7];

    private Map<String, Shift> shiftsMap = new HashMap<>();
    private String[] weekDates = new String[7];
    private Integer selectedDayIndex = null;

    private String activeShiftDocId = null;
    private long activeShiftStartTime = 0;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnStartShift = view.findViewById(R.id.btnStartShift);
        btnEndShift = view.findViewById(R.id.btnEndShift);
        tvShiftTimer = view.findViewById(R.id.tvShiftTimer);
        tvTodayTime = view.findViewById(R.id.tvTodayTime);
        tvTodayDuration = view.findViewById(R.id.tvTodayDuration);
        tvTodayPosition = view.findViewById(R.id.tvTodayPosition);

        cardOtherDay = view.findViewById(R.id.cardOtherDay);
        tvOtherTitle = view.findViewById(R.id.tvOtherTitle);
        tvOtherTime = view.findViewById(R.id.tvOtherTime);
        tvOtherDuration = view.findViewById(R.id.tvOtherDuration);
        tvOtherPosition = view.findViewById(R.id.tvOtherPosition);
        tvOtherDayOff = view.findViewById(R.id.tvOtherDayOff);

        dayMon = view.findViewById(R.id.dayMon); dayTue = view.findViewById(R.id.dayTue);
        dayWed = view.findViewById(R.id.dayWed); dayThu = view.findViewById(R.id.dayThu);
        dayFri = view.findViewById(R.id.dayFri); daySat = view.findViewById(R.id.daySat);
        daySun = view.findViewById(R.id.daySun);

        dateTexts[0] = view.findViewById(R.id.tvDate1); dateTexts[1] = view.findViewById(R.id.tvDate2);
        dateTexts[2] = view.findViewById(R.id.tvDate3); dateTexts[3] = view.findViewById(R.id.tvDate4);
        dateTexts[4] = view.findViewById(R.id.tvDate5); dateTexts[5] = view.findViewById(R.id.tvDate6);
        dateTexts[6] = view.findViewById(R.id.tvDate7);

        iconTexts[0] = view.findViewById(R.id.tvIcon1); iconTexts[1] = view.findViewById(R.id.tvIcon2);
        iconTexts[2] = view.findViewById(R.id.tvIcon3); iconTexts[3] = view.findViewById(R.id.tvIcon4);
        iconTexts[4] = view.findViewById(R.id.tvIcon5); iconTexts[5] = view.findViewById(R.id.tvIcon6);
        iconTexts[6] = view.findViewById(R.id.tvIcon7);

        dayMon.setOnClickListener(v -> onDayClick(0));
        dayTue.setOnClickListener(v -> onDayClick(1));
        dayWed.setOnClickListener(v -> onDayClick(2));
        dayThu.setOnClickListener(v -> onDayClick(3));
        dayFri.setOnClickListener(v -> onDayClick(4));
        daySat.setOnClickListener(v -> onDayClick(5));
        daySun.setOnClickListener(v -> onDayClick(6));

        btnStartShift.setOnClickListener(v -> startShift());
        btnEndShift.setOnClickListener(v -> endShift());

        setupCurrentWeek();
        loadMySchedule();
        checkActiveShift();

        return view;
    }

    private void checkActiveShift() {
        SharedPreferences prefs = requireContext().getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        String myLogin = prefs.getString("user_login", "");

        FirebaseHelper.getActiveShift(myLogin).addOnSuccessListener(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                activeShiftDocId = doc.getId();
                activeShiftStartTime = doc.getLong("startTime");

                btnStartShift.setEnabled(false);
                btnStartShift.setText("Смена идет...");
                btnEndShift.setEnabled(true);
                tvShiftTimer.setVisibility(View.VISIBLE);
                startTimer();
            } else {
                activeShiftDocId = null;
                btnStartShift.setEnabled(true);
                btnStartShift.setText("Начать смену");
                btnEndShift.setEnabled(false);
                tvShiftTimer.setVisibility(View.GONE);
            }
        });
    }

    private void startShift() {
        SharedPreferences prefs = requireContext().getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        String myLogin = prefs.getString("user_login", "");
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        FirebaseHelper.startShift(myLogin, todayDate, System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Смена начата!", Toast.LENGTH_SHORT).show();
                    checkActiveShift();
                });
    }

    private void endShift() {
        if (activeShiftDocId == null) return;

        // Делаем переменные final, чтобы лямбда их видела
        final long endTime = System.currentTimeMillis();
        final double rawDiffInHours = (endTime - activeShiftStartTime) / (1000.0 * 60 * 60);
        final double diffInHours = Math.round(rawDiffInHours * 100.0) / 100.0; // Округляем и сохраняем в final

        SharedPreferences prefs = requireContext().getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        final String myLogin = prefs.getString("user_login", "");

        FirebaseHelper.loadUser(myLogin).addOnSuccessListener(doc -> {
            Long rateLong = doc.getLong("rate");
            final double rate = rateLong != null ? rateLong.doubleValue() : 0.0;
            final double earnings = diffInHours * rate; // Теперь earnings тоже final

            FirebaseHelper.endShift(activeShiftDocId, endTime, diffInHours, earnings)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Смена закрыта. Отработано: " + diffInHours + " ч. Заработано: " + (int)earnings + " руб.", Toast.LENGTH_LONG).show();
                        if (timerRunnable != null) timerHandler.removeCallbacks(timerRunnable);
                        checkActiveShift();
                    });
        });
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - activeShiftStartTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                int hours = minutes / 60;
                seconds = seconds % 60;
                minutes = minutes % 60;

                tvShiftTimer.setText(String.format(Locale.getDefault(), "Смена идет: %02d:%02d:%02d", hours, minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void setupCurrentWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            weekDates[i] = sdf.format(calendar.getTime());
            dateTexts[i].setText(dayFormat.format(calendar.getTime()));
            iconTexts[i].setText("—");
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void loadMySchedule() {
        SharedPreferences prefs = requireContext().getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        String myLogin = prefs.getString("user_login", "");
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        FirebaseHelper.loadSchedules().addOnSuccessListener(querySnapshot -> {
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

            for (int i = 0; i < 7; i++) {
                iconTexts[i].setText(shiftsMap.containsKey(weekDates[i]) ? "🕐" : "—");
            }
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
            this.timeStart = timeStart; this.timeEnd = timeEnd; this.position = position; this.hours = hours;
        }
    }
}