package com.example.kumpan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class KnowledgeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_knowledge, container, false);

        LinearLayout cardAlco = view.findViewById(R.id.cardAlco);
        LinearLayout cardDesserts = view.findViewById(R.id.cardDesserts);
        LinearLayout cardCoffee = view.findViewById(R.id.cardCoffee);
        LinearLayout cardPrep = view.findViewById(R.id.cardPrep);
        LinearLayout cardSeason = view.findViewById(R.id.cardSeason);
        LinearLayout cardGrain = view.findViewById(R.id.cardGrain);

        // АЛКО
        cardAlco.setOnClickListener(v -> openList("alco"));

        // ДЕСЕРТЫ
        cardDesserts.setOnClickListener(v -> openList("desserts"));

        // КОФЕ / ЧАЙ
        cardCoffee.setOnClickListener(v -> openList("coffee"));

        // ЗАГОТОВКИ
        cardPrep.setOnClickListener(v -> openList("prep"));

        // СЕЗОНКА — открывает экран выбора сезона
        cardSeason.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SeasonActivity.class);
            startActivity(intent);
        });

        // ЗЕРНО
        cardGrain.setOnClickListener(v -> openList("grain"));

        return view;
    }

    /**
     * Открывает экран списка ТТК для указанной категории
     */
    private void openList(String category) {
        Intent intent = new Intent(getContext(), TtkListActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }
}