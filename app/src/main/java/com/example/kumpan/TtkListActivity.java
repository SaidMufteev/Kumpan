package com.example.kumpan;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class TtkListActivity extends AppCompatActivity {

    private String category;
    private String collectionName;
    private String userDepartment = "";
    private String currentRole = "";
    private List<DocumentSnapshot> docs = new ArrayList<>();
    private List<String> names = new ArrayList<>();
    private ItemsAdapter adapter;
    private Button btnAddTtk;

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

        SharedPreferences prefs = getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        currentRole = prefs.getString("user_role", "");

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tvListTitle);
        tvTitle.setText(getTitleForCategory(category));

        btnAddTtk = findViewById(R.id.btnAddTtk);
        if ("manager".equals(currentRole) || "chef".equals(currentRole)) {
            btnAddTtk.setVisibility(View.VISIBLE);
        }
        btnAddTtk.setOnClickListener(v -> showAddTtkDialog());

        ListView listItems = findViewById(R.id.listItems);
        adapter = new ItemsAdapter();
        listItems.setAdapter(adapter);

        listItems.setOnItemClickListener((parent, view, position, id) -> {
            DocumentSnapshot clickedDoc = docs.get(position);
            Intent intent;

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

                        if (category.equals("prep") || category.startsWith("season_")) {
                            String docDept = doc.getString("department");
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

    // === ДОБАВЛЕНИЕ НОВОГО ТТК ===
    private void showAddTtkDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        EditText etName = new EditText(this);
        etName.setHint("Название (например: Мохито 300 мл)");
        layout.addView(etName);

        EditText etIngredients = new EditText(this);
        etIngredients.setHint("Ингредиенты через запятую\n(Ром - 50мл, Лайм - 10гр)");
        etIngredients.setMinLines(2);
        etIngredients.setGravity(android.view.Gravity.TOP);
        layout.addView(etIngredients);

        EditText etTech = new EditText(this);
        etTech.setHint("Технология / Описание");
        layout.addView(etTech);

        new AlertDialog.Builder(this)
                .setTitle("Добавить в " + getTitleForCategory(category))
                .setView(layout)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String ingStr = etIngredients.getText().toString().trim();
                    String tech = etTech.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> ingredientsList = new ArrayList<>();
                    if (!ingStr.isEmpty()) {
                        for (String s : ingStr.split(",")) {
                            ingredientsList.add(s.trim());
                        }
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("name", name);
                    data.put("ingredients", ingredientsList);
                    data.put("technology", tech);
                    data.put("department", userDepartment); // Бар или Кухня

                    // Если это десерт или блюдо, добавим пустые поля для карточки
                    if (category.equals("desserts") || category.equals("dishes")) {
                        data.put("guestDescription", tech);
                        data.put("allergens", "—");
                        data.put("storage", "—");
                        data.put("freezing", false);
                    }
                    if (category.startsWith("season_")) {
                        data.put("season", category.replace("season_", ""));
                    }

                    FirebaseHelper.addTtkItem(collectionName, data)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Успешно добавлено!", Toast.LENGTH_SHORT).show();
                                loadData();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private String getTitleForCategory(String category) {
        switch (category) {
            case "alco": return "ТТК алко";
            case "coffee": return "ТТК кофе/чай";
            case "prep": return "ТТК заготовки";
            case "desserts": return "ТТК десерты";
            case "grain": return "ТТК зерно";
            case "dishes": return "ТТК Блюда";
            case "wines": return "Вина";
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