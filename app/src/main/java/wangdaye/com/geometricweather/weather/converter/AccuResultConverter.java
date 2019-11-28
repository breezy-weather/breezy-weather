package wangdaye.com.geometricweather.weather.converter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
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
import wangdaye.com.geometricweather.weather.json.accu.AccuAlertResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuAqiResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuDailyResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuHourlyResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuLocationResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuMinuteResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuCurrentResult;

public class AccuResultConverter {

    public static Location convert(@Nullable Location location, AccuLocationResult result) {
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
                    location.getDistrict(),
                    null,
                    WeatherSource.ACCU,
                    false,
                    false,
                    !TextUtils.isEmpty(result.Country.ID)
                            && (result.Country.ID.equals("CN")
                            || result.Country.ID.equals("cn")
                            || result.Country.ID.equals("HK")
                            || result.Country.ID.equals("hk")
                            || result.Country.ID.equals("TW")
                            || result.Country.ID.equals("tw"))
            );
        } else {
            return new Location(
                    result.Key,
                    (float) result.GeoPosition.Latitude,
                    (float) result.GeoPosition.Longitude,
                    TimeZone.getTimeZone(result.TimeZone.Name),
                    result.Country.LocalizedName,
                    result.AdministrativeArea == null ? "" : result.AdministrativeArea.LocalizedName,
                    result.LocalizedName,
                    "",
                    null,
                    WeatherSource.ACCU,
                    false,
                    false,
                    !TextUtils.isEmpty(result.Country.ID)
                            && (result.Country.ID.equals("CN")
                            || result.Country.ID.equals("cn")
                            || result.Country.ID.equals("HK")
                            || result.Country.ID.equals("hk")
                            || result.Country.ID.equals("TW")
                            || result.Country.ID.equals("tw"))
            );
        }
    }

    public static Weather convert(Context context,
                                  Location location,
                                  AccuCurrentResult currentResult,
                                  AccuDailyResult dailyResult,
                                  List<AccuHourlyResult> hourlyResultList,
                                  @Nullable AccuMinuteResult minuteResult,
                                  @Nullable AccuAqiResult aqiResult,
                                  List<AccuAlertResult> alertResultList) {
        try {
            return new Weather(
                    new Base(
                            location.getCityId(),
                            System.currentTimeMillis(),
                            new Date(currentResult.EpochTime * 1000),
                            currentResult.EpochTime * 1000,
                            new Date(),
                            System.currentTimeMillis()
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
                            new PrecipitationProbability(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new Wind(
                                    currentResult.Wind.Direction.Localized,
                                    new WindDegree(currentResult.Wind.Direction.Degrees, false),
                                    (float) currentResult.WindGust.Speed.Metric.Value,
                                    CommonConverter.getWindLevel(context, currentResult.WindGust.Speed.Metric.Value)
                            ),
                            new UV(currentResult.UVIndex, currentResult.UVIndexText, null),
                            aqiResult == null ? new AirQuality(
                                    null, null, null, null,
                                    null, null, null, null
                            ) : new AirQuality(
                                    CommonConverter.getAqiQuality(context, aqiResult.Index),
                                    aqiResult.Index,
                                    aqiResult.ParticulateMatter2_5,
                                    aqiResult.ParticulateMatter10,
                                    aqiResult.SulfurDioxide,
                                    aqiResult.NitrogenDioxide,
                                    aqiResult.Ozone,
                                    aqiResult.CarbonMonoxide
                            ),
                            (float) currentResult.RelativeHumidity,
                            (float) currentResult.Pressure.Metric.Value,
                            (float) currentResult.Visibility.Metric.Value,
                            toInt(currentResult.DewPoint.Metric.Value),
                            currentResult.CloudCover,
                            (float) (currentResult.Ceiling.Metric.Value / 1000.0),
                            dailyResult.Headline.Text,
                            minuteResult != null ? minuteResult.Summary.LongPhrase : null
                    ),
                    new History(
                            new Date((currentResult.EpochTime - 24 * 60 * 60) * 1000),
                            (currentResult.EpochTime - 24 * 60 * 60) * 1000,
                            toInt(currentResult.TemperatureSummary.Past24HourRange.Maximum.Metric.Value),
                            toInt(currentResult.TemperatureSummary.Past24HourRange.Minimum.Metric.Value)
                    ),
                    getDailyList(context, dailyResult),
                    getHourlyList(hourlyResultList),
                    getMinutelyList(
                            dailyResult.DailyForecasts.get(0).Sun.Rise,
                            dailyResult.DailyForecasts.get(0).Sun.Set,
                            minuteResult
                    ),
                    getAlertList(alertResultList)
            );
        } catch (Exception ignored) {
            return null;
        }
    }

    private static List<Daily> getDailyList(Context context, AccuDailyResult dailyResult) {
        List<Daily> dailyList = new ArrayList<>(dailyResult.DailyForecasts.size());

        for (AccuDailyResult.DailyForecasts forecasts : dailyResult.DailyForecasts) {
            dailyList.add(
                    new Daily(
                            forecasts.Date,
                            forecasts.EpochDate * 1000,
                            new HalfDay(
                                    forecasts.Day.LongPhrase,
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
                                            new WindDegree(forecasts.Day.Wind.Direction.Degrees, false),
                                            (float) forecasts.Day.WindGust.Speed.Value,
                                            CommonConverter.getWindLevel(context, forecasts.Day.WindGust.Speed.Value)
                                    ),
                                    forecasts.Day.CloudCover
                            ),
                            new HalfDay(
                                    forecasts.Night.LongPhrase,
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
                                            new WindDegree(forecasts.Night.Wind.Direction.Degrees, false),
                                            (float) forecasts.Night.WindGust.Speed.Value,
                                            CommonConverter.getWindLevel(context, forecasts.Night.WindGust.Speed.Value)
                                    ),
                                    forecasts.Night.CloudCover
                            ),
                            new Astro(forecasts.Sun.Rise, forecasts.Sun.Set),
                            new Astro(forecasts.Moon.Rise, forecasts.Moon.Set),
                            new MoonPhase(
                                    CommonConverter.getMoonPhaseAngle(forecasts.Moon.Phase),
                                    forecasts.Moon.Phase
                            ),
                            getDailyAirQuality(context, forecasts.AirAndPollen),
                            getDailyPollen(forecasts.AirAndPollen),
                            getDailyUV(forecasts.AirAndPollen),
                            (float) forecasts.HoursOfSun
                    )
            );
        }
        return dailyList;
    }

    private static AirQuality getDailyAirQuality(Context context,
                                                 List<AccuDailyResult.DailyForecasts.AirAndPollen> list) {
        AccuDailyResult.DailyForecasts.AirAndPollen aqi = getAirAndPollen(list, "AirQuality");
        Integer index = aqi == null ? null : aqi.Value;
        if (index != null && index == 0) {
            index = null;
        }
        return new AirQuality(
                CommonConverter.getAqiQuality(context, index),
                index,
                null,
                null,
                null,
                null,
                null,
                null
        );
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

    private static List<Hourly> getHourlyList(List<AccuHourlyResult> resultList) {
        List<Hourly> hourlyList = new ArrayList<>(resultList.size());
        for (AccuHourlyResult result : resultList) {
            hourlyList.add(
                    new Hourly(
                            result.DateTime,
                            result.EpochDateTime * 1000,
                            result.IsDaylight,
                            result.IconPhrase,
                            getWeatherCode(result.WeatherIcon),
                            new Temperature(
                                    toInt(result.Temperature.Value),
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
                                    (float) result.PrecipitationProbability,
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
                                                  @Nullable AccuMinuteResult minuteResult) {
        if (minuteResult == null) {
            return new ArrayList<>();
        }
        List<Minutely> minutelyList = new ArrayList<>(minuteResult.Intervals.size());
        for (AccuMinuteResult.IntervalsBean interval : minuteResult.Intervals) {
            minutelyList.add(
                    new Minutely(
                            interval.StartDateTime,
                            interval.StartEpochDateTime,
                            CommonConverter.isDaylight(sunrise, sunset, interval.StartDateTime),
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
                            result.Area.get(0).StartTime,
                            result.Area.get(0).EpochStartTime * 1000,
                            result.Description.Localized,
                            result.Area.get(0).Text,
                            result.TypeID,
                            result.Priority,
                            Color.rgb(result.Color.Red, result.Color.Green, result.Color.Blue)
                    )
            );
        }
        Alert.deduplication(alertList);
        return alertList;
    }

    private static int toInt(double value) {
        return (int) (value + 0.5);
    }

    private static WeatherCode getWeatherCode(int icon) {
        if (icon == 1 || icon == 2 || icon == 30
                || icon == 33 || icon == 34) {
            return WeatherCode.CLEAR;
        } else if (icon == 3 || icon == 4 || icon == 6 || icon == 7
                || icon == 35 || icon == 36 || icon == 38) {
            return WeatherCode.PARTLY_CLOUDY;
        } else if (icon == 5 || icon == 37) {
            return WeatherCode.HAZE;
        } else if (icon == 8) {
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
}
