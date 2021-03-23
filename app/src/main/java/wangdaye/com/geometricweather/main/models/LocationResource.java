package wangdaye.com.geometricweather.main.models;

import androidx.annotation.NonNull;

import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.resources.Resource;

public class LocationResource extends Resource<Location> {

    public final boolean defaultLocation;
    public final boolean locateFailed;
    
    public final Event event;
    public enum Event {
        INITIALIZE, BACKGROUND_UPDATE, UPDATE
    }

    public LocationResource(@NonNull Location data, @NonNull Status status,
                            boolean defaultLocation, boolean locateFailed, Event event) {
        super(data, status);
        this.defaultLocation = defaultLocation;
        this.locateFailed = locateFailed;
        this.event = event;
    }

    public static LocationResource success(@NonNull Location data, boolean defaultLocation, Event event) {
        return new LocationResource(data, Status.SUCCESS, defaultLocation, false, event);
    }

    public static LocationResource error(@NonNull Location data, boolean defaultLocation, Event event) {
        return error(data, defaultLocation, false, event);
    }

    public static LocationResource error(@NonNull Location data, boolean defaultLocation, boolean locateFailed, Event event) {
        return new LocationResource(data, Status.ERROR, defaultLocation, locateFailed, event);
    }

    public static LocationResource loading(@NonNull Location data, boolean defaultLocation, Event event) {
        return loading(data, defaultLocation, false, event);
    }

    public static LocationResource loading(@NonNull Location data, boolean defaultLocation, boolean locateFailed, Event event) {
        return new LocationResource(data, Status.LOADING, defaultLocation, locateFailed, event);
    }
}
