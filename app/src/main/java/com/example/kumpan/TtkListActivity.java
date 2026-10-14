package com.example.kumpan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class TtkListActivity extends AppCompatActivity {

    private String category;
    private ArrayList<TtkItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttk_list);

        category = getIntent().getStringExtra("category");
        if (category == null) category = "alco";

        // Загружаем список имён
        ArrayList<String> itemNames = new ArrayList<>();

        if ("desserts".equals(category)) {
            for (DessertItem d : DessertData.getItems()) {
                itemNames.add(d.name);
            }
        } else if ("grain".equals(category)) {
            for (GrainItem g : GrainData.getItems()) {
                itemNames.add(g.name);
            }
        } else {
            items = TtkData.getItems(category);
            for (TtkItem t : items) {
                itemNames.add(t.name);
            }
        }

        // Кнопка "Назад"
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Заголовок
        TextView tvTitle = findViewById(R.id.tvListTitle);
        tvTitle.setText(getTitleForCategory(category));

        // Список
        ListView listItems = findViewById(R.id.listItems);
        ItemsAdapter adapter = new ItemsAdapter(itemNames);
        listItems.setAdapter(adapter);

        // Клик — открыть карточку
        listItems.setOnItemClickListener((parent, view, position, id) -> {
            if ("desserts".equals(category)) {
                Intent intent = new Intent(TtkListActivity.this, DessertDetailActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            } else if ("grain".equals(category)) {
                Intent intent = new Intent(TtkListActivity.this, GrainDetailActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            } else {
                Intent intent = new Intent(TtkListActivity.this, CocktailDetailActivity.class);
                intent.putExtra("category", category);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
    }

    private String getTitleForCategory(String category) {
        switch (category) {
            case "alco":           return "ТТК алко";
            case "coffee":         return "ТТК кофе/чай";
            case "prep":           return "ТТК заготовки";
            case "desserts":       return "ТТК десерты";
            case "grain":          return "ТТК зерно";
            case "season_summer":  return "Сезонка — Лето";
            case "season_autumn":  return "Сезонка — Осень";
            case "season_winter":  return "Сезонка — Зима";
            case "season_spring":  return "Сезонка — Весна";
            default:               return "ТТК";
        }
    }

    private class ItemsAdapter extends ArrayAdapter<String> {
        ItemsAdapter(ArrayList<String> names) {
            super(TtkListActivity.this, 0, names);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(TtkListActivity.this)
                        .inflate(R.layout.item_ttk, parent, false);
            }

            String name = getItem(position);

            TextView tvName = convertView.findViewById(R.id.tvItemName);
            tvName.setText(name);

            return convertView;
        }
    }
}