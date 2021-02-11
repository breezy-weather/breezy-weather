package wangdaye.com.geometricweather.main.models;

import androidx.annotation.NonNull;

import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.basic.models.resources.Resource;

public class LocationResource extends Resource<Location> {

    public final boolean defaultLocation;
    public final boolean locateFailed;

    public final Source source;
    public enum  Source {
        SWITCH, REFRESH, BACKGROUND
    }

    private LocationResource(@NonNull Location data, @NonNull Status status,
                             boolean defaultLocation, boolean locateFailed, Source source) {
        super(data, status);
        this.defaultLocation = defaultLocation;
        this.locateFailed = locateFailed;
        this.source = source;
    }

    public static LocationResource success(@NonNull Location data, boolean defaultLocation, Source source) {
        return new LocationResource(data, Status.SUCCESS, defaultLocation, false, source);
    }

    public static LocationResource error(@NonNull Location data, boolean defaultLocation, Source source) {
        return error(data, defaultLocation, false, source);
    }

    public static LocationResource error(@NonNull Location data, boolean defaultLocation, boolean locateFailed, Source source) {
        return new LocationResource(data, Status.ERROR, defaultLocation, locateFailed, source);
    }

    public static LocationResource loading(@NonNull Location data, boolean defaultLocation, Source source) {
        return loading(data, defaultLocation, false, source);
    }

    public static LocationResource loading(@NonNull Location data, boolean defaultLocation, boolean locateFailed, Source source) {
        return new LocationResource(data, Status.LOADING, defaultLocation, locateFailed, source);
    }
}
