package wangdaye.com.geometricweather.weather.json.nominatim;

import java.util.List;

/**
 * Nominatim location result.
 * */

public class NominatimLocationResult {

    public Integer place_id;
    public Float lat;
    public Float lon;
    public String display_name;
    public Address address;

    public static class Address {
        public String city;
        public String state_district;
        public String state;
        public String country;
        public String country_code;
    }
}
