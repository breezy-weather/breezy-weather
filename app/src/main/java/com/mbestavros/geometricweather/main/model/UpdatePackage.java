package com.mbestavros.geometricweather.main.model;

import com.mbestavros.geometricweather.basic.model.location.Location;

public class UpdatePackage {

    public Location location;
    public Indicator indicator;

    public UpdatePackage(Location location, Indicator indicator) {
        this.location = location;
        this.indicator = indicator;
    }
}
