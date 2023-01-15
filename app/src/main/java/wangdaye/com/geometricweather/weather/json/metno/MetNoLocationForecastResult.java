package wangdaye.com.geometricweather.weather.json.metno;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * MET Norway location forecast.
 **/
public class MetNoLocationForecastResult {
    public Properties properties;

    public static class Properties {
        public Meta meta;
        public List<Timeseries> timeseries;

        public static class Meta {
            @SerializedName("updated_at")
            public Date updatedAt;
        }
        public static class Timeseries {
            public Date time;
            public Data data;

            public static class Data {
                public Instant instant;
                @SerializedName("next_12_hours")
                public NextHours next12Hours;
                @SerializedName("next_1_hours")
                public NextHours next1Hours;
                @SerializedName("next_6_hours")
                public NextHours next6Hours;

                public static class Summary {
                    @SerializedName("symbol_code")
                    public String symbolCode;
                }
                // All of them are nullable
                public static class Details {
                    @SerializedName("air_pressure_at_sea_level")
                    public Float airPressureAtSeaLevel;
                    @SerializedName("air_temperature")
                    public Float airTemperature;
                    @SerializedName("precipitation_amount")
                    public Float precipitationAmount;
                    @SerializedName("probability_of_precipitation")
                    public Float probabilityOfPrecipitation;
                    @SerializedName("probability_of_thunder")
                    public Float probabilityOfThunder;
                    @SerializedName("relative_humidity")
                    public Float relativeHumidity;
                    @SerializedName("ultraviolet_index_clear_sky")
                    public Float ultravioletIndexClearSky; // Nullable
                    @SerializedName("wind_from_direction")
                    public Float windFromDirection;
                    @SerializedName("wind_speed")
                    public Float windSpeed;
                }

                public static class Instant {
                    public Details details;
                }

                public static class NextHours {
                    public Summary summary;
                    public Details details;

                }
            }
        }
    }
}
