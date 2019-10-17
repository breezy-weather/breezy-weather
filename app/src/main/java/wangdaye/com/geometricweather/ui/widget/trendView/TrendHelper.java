package wangdaye.com.geometricweather.ui.widget.trendView;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import wangdaye.com.geometricweather.basic.model.weather.Weather;

/**
 * Trend helper.
 * */

public class TrendHelper {

    public static @Size(2) int[] getHighestAndLowestDailyTemperature(@NonNull Weather weather) {
        int highest = Integer.MIN_VALUE;
        int lowest = Integer.MAX_VALUE;
        if (weather.getYesterday() != null) {
            highest = weather.getYesterday().getDaytimeTemperature();
            lowest = weather.getYesterday().getNighttimeTemperature();
        }
        for (int i = 0; i < weather.getDailyForecast().size(); i ++) {
            if (weather.getDailyForecast().get(i).day().getTemperature().getTemperature() > highest) {
                highest = weather.getDailyForecast().get(i).day().getTemperature().getTemperature();
            }
            if (weather.getDailyForecast().get(i).night().getTemperature().getTemperature() < lowest) {
                lowest = weather.getDailyForecast().get(i).night().getTemperature().getTemperature();
            }
        }
        return new int[] {highest, lowest};
    }

    public static @Size(2) int[] getHighestAndLowestHourlyTemperature(@NonNull Weather weather) {
        int highest = Integer.MIN_VALUE;
        int lowest = Integer.MAX_VALUE;
        if (weather.getYesterday() != null) {
            highest = weather.getYesterday().getDaytimeTemperature();
            lowest = weather.getYesterday().getNighttimeTemperature();
        }
        for (int i = 0; i < weather.getHourlyForecast().size(); i ++) {
            if (weather.getHourlyForecast().get(i).getTemperature().getTemperature() > highest) {
                highest = weather.getHourlyForecast().get(i).getTemperature().getTemperature();
            }
            if (weather.getHourlyForecast().get(i).getTemperature().getTemperature() < lowest) {
                lowest = weather.getHourlyForecast().get(i).getTemperature().getTemperature();
            }
        }
        return new int[] {highest, lowest};
    }
}
