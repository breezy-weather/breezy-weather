package wangdaye.com.geometricweather.main.model;

import androidx.annotation.NonNull;

import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.resource.Resource;

public class LocationResource extends Resource<Location> {

    private boolean defaultLocation;
    private boolean locateFailed;

    private Source source;
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

    public boolean isDefaultLocation() {
        return defaultLocation;
    }

    public boolean isLocateFailed() {
        return locateFailed;
    }

    public Source getSource() {
        return source;
    }
}
