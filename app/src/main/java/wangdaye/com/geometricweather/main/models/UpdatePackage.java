package wangdaye.com.geometricweather.main.models;

import wangdaye.com.geometricweather.basic.models.Location;

public class UpdatePackage {

    public Location location;
    public Indicator indicator;

    public UpdatePackage(Location location, Indicator indicator) {
        this.location = location;
        this.indicator = indicator;
    }
}
