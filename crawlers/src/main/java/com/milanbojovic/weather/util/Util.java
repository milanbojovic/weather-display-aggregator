package com.milanbojovic.weather.util;

import java.util.HashMap;
import java.util.Map;

public class Util {
    public static final String CITY_BEOGRAD = "beograd";
    public static final String CITY_NOVI_SAD = "novi sad";
    public static final String CITY_KRAGUJEVAC = "kragujevac";
    public static final String CITY_ZLATIBOR = "zlatibor";
    public static final String CITY_PRISTINA = "pristina";
    public static final String CITY_NIS = "nis";

    public static Map<String, String> translateDayToRsCyrilic;
    public static Map<String, Integer> monthNumberMap;
    public static Map<String, Integer> accuWeatherLocationIdMap;
    public static Map<String, Integer> rhmzLocationIdMap;

    static {
        translateDayToRsCyrilic = initDayTranslationCyrilic();
        monthNumberMap = initMonthNumberMap();
        accuWeatherLocationIdMap = initCityLocationIdMap();
        rhmzLocationIdMap = initCityLocationIdMapRhmz();
    }

    private Util() {}

    private static HashMap<String, String> initDayTranslationCyrilic() {
        HashMap<String, String> map = new HashMap<>();
        map.put("monday", "Понедељак");
        map.put("tuesday", "Уторак");
        map.put("wednesday", "Среда");
        map.put("thursday", "Четвртак");
        map.put("friday", "Петак");
        map.put("saturday", "Субота");
        map.put("sunday", "Недеља");
        return map;
    }

    private static HashMap<String, Integer> initMonthNumberMap() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("jan", 1);
        map.put("januar", 1);
        map.put("feb", 2);
        map.put("februar", 2);
        map.put("mar", 3);
        map.put("mart", 3);
        map.put("apr", 4);
        map.put("april", 4);
        map.put("maj", 5);
        map.put("jun", 6);
        map.put("jul", 7);
        map.put("avgust", 8);
        map.put("avg", 8);
        map.put("septembar", 9);
        map.put("sep", 9);
        map.put("oktobar", 10);
        map.put("okt", 10);
        map.put("novembar", 11);
        map.put("nov", 11);
        map.put("decembar", 12);
        map.put("dec", 12);
        return map;
    }

    private static HashMap<String, Integer> initCityLocationIdMap() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put(CITY_BEOGRAD, 298198);
        map.put(CITY_NOVI_SAD, 298486);
        map.put(CITY_KRAGUJEVAC, 301638);
        map.put(CITY_ZLATIBOR, 1691350);
        map.put(CITY_PRISTINA, 1672284);
        map.put(CITY_NIS, 299758);
        return map;
    }

    private static HashMap<String, Integer> initCityLocationIdMapRhmz() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put(CITY_BEOGRAD, 13274);
        map.put(CITY_NOVI_SAD, 13168);
        map.put(CITY_KRAGUJEVAC, 13278);
        map.put(CITY_ZLATIBOR, 13367);
        map.put(CITY_PRISTINA, 13481);
        map.put(CITY_NIS, 13388);
        return map;
    }
}
