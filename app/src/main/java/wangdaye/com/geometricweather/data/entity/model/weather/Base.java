package wangdaye.com.geometricweather.data.entity.model.weather;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.result.NewRealtimeResult;
import wangdaye.com.geometricweather.data.entity.table.weather.WeatherEntity;

/**
 * Base.
 * */

public class Base {
    // data
    public String cityId;
    public String city;
    public String date;
    public String time;

    /** <br> life cycle. */

    Base() {
    }
/*
    void buildBase(FWResult result) {
        cityId = "CN" + result.cityid;
        city = result.city;
        date = result.realtime.time.split(" ")[0];
        time = result.realtime.time.split(" ")[1].split(":")[0]
                + ":" + result.realtime.time.split(" ")[1].split(":")[1];
    }

    void buildBase(HefengResult result, int p) {
        cityId = result.heWeather.get(p).basic.id;
        city = result.heWeather.get(p).basic.city;
        date = result.heWeather.get(p).basic.update.loc.split(" ")[0];
        time = result.heWeather.get(p).basic.update.loc.split(" ")[1];
    }
*/
    public void buildBase(Location location, NewRealtimeResult result) {
        cityId = location.cityId;
        city = location.city;
        date = result.LocalObservationDateTime.split("T")[0];
        time = result.LocalObservationDateTime.split("T")[1].split(":")[0]
                + ":" + result.LocalObservationDateTime.split("T")[1].split(":")[1];
    }

    void buildBase(WeatherEntity entity) {
        cityId = entity.cityId;
        city = entity.city;
        date = entity.date;
        time = entity.time;
    }
}
