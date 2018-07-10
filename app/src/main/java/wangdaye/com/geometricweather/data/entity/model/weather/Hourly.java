package wangdaye.com.geometricweather.data.entity.model.weather;

import android.content.Context;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuHourlyResult;
import wangdaye.com.geometricweather.data.entity.result.cn.CNWeatherResult;
import wangdaye.com.geometricweather.data.entity.table.weather.HourlyEntity;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

/**
 * Hourly.
 * */

public class Hourly {

    public String time;
    public boolean dayTime;
    public String weather;
    public String weatherKind;
    public int temp;
    public int precipitation;

    public Hourly() {}

/*
    Hourly buildHourly(Calendar c, Daily daily, FWResult.WeatherDetailsInfo.Weather24HoursDetailsInfos info) {
        time = info
                .startTime.split(" ")[1].split(":")[0] + "时";
        int hour = c.get(Calendar.HOUR_OF_DAY);
        dayTime = (Integer.parseInt(daily.astros[0].split(":")[0]) <= hour)
                || (hour <= Integer.parseInt(daily.astros[1].split(":")[0]));
        weather = info.weather;
        weatherKind = WeatherHelper.getFWeatherKind(weather);
        temp = Integer.parseInt(info.highestTemperature);
        precipitation = WeatherHelper.getPrecipitation(Integer.parseInt(info.precipitation));
        return this;
    }
    
    Hourly buildHourly(Calendar c, Daily daily, HefengResult.HeWeather.HourlyForecast hourly) {
        if (Integer.parseInt(hourly.pop) > 5) {
            if (Integer.parseInt(hourly.tmp) > 1) {
                weather = "Rain";
                weatherKind = WeatherHelper.getHefengWeatherKind("300");
            } else if (Integer.parseInt(hourly.tmp) > -2) {
                weather = "Sleet";
                weatherKind = WeatherHelper.getHefengWeatherKind("313");
            } else {
                weather = "Snow";
                weatherKind = WeatherHelper.getHefengWeatherKind("400");
            }
        } else {
            int hour = c.get(Calendar.HOUR_OF_DAY);
            if ((Integer.parseInt(daily.astros[0].split(":")[0]) <= hour)
                    || (hour <= Integer.parseInt(daily.astros[1].split(":")[0]))) {
                dayTime = true;
                weather = daily.weathers[0];
                weatherKind = WeatherHelper.getHefengWeatherKind(daily.weatherKinds[0]);
            } else {
                dayTime = false;
                weather = daily.weathers[1];
                weatherKind = WeatherHelper.getHefengWeatherKind(daily.weatherKinds[1]);
            }
        }
        time = hourly.date.split(" ")[1].split(":")[0];
        temp = Integer.parseInt(hourly.tmp);
        precipitation = Integer.parseInt(hourly.pop);
        return this;
    }
*/

    public Hourly buildHourly(Context c, AccuHourlyResult result) {
        time = buildTime(c, result.DateTime.split("T")[1].split(":")[0]);
        dayTime = result.IsDaylight;
        weather = result.IconPhrase;
        weatherKind = WeatherHelper.getNewWeatherKind(result.WeatherIcon);
        temp = (int) result.Temperature.Value;
        precipitation = result.PrecipitationProbability;
        return this;
    }

    public Hourly buildHourly(Context c,
                              CNWeatherResult.WeatherX today, CNWeatherResult.HourlyForecast hourly) {
        time = buildTime(c, hourly.hour);
        dayTime = TimeManager.isDaylight(hourly.hour, today.info.day.get(5), today.info.night.get(5));
        weather = hourly.info;
        weatherKind = WeatherHelper.getNewWeatherKind(hourly.img);
        temp = Integer.parseInt(hourly.temperature);
        precipitation = -1;
        return this;
    }

    Hourly buildHourly(HourlyEntity entity) {
        time = entity.time;
        dayTime = entity.dayTime;
        weather = entity.weather;
        weatherKind = entity.weatherKind;
        temp = entity.temp;
        precipitation = entity.precipitation;
        return this;
    }

    private String buildTime(Context c, String hourString) {
        if (TimeManager.is12Hour(c)) {
            try {
                int hour = Integer.parseInt(hourString);
                if (hour == 0) {
                    hour = 24;
                }
                if (hour > 12) {
                    hour -= 12;
                }
                return hour + c.getString(R.string.of_clock);
            } catch (Exception ignored) {
                // do nothing.
            }
        }
        return hourString + c.getString(R.string.of_clock);
    }
}
