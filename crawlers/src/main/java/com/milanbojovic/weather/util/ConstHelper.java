package com.milanbojovic.weather.util;

public class ConstHelper {
    private ConstHelper() {}

    //Rhmz
    public static final String RHMZ_URI_UV_INDEX = "/prognoza/uv1.php";
    public static final String RHMZ_TAG_TBODY = "tbody";
    public static final String RHMZ_TAG_THEAD = "thead";
    public static final String RHMZ_URI_PATH = "/ciril";
    public static final String RHMZ_CURRENT_WEATHER_ALL_CITIES_TABLE = "Подаци са метеоролошких станица - Србија";
    public static final String RHMZ_FIVE_DAY_FORECAST_ALL_CITIES_TABLE = "5 day forecast for Beograd";

    //AccuWeather
    public static final String ACCU_WEATHER_API_IMAGES_LOCATION = "/sites/default/files/%s-s.png";
    public static final String ACCU_WEATHER_QUERY_PARAM_API_KEY = "apikey";
    public static final String ACCU_WEATHER_QUERY_PARAM_DETAILS_NEEDED = "details";
    public static final String ACCU_WEATHER_QUERY_PARAM_METRIC = "metric";
    public static final String ACCU_WEATHER_QUERY_PARAM_LANGUAGE = "language";

    //Weather2Umbrella
    public static final String W2U_SUMMARY = "summary";
    public static final String W2U_CITY = "/vremenska-prognoza-%s-srbija-sr";
}
