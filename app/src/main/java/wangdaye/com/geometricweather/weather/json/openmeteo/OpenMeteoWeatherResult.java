package wangdaye.com.geometricweather.weather.json.openmeteo;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Open-Meteo weather
 **/
public class OpenMeteoWeatherResult {
    @SerializedName("current_weather")
    public CurrentWeather currentWeather;
    public Daily daily;
    public Hourly hourly;

    public static class CurrentWeather {
        public Float temperature;
        @SerializedName("windspeed")
        public Float windSpeed;
        @SerializedName("winddirection")
        public Integer windDirection;
        @SerializedName("weathercode")
        public Integer weatherCode;
        @SerializedName("is_day")
        public int isDay; /* Should be a boolean (true or false) but API returns an integer */
        public long time;
    }

    public static class Daily {
        public long[] time;
        @SerializedName("temperature_2m_max")
        public Float[] temperatureMax;
        @SerializedName("temperature_2m_min")
        public Float[] temperatureMin;
        @SerializedName("apparent_temperature_max")
        public Float[] apparentTemperatureMax;
        @SerializedName("apparent_temperature_min")
        public Float[] apparentTemperatureMin;
        public Long[] sunrise;
        public Long[] sunset;
        @SerializedName("uv_index_max")
        public Float[] uvIndexMax;
    }

    public static class Hourly {
        public long[] time;
        @SerializedName("temperature_2m")
        public Float[] temperature;
        @SerializedName("apparent_temperature")
        public Float[] apparentTemperature;
        @SerializedName("precipitation_probability")
        public Integer[] precipitationProbability;
        public Float[] precipitation;
        public Float[] rain;
        public Float[] showers;
        public Float[] snowfall;
        @SerializedName("weathercode")
        public Integer[] weatherCode;
        @SerializedName("windspeed_10m")
        public Float[] windSpeed;
        @SerializedName("winddirection_10m")
        public Integer[] windDirection;
        @SerializedName("uv_index")
        public Float[] uvIndex;
        @SerializedName("is_day")
        public int[] isDay; /* Should be a boolean (true or false) but API returns an integer */
    }
}
