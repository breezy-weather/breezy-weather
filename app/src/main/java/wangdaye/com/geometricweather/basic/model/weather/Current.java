package wangdaye.com.geometricweather.basic.model.weather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Current.
 *
 * default unit
 * {@link #relativeHumidity} : {@link wangdaye.com.geometricweather.basic.model.option.unit.RelativeHumidityUnit#PERCENT}
 * {@link #dewPoint} : {@link wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit#C}
 * {@link #visibility} : {@link wangdaye.com.geometricweather.basic.model.option.unit.DistanceUnit#KM}
 * {@link #ceiling} : {@link wangdaye.com.geometricweather.basic.model.option.unit.DistanceUnit#KM}
 * */
public class Current {

    @NonNull private String weatherText;
    @NonNull private WeatherCode weatherCode;

    @NonNull private Temperature temperature;
    @NonNull private Precipitation precipitation;
    @NonNull private PrecipitationProbability precipitationProbability;
    @NonNull private Wind wind;
    @NonNull private UV uv;
    @NonNull private AirQuality airQuality;

    @Nullable private Float relativeHumidity;
    @Nullable private Float pressure;
    @Nullable private Float visibility;
    @Nullable private Integer dewPoint;
    @Nullable private Integer cloudCover;
    @Nullable private Float ceiling;

    @Nullable private String dailyForecast;
    @Nullable private String hourlyForecast;

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
