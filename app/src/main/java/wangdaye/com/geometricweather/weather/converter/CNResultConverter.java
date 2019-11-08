package wangdaye.com.geometricweather.weather.converter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.weather.AirQuality;
import wangdaye.com.geometricweather.basic.model.weather.Alert;
import wangdaye.com.geometricweather.basic.model.weather.Astro;
import wangdaye.com.geometricweather.basic.model.weather.Base;
import wangdaye.com.geometricweather.basic.model.weather.Current;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.HalfDay;
import wangdaye.com.geometricweather.basic.model.weather.History;
import wangdaye.com.geometricweather.basic.model.weather.Hourly;
import wangdaye.com.geometricweather.basic.model.weather.MoonPhase;
import wangdaye.com.geometricweather.basic.model.weather.Pollen;
import wangdaye.com.geometricweather.basic.model.weather.Precipitation;
import wangdaye.com.geometricweather.basic.model.weather.PrecipitationDuration;
import wangdaye.com.geometricweather.basic.model.weather.PrecipitationProbability;
import wangdaye.com.geometricweather.basic.model.weather.Temperature;
import wangdaye.com.geometricweather.basic.model.weather.UV;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.basic.model.weather.WeatherCode;
import wangdaye.com.geometricweather.basic.model.weather.Wind;
import wangdaye.com.geometricweather.basic.model.weather.WindDegree;
import wangdaye.com.geometricweather.weather.json.cn.CNWeatherResult;

public class CNResultConverter {

