package wangdaye.com.geometricweather.Data;

/**
 * Created by WangDaYe on 2016/2/4.
 */

public class Location {
    // data
    public String location;
    public boolean update;
    public JuheResult juheResult;
    public HefengResult hefengResult;

    public Location(String location) {
        this.location = location;
        this.update = false;
        juheResult = null;
        hefengResult = null;
    }

    public Location(String location, boolean update) {
        this.location = location;
        this.update = update;
        juheResult = null;
        hefengResult = null;
    }

    public Location(String location, boolean update, JuheResult juheResult, HefengResult hefengResult) {
        this.location = location;
        this.update = update;
        this.juheResult = juheResult;
        this.hefengResult = hefengResult;
    }
}
