package wangdaye.com.geometricweather.basic.model.resource;

import androidx.annotation.NonNull;

import wangdaye.com.geometricweather.basic.model.Location;

public class LocationResource extends Resource<Location> {

    private boolean locateFailed;
    private boolean updatedInBackground;

    private LocationResource(@NonNull Location data, @NonNull Status status,
                             boolean locateFailed, boolean updatedInBackground) {
        super(data, status);
        this.locateFailed = locateFailed;
        this.updatedInBackground = updatedInBackground;
    }

    public static LocationResource success(@NonNull Location data) {
        return new LocationResource(data, Status.SUCCESS, false, false);
    }

    public static LocationResource success(@NonNull Location data, boolean updatedInBackground) {
        return new LocationResource(data, Status.SUCCESS, false, updatedInBackground);
    }

    public static LocationResource error(@NonNull Location data) {
        return error(data, false);
    }

    public static LocationResource error(@NonNull Location data, boolean locateFailed) {
        return new LocationResource(data, Status.ERROR, locateFailed, false);
    }

    public static LocationResource loading(@NonNull Location data) {
        return loading(data, false);
    }

    public static LocationResource loading(@NonNull Location data, boolean locateFailed) {
        return new LocationResource(data, Status.LOADING, locateFailed, false);
    }

    public boolean isLocateFailed() {
        return locateFailed;
    }

    public boolean isUpdatedInBackground() {
        return updatedInBackground;
    }
}
