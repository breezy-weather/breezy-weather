package org.breezyweather.common.utils.helpers;

import com.xhinliang.lunarcalendar.LunarCalendar;

import java.util.Calendar;
import java.util.Date;

/**
 * Lunar helper.
 * */

public class LunarHelper {

    public static String getLunarDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return getLunarDate(calendar);
    }

    private static String getLunarDate(Calendar calendar) {
        return getLunarDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private static String getLunarDate(int year, int month, int day) {
        try {
            LunarCalendar lunarCalendar = LunarCalendar.obtainCalendar(year, month, day);
            return lunarCalendar.getFullLunarStr().split("年")[1]
                    .replace("廿十", "二十")
                    .replace("卅十", "三十");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
