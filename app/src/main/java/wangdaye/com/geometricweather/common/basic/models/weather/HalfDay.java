package wangdaye.com.geometricweather.common.basic.models.weather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Half day.
 * */
public class HalfDay implements Serializable {

    @NonNull private final String weatherText;
    @NonNull private final String weatherPhase;
    @NonNull private final WeatherCode weatherCode;

    @NonNull private final Temperature temperature;
    @NonNull private final Precipitation precipitation;
    @NonNull private final PrecipitationProbability precipitationProbability;
    @NonNull private final PrecipitationDuration precipitationDuration;
    @NonNull private final Wind wind;

    @Nullable private final Integer cloudCover;

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