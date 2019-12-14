package wangdaye.com.geometricweather.db.converter;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.model.weather.AirQuality;
import wangdaye.com.geometricweather.basic.model.weather.Astro;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.HalfDay;
import wangdaye.com.geometricweather.basic.model.weather.MoonPhase;
import wangdaye.com.geometricweather.basic.model.weather.Pollen;
import wangdaye.com.geometricweather.basic.model.weather.Precipitation;
import wangdaye.com.geometricweather.basic.model.weather.PrecipitationDuration;
import wangdaye.com.geometricweather.basic.model.weather.PrecipitationProbability;
import wangdaye.com.geometricweather.basic.model.weather.Temperature;
import wangdaye.com.geometricweather.basic.model.weather.UV;
import wangdaye.com.geometricweather.basic.model.weather.Wind;
import wangdaye.com.geometricweather.db.entity.DailyEntity;
import wangdaye.com.geometricweather.db.propertyConverter.WeatherSourceConverter;

public class DailyEntityConverter {

    public static DailyEntity convertToEntity(String cityId, WeatherSource source, Daily daily) {
        DailyEntity entity = new DailyEntity();

        entity.cityId = cityId;
        entity.weatherSource = new WeatherSourceConverter().convertToDatabaseValue(source);
        entity.date = daily.getDate();
        entity.time = daily.getTime();

        // daytime.
        entity.daytimeWeatherText = daily.day().getWeatherText();
        entity.daytimeWeatherPhase = daily.day().getWeatherPhase();
        entity.daytimeWeatherCode = daily.day().getWeatherCode();

        entity.daytimeTemperature = daily.day().getTemperature().getTemperature();
        entity.daytimeRealFeelTemperature = daily.day().getTemperature().getRealFeelTemperature();
        entity.daytimeRealFeelShaderTemperature = daily.day().getTemperature().getRealFeelShaderTemperature();
        entity.daytimeApparentTemperature = daily.day().getTemperature().getApparentTemperature();
        entity.daytimeWindChillTemperature = daily.day().getTemperature().getWindChillTemperature();
        entity.daytimeWetBulbTemperature = daily.day().getTemperature().getWetBulbTemperature();
        entity.daytimeDegreeDayTemperature = daily.day().getTemperature().getDegreeDayTemperature();

        entity.daytimeTotalPrecipitation = daily.day().getPrecipitation().getTotal();
        entity.daytimeThunderstormPrecipitation = daily.day().getPrecipitation().getThunderstorm();
        entity.daytimeRainPrecipitation = daily.day().getPrecipitation().getRain();
        entity.daytimeSnowPrecipitation = daily.day().getPrecipitation().getSnow();
        entity.daytimeIcePrecipitation = daily.day().getPrecipitation().getIce();

        entity.daytimeTotalPrecipitationProbability = daily.day().getPrecipitationProbability().getTotal();
        entity.daytimeThunderstormPrecipitationProbability = daily.day().getPrecipitationProbability().getThunderstorm();
        entity.daytimeRainPrecipitationProbability = daily.day().getPrecipitationProbability().getRain();
        entity.daytimeSnowPrecipitationProbability = daily.day().getPrecipitationProbability().getSnow();
        entity.daytimeIcePrecipitationProbability = daily.day().getPrecipitationProbability().getIce();

        entity.daytimeTotalPrecipitationDuration = daily.day().getPrecipitationDuration().getTotal();
        entity.daytimeThunderstormPrecipitationDuration = daily.day().getPrecipitationDuration().getThunderstorm();
        entity.daytimeRainPrecipitationDuration = daily.day().getPrecipitationDuration().getRain();
        entity.daytimeSnowPrecipitationDuration = daily.day().getPrecipitationDuration().getSnow();
        entity.daytimeIcePrecipitationDuration = daily.day().getPrecipitationDuration().getIce();

        entity.daytimeWindDirection = daily.day().getWind().getDirection();
        entity.daytimeWindDegree = daily.day().getWind().getDegree();
        entity.daytimeWindSpeed = daily.day().getWind().getSpeed();
        entity.daytimeWindLevel = daily.day().getWind().getLevel();

        entity.daytimeCloudCover = daily.day().getCloudCover();

        // nighttime.
        entity.nighttimeWeatherText = daily.night().getWeatherText();
        entity.nighttimeWeatherPhase = daily.night().getWeatherPhase();
        entity.nighttimeWeatherCode = daily.night().getWeatherCode();

        entity.nighttimeTemperature = daily.night().getTemperature().getTemperature();
        entity.nighttimeRealFeelTemperature = daily.night().getTemperature().getRealFeelTemperature();
        entity.nighttimeRealFeelShaderTemperature = daily.night().getTemperature().getRealFeelShaderTemperature();
        entity.nighttimeApparentTemperature = daily.night().getTemperature().getApparentTemperature();
        entity.nighttimeWindChillTemperature = daily.night().getTemperature().getWindChillTemperature();
        entity.nighttimeWetBulbTemperature = daily.night().getTemperature().getWetBulbTemperature();
        entity.nighttimeDegreeDayTemperature = daily.night().getTemperature().getDegreeDayTemperature();

        entity.nighttimeTotalPrecipitation = daily.night().getPrecipitation().getTotal();
        entity.nighttimeThunderstormPrecipitation = daily.night().getPrecipitation().getThunderstorm();
        entity.nighttimeRainPrecipitation = daily.night().getPrecipitation().getRain();
        entity.nighttimeSnowPrecipitation = daily.night().getPrecipitation().getSnow();
        entity.nighttimeIcePrecipitation = daily.night().getPrecipitation().getIce();

        entity.nighttimeTotalPrecipitationProbability = daily.night().getPrecipitationProbability().getTotal();
        entity.nighttimeThunderstormPrecipitationProbability = daily.night().getPrecipitationProbability().getThunderstorm();
        entity.nighttimeRainPrecipitationProbability = daily.night().getPrecipitationProbability().getRain();
        entity.nighttimeSnowPrecipitationProbability = daily.night().getPrecipitationProbability().getSnow();
        entity.nighttimeIcePrecipitationProbability = daily.night().getPrecipitationProbability().getIce();

        entity.nighttimeTotalPrecipitationDuration = daily.night().getPrecipitationDuration().getTotal();
        entity.nighttimeThunderstormPrecipitationDuration = daily.night().getPrecipitationDuration().getThunderstorm();
        entity.nighttimeRainPrecipitationDuration = daily.night().getPrecipitationDuration().getRain();
        entity.nighttimeSnowPrecipitationDuration = daily.night().getPrecipitationDuration().getSnow();
        entity.nighttimeIcePrecipitationDuration = daily.night().getPrecipitationDuration().getIce();

        entity.nighttimeWindDirection = daily.night().getWind().getDirection();
        entity.nighttimeWindDegree = daily.night().getWind().getDegree();
        entity.nighttimeWindSpeed = daily.night().getWind().getSpeed();
        entity.nighttimeWindLevel = daily.night().getWind().getLevel();

        entity.nighttimeCloudCover = daily.night().getCloudCover();

        // sun.
        entity.sunRiseDate = daily.sun().getRiseDate();
        entity.sunSetDate = daily.sun().getSetDate();

        // moon.
        entity.moonRiseDate = daily.moon().getRiseDate();
        entity.moonSetDate = daily.moon().getSetDate();

        // moon phase.
        entity.moonPhaseAngle = daily.getMoonPhase().getAngle();
        entity.moonPhaseDescription = daily.getMoonPhase().getDescription();

        // aqi.
        entity.aqiText = daily.getAirQuality().getAqiText();
        entity.aqiIndex = daily.getAirQuality().getAqiIndex();
        entity.pm25 = daily.getAirQuality().getPM25();
        entity.pm10 = daily.getAirQuality().getPM10();
        entity.so2 = daily.getAirQuality().getSO2();
        entity.no2 = daily.getAirQuality().getNO2();
        entity.o3 = daily.getAirQuality().getO3();
        entity.co = daily.getAirQuality().getCO();

        // pollen.
        entity.grassIndex = daily.getPollen().getGrassIndex();
        entity.grassLevel = daily.getPollen().getGrassLevel();
        entity.grassDescription = daily.getPollen().getGrassDescription();
        entity.moldIndex = daily.getPollen().getMoldIndex();
        entity.moldLevel = daily.getPollen().getMoldLevel();
        entity.moldDescription = daily.getPollen().getMoldDescription();
        entity.ragweedIndex = daily.getPollen().getRagweedIndex();
        entity.ragweedLevel = daily.getPollen().getRagweedLevel();
        entity.ragweedDescription = daily.getPollen().getRagweedDescription();
        entity.treeIndex = daily.getPollen().getTreeIndex();
        entity.treeLevel = daily.getPollen().getTreeLevel();
        entity.treeDescription = daily.getPollen().getTreeDescription();

        // uv.
        entity.uvIndex = daily.getUV().getIndex();
        entity.uvLevel = daily.getUV().getLevel();
        entity.uvDescription = daily.getUV().getDescription();

        entity.hoursOfSun = daily.getHoursOfSun();

        return entity;
    }

    public static List<DailyEntity> convertToEntityList(String cityId, WeatherSource source,
                                                        List<Daily> dailyList) {
        List<DailyEntity> entityList = new ArrayList<>(dailyList.size());
        for (Daily daily : dailyList) {
            entityList.add(convertToEntity(cityId, source, daily));
        }
        return entityList;
    }

    public static Daily convertToModule(DailyEntity entity) {
        return new Daily(
                entity.date, entity.time,
                new HalfDay(
                        entity.daytimeWeatherText, entity.daytimeWeatherPhase, entity.daytimeWeatherCode,
                        new Temperature(
                                entity.daytimeTemperature,
                                entity.daytimeRealFeelTemperature,
                                entity.daytimeRealFeelShaderTemperature,
                                entity.daytimeApparentTemperature,
                                entity.daytimeWindChillTemperature,
                                entity.daytimeWetBulbTemperature,
                                entity.daytimeDegreeDayTemperature
                        ),
                        new Precipitation(
                                entity.daytimeTotalPrecipitation,
                                entity.daytimeThunderstormPrecipitation,
                                entity.daytimeRainPrecipitation,
                                entity.daytimeSnowPrecipitation,
                                entity.daytimeIcePrecipitation
                        ),
                        new PrecipitationProbability(
                                entity.daytimeTotalPrecipitationProbability,
                                entity.daytimeThunderstormPrecipitationProbability,
                                entity.daytimeRainPrecipitationProbability,
                                entity.daytimeSnowPrecipitationProbability,
                                entity.daytimeIcePrecipitationProbability
                        ),
                        new PrecipitationDuration(
                                entity.daytimeTotalPrecipitationDuration,
                                entity.daytimeThunderstormPrecipitationDuration,
                                entity.daytimeRainPrecipitationDuration,
                                entity.daytimeSnowPrecipitationDuration,
                                entity.daytimeIcePrecipitationDuration
                        ),
                        new Wind(
                                entity.daytimeWindDirection,
                                entity.daytimeWindDegree,
                                entity.daytimeWindSpeed,
                                entity.daytimeWindLevel
                        ),
                        entity.daytimeCloudCover
                ),
                new HalfDay(
                        entity.nighttimeWeatherText, entity.nighttimeWeatherPhase, entity.nighttimeWeatherCode,
                        new Temperature(
                                entity.nighttimeTemperature,
                                entity.nighttimeRealFeelTemperature,
                                entity.nighttimeRealFeelShaderTemperature,
                                entity.nighttimeApparentTemperature,
                                entity.nighttimeWindChillTemperature,
                                entity.nighttimeWetBulbTemperature,
                                entity.nighttimeDegreeDayTemperature
                        ),
                        new Precipitation(
                                entity.nighttimeTotalPrecipitation,
                                entity.nighttimeThunderstormPrecipitation,
                                entity.nighttimeRainPrecipitation,
                                entity.nighttimeSnowPrecipitation,
                                entity.nighttimeIcePrecipitation
                        ),
                        new PrecipitationProbability(
                                entity.nighttimeTotalPrecipitationProbability,
                                entity.nighttimeThunderstormPrecipitationProbability,
                                entity.nighttimeRainPrecipitationProbability,
                                entity.nighttimeSnowPrecipitationProbability,
                                entity.nighttimeIcePrecipitationProbability
                        ),
                        new PrecipitationDuration(
                                entity.nighttimeTotalPrecipitationDuration,
                                entity.nighttimeThunderstormPrecipitationDuration,
                                entity.nighttimeRainPrecipitationDuration,
                                entity.nighttimeSnowPrecipitationDuration,
                                entity.nighttimeIcePrecipitationDuration
                        ),
                        new Wind(
                                entity.nighttimeWindDirection,
                                entity.nighttimeWindDegree,
                                entity.nighttimeWindSpeed,
                                entity.nighttimeWindLevel
                        ),
                        entity.nighttimeCloudCover
                ),
                new Astro(entity.sunRiseDate, entity.sunSetDate),
                new Astro(entity.moonRiseDate, entity.moonSetDate),
                new MoonPhase(entity.moonPhaseAngle, entity.moonPhaseDescription),
                new AirQuality(
                        entity.aqiText,
                        entity.aqiIndex,
                        entity.pm25,
                        entity.pm10,
                        entity.so2,
                        entity.no2,
                        entity.o3,
                        entity.co
                ),
                new Pollen(
                        entity.grassIndex, entity.grassLevel, entity.grassDescription,
                        entity.moldIndex, entity.moldLevel, entity.moldDescription,
                        entity.ragweedIndex, entity.ragweedLevel, entity.ragweedDescription,
                        entity.treeIndex, entity.treeLevel, entity.treeDescription
                ),
                new UV(entity.uvIndex, entity.uvLevel, entity.uvDescription),
                entity.hoursOfSun
        );
    }

    public static List<Daily> convertToModuleList(List<DailyEntity> entityList) {
        List<Daily> dailyList = new ArrayList<>(entityList.size());
        for (DailyEntity entity : entityList) {
            dailyList.add(convertToModule(entity));
        }
        return dailyList;
    }
}