    public static Weather convert(Context context,
                                  @NonNull Location location, @Nullable CNWeatherResult result) {
        if (result == null) {
            return null;
        }

        try {
            long publishTime = (Long.parseLong(result.realtime.dataUptime) - 24 * 60 * 60) * 1000;

            History yesterday = new History(
                    new Date(publishTime),
                    publishTime,
                    Integer.parseInt(result.weather.get(0).info.day.get(2)),
                    Integer.parseInt(result.weather.get(0).info.night.get(2))
            );
            result.weather.remove(0);

            return new Weather(
                    new Base(
                            location.getCityId(),
                            System.currentTimeMillis(),
                            new Date(publishTime),
                            publishTime,
                            new Date(),
                            System.currentTimeMillis()
                    ),
                    new Current(
                            result.realtime.weather.info,
                            getWeatherCode(result.realtime.weather.img),
                            new Temperature(
                                    Integer.parseInt(result.realtime.weather.temperature),
                                    Integer.parseInt(result.realtime.feelslike_c),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new Precipitation(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new PrecipitationProbability(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new Wind(
                                    result.realtime.wind.direct,
                                    new WindDegree(getWindDegree(result.realtime.wind.direct), false),
                                    Float.parseFloat(result.realtime.wind.windspeed),
                                    result.realtime.wind.power
                            ),
                            new UV(
                                   null,
                                   result.life.info.ziwaixian.get(0),
                                   result.life.info.ziwaixian.get(1)
                            ),
                            new AirQuality(
                                    CommonConverter.getAqiQuality(context, result.pm25.aqi),
                                    result.pm25.aqi,
                                    (float) result.pm25.pm25,
                                    (float) result.pm25.pm10,
                                    (float) result.pm25.so2,
                                    (float) result.pm25.no2,
                                    (float) result.pm25.o3,
                                    getCO(result)
                            ),
                            Float.parseFloat(result.realtime.weather.humidity),
                            Float.parseFloat(result.realtime.pressure),
                            null,
                            null,
                            null,
                            null,
                            null,
                            result.life.info.daisan.get(1)
                    ),
                    yesterday,
                    getDailyList(context, result.weather),
                    getHourlyList(
                            publishTime,
                            getDate(result.weather.get(0).info.day.get(5)),
                            getDate(result.weather.get(0).info.night.get(5)),
                            result.hourly_forecast
                    ),
                    new ArrayList<>(),
                    getAlertList(result.alert)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressLint("SimpleDateFormat")
    private static List<Daily> getDailyList(Context context, List<CNWeatherResult.WeatherX> weatherXList)
            throws Exception {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<Daily> dailyList = new ArrayList<>(weatherXList.size());
        for (CNWeatherResult.WeatherX x : weatherXList) {
            Date date = format.parse(x.date);
            if (date == null) {
                throw new NullPointerException("Cannot get date object.");
            }
            dailyList.add(
                    new Daily(
                            date,
                            date.getTime(),
                            new HalfDay(
                                    x.info.day.get(1),
                                    x.info.day.get(1),
                                    getWeatherCode(x.info.day.get(0)),
                                    new Temperature(
                                            Integer.parseInt(x.info.day.get(2)),
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new Precipitation(
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new PrecipitationProbability(
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new PrecipitationDuration(
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new Wind(
                                            x.info.day.get(3),
                                            new WindDegree(getWindDegree(x.info.day.get(3)), false),
                                            null,
                                            x.info.day.get(4)
                                    ),
                                    null
                            ),
                            new HalfDay(
                                    x.info.night.get(1),
                                    x.info.night.get(1),
                                    getWeatherCode(x.info.night.get(0)),
                                    new Temperature(
                                            Integer.parseInt(x.info.night.get(2)),
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new Precipitation(
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new PrecipitationProbability(
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new PrecipitationDuration(
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new Wind(
                                            x.info.night.get(3),
                                            new WindDegree(getWindDegree(x.info.night.get(3)), false),
                                            null,
                                            x.info.night.get(4)
                                    ),
                                    null
                            ),
                            new Astro(
                                    getDate(x.info.day.get(5)),
                                    getDate(x.info.night.get(5))
                            ),
                            new Astro(null, null),
                            new MoonPhase(null, null),
                            getDailyAirQuality(context, x.aqi),
                            new Pollen(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new UV(null, null, null),
                            getHoursOfDay(
                                    getDate(x.info.day.get(5)),
                                    getDate(x.info.night.get(5))
                            )
                    )
            );
        }
        return dailyList;
    }

    private static AirQuality getDailyAirQuality(Context context, @Nullable String index) {
        if (!TextUtils.isEmpty(index)) {
            try {
                return new AirQuality(
                        CommonConverter.getAqiQuality(context, Integer.parseInt(index)),
                        Integer.parseInt(index),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
            } catch (Exception ignored) {

            }
        }
        return new AirQuality(null, null, null, null, null,
                null, null, null);
    }

    private static List<Hourly> getHourlyList(long publishTime,
                                              Date sunrise, Date sunset,
                                              List<CNWeatherResult.HourlyForecast> forecastList) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(publishTime);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        List<Hourly> hourlyList = new ArrayList<>(forecastList.size());
        for (CNWeatherResult.HourlyForecast forecast : forecastList) {

            int itemHour = Integer.parseInt(forecast.hour);
            int deltaHour;
            if (itemHour >= currentHour) {
                deltaHour = itemHour - currentHour;
            } else if (itemHour == 0 && currentHour == 23) {
                deltaHour = 1;
            } else {
                continue;
            }
            currentHour = itemHour;

            calendar.add(Calendar.HOUR_OF_DAY, deltaHour);
            Date date = calendar.getTime();

            hourlyList.add(
                    new Hourly(
                            date,
                            date.getTime(),
                            CommonConverter.isDaylight(sunrise, sunset, date),
                            forecast.info,
                            getWeatherCode(forecast.img),
                            new Temperature(
                                    Integer.parseInt(forecast.temperature),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new Precipitation(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new PrecipitationProbability(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            )
                    )
            );
        }
        return hourlyList;
    }

    @SuppressLint("SimpleDateFormat")
    private static List<Alert> getAlertList(List<CNWeatherResult.Alert> cnAlertList) throws Exception {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        List<Alert> alertList = new ArrayList<>(cnAlertList.size());
        for (CNWeatherResult.Alert alert : cnAlertList) {

            Date date = format.parse(alert.pubTime);
            if (date == null) {
                throw new NullPointerException("Get null date object.");
            }

            alertList.add(
                    new Alert(
                            date.getTime(),
                            date,
                            date.getTime(),
                            alert.alarmTp1 + alert.alarmTp2 + "预警",
                            alert.content,
                            alert.alarmPic1,
                            getAlertPriority(alert.alarmTp2),
                            getAlertColor(alert.alarmTp2)
                    )
            );
        }

        Alert.deduplication(alertList);
        return alertList;
    }

    private static WeatherCode getWeatherCode(String icon) {
        if (TextUtils.isEmpty(icon)) {
            return WeatherCode.CLOUDY;
        }

        switch (icon) {
            case "0":
            case "00":
                return WeatherCode.CLEAR;

            case "1":
            case "01":
                return WeatherCode.PARTLY_CLOUDY;

            case "3":
            case "7":
            case "8":
            case "9":
            case "03":
            case "07":
            case "08":
            case "09":
            case "10":
            case "11":
            case "12":
            case "21":
            case "22":
            case "23":
            case "24":
            case "25":
                return WeatherCode.RAIN;

            case "4":
            case "04":
                return WeatherCode.THUNDERSTORM;

            case "5":
            case "05":
                return WeatherCode.HAIL;

            case "6":
            case "06":
            case "19":
                return WeatherCode.SLEET;

            case "13":
            case "14":
            case "15":
            case "16":
            case "17":
            case "26":
            case "27":
            case "28":
                return WeatherCode.SNOW;

            case "18":
            case "32":
            case "49":
            case "57":
                return WeatherCode.FOG;

            case "20":
            case "29":
            case "30":
                return WeatherCode.WIND;

            case "53":
            case "54":
            case "55":
            case "56":
                return WeatherCode.HAZE;

            default:
                return WeatherCode.CLOUDY;
        }
    }

    private static Float getCO(CNWeatherResult result) {
        try {
            return Float.parseFloat(result.pm25.co);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressLint("SimpleDateFormat")
    private static Date getDate(@NonNull String time) throws Exception {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(date + "T" + time);
    }

    private static float getHoursOfDay(Date sunrise, Date sunset) {
        return (float) (
                (sunset.getTime() - sunrise.getTime()) // get delta millisecond.
                        / 1000 // second.
                        / 60 // minutes.
                        / 60.0 // hours.
        );
    }

    private static float getWindDegree(@NonNull String direction) {
        switch (direction) {
            case "东北":
            case "东北风":
                return 45;

            case "东":
            case "东风":
                return 90;

            case "东南":
            case "东南风":
                return 135;

            case "南":
            case "南风":
                return 180;

            case "西南":
            case "西南风":
                return 225;

            case "西":
            case "西风":
                return 270;

            case "西北":
            case "西北风":
                return 315;

            case "北":
            case "北风":
                return 0;
        }
        return 0;
    }

    @ColorInt
    private static int getAlertPriority(@Nullable String color) {
        if (TextUtils.isEmpty(color)) {
            return 0;
        }
        switch (color) {
            case "蓝":
            case "蓝色":
                return 1;

            case "黄":
            case "黄色":
                return 2;

            case "橙":
            case "橙色":
            case "橘":
            case "橘色":
            case "橘黄":
            case "橘黄色":
                return 3;

            case "红":
            case "红色":
                return 4;
        }

        return 0;
    }

    @ColorInt
    private static int getAlertColor(@Nullable String color) {
        if (TextUtils.isEmpty(color)) {
            return Color.TRANSPARENT;
        }
        switch (color) {
            case "蓝":
            case "蓝色":
                return Color.rgb(51, 100, 255);

            case "黄":
            case "黄色":
                return Color.rgb(250, 237, 36);

            case "橙":
            case "橙色":
            case "橘":
            case "橘色":
            case "橘黄":
            case "橘黄色":
                return Color.rgb(249, 138, 30);

            case "红":
            case "红色":
                return Color.rgb(215, 48, 42);
        }

        return Color.TRANSPARENT;
    }
}
