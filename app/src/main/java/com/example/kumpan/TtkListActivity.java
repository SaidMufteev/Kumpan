package com.example.kumpan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TtkListActivity extends AppCompatActivity {

    private String category;
    private String collectionName;
    private String userDepartment = "";
    private List<DocumentSnapshot> docs = new ArrayList<>();
    private List<String> names = new ArrayList<>();
    private ItemsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttk_list);

        category = getIntent().getStringExtra("category");
        userDepartment = getIntent().getStringExtra("department");
        if (category == null) category = "alco";

        if (category.equals("alco")) collectionName = "cocktails";
        else if (category.equals("coffee")) collectionName = "coffees";
        else if (category.equals("prep")) collectionName = "prep";
        else if (category.equals("desserts")) collectionName = "desserts";
        else if (category.equals("grain")) collectionName = "grain";
        else if (category.equals("dishes")) collectionName = "dishes";
        else if (category.equals("wines")) collectionName = "wines";
        else if (category.startsWith("season_")) collectionName = "season";
        else collectionName = "cocktails";

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tvListTitle);
        tvTitle.setText(getTitleForCategory(category));

        ListView listItems = findViewById(R.id.listItems);
        adapter = new ItemsAdapter();
        listItems.setAdapter(adapter);

        listItems.setOnItemClickListener((parent, view, position, id) -> {
            DocumentSnapshot clickedDoc = docs.get(position);
            Intent intent;

            // Для десертов И блюд открываем DessertDetailActivity (там есть аллергены и описание)
            if (category.equals("desserts") || category.equals("dishes")) {
                intent = new Intent(TtkListActivity.this, DessertDetailActivity.class);
            } else if (category.equals("grain")) {
                intent = new Intent(TtkListActivity.this, GrainDetailActivity.class);
            } else {
                intent = new Intent(TtkListActivity.this, CocktailDetailActivity.class);
            }

            intent.putExtra("collectionName", collectionName);
            intent.putExtra("docId", clickedDoc.getId());
            intent.putExtra("category", category);
            startActivity(intent);
        });

        loadData();
    }

    private void loadData() {
        FirebaseHelper.loadTtkCollection(collectionName)
                .addOnSuccessListener(querySnapshot -> {
                    docs.clear();
                    names.clear();

                    List<DocumentSnapshot> allSeasonDocs = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String name = doc.getString("name");
                        if (name == null) name = "Без названия";

                        // ФИЛЬТРАЦИЯ ПО ОТДЕЛУ (для заготовок и сезонки)
                        if (category.equals("prep") || category.startsWith("season_")) {
                            String docDept = doc.getString("department");
                            // Если у документа есть отдел, и он не совпадает с отделом пользователя — пропускаем
                            if (docDept != null && userDepartment != null
                                    && !userDepartment.isEmpty() && !docDept.equals(userDepartment)) {
                                continue;
                            }
                        }

                        if (category.startsWith("season_")) {
                            allSeasonDocs.add(doc);
                            String season = doc.getString("season");
                            String targetSeason = category.replace("season_", "");

                            boolean matches = false;
                            if (season != null) matches = season.toLowerCase().contains(targetSeason);

                            if (matches) {
                                docs.add(doc);
                                names.add(name);
                            }
                        } else {
                            docs.add(doc);
                            names.add(name);
                        }
                    }

                    if (category.startsWith("season_") && docs.isEmpty() && !allSeasonDocs.isEmpty()) {
                        Toast.makeText(this, "Внимание: в базе нет поля 'season'.", Toast.LENGTH_LONG).show();
                        for (DocumentSnapshot doc : allSeasonDocs) {
                            docs.add(doc);
                            names.add(doc.getString("name"));
                        }
                    }

                    adapter.notifyDataSetChanged();
                    if (names.isEmpty()) Toast.makeText(this, "В коллекции " + collectionName + " нет данных", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_LONG).show());
    }

    private String getTitleForCategory(String category) {
        switch (category) {
            case "alco": return "ТТК алко";
            case "coffee": return "ТТК кофе/чай";
            case "prep": return "ТТК заготовки";
            case "desserts": return "ТТК десерты";
            case "grain": return "ТТК зерно";
            case "dishes": return "ТТК Блюда";
            case "wines": return "Вина (Карта бара)";
            case "season_summer": return "Сезонка — Лето";
            case "season_autumn": return "Сезонка — Осень";
            case "season_winter": return "Сезонка — Зима";
            case "season_spring": return "Сезонка — Весна";
            default: return "ТТК";
        }
    }

    private class ItemsAdapter extends ArrayAdapter<String> {
        ItemsAdapter() {
            super(TtkListActivity.this, 0, names);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(TtkListActivity.this)
                        .inflate(R.layout.item_ttk, parent, false);
            }
            TextView tvName = convertView.findViewById(R.id.tvItemName);
            tvName.setText(names.get(position));
            return convertView;
        }
    }
}