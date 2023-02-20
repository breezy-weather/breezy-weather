package wangdaye.com.geometricweather.db.generators;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly;
import wangdaye.com.geometricweather.common.basic.models.weather.Precipitation;
import wangdaye.com.geometricweather.common.basic.models.weather.PrecipitationProbability;
import wangdaye.com.geometricweather.common.basic.models.weather.Temperature;
import wangdaye.com.geometricweather.common.basic.models.weather.UV;
import wangdaye.com.geometricweather.common.basic.models.weather.Wind;
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter;
import wangdaye.com.geometricweather.db.entities.HourlyEntity;

public class HourlyEntityGenerator {

    public static HourlyEntity generate(String cityId, WeatherSource source, Hourly hourly) {
        HourlyEntity entity = new HourlyEntity();

        entity.cityId = cityId;
        entity.weatherSource = new WeatherSourceConverter().convertToDatabaseValue(source);

        entity.date = hourly.getDate();
        entity.time = hourly.getTime();
        entity.daylight = hourly.isDaylight();

        entity.weatherCode = hourly.getWeatherCode();
        entity.weatherText = hourly.getWeatherText();

        entity.temperature = hourly.getTemperature().getTemperature();
        entity.realFeelTemperature = hourly.getTemperature().getRealFeelTemperature();
        entity.realFeelShaderTemperature = hourly.getTemperature().getRealFeelShaderTemperature();
        entity.apparentTemperature = hourly.getTemperature().getApparentTemperature();
        entity.windChillTemperature = hourly.getTemperature().getWindChillTemperature();
        entity.wetBulbTemperature = hourly.getTemperature().getWetBulbTemperature();
        entity.degreeDayTemperature = hourly.getTemperature().getDegreeDayTemperature();

        entity.totalPrecipitation = hourly.getPrecipitation().getTotal();
        entity.thunderstormPrecipitation = hourly.getPrecipitation().getThunderstorm();
        entity.rainPrecipitation = hourly.getPrecipitation().getRain();
        entity.snowPrecipitation = hourly.getPrecipitation().getSnow();
        entity.icePrecipitation = hourly.getPrecipitation().getIce();

        entity.totalPrecipitationProbability = hourly.getPrecipitationProbability().getTotal();
        entity.thunderstormPrecipitationProbability = hourly.getPrecipitationProbability().getThunderstorm();
        entity.rainPrecipitationProbability = hourly.getPrecipitationProbability().getRain();
        entity.snowPrecipitationProbability = hourly.getPrecipitationProbability().getSnow();
        entity.icePrecipitationProbability = hourly.getPrecipitationProbability().getIce();

        entity.windDirection = hourly.getWind().getDirection();
        entity.windDegree = hourly.getWind().getDegree();
        entity.windSpeed = hourly.getWind().getSpeed();
        entity.windLevel = hourly.getWind().getLevel();

        // uv.
        entity.uvIndex = hourly.getUV().getIndex();
        entity.uvLevel = hourly.getUV().getLevel();
        entity.uvDescription = hourly.getUV().getDescription();

        return entity;
    }

    public static List<HourlyEntity> generateEntityList(String cityId, WeatherSource source,
                                                        List<Hourly> hourlyList) {
        List<HourlyEntity> entityList = new ArrayList<>(hourlyList.size());
        for (Hourly hourly : hourlyList) {
            entityList.add(generate(cityId, source, hourly));
        }
        return entityList;
    }

    public static Hourly generate(HourlyEntity entity) {
        return new Hourly(
                entity.date, entity.time, entity.daylight,
                entity.weatherText, entity.weatherCode,
                new Temperature(
                        entity.temperature,
                        entity.realFeelTemperature,
                        entity.realFeelShaderTemperature,
                        entity.apparentTemperature,
                        entity.windChillTemperature,
                        entity.wetBulbTemperature,
                        entity.degreeDayTemperature
                ),
                new Precipitation(
                        entity.totalPrecipitation,
                        entity.thunderstormPrecipitation,
                        entity.rainPrecipitation,
                        entity.snowPrecipitation,
                        entity.icePrecipitation
                ),
                new PrecipitationProbability(
                        entity.totalPrecipitationProbability,
                        entity.thunderstormPrecipitationProbability,
                        entity.rainPrecipitationProbability,
                        entity.snowPrecipitationProbability,
                        entity.icePrecipitationProbability
                ),
                new Wind(
                        entity.windDirection,
                        entity.windDegree,
                        entity.windSpeed,
                        entity.windLevel
                ),
                new UV(entity.uvIndex, entity.uvLevel, entity.uvDescription)
        );
    }

    public static List<Hourly> generateModuleList(List<HourlyEntity> entityList) {
        List<Hourly> dailyList = new ArrayList<>(entityList.size());
        for (HourlyEntity entity : entityList) {
            dailyList.add(generate(entity));
        }
        return dailyList;
    }
}