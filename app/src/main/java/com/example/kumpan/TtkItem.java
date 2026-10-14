package com.example.kumpan;

import java.util.ArrayList;

public class TtkItem {

    public String name;           // Название коктейля
    public ArrayList<String> ingredients;  // Состав (список ингредиентов)
    public String note;           // Примечание (способ приготовления)

    public TtkItem(String name, ArrayList<String> ingredients, String note) {
        this.name = name;
        this.ingredients = ingredients;
        this.note = note;
    }
}