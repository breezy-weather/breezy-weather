package wangdaye.com.geometricweather.Data;

/**
 * Created by WangDaYe on 2016/2/4.
 */

public class Location {
    // data
    public String location;
    public boolean update;
    public GsonResult gsonResult;

    public Location(String location) {
        this.location = location;
        this.update = false;
        gsonResult = null;
    }

    public Location(String location, boolean update) {
        this.location = location;
        this.update = update;
        gsonResult = null;
    }

    public Location(String location, boolean update, GsonResult gsonResult) {
        this.location = location;
        this.update = update;
        this.gsonResult = gsonResult;
    }
}
