package com.example.kumpan;

import java.util.ArrayList;

public class DessertItem {

    public String name;                // Название десерта
    public ArrayList<String> ingredients;  // Состав
    public String storage;             // Срок хранения
    public String allergens;           // Аллергены
    public String guestDescription;    // Что говорит официант гостю

    public DessertItem(String name, ArrayList<String> ingredients,
                       String storage, String allergens, String guestDescription) {
        this.name = name;
        this.ingredients = ingredients;
        this.storage = storage;
        this.allergens = allergens;
        this.guestDescription = guestDescription;
    }
}