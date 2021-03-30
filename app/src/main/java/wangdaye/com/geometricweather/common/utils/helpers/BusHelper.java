package wangdaye.com.geometricweather.common.utils.helpers;

import androidx.lifecycle.Observer;

import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.bus.DataBus;

public class BusHelper {

    private static final String KEY_LOCATION_CHANGED = "KEY_LOCATION_LIST_CACHE_CHANGED";

    public static void postLocationChanged(Location location) {
        DataBus.getInstance().with(KEY_LOCATION_CHANGED, Location.class).postValue(location);
    }

    public static void observeLocationChangedForever(Observer<Location> observer) {
        DataBus.getInstance().with(KEY_LOCATION_CHANGED, Location.class).observeForever(observer);
    }

    public static void cancelObserveLocationChanged(Observer<Location> observer) {
        DataBus.getInstance().with(KEY_LOCATION_CHANGED, Location.class).removeObserver(observer);
    }
}
