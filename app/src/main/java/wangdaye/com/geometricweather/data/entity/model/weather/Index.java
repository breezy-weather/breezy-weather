package wangdaye.com.geometricweather.data.entity.model.weather;

import android.content.Context;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.result.NewAqiResult;
import wangdaye.com.geometricweather.data.entity.result.NewDailyResult;
import wangdaye.com.geometricweather.data.entity.result.NewRealtimeResult;
import wangdaye.com.geometricweather.data.entity.table.weather.WeatherEntity;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Index.
 * */

public class Index {
    // data
    public String[] simpleForecasts;
    public String[] briefings;
    public String[] winds;
    public String[] aqis;
    public String[] humidities;
    public String[] uvs;
    public String[] exercises;
    public String[] colds;
    public String[] carWashes;

    Index() {
        simpleForecasts = new String[] {"", ""};
        briefings = new String[] {"", ""};
        winds = new String[] {"", ""};
        aqis = new String[] {"", ""};
        humidities = new String[] {"", ""};
        uvs = new String[] {"", ""};
        exercises = new String[] {"", ""};
        colds = new String[] {"", ""};
        carWashes = new String[] {"", ""};
    }
/*
    void buildIndex(RealTime realTime, Daily daily, Hourly hourly, Aqi aqi, FWResult result) {
        simpleForecasts = new String[] {"", ""};
        briefings = new String[] {"", ""};
        winds = new String[] {
                "实时 : " + realTime.windDir + " " + realTime.windLevel,
                "今日 : " + daily.windDirs[hourly.dayTime ? 0 : 1] + " " + daily.windLevels[hourly.dayTime ? 0 : 1]};
        aqis = new String[] {
                "空气 : " + aqi.quality + " (" + aqi.aqi + ")",
                "PM2.5 : " + aqi.pm25 + " / " + "PM10 : " + aqi.pm10};
        humidities = new String[] {
                "体感温度",
                realTime.sensibleTemp +  "℃"};
        uvs = new String[] {
                result.indexes.get(7).name + " : " + result.indexes.get(7).level,
                result.indexes.get(7).content};
        exercises = new String[] {
                result.indexes.get(3).name + " : " + result.indexes.get(3).level,
                result.indexes.get(3).content};
        colds = new String[] {
                result.indexes.get(19).name + " : " + result.indexes.get(19).level,
                result.indexes.get(19).content};
        carWashes = new String[] {
                result.indexes.get(5).name + " : " + result.indexes.get(5).level,
                result.indexes.get(5).content};
    }

    void buildIndex(RealTime realTime, Daily daily, Hourly hourly) {
        simpleForecasts = new String[] {"", ""};
        briefings = new String[] {"", ""};
        winds = new String[] {
                "Live : " + realTime.windDir + realTime.windLevel,
                "Today : "
                        + daily.windDirs[hourly.dayTime ? 0 : 1] + " " + daily.windSpeeds[hourly.dayTime ? 0 : 1]
                        + " (" + daily.windLevels[hourly.dayTime ? 0 : 1] + ")"};
        aqis = new String[] {"", ""};
        humidities = new String[] {
                "Apparent temperature",
                realTime.sensibleTemp + "℃"};
        uvs = new String[] {
                "Sunrise : " + daily.astros[0],
                "Sunset : " + daily.astros[1]};
        exercises = new String[] {"", ""};
        colds = new String[] {"", ""};
        carWashes = new String[] {"", ""};
    }
*/
    public void buildIndex(Context c, NewRealtimeResult result) {
        if (winds == null) {
            winds = new String[] {"", ""};
        }
        winds[0] = c.getString(R.string.live) + " : " + result.Wind.Direction.Localized
                + " " + result.Wind.Speed.Metric.Value + "km/h"
                + " (" + WeatherHelper.getWindLevel(c, result.Wind.Speed.Metric.Value) + ")";
        humidities = new String[2];
        humidities[0] = c.getString(R.string.sensible_temp) + " : " + result.RealFeelTemperature.Metric.Value + "℃";
        humidities[1] = c.getString(R.string.humidity) + " : " + result.RelativeHumidity + "%";
        uvs = new String[2];
        uvs[0] = c.getString(R.string.uv_index) + " : " + result.UVIndex;
        uvs[1] = result.UVIndexText;
    }

    public void buildIndex(Context c, NewDailyResult result) {
        simpleForecasts = new String[] {c.getString(R.string.forecast), result.Headline.Text};
        briefings = new String[] {
                c.getString(R.string.briefings),
                c.getString(R.string.daytime) + " : " + result.DailyForecasts.get(0).Day.LongPhrase + "\n"
                        + c.getString(R.string.nighttime) + " : " + result.DailyForecasts.get(0).Night.LongPhrase};
        if (winds == null) {
            winds = new String[] {"", ""};
        }
        winds[1] =
                c.getString(R.string.daytime) + " : " + result.DailyForecasts.get(0).Day.Wind.Direction.Localized
                        + " " + result.DailyForecasts.get(0).Day.Wind.Speed.Value + "km/h"
                        + " (" + WeatherHelper.getWindLevel(c, result.DailyForecasts.get(0).Day.Wind.Speed.Value) + ")" + "\n"
                        + c.getString(R.string.nighttime) + " : " + result.DailyForecasts.get(0).Night.Wind.Direction.Localized
                        + " " + result.DailyForecasts.get(0).Night.Wind.Speed.Value + "km/h"
                        + " (" + WeatherHelper.getWindLevel(c, result.DailyForecasts.get(0).Night.Wind.Speed.Value) + ")";
    }

    public void buildIndex(Context c, NewAqiResult result) {
        if (result == null) {
            aqis = new String[] {"", ""};
        } else {
            aqis = new String[] {
                    "AQI : " + result.Index + " (" + WeatherHelper.getAqiQuality(c, result.Index) + ")",
                    "PM 2.5 : " + (int) (result.ParticulateMatter2_5) + "\n"
                            + "PM 10 : " + (int) (result.ParticulateMatter10) + "\n"
                            + "O₃ : " + (int) (result.Ozone) + "\n"
                            + "CO : " + (int) (result.CarbonMonoxide) + "\n"
                            + "NO : " + (int) (result.NitrogenMonoxide) + "\n"
                            + "NO₂ : " + (int) (result.NitrogenDioxide) + "\n"
                            + "SO₂ : " + (int) (result.SulfurDioxide) + "\n"
                            + "Pb : " + (int) (result.Lead)};
        }
    }
    
    void buildIndex(WeatherEntity entity) {
        simpleForecasts[0] = entity.indexSimpleForecastTitle;
        simpleForecasts[1] = entity.indexSimpleForecastContent;
        briefings[0] = entity.indexBriefingTitle;
        briefings[1] = entity.indexBriefingContent;
        winds[0] = entity.indexWindTitle;
        winds[1] = entity.indexWindContent;
        aqis[0] = entity.indexAqiTitle;
        aqis[1] = entity.indexAqiContent;
        humidities[0] = entity.indexHumidityTitle;
        humidities[1] = entity.indexHumidityContent;
        uvs[0] = entity.indexUvTitle;
        uvs[1] = entity.indexUvContent;
        exercises[0] = entity.indexExerciseTitle;
        exercises[1] = entity.indexExerciseContent;
        colds[0] = entity.indexColdTitle;
        colds[1] = entity.indexColdContent;
        carWashes[0] = entity.indexCarWashTitle;
        carWashes[1] = entity.indexCarWashContent;
    }
}
