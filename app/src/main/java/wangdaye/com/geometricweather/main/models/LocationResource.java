package wangdaye.com.geometricweather.main.models;

import androidx.annotation.NonNull;

import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.basic.models.resources.Resource;

public class LocationResource extends Resource<Location> {

    public final boolean defaultLocation;
    public final boolean locateFailed;
    public final boolean fromBackgroundUpdate;

    public LocationResource(@NonNull Location data, @NonNull Status status,
                            boolean defaultLocation, boolean locateFailed, boolean fromBackgroundUpdate) {
        super(data, status);
        this.defaultLocation = defaultLocation;
        this.locateFailed = locateFailed;
        this.fromBackgroundUpdate = fromBackgroundUpdate;
    }

    public static LocationResource success(@NonNull Location data, boolean defaultLocation, boolean fromBackgroundUpdate) {
        return new LocationResource(data, Status.SUCCESS, defaultLocation, false, fromBackgroundUpdate);
    }

    public static LocationResource error(@NonNull Location data, boolean defaultLocation, boolean fromBackgroundUpdate) {
        return error(data, defaultLocation, false, fromBackgroundUpdate);
    }

    public static LocationResource error(@NonNull Location data, boolean defaultLocation, boolean locateFailed, boolean fromBackgroundUpdate) {
        return new LocationResource(data, Status.ERROR, defaultLocation, locateFailed, fromBackgroundUpdate);
    }

    public static LocationResource loading(@NonNull Location data, boolean defaultLocation, boolean fromBackgroundUpdate) {
        return loading(data, defaultLocation, false, fromBackgroundUpdate);
    }

    public static LocationResource loading(@NonNull Location data, boolean defaultLocation, boolean locateFailed, boolean fromBackgroundUpdate) {
        return new LocationResource(data, Status.LOADING, defaultLocation, locateFailed, fromBackgroundUpdate);
    }
}
