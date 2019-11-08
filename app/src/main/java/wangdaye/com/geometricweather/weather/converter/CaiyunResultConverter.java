package wangdaye.com.geometricweather.weather.converter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

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
import wangdaye.com.geometricweather.basic.model.weather.Minutely;
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
import wangdaye.com.geometricweather.weather.json.caiyun.CaiYunForecastResult;
import wangdaye.com.geometricweather.weather.json.caiyun.CaiYunMainlyResult;

public class CaiyunResultConverter {

    @Nullable
    public static Weather convert(Context context, Location location,
                                  CaiYunMainlyResult mainlyResult,
                                  CaiYunForecastResult forecastResult) {
        try {
            return new Weather(
                    new Base(
                            location.getCityId(),
                            System.currentTimeMillis(),
                            mainlyResult.current.pubTime,
                            mainlyResult.current.pubTime.getTime(),
                            new Date(System.currentTimeMillis()),
                            System.currentTimeMillis()
                    ),
                    new Current(
                            getWeatherText(mainlyResult.current.weather),
                            getWeatherCode(mainlyResult.current.weather),
                            new Temperature(
                                    Integer.parseInt(mainlyResult.current.temperature.value),
                                    Integer.parseInt(mainlyResult.current.feelsLike.value),
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
                                    getWindDirection(Float.parseFloat(mainlyResult.current.wind.direction.value)),
                                    new WindDegree(
                                            Float.parseFloat(mainlyResult.current.wind.direction.value),
                                            false
                                    ),
                                    Float.parseFloat(mainlyResult.current.wind.speed.value),
                                    CommonConverter.getWindLevel(
                                            context,
                                            Float.parseFloat(mainlyResult.current.wind.speed.value)
                                    )
                            ),
                            new UV(
                                    Integer.parseInt(mainlyResult.current.uvIndex),
                                    getUVDescription(mainlyResult.current.uvIndex),
                                    null
                            ),
                            getAirQuality(context, mainlyResult),
                            !TextUtils.isEmpty(mainlyResult.current.humidity.value)
                                    ? Float.parseFloat(mainlyResult.current.humidity.value)
                                    : null,
                            !TextUtils.isEmpty(mainlyResult.current.pressure.value)
                                    ? Float.parseFloat(mainlyResult.current.pressure.value)
                                    : null,
                            !TextUtils.isEmpty(mainlyResult.current.visibility.value)
                                    ? Float.parseFloat(mainlyResult.current.visibility.value)
                                    : null,
                            null,
                            null,
                            null,
                            null,
                            forecastResult.precipitation.description
                    ),
                    getYesterday(mainlyResult),
                    getDailyList(context, mainlyResult.current.pubTime, mainlyResult.forecastDaily),
                    getHourlyList(
                            mainlyResult.current.pubTime,
                            mainlyResult.forecastDaily.sunRiseSet.value.get(0).from,
                            mainlyResult.forecastDaily.sunRiseSet.value.get(0).to,
                            mainlyResult.forecastHourly
                    ),
                    getMinutelyList(
                            mainlyResult.forecastDaily.sunRiseSet.value.get(0).from,
                            mainlyResult.forecastDaily.sunRiseSet.value.get(0).to,
                            getWeatherText(mainlyResult.current.weather),
                            getWeatherCode(mainlyResult.current.weather),
                            forecastResult
                    ),
                    getAlertList(mainlyResult)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static AirQuality getAirQuality(Context context, CaiYunMainlyResult result) {
        String quality = CommonConverter.getAqiQuality(
                context, Integer.parseInt(result.aqi.aqi));

        Integer index;
        try {
            index = (int) Double.parseDouble(result.aqi.aqi);
        } catch (Exception e) {
            index = null;
        }

        Float pm25;
        try {
            pm25 = Float.parseFloat(result.aqi.pm25);
        } catch (Exception e) {
            pm25 = null;
        }

        Float pm10;
        try {
            pm10 = Float.parseFloat(result.aqi.pm10);
        } catch (Exception e) {
            pm10 = null;
        }

        Float so2;
        try {
            so2 = Float.parseFloat(result.aqi.so2);
        } catch (Exception e) {
            so2 = null;
        }

        Float no2;
        try {
            no2 = Float.parseFloat(result.aqi.no2);
        } catch (Exception e) {
            no2 = null;
        }

        Float o3;
        try {
            o3 = Float.parseFloat(result.aqi.o3);
        } catch (Exception e) {
            o3 = null;
        }

        Float co;
        try {
            co = Float.parseFloat(result.aqi.co);
        } catch (Exception e) {
            co = null;
        }

        return new AirQuality(quality, index, pm25, pm10, so2, no2, o3, co);
    }

    @Nullable
    private static History getYesterday(CaiYunMainlyResult result) {
        try {
            return new History(
                    new Date(result.updateTime - 24 * 60 * 60 * 1000),
                    result.updateTime - 24 * 60 * 60 * 1000,
                    Integer.parseInt(result.yesterday.tempMax),
                    Integer.parseInt(result.yesterday.tempMin)
            );
        } catch (Exception ignore) {
            return null;
        }
    }

    private static List<Daily> getDailyList(Context context,
                                            Date publishDate, CaiYunMainlyResult.ForecastDailyBean forecast) {
        List<Daily> dailyList = new ArrayList<>(forecast.weather.value.size());
        for (int i = 0; i < forecast.weather.value.size(); i ++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(publishDate);
            calendar.add(Calendar.DATE, i);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            dailyList.add(
                    new Daily(
                            calendar.getTime(),
                            calendar.getTimeInMillis(),
                            new HalfDay(
                                    getWeatherText(forecast.weather.value.get(i).from),
                                    getWeatherText(forecast.weather.value.get(i).from),
                                    getWeatherCode(forecast.weather.value.get(i).from),
                                    new Temperature(
                                            Integer.parseInt(forecast.temperature.value.get(i).from),
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
                                            getPrecipitationProbability(forecast, i),
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
                                            getWindDirection(Float.parseFloat(forecast.wind.direction.value.get(i).from)),
                                            new WindDegree(
                                                    Float.parseFloat(forecast.wind.direction.value.get(i).from),
                                                    false
                                            ),
                                            Float.parseFloat(forecast.wind.speed.value.get(i).from),
                                            CommonConverter.getWindLevel(
                                                    context,
                                                    Float.parseFloat(forecast.wind.speed.value.get(i).from)
                                            )
                                    ),
                                    null
                            ),
                            new HalfDay(
                                    getWeatherText(forecast.weather.value.get(i).to),
                                    getWeatherText(forecast.weather.value.get(i).to),
                                    getWeatherCode(forecast.weather.value.get(i).to),
                                    new Temperature(
                                            Integer.parseInt(forecast.temperature.value.get(i).to),
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
                                            getPrecipitationProbability(forecast, i),
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
                                            getWindDirection(Float.parseFloat(forecast.wind.direction.value.get(i).to)),
                                            new WindDegree(
                                                    Float.parseFloat(forecast.wind.direction.value.get(i).to),
                                                    false
                                            ),
                                            Float.parseFloat(forecast.wind.speed.value.get(i).to),
                                            CommonConverter.getWindLevel(
                                                    context,
                                                    Float.parseFloat(forecast.wind.speed.value.get(i).to)
                                            )
                                    ),
                                    null
                            ),
                            new Astro(
                                    forecast.sunRiseSet.value.get(i).from,
                                    forecast.sunRiseSet.value.get(i).to
                            ),
                            new Astro(null, null),
                            new MoonPhase(null, null),
                            new AirQuality(
                                    CommonConverter.getAqiQuality(context, forecast.aqi.value.get(i)),
                                    forecast.aqi.value.get(i),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
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
                            new UV(
                                    null,
                                    null,
                                    null
                            ),
                            (float) (
                                    (forecast.sunRiseSet.value.get(i).to.getTime()
                                            - forecast.sunRiseSet.value.get(i).from.getTime()) // millisecond.
                                            / 1000 // second.
                                            / 60 // minute.
                                            / 60.0 // hour.
                            )
                    )
            );
        }
        return dailyList;
    }

    private static Float getPrecipitationProbability(CaiYunMainlyResult.ForecastDailyBean forecast, int index) {
        try {
            if (index < forecast.precipitationProbability.value.size()) {
                return Float.parseFloat(forecast.precipitationProbability.value.get(index));
            } else {
                return null;
            }
        } catch (Exception ignore) {
            return null;
        }
    }

    private static List<Hourly> getHourlyList(Date publishDate,
                                              Date sunrise, Date sunset,
                                              CaiYunMainlyResult.ForecastHourlyBean forecast) {
        List<Hourly> hourlyList = new ArrayList<>(forecast.weather.value.size());
        for (int i = 0; i < forecast.weather.value.size(); i ++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(publishDate);
            calendar.add(Calendar.HOUR_OF_DAY, i);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            Date date = calendar.getTime();
            hourlyList.add(
                    new Hourly(
                            date,
                            date.getTime(),
                            CommonConverter.isDaylight(sunrise, sunset, date),
                            getWeatherText(String.valueOf(forecast.weather.value.get(i))),
                            getWeatherCode(String.valueOf(forecast.weather.value.get(i))),
                            new Temperature(
                                    forecast.temperature.value.get(i),
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

    private static List<Minutely> getMinutelyList(Date sunrise, Date sunset,
                                                  String currentWeatherText,
                                                  WeatherCode currentWeatherCode,
                                                  CaiYunForecastResult result) {
        Date current = result.precipitation.pubTime;

        List<Minutely> minutelyList = new ArrayList<>(result.precipitation.value.size());
        for (int i = 0; i < result.precipitation.value.size(); i ++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(current);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            minutelyList.add(
                    new Minutely(
                            calendar.getTime(),
                            calendar.getTimeInMillis(),
                            CommonConverter.isDaylight(sunrise, sunset, calendar.getTime()),
                            getMinuteWeatherText(
                                    result.precipitation.value.get(i),
                                    currentWeatherText,
                                    currentWeatherCode
                            ),
                            getMinuteWeatherCode(
                                    result.precipitation.value.get(i),
                                    currentWeatherCode
                            ),
                            1,
                            null,
                            null
                    )
            );
        }
        return minutelyList;
    }

    private static String getMinuteWeatherText(double precipitation,
                                               String currentWeatherText,
                                               WeatherCode currentWeatherCode) {
        if (precipitation > 0) {
            if (isPrecipitation(currentWeatherCode)) {
                return currentWeatherText;
            } else {
                return "阴";
            }
        } else {
            if (isPrecipitation(currentWeatherCode)) {
                return "阴";
            } else {
                return currentWeatherText;
            }
        }
    }

    private static WeatherCode getMinuteWeatherCode(double precipitation,
                                                    WeatherCode currentWeatherCode) {
        if (precipitation > 0) {
            if (isPrecipitation(currentWeatherCode)) {
                return currentWeatherCode;
            } else {
                return WeatherCode.CLOUDY;
            }
        } else {
            if (isPrecipitation(currentWeatherCode)) {
                return WeatherCode.CLOUDY;
            } else {
                return currentWeatherCode;
            }
        }
    }

    private static boolean isPrecipitation(WeatherCode code) {
        return code == WeatherCode.RAIN
                || code == WeatherCode.SNOW
                || code == WeatherCode.HAIL
                || code == WeatherCode.SLEET
                || code == WeatherCode.THUNDERSTORM;
    }

    private static List<Alert> getAlertList(CaiYunMainlyResult result) {
        List<Alert> alertList = new ArrayList<>(result.alerts.size());
        for (CaiYunMainlyResult.AlertsBean a : result.alerts) {
            alertList.add(
                    new Alert(
                            a.pubTime.getTime(),
                            a.pubTime,
                            a.pubTime.getTime(),
                            a.title,
                            a.detail,
                            a.type,
                            getAlertPriority(a.level),
                            getAlertColor(a.level)
                    )
            );
        }
        Alert.deduplication(alertList);
        return alertList;
    }

    private static String getWeatherText(String icon) {
        if (TextUtils.isEmpty(icon)) {
            return "未知";
        }

        switch (icon) {
            case "0":
            case "00":
                return "晴";

            case "1":
            case "01":
                return "多云";

            case "2":
            case "02":
                return "阴";

            case "3":
            case "03":
                return "阵雨";

            case "4":
            case "04":
                return "雷阵雨";

            case "5":
            case "05":
                return "雷阵雨伴有冰雹";

            case "6":
            case "06":
                return "雨夹雪";

            case "7":
            case "07":
                return "小雨";

            case "8":
            case "08":
                return  "中雨";

            case "9":
            case "09":
                return  "大雨";

            case "10":
                return  "暴雨";

            case "11":
                return  "大暴雨";

            case "12":
                return  "特大暴雨";

            case "13":
                return  "阵雪";

            case "14":
                return  "小雪";

            case "15":
                return  "中雪";

            case "16":
                return  "大雪";

            case "17":
                return  "暴雪";

            case "18":
                return  "雾";

            case "19":
                return  "冻雨";

            case "20":
                return  "沙尘暴";

            case "21":
                return  "小到中雨";

            case "22":
                return  "中到大雨";

            case "23":
                return  "大到暴雨";

            case "24":
                return  "暴雨到大暴雨";

            case "25":
                return  "大暴雨到特大暴雨";

            case "26":
                return  "小到中雪";

            case "27":
                return  "中到大雪";

            case "28":
                return  "大到暴雪";

            case "29":
                return  "浮尘";

            case "30":
                return  "扬沙";

            case "31":
                return  "强沙尘暴";

            case "53":
            case "54":
            case "55":
            case "56":
                return  "霾";

            default:
                return "未知";
        }
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

    private static String getWindDirection(float degree) {
        if (degree < 0) {
            return "无风向";
        }if (22.5 < degree && degree <= 67.5) {
            return "东北风";
        } else if (67.5 < degree && degree <= 112.5) {
            return "东风";
        } else if (112.5 < degree && degree <= 157.5) {
            return "东南风";
        } else if (157.5 < degree && degree <= 202.5) {
            return "南风";
        } else if (202.5 < degree && degree <= 247.5) {
            return "西南风";
        } else if (247.5 < degree && degree <= 292.5) {
            return "西风";
        } else if (292. < degree && degree <= 337.5) {
            return "西北风";
        } else {
            return "北风";
        }
    }

    @Nullable
    private static String getUVDescription(String index) {
        try {
            int num = Integer.parseInt(index);
            if (num <= 2) {
                return "最弱";
            } else if (num <= 4) {
                return "弱";
            } else if (num <= 6) {
                return "中等";
            } else if (num <= 9) {
                return "强";
            } else {
                return "很强";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
