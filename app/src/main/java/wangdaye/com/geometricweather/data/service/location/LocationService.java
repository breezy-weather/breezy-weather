package wangdaye.com.geometricweather.data.service.location;

import android.content.Context;
import android.support.annotation.Nullable;

/**
 * Location service.
 * */

public abstract class LocationService {

    public class Result {
        public String district;
        public String city;
        public String province;
        public String contry;
        public String latitude;
        public String longitude;
        public boolean inChina;
    }

    public abstract void requestLocation(Context context, LocationCallback callback);

    public abstract void cancel();

    // interface.

    public interface LocationCallback {
        void onCompleted(@Nullable Result result);
    }
}
