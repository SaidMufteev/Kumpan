package com.example.kumpan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private Button btnStartShift, btnEndShift;

    private LinearLayout cardOtherDay;
    private TextView tvOtherTitle, tvOtherTime, tvOtherDuration, tvOtherPosition, tvOtherDayOff;

    private LinearLayout dayMon, dayTue, dayWed, dayThu, dayFri, daySat, daySun;

    private Integer selectedDay = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnStartShift = view.findViewById(R.id.btnStartShift);
        btnEndShift = view.findViewById(R.id.btnEndShift);

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

        dayMon.setOnClickListener(v -> onDayClick(0));
        dayTue.setOnClickListener(v -> onDayClick(1));
        dayWed.setOnClickListener(v -> onDayClick(2));
        dayThu.setOnClickListener(v -> onDayClick(3));
        dayFri.setOnClickListener(v -> onDayClick(4));
        daySat.setOnClickListener(v -> onDayClick(5));
        daySun.setOnClickListener(v -> onDayClick(6));

        btnStartShift.setOnClickListener(v ->
                Toast.makeText(getContext(), "Смена началась!", Toast.LENGTH_SHORT).show());
        btnEndShift.setOnClickListener(v ->
                Toast.makeText(getContext(), "Смена завершена!", Toast.LENGTH_SHORT).show());

        return view;
    }

    private void onDayClick(int dayIndex) {
        if (dayIndex == 0) return;

        if (selectedDay != null && selectedDay == dayIndex) {
            cardOtherDay.setVisibility(View.GONE);
            selectedDay = null;
            return;
        }

        selectedDay = dayIndex;
        setDayData(dayIndex);
        cardOtherDay.setVisibility(View.VISIBLE);
    }

    private void setDayData(int dayIndex) {
        switch (dayIndex) {
            case 1:
                tvOtherTitle.setText("Вторник");
                showDayOff();
                break;
            case 2:
                tvOtherTitle.setText("Среда");
                showWork("16:00 - 23:00", "(7 часов)", "Позиция: Бар");
                break;
            case 3:
                tvOtherTitle.setText("Четверг");
                showWork("7:00 - 16:00", "(9 часов)", "Позиция: Офик");
                break;
            case 4:
                tvOtherTitle.setText("Пятница");
                showDayOff();
                break;
            case 5:
                tvOtherTitle.setText("Суббота");
                showWork("7:00 - 00:00", "(17 часов)", "Позиция: Офик");
                break;
            case 6:
                tvOtherTitle.setText("Воскресенье");
                showDayOff();
                break;
        }
    }

    private void showWork(String time, String duration, String position) {
        tvOtherTime.setText(time);
        tvOtherDuration.setText(duration);
        tvOtherPosition.setText(position);

        tvOtherTime.setVisibility(View.VISIBLE);
        tvOtherDuration.setVisibility(View.VISIBLE);
        tvOtherPosition.setVisibility(View.VISIBLE);
        tvOtherDayOff.setVisibility(View.GONE);
    }

    private void showDayOff() {
        tvOtherTime.setVisibility(View.GONE);
        tvOtherDuration.setVisibility(View.GONE);
        tvOtherPosition.setVisibility(View.GONE);
        tvOtherDayOff.setVisibility(View.VISIBLE);
    }
}