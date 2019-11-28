package wangdaye.com.geometricweather.main.model;

import androidx.annotation.NonNull;

import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.resource.Resource;

public class LocationResource extends Resource<Location> {

    private boolean defaultLocation;
    private boolean locateFailed;
    private boolean updatedInBackground;

    private LocationResource(@NonNull Location data, @NonNull Status status,
                             boolean defaultLocation, boolean locateFailed, boolean updatedInBackground) {
        super(data, status);
        this.defaultLocation = defaultLocation;
        this.locateFailed = locateFailed;
        this.updatedInBackground = updatedInBackground;
    }

    public static LocationResource success(@NonNull Location data, boolean defaultLocation) {
        return new LocationResource(data, Status.SUCCESS, defaultLocation, false, false);
    }

    public static LocationResource success(@NonNull Location data, boolean defaultLocation, boolean updatedInBackground) {
        return new LocationResource(data, Status.SUCCESS, defaultLocation, false, updatedInBackground);
    }

    public static LocationResource error(@NonNull Location data, boolean defaultLocation) {
        return error(data, defaultLocation, false);
    }

    public static LocationResource error(@NonNull Location data, boolean defaultLocation, boolean locateFailed) {
        return new LocationResource(data, Status.ERROR, defaultLocation, locateFailed, false);
    }

    public static LocationResource loading(@NonNull Location data, boolean defaultLocation) {
        return loading(data, defaultLocation, false);
    }

    public static LocationResource loading(@NonNull Location data, boolean defaultLocation, boolean locateFailed) {
        return new LocationResource(data, Status.LOADING, defaultLocation, locateFailed, false);
    }

    public boolean isDefaultLocation() {
        return defaultLocation;
    }

    public boolean isLocateFailed() {
        return locateFailed;
    }

    public boolean isUpdatedInBackground() {
        return updatedInBackground;
    }
}
