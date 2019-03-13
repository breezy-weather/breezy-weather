package wangdaye.com.geometricweather.basic.model.weather;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Daily.
 * */

public class Daily {

    public String date;
    public String week;
    public String[] weathers;
    public String[] weatherKinds;
    public int[] temps;
    public String[] windDirs;
    public String[] windSpeeds;
    public String[] windLevels;
    public int[] windDegrees;
    public String[] astros;
    public String moonPhase;
    public int[] precipitations;

    public Daily(String date, String week,
                 String[] weathers, String[] weatherKinds, int[] temps,
                 String[] windDirs, String[] windSpeeds, String[] windLevels, int[] windDegrees,
                 String[] astros, String moonPhase,
                 int[] precipitations) {
        this.date = date;
        this.week = week;
        this.weathers = weathers;
        this.weatherKinds = weatherKinds;
        this.temps = temps;
        this.windDirs = windDirs;
        this.windSpeeds = windSpeeds;
        this.windLevels = windLevels;
        this.windDegrees = windDegrees;
        this.astros = astros;
        this.moonPhase = moonPhase;
        this.precipitations = precipitations;
    }

    @SuppressLint("SimpleDateFormat")
    public String getDateInFormat(String format) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(date.split("-")[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(date.split("-")[1]) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.split("-")[2]));
        return new SimpleDateFormat(format).format(calendar.getTime());
    }
}
