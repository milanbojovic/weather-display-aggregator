package com.milanbojovic.weather.util;

import java.util.HashMap;
import java.util.Map;

public class Util {
    public static Map<String, String> translateDayToCyrilic;
    public static Map<String, String> translateCityNameToCyrilic;
    public static Map<String, Integer> monthNumberMap;
    public static Map<String, Integer> locationIdMap;

    static {
        translateCityNameToCyrilic = initDayTranslationCyrilic();
        translateDayToCyrilic = initCityTranslationCyrilic();
        monthNumberMap = initMonthNumberMap();
        locationIdMap = initCityLocationIdMap();
    }

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

    private static HashMap<String, String> initCityTranslationCyrilic() {
        HashMap<String, String> map = new HashMap<>();
        map.put("beograd", "Београд");
        map.put("novi sad", "Нови Сад");
        map.put("kragujevac", "Крагујевац");
        map.put("zlatibor", "Златибор");
        map.put("pristina", "Приштина");
        map.put("nis", "Ниш");
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
        map.put("maj", 5);
        map.put("jun", 6);
        map.put("jun", 6);
        map.put("jul", 7);
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
        map.put("beograd", 298198);
        map.put("novi sad", 298486);
        map.put("kragujevac", 301638);
        map.put("zlatibor", 1691350);
        map.put("pristina", 1672284);
        map.put("nis", 299758);
        return map;
    }
}
