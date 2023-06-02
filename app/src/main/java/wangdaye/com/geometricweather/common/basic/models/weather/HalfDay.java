package wangdaye.com.geometricweather.common.basic.models.weather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Half day.
 * */
public class HalfDay implements Serializable {

    @Nullable private String weatherText;
    @Nullable private String weatherPhase;
    @Nullable private WeatherCode weatherCode;

    @Nullable private Temperature temperature;
    @Nullable private Precipitation precipitation;
    @Nullable private PrecipitationProbability precipitationProbability;
    @Nullable private PrecipitationDuration precipitationDuration;
    @Nullable private Wind wind;

    @Nullable private Integer cloudCover;

    // Initialize a null object
    public HalfDay() {
        this.weatherText = null;
        this.weatherPhase = null;
        this.weatherCode = null;
        this.temperature = null;
        this.precipitation = null;
        this.precipitationProbability = null;
        this.precipitationDuration = null;
        this.wind = null;
        this.cloudCover = null;
    }

    public HalfDay(@Nullable String weatherText, @Nullable String weatherPhase, @Nullable WeatherCode weatherCode,
                   @Nullable Temperature temperature,
                   @Nullable Precipitation precipitation,
                   @Nullable PrecipitationProbability precipitationProbability,
                   @Nullable PrecipitationDuration precipitationDuration,
                   @Nullable Wind wind, @Nullable Integer cloudCover) {
        this.weatherText = weatherText;
        this.weatherPhase = weatherPhase;
        this.weatherCode = weatherCode;
        this.temperature = temperature;
        this.precipitation = precipitation;
        this.precipitationProbability = precipitationProbability;
        this.precipitationDuration = precipitationDuration;
        this.wind = wind;
        this.cloudCover = cloudCover;
    }

    @Nullable
    public String getWeatherText() {
        return weatherText;
    }

    public void setWeatherText(@Nullable String weatherText) {
        this.weatherText = weatherText;
    }

    @Nullable
    public String getWeatherPhase() {
        return weatherPhase;
    }

    public void setWeatherPhase(@Nullable String weatherPhase) {
        this.weatherPhase = weatherPhase;
    }

    @Nullable
    public WeatherCode getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(@Nullable WeatherCode weatherCode) {
        this.weatherCode = weatherCode;
    }

    @Nullable
    public Temperature getTemperature() {
        return temperature;
    }

    @Nullable
    public Precipitation getPrecipitation() {
        return precipitation;
    }

    @Nullable
    public PrecipitationProbability getPrecipitationProbability() {
        return precipitationProbability;
    }

    @Nullable
    public PrecipitationDuration getPrecipitationDuration() {
        return precipitationDuration;
    }

    @Nullable
    public Wind getWind() {
        return wind;
    }

    @Nullable
    public Integer getCloudCover() {
        return cloudCover;
    }
}