package wangdaye.com.geometricweather.service.notification.job;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import wangdaye.com.geometricweather.basic.GeoJobService;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.utils.NotificationUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;

/**
 * Notification job service.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NotificationJobService extends GeoJobService {
    // data.
    public static final int SCHEDULE_CODE = 37;

    /** <br> life cycle. */

    @Override
    protected String readSettings() {
        return null;
    }

    @Override
    protected Location readLocation(String locationName) {
        return DatabaseHelper.getInstance(this).readLocationList().get(0);
    }

    @Override
    protected void doRefresh(Location location) {
        requestData(location);
    }

    @Override
    public void updateView(Context context, Location location, Weather weather) {
        NotificationUtils.buildNotificationAndSendIt(context, weather);
    }
}
