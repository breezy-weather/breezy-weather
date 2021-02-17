package wangdaye.com.geometricweather.common.basic.models.weather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

import wangdaye.com.geometricweather.common.basic.models.options.unit.DistanceUnit;
import wangdaye.com.geometricweather.common.basic.models.options.unit.RelativeHumidityUnit;
import wangdaye.com.geometricweather.common.basic.models.options.unit.TemperatureUnit;

/**
 * Current.
 *
 * default unit
 * {@link #relativeHumidity} : {@link RelativeHumidityUnit#PERCENT}
 * {@link #dewPoint} : {@link TemperatureUnit#C}
 * {@link #visibility} : {@link DistanceUnit#KM}
 * {@link #ceiling} : {@link DistanceUnit#KM}
 * */
public class Current implements Serializable {

    @NonNull private final String weatherText;
    @NonNull private final WeatherCode weatherCode;

    @NonNull private final Temperature temperature;
    @NonNull private final Precipitation precipitation;
    @NonNull private final PrecipitationProbability precipitationProbability;
    @NonNull private final Wind wind;
    @NonNull private final UV uv;
    @NonNull private final AirQuality airQuality;

    @Nullable private final Float relativeHumidity;
    @Nullable private final Float pressure;
    @Nullable private final Float visibility;
    @Nullable private final Integer dewPoint;
    @Nullable private final Integer cloudCover;
    @Nullable private final Float ceiling;

    @Nullable private final String dailyForecast;
    @Nullable private final String hourlyForecast;

    public Current(@NonNull String weatherText, @NonNull WeatherCode weatherCode,
                   @NonNull Temperature temperature,
                   @NonNull Precipitation precipitation, @NonNull PrecipitationProbability precipitationProbability,
                   @NonNull Wind wind, @NonNull UV uv, @NonNull AirQuality airQuality,
                   @Nullable Float relativeHumidity, @Nullable Float pressure, @Nullable Float visibility,
                   @Nullable Integer dewPoint, @Nullable Integer cloudCover, @Nullable Float ceiling,
                   @Nullable String dailyForecast, @Nullable String hourlyForecast) {
        this.weatherText = weatherText;
        this.weatherCode = weatherCode;
        this.temperature = temperature;
        this.precipitation = precipitation;
        this.precipitationProbability = precipitationProbability;
        this.wind = wind;
        this.uv = uv;
        this.airQuality = airQuality;
        this.relativeHumidity = relativeHumidity;
        this.pressure = pressure;
        this.visibility = visibility;
        this.dewPoint = dewPoint;
        this.cloudCover = cloudCover;
        this.ceiling = ceiling;
        this.dailyForecast = dailyForecast;
        this.hourlyForecast = hourlyForecast;
    }

    @NonNull
    public String getWeatherText() {
        return weatherText;
    }

    @NonNull
    public WeatherCode getWeatherCode() {
        return weatherCode;
    }

    @NonNull
    public Temperature getTemperature() {
        return temperature;
    }

    @NonNull
    public Precipitation getPrecipitation() {
        return precipitation;
    }

    @NonNull
    public PrecipitationProbability getPrecipitationProbability() {
        return precipitationProbability;
    }

    @NonNull
    public Wind getWind() {
        return wind;
    }

    @NonNull
    public UV getUV() {
        return uv;
    }

    @NonNull
    public AirQuality getAirQuality() {
        return airQuality;
    }

    @Nullable
    public Float getRelativeHumidity() {
        return relativeHumidity;
    }

    @Nullable
    public Float getPressure() {
        return pressure;
    }

    @Nullable
    public Float getVisibility() {
        return visibility;
    }

    @Nullable
    public Integer getDewPoint() {
        return dewPoint;
    }

    @Nullable
    public Integer getCloudCover() {
        return cloudCover;
    }

    @Nullable
    public Float getCeiling() {
        return ceiling;
    }

    @Nullable
    public String getDailyForecast() {
        return dailyForecast;
    }

    @Nullable
    public String getHourlyForecast() {
        return hourlyForecast;
    }
}
