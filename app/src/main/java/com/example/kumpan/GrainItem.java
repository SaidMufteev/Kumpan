package com.example.kumpan;

public class GrainItem {

    public String name;             // Название ("Эфиопия", "Лайт")
    public String composition;      // Состав для блендов ("Перу 40%..."), у сортов = null
    public String processing;       // Обработка ("мытая", "натуральная")
    public String body;             // Тело ("среднее", "плотное")
    public String acidity;          // Кислотность ("средняя", "выше среднего")
    public String sweetness;        // Сладость ("средняя", "выше среднего")
    public String bitterness;       // Горечь ("средняя", "выше среднего")
    public String aftertaste;       // Послевкусие ("продолжительное, приятное")
    public String bouquet;          // Букет ("Цитрус, чёрный чай...")
    public String brewMethods;      // Способы заваривания

    public GrainItem(String name, String composition, String processing,
                     String body, String acidity, String sweetness,
                     String bitterness, String aftertaste,
                     String bouquet, String brewMethods) {
        this.name = name;
        this.composition = composition;
        this.processing = processing;
        this.body = body;
        this.acidity = acidity;
        this.sweetness = sweetness;
        this.bitterness = bitterness;
        this.aftertaste = aftertaste;
        this.bouquet = bouquet;
        this.brewMethods = brewMethods;
    }
}