package com.milanbojovic.weather.data.model.extraction;

import com.milanbojovic.weather.util.Util;
import org.jsoup.nodes.Element;

import java.time.LocalDate;
import java.util.Optional;

public interface DataParser {

    default int parseHumidity(Element humidity) {
        String strHumidity = Optional.ofNullable(humidity.text()).orElse("0%");
        return Integer.parseInt(strHumidity.substring(0, strHumidity.indexOf("%")));
    }

    default int parseAirPressure(Element airPressure) {
        String strPressure = Optional.ofNullable(airPressure.text()).orElse("0.0 mbar");
        return Integer.parseInt(strPressure.substring(0, strPressure.indexOf(" mbar")));
    }

    default double parseWindSpeed(Element windSpeed) {
        String windSpeedStr = windSpeed.text().equals("-")? "0.0 m/s" : windSpeed.text();
        return Double.parseDouble(windSpeedStr.split(" ")[0]);
    }

    default double parseTemperature(Element dayMinTemp) {
        String strTemp = Optional.ofNullable(dayMinTemp.text()).orElse("0°");
        return Double.parseDouble(strTemp.substring(0, strTemp.indexOf("°")));
    }

    default String getDayFromDateString(String dateStr) {
        String[] dateSplit = dateStr.split("\\.");
        int day = Integer.parseInt(dateSplit[0]);
        int month = Integer.parseInt(dateSplit[1]);
        int year = Integer.parseInt(dateSplit[2]);

        String stringDate = LocalDate.of(year, month, day)
                .getDayOfWeek()
                .toString()
                .toLowerCase();
        return Util.translateDayToRsCyrilic.get(stringDate);
    }

    default String toDate(String dateStr) {
        String[] dateArr = dateStr.split("T")[0].split("-");
        int year = Integer.parseInt(dateArr[0]);
        int month = Integer.parseInt(dateArr[1]);
        int day = Integer.parseInt(dateArr[2]);
        return Util.formatDate(year, month, day);
    }
}
