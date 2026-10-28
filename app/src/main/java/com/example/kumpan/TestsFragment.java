package com.example.kumpan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class TestsFragment extends Fragment {

    private ListView listTests;
    private List<String> displayNames = new ArrayList<>();
    private List<String> testKeys = new ArrayList<>();
    private TestsAdapter adapter;
    private String currentRole;

    private final String[] ALL_NAMES = {"Алко", "Десерты", "Кофе/Чай", "Заготовки", "Сезонка", "Зерно", "Блюда", "Вина"};
    private final String[] ALL_KEYS = {"alco", "desserts", "coffee", "prep", "season", "grain", "dishes", "wines"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tests, container, false);

        SharedPreferences prefs = requireContext().getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        currentRole = prefs.getString("user_role", "guest");

        listTests = view.findViewById(R.id.listTests);
        adapter = new TestsAdapter();
        listTests.setAdapter(adapter);

        filterTestsByRole();

        return view;
    }

    private void filterTestsByRole() {
        displayNames.clear();
        testKeys.clear();

        List<String> allowedKeys = new ArrayList<>();

        // Синхронизировано с KnowledgeFragment
        if ("manager".equals(currentRole)) {
            allowedKeys.add("alco"); allowedKeys.add("desserts"); allowedKeys.add("coffee");
            allowedKeys.add("prep"); allowedKeys.add("season"); allowedKeys.add("grain");
            allowedKeys.add("dishes"); allowedKeys.add("wines");
        } else if ("bartender".equals(currentRole)) {
            allowedKeys.add("alco"); allowedKeys.add("coffee"); allowedKeys.add("prep");
            allowedKeys.add("season"); allowedKeys.add("grain"); allowedKeys.add("desserts");
        } else if ("waiter".equals(currentRole)) {
            allowedKeys.add("desserts"); allowedKeys.add("season"); allowedKeys.add("wines"); allowedKeys.add("dishes");
        } else if ("cook".equals(currentRole) || "chef".equals(currentRole)) {
            allowedKeys.add("prep"); allowedKeys.add("season"); allowedKeys.add("dishes");
        }

        for (int i = 0; i < ALL_KEYS.length; i++) {
            if (allowedKeys.contains(ALL_KEYS[i])) {
                testKeys.add(ALL_KEYS[i]);
                displayNames.add(ALL_NAMES[i]);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private class TestsAdapter extends ArrayAdapter<String> {
        TestsAdapter() {
            super(requireContext(), 0, displayNames);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_test, parent, false);
            }

            String testName = displayNames.get(position);
            String testKey = testKeys.get(position);

            TextView tvTestName = convertView.findViewById(R.id.tvTestName);
            tvTestName.setText(testName);

            TextView tvTestCount = convertView.findViewById(R.id.tvTestCount);
            tvTestCount.setText("Проверка знаний");

            Button btnStart = convertView.findViewById(R.id.btnStart);
            btnStart.setOnClickListener(v -> checkCooldownAndStart(testKey));

            return convertView;
        }
    }

    // ПРОВЕРКА ТАЙМЕРА 10 МИНУТ
    private void checkCooldownAndStart(String category) {
        SharedPreferences prefs = requireContext().getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        long lastAttempt = prefs.getLong("last_attempt_" + category, 0);
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastAttempt;
        long cooldown = 10 * 60 * 1000; // 10 минут

        if (timePassed < cooldown) {
            long minutesLeft = (cooldown - timePassed) / 60000 + 1;
            Toast.makeText(requireContext(), "Попробуйте снова через " + minutesLeft + " мин.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(getContext(), QuizActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }
}