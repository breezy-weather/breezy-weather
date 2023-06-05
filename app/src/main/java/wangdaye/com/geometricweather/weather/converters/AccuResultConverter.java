package wangdaye.com.geometricweather.weather.converters;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.basic.models.options.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.common.basic.models.weather.AirQuality;
import wangdaye.com.geometricweather.common.basic.models.weather.Alert;
import wangdaye.com.geometricweather.common.basic.models.weather.Astro;
import wangdaye.com.geometricweather.common.basic.models.weather.Base;
import wangdaye.com.geometricweather.common.basic.models.weather.Current;
import wangdaye.com.geometricweather.common.basic.models.weather.Daily;
import wangdaye.com.geometricweather.common.basic.models.weather.HalfDay;
import wangdaye.com.geometricweather.common.basic.models.weather.History;
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly;
import wangdaye.com.geometricweather.common.basic.models.weather.Minutely;
import wangdaye.com.geometricweather.common.basic.models.weather.MoonPhase;
import wangdaye.com.geometricweather.common.basic.models.weather.Pollen;
import wangdaye.com.geometricweather.common.basic.models.weather.Precipitation;
import wangdaye.com.geometricweather.common.basic.models.weather.PrecipitationDuration;
import wangdaye.com.geometricweather.common.basic.models.weather.PrecipitationProbability;
import wangdaye.com.geometricweather.common.basic.models.weather.Temperature;
import wangdaye.com.geometricweather.common.basic.models.weather.UV;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode;
import wangdaye.com.geometricweather.common.basic.models.weather.Wind;
import wangdaye.com.geometricweather.common.basic.models.weather.WindDegree;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.weather.json.accu.AccuAlertResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuCurrentResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuDailyResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuHourlyResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuLocationResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuMinuteResult;
import wangdaye.com.geometricweather.weather.services.WeatherService;

public class AccuResultConverter {

    @NonNull
    public static Location convert(@Nullable Location location, AccuLocationResult result,
                                   @Nullable String zipCode) {
        if (location != null
                && !TextUtils.isEmpty(location.getProvince())
                && !TextUtils.isEmpty(location.getCity())
                && !TextUtils.isEmpty(location.getDistrict())) {
            return new Location(
                    result.Key,
                    (float) result.GeoPosition.Latitude,
                    (float) result.GeoPosition.Longitude,
                    TimeZone.getTimeZone(result.TimeZone.Name),
                    result.Country.LocalizedName,
                    location.getProvince(),
                    location.getCity(),
                    location.getDistrict() + (zipCode == null ? "" : (" (" + zipCode + ")")),
                    null,
                    WeatherSource.ACCU,
                    false,
                    false,
                    !TextUtils.isEmpty(result.Country.ID)
                            && (result.Country.ID.equalsIgnoreCase("cn")
                            || result.Country.ID.equalsIgnoreCase("hk")
                            || result.Country.ID.equalsIgnoreCase("tw"))
            );
        } else {
            return new Location(
                    result.Key,
                    (float) result.GeoPosition.Latitude,
                    (float) result.GeoPosition.Longitude,
                    TimeZone.getTimeZone(result.TimeZone.Name),
                    result.Country.LocalizedName,
                    result.AdministrativeArea == null ? "" : result.AdministrativeArea.LocalizedName,
                    result.LocalizedName + (zipCode == null ? "" : (" (" + zipCode + ")")),
                    "",
                    null,
                    WeatherSource.ACCU,
                    false,
                    false,
                    !TextUtils.isEmpty(result.Country.ID)
                            && (result.Country.ID.equalsIgnoreCase("cn")
                            || result.Country.ID.equalsIgnoreCase("hk")
                            || result.Country.ID.equalsIgnoreCase("tw"))
            );
        }
    }

    @NonNull
    public static WeatherService.WeatherResultWrapper convert(Context context,
                                                              Location location,
                                                              AccuCurrentResult currentResult,
                                                              AccuDailyResult dailyResult,
                                                              List<AccuHourlyResult> hourlyResultList,
                                                              @Nullable AccuMinuteResult minuteResult,
                                                              List<AccuAlertResult> alertResultList) {
        try {
            Weather weather = new Weather(
                    new Base(
                            location.getCityId(),
                            new Date(currentResult.EpochTime * 1000),
                            new Date()
                    ),
                    new Current(
                            currentResult.WeatherText,
                            getWeatherCode(currentResult.WeatherIcon),
                            new Temperature(
                                    toInt(currentResult.Temperature.Metric.Value),
                                    toInt(currentResult.RealFeelTemperature.Metric.Value),
                                    toInt(currentResult.RealFeelTemperatureShade.Metric.Value),
                                    toInt(currentResult.ApparentTemperature.Metric.Value),
                                    toInt(currentResult.WindChillTemperature.Metric.Value),
                                    toInt(currentResult.WetBulbTemperature.Metric.Value),
                                    null
                            ),
                            new Precipitation(
                                    (float) currentResult.Precip1hr.Metric.Value,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new Wind(
                                    currentResult.Wind.Direction.Localized,
                                    new WindDegree((float) currentResult.Wind.Direction.Degrees, false),
                                    (float) currentResult.WindGust.Speed.Metric.Value,
                                    CommonConverterKt.getWindLevel(context, (float) currentResult.WindGust.Speed.Metric.Value)
                            ),
                            new UV(currentResult.UVIndex, currentResult.UVIndexText, null),
                            null,
                            (float) currentResult.RelativeHumidity,
                            (float) currentResult.Pressure.Metric.Value,
                            (float) currentResult.Visibility.Metric.Value,
                            toInt(currentResult.DewPoint.Metric.Value),
                            currentResult.CloudCover,
                            (float) (currentResult.Ceiling.Metric.Value / 1000.0),
                            convertUnit(context, dailyResult.Headline.Text),
                            convertUnit(context, minuteResult != null ? minuteResult.Summary.LongPhrase : null)
                    ),
                    new History(
                            new Date((currentResult.EpochTime - 24 * 60 * 60) * 1000),
                            toInt(currentResult.TemperatureSummary.Past24HourRange.Maximum.Metric.Value),
                            toInt(currentResult.TemperatureSummary.Past24HourRange.Minimum.Metric.Value)
                    ),
                    getDailyList(context, dailyResult),
                    getHourlyList(context, hourlyResultList),
                    getMinutelyList(minuteResult),
                    getAlertList(alertResultList)
            );
            return new WeatherService.WeatherResultWrapper(weather);
        } catch (Exception ignored) {
            return new WeatherService.WeatherResultWrapper(null);
        }
    }

    private static List<Daily> getDailyList(Context context, AccuDailyResult dailyResult) {
        List<Daily> dailyList = new ArrayList<>(dailyResult.DailyForecasts.size());

        for (AccuDailyResult.DailyForecasts forecasts : dailyResult.DailyForecasts) {
            dailyList.add(
                    new Daily(
                            forecasts.Date,
                            new HalfDay(
                                    convertUnit(context, forecasts.Day.LongPhrase),
                                    forecasts.Day.ShortPhrase,
                                    getWeatherCode(forecasts.Day.Icon),
                                    new Temperature(
                                            toInt(forecasts.Temperature.Maximum.Value),
                                            toInt(forecasts.RealFeelTemperature.Maximum.Value),
                                            toInt(forecasts.RealFeelTemperatureShade.Maximum.Value),
                                            null,
                                            null,
                                            null,
                                            toInt(forecasts.DegreeDaySummary.Heating.Value)
                                    ),
                                    new Precipitation(
                                            (float) forecasts.Day.TotalLiquid.Value,
                                            null,
                                            (float) forecasts.Day.Rain.Value,
                                            (float) (forecasts.Day.Snow.Value * 10),
                                            (float) forecasts.Day.Ice.Value
                                    ),
                                    new PrecipitationProbability(
                                            (float) forecasts.Day.PrecipitationProbability,
                                            (float) forecasts.Day.ThunderstormProbability,
                                            (float) forecasts.Day.RainProbability,
                                            (float) forecasts.Day.SnowProbability,
                                            (float) forecasts.Day.IceProbability
                                    ),
                                    new PrecipitationDuration(
                                            (float) forecasts.Day.HoursOfPrecipitation,
                                            null,
                                            (float) forecasts.Day.HoursOfRain,
                                            (float) forecasts.Day.HoursOfSnow,
                                            (float) forecasts.Day.HoursOfIce
                                    ),
                                    new Wind(
                                            forecasts.Day.Wind.Direction.Localized,
                                            new WindDegree((float) forecasts.Day.Wind.Direction.Degrees, false),
                                            (float) forecasts.Day.WindGust.Speed.Value,
                                            CommonConverterKt.getWindLevel(context, (float) forecasts.Day.WindGust.Speed.Value)
                                    ),
                                    forecasts.Day.CloudCover
                            ),
                            new HalfDay(
                                    convertUnit(context, forecasts.Night.LongPhrase),
                                    forecasts.Night.ShortPhrase,
                                    getWeatherCode(forecasts.Night.Icon),
                                    new Temperature(
                                            toInt(forecasts.Temperature.Minimum.Value),
                                            toInt(forecasts.RealFeelTemperature.Minimum.Value),
                                            toInt(forecasts.RealFeelTemperatureShade.Minimum.Value),
                                            null,
                                            null,
                                            null,
                                            toInt(forecasts.DegreeDaySummary.Cooling.Value)
                                    ),
                                    new Precipitation(
                                            (float) forecasts.Night.TotalLiquid.Value,
                                            null,
                                            (float) forecasts.Night.Rain.Value,
                                            (float) (forecasts.Day.Snow.Value * 10),
                                            (float) forecasts.Night.Ice.Value
                                    ),
                                    new PrecipitationProbability(
                                            (float) forecasts.Night.PrecipitationProbability,
                                            (float) forecasts.Night.ThunderstormProbability,
                                            (float) forecasts.Night.RainProbability,
                                            (float) forecasts.Night.SnowProbability,
                                            (float) forecasts.Night.IceProbability
                                    ),
                                    new PrecipitationDuration(
                                            (float) forecasts.Night.HoursOfPrecipitation,
                                            null,
                                            (float) forecasts.Night.HoursOfRain,
                                            (float) forecasts.Night.HoursOfSnow,
                                            (float) forecasts.Night.HoursOfIce
                                    ),
                                    new Wind(
                                            forecasts.Night.Wind.Direction.Localized,
                                            new WindDegree((float) forecasts.Night.Wind.Direction.Degrees, false),
                                            (float) forecasts.Night.WindGust.Speed.Value,
                                            CommonConverterKt.getWindLevel(context, (float) forecasts.Night.WindGust.Speed.Value)
                                    ),
                                    forecasts.Night.CloudCover
                            ),
                            new Astro(
                                    new Date(forecasts.Sun.EpochRise * 1000),
                                    new Date(forecasts.Sun.EpochSet * 1000)
                            ),
                            new Astro(
                                    new Date(forecasts.Moon.EpochRise * 1000),
                                    new Date(forecasts.Moon.EpochSet * 1000)
                            ),
                            new MoonPhase(
                                    CommonConverterKt.getMoonPhaseAngle(forecasts.Moon.Phase),
                                    forecasts.Moon.Phase
                            ),
                            getDailyAirQuality(forecasts.AirAndPollen),
                            getDailyPollen(forecasts.AirAndPollen),
                            getDailyUV(forecasts.AirAndPollen),
                            (float) forecasts.HoursOfSun
                    )
            );
        }
        return dailyList;
    }

    private static AirQuality getDailyAirQuality(List<AccuDailyResult.DailyForecasts.AirAndPollen> list) {
        AccuDailyResult.DailyForecasts.AirAndPollen aqi = getAirAndPollen(list, "AirQuality");
        Integer index = aqi == null ? null : aqi.Value;
        if (index != null && index == 0) {
            index = null;
        }
        return new AirQuality(index, null, null, null, null, null, null, null, null);
    }

    private static Pollen getDailyPollen(List<AccuDailyResult.DailyForecasts.AirAndPollen> list) {
        AccuDailyResult.DailyForecasts.AirAndPollen grass = getAirAndPollen(list, "Grass");
        AccuDailyResult.DailyForecasts.AirAndPollen mold = getAirAndPollen(list, "Mold");
        AccuDailyResult.DailyForecasts.AirAndPollen ragweed = getAirAndPollen(list, "Ragweed");
        AccuDailyResult.DailyForecasts.AirAndPollen tree = getAirAndPollen(list, "Tree");
        return new Pollen(
                grass == null ? null : grass.Value,
                grass == null ? null : grass.CategoryValue,
                grass == null ? null : grass.Category,
                mold == null ? null : mold.Value,
                mold == null ? null : mold.CategoryValue,
                mold == null ? null : mold.Category,
                ragweed == null ? null : ragweed.Value,
                ragweed == null ? null : ragweed.CategoryValue,
                ragweed == null ? null : ragweed.Category,
                tree == null ? null : tree.Value,
                tree == null ? null : tree.CategoryValue,
                tree == null ? null : tree.Category
        );
    }

    private static UV getDailyUV(List<AccuDailyResult.DailyForecasts.AirAndPollen> list) {
        AccuDailyResult.DailyForecasts.AirAndPollen uv = getAirAndPollen(list, "UVIndex");
        return new UV(
                uv == null ? null : uv.Value,
                uv == null ? null : uv.Category,
                null
        );
    }

    @Nullable
    private static AccuDailyResult.DailyForecasts.AirAndPollen getAirAndPollen(
            List<AccuDailyResult.DailyForecasts.AirAndPollen> list, String name) {
        for (AccuDailyResult.DailyForecasts.AirAndPollen item : list) {
            if (item.Name.equals(name)) {
                return item;
            }
        }
        return null;
    }

    private static List<Hourly> getHourlyList(Context context, List<AccuHourlyResult> resultList) {
        List<Hourly> hourlyList = new ArrayList<>(resultList.size());
        for (AccuHourlyResult result : resultList) {
            hourlyList.add(
                    new Hourly(
                            result.DateTime,
                            result.IsDaylight,
                            result.IconPhrase,
                            getWeatherCode(result.WeatherIcon),
                            new Temperature(
                                    toInt(result.Temperature.Value),
                                    toInt(result.RealFeelTemperature.Value),
                                    toInt(result.RealFeelTemperatureShade.Value),
                                    null,
                                    null,
                                    toInt(result.WetBulbTemperature.Value),
                                    null
                            ),
                            new Precipitation(
                                    (float) result.TotalLiquid.Value,
                                    null,
                                    (float) result.Rain.Value,
                                    (float) (result.Snow.Value * 10),
                                    (float) result.Ice.Value
                            ),
                            new PrecipitationProbability(
                                    (float) result.PrecipitationProbability,
                                    (float) result.ThunderstormProbability,
                                    (float) result.RainProbability,
                                    (float) result.SnowProbability,
                                    (float) result.IceProbability
                            ),
                            new Wind(
                                    result.Wind.Direction.Localized,
                                    new WindDegree((float) result.Wind.Direction.Degrees, false),
                                    (float) result.WindGust.Speed.Value,
                                    CommonConverterKt.getWindLevel(context, (float) result.WindGust.Speed.Value)
                            ),
                            null,
                            null,
                            new UV(result.UVIndex, null, result.UVIndexText)
                    )
            );
        }
        return hourlyList;
    }

    private static List<Minutely> getMinutelyList(@Nullable AccuMinuteResult minuteResult) {
        if (minuteResult == null) {
            return new ArrayList<>();
        }
        List<Minutely> minutelyList = new ArrayList<>(minuteResult.Intervals.size());
        for (AccuMinuteResult.IntervalsBean interval : minuteResult.Intervals) {
            minutelyList.add(
                    new Minutely(
                            interval.StartDateTime,
                            interval.StartEpochDateTime,
                            interval.ShortPhrase,
                            getWeatherCode(interval.IconCode),
                            interval.Minute,
                            toInt(interval.Dbz),
                            interval.CloudCover
                    )
            );
        }
        return minutelyList;
    }

    private static List<Alert> getAlertList(List<AccuAlertResult> resultList) {
        List<Alert> alertList = new ArrayList<>(resultList.size());
        for (AccuAlertResult result : resultList) {
            alertList.add(
                    new Alert(
                            result.AlertID,
                            result.Area.size() > 1 ? result.Area.get(0).StartTime : null,
                            result.Area.size() > 1 ? result.Area.get(0).EpochStartTime * 1000 : 0,
                            result.Description.Localized,
                            result.Area.size() > 1 ? result.Area.get(0).Text : "",
                            result.TypeID,
                            result.Priority,
                            Color.rgb(result.Color.Red, result.Color.Green, result.Color.Blue)
                    )
            );
        }
        Alert.deduplication(alertList);
        Alert.descByTime(alertList);
        return alertList;
    }

    private static int toInt(double value) {
        return (int) (value + 0.5);
    }

    private static WeatherCode getWeatherCode(int icon) {
        if (icon == 1 || icon == 2 || icon == 30
                || icon == 33 || icon == 34) {
            return WeatherCode.CLEAR;
        } else if (icon == 3 || icon == 4 || icon == 6
                || icon == 35 || icon == 36 || icon == 38) {
            return WeatherCode.PARTLY_CLOUDY;
        } else if (icon == 5 || icon == 37) {
            return WeatherCode.HAZE;
        } else if (icon == 7 || icon == 8) {
            return WeatherCode.CLOUDY;
        } else if (icon == 11) {
            return WeatherCode.FOG;
        } else if (icon == 12 || icon == 13 || icon == 14 || icon == 18
                || icon == 39 || icon == 40) {
            return WeatherCode.RAIN;
        } else if (icon == 15 || icon == 16 || icon == 17 || icon == 41 || icon == 42) {
            return WeatherCode.THUNDERSTORM;
        } else if (icon == 19 || icon == 20 || icon == 21 || icon == 22 || icon == 23 || icon == 24
                || icon == 31 || icon == 43 || icon == 44) {
            return WeatherCode.SNOW;
        } else if (icon == 25) {
            return WeatherCode.HAIL;
        } else if (icon == 26 || icon == 29) {
            return WeatherCode.SLEET;
        } else if (icon == 32) {
            return WeatherCode.WIND;
        } else {
            return WeatherCode.CLOUDY;
        }
    }

    private static String convertUnit(Context context, @Nullable String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }

        // precipitation.
        PrecipitationUnit precipitationUnit = SettingsManager.getInstance(context).getPrecipitationUnit();

        // cm.
        str = convertUnit(context, str, PrecipitationUnit.CM, precipitationUnit);

        // mm.
        str = convertUnit(context, str, PrecipitationUnit.MM, precipitationUnit);

        return str;
    }

    private static String convertUnit(Context context,
                                      @NonNull String str,
                                      PrecipitationUnit targetUnit,
                                      PrecipitationUnit resultUnit) {
        try {
            String numberPattern = "\\d+-\\d+(\\s+)?";
            Matcher matcher = Pattern.compile(numberPattern + targetUnit).matcher(str);

            List<String> targetList = new ArrayList<>();
            List<String> resultList = new ArrayList<>();

            while (matcher.find()) {
                String target = str.substring(matcher.start(), matcher.end());
                targetList.add(target);

                String[] targetSplitResults = target.replaceAll(" ", "").split(
                        targetUnit.getName(context));
                String[] numberTexts = targetSplitResults[0].split("-");

                for (int i = 0; i < numberTexts.length; i ++) {
                    float number = Float.parseFloat(numberTexts[i]);
                    number = targetUnit.getValueInDefaultUnit(number);
                    numberTexts[i] = resultUnit.getValueWithoutUnit(number).toString();
                }

                resultList.add(arrayToString(numberTexts) + " " + resultUnit.getName(context));
            }

            for (int i = 0; i < targetList.size(); i ++) {
                str = str.replace(targetList.get(i), resultList.get(i));
            }

            return str;
        } catch (Exception ignore) {
            return str;
        }
    }

    private static String arrayToString(String[] array) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            builder.append(array[i]);
            if (i < array.length - 1) {
                builder.append("-");
            }
        }
        return builder.toString();
    }
}
