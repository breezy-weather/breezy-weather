package wangdaye.com.geometricweather.basic.model.weather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Half day.
 * */
public class HalfDay {

    @NonNull private String weatherText;
    @NonNull private String weatherPhase;
    @NonNull private WeatherCode weatherCode;

    @NonNull private Temperature temperature;
    @NonNull private Precipitation precipitation;
    @NonNull private PrecipitationProbability precipitationProbability;
    @NonNull private PrecipitationDuration precipitationDuration;
    @NonNull private Wind wind;

    @Nullable private Integer cloudCover;

    public HalfDay(@NonNull String weatherText, @NonNull String weatherPhase, @NonNull WeatherCode weatherCode,
                   @NonNull Temperature temperature,
                   @NonNull Precipitation precipitation,
                   @NonNull PrecipitationProbability precipitationProbability,
                   @NonNull PrecipitationDuration precipitationDuration,
                   @NonNull Wind wind, @Nullable Integer cloudCover) {
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

    @NonNull
    public String getWeatherText() {
        return weatherText;
    }

    @NonNull
    public String getWeatherPhase() {
        return weatherPhase;
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
    public PrecipitationDuration getPrecipitationDuration() {
        return precipitationDuration;
    }

    @NonNull
    public Wind getWind() {
        return wind;
    }

    @Nullable
    public Integer getCloudCover() {
        return cloudCover;
    }
}