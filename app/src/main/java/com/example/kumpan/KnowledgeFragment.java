package com.example.kumpan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import static android.content.Context.MODE_PRIVATE;

public class KnowledgeFragment extends Fragment {

    private String currentRole;
    private String userId;
    private String currentDepartment = "bar"; // По умолчанию бар

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_knowledge, container, false);

        SharedPreferences prefs = requireContext().getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        currentRole = prefs.getString("user_role", "guest");
        userId = prefs.getString("user_login", "");

        // Определяем отдел пользователя
        if ("cook".equals(currentRole) || "chef".equals(currentRole)) {
            currentDepartment = "kitchen";
        } else {
            currentDepartment = "bar";
        }

        LinearLayout row1 = view.findViewById(R.id.row1);
        LinearLayout row2 = view.findViewById(R.id.row2);
        LinearLayout row3 = view.findViewById(R.id.row3);
        LinearLayout row4 = view.findViewById(R.id.row4);

        LinearLayout cellAlco = view.findViewById(R.id.cellAlco);
        LinearLayout cellDesserts = view.findViewById(R.id.cellDesserts);
        LinearLayout cellCoffee = view.findViewById(R.id.cellCoffee);
        LinearLayout cellPrep = view.findViewById(R.id.cellPrep);
        LinearLayout cellSeason = view.findViewById(R.id.cellSeason);
        LinearLayout cellGrain = view.findViewById(R.id.cellGrain);
        LinearLayout cellDishes = view.findViewById(R.id.cellDishes);
        LinearLayout cellWines = view.findViewById(R.id.cellWines);

        cellAlco.setVisibility(View.GONE); cellDesserts.setVisibility(View.GONE);
        cellCoffee.setVisibility(View.GONE); cellPrep.setVisibility(View.GONE);
        cellSeason.setVisibility(View.GONE); cellGrain.setVisibility(View.GONE);
        cellDishes.setVisibility(View.GONE); cellWines.setVisibility(View.GONE);

        // ВКЛЮЧАЕМ ТОЛЬКО ТО, ЧТО РАЗРЕШЕНО РОЛЬЮ
        switch (currentRole) {
            case "manager":
                cellAlco.setVisibility(View.VISIBLE); cellDesserts.setVisibility(View.VISIBLE);
                cellCoffee.setVisibility(View.VISIBLE); cellPrep.setVisibility(View.VISIBLE);
                cellSeason.setVisibility(View.VISIBLE); cellGrain.setVisibility(View.VISIBLE);
                cellDishes.setVisibility(View.VISIBLE); cellWines.setVisibility(View.VISIBLE);
                break;
            case "bartender":
                cellAlco.setVisibility(View.VISIBLE); cellDesserts.setVisibility(View.VISIBLE);
                cellCoffee.setVisibility(View.VISIBLE); cellPrep.setVisibility(View.VISIBLE);
                cellSeason.setVisibility(View.VISIBLE); cellGrain.setVisibility(View.VISIBLE);
                break;
            case "waiter":
                cellDesserts.setVisibility(View.VISIBLE); cellSeason.setVisibility(View.VISIBLE);
                cellWines.setVisibility(View.VISIBLE);
                cellDishes.setVisibility(View.VISIBLE); // ОФИЦИАНТЫ ВИДЯТ БЛЮДА
                break;
            case "cook":
            case "chef":
                cellPrep.setVisibility(View.VISIBLE); cellSeason.setVisibility(View.VISIBLE);
                cellDishes.setVisibility(View.VISIBLE);
                break;
        }

        // СКРЫВАЕМ ПУСТЫЕ РЯДЫ
        if (cellAlco.getVisibility() == View.GONE && cellDesserts.getVisibility() == View.GONE) row1.setVisibility(View.GONE);
        if (cellCoffee.getVisibility() == View.GONE && cellPrep.getVisibility() == View.GONE) row2.setVisibility(View.GONE);
        if (cellSeason.getVisibility() == View.GONE && cellGrain.getVisibility() == View.GONE) row3.setVisibility(View.GONE);
        if (cellDishes.getVisibility() == View.GONE && cellWines.getVisibility() == View.GONE) row4.setVisibility(View.GONE);

        // КЛИКИ
        view.findViewById(R.id.cardAlco).setOnClickListener(v -> openList("alco", "cocktails"));
        view.findViewById(R.id.cardDesserts).setOnClickListener(v -> openList("desserts", "desserts"));
        view.findViewById(R.id.cardCoffee).setOnClickListener(v -> openList("coffee", "coffees"));
        view.findViewById(R.id.cardPrep).setOnClickListener(v -> openList("prep", "prep"));
        view.findViewById(R.id.cardGrain).setOnClickListener(v -> openList("grain", "grain"));
        view.findViewById(R.id.cardDishes).setOnClickListener(v -> openList("dishes", "dishes"));
        view.findViewById(R.id.cardWines).setOnClickListener(v -> openList("wines", "wines"));

        view.findViewById(R.id.cardSeason).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SeasonActivity.class);
            startActivity(intent);
        });

        // ПРОГРЕСС-БАРЫ
        updateProgressBar(view, R.id.pbAlco, "alco", "cocktails");
        updateProgressBar(view, R.id.pbDesserts, "desserts", "desserts");
        updateProgressBar(view, R.id.pbCoffee, "coffee", "coffees");
        updateProgressBar(view, R.id.pbPrep, "prep", "prep");
        updateProgressBar(view, R.id.pbGrain, "grain", "grain");
        updateProgressBar(view, R.id.pbDishes, "dishes", "dishes");
        updateProgressBar(view, R.id.pbWines, "wines", "wines");

        ProgressBar pbSeason = view.findViewById(R.id.pbSeason);
        pbSeason.setProgress(0);

        return view;
    }

    private void openList(String category, String collectionName) {
        Intent intent = new Intent(getContext(), TtkListActivity.class);
        intent.putExtra("category", category);
        intent.putExtra("department", currentDepartment); // Передаем отдел!
        startActivity(intent);
    }

    private void updateProgressBar(View rootView, int progressBarId, String category, String collectionName) {
        ProgressBar pb = rootView.findViewById(progressBarId);

        FirebaseHelper.loadTtkCollection(collectionName)
                .addOnSuccessListener(totalSnapshot -> {
                    int totalItems = totalSnapshot.size();
                    if (totalItems == 0) {
                        pb.setProgress(0);
                        return;
                    }

                    FirebaseHelper.getReadCount(userId, category)
                            .addOnSuccessListener(readSnapshot -> {
                                int readItems = readSnapshot.size();
                                int percent = (int) (((float) readItems / totalItems) * 100);
                                pb.setProgress(percent);
                            });
                });
    }
}