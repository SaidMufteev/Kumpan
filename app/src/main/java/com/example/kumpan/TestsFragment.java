package com.example.kumpan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class TestsFragment extends Fragment {

    private static final String[] TEST_NAMES = {
            "Алко",
            "Десерты",
            "Кофе/Чай",
            "Заготовки",
            "Сезонка",
            "Зерно"
    };

    private static final String[] TEST_KEYS = {
            "alco",
            "desserts",
            "coffee",
            "prep",
            "season",
            "grain"
    };

    private static final int QUESTIONS_COUNT = 9;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tests, container, false);

        ListView listTests = view.findViewById(R.id.listTests);

        ArrayList<String> names = new ArrayList<>();
        for (String name : TEST_NAMES) {
            names.add(name);
        }

        TestsAdapter adapter = new TestsAdapter(names);
        listTests.setAdapter(adapter);

        return view;
    }

    private class TestsAdapter extends ArrayAdapter<String> {

        TestsAdapter(ArrayList<String> names) {
            super(requireContext(), 0, names);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_test, parent, false);
            }

            String testName = getItem(position);
            String testKey = TEST_KEYS[position];

            TextView tvTestName = convertView.findViewById(R.id.tvTestName);
            tvTestName.setText(testName);

            TextView tvTestCount = convertView.findViewById(R.id.tvTestCount);
            tvTestCount.setText(QUESTIONS_COUNT + " вопросов");

            Button btnStart = convertView.findViewById(R.id.btnStart);
            btnStart.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), QuizActivity.class);
                intent.putExtra("category", testKey);
                startActivity(intent);
            });

            return convertView;
        }
    }
}