package wangdaye.com.geometricweather.weather.json.openmeteo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Open Meteo geocoding
 **/
public class OpenMeteoLocationResults {

    public List<Result> results;

    public static class Result {
        public Integer id;
        public String name;
        public float latitude;
        public float longitude;
        public String timezone;
        @SerializedName("country_code")
        public String countryCode;
        public String country;
        public String admin1;
        public String admin2;
        public String admin3;
        public String admin4;
    }
}