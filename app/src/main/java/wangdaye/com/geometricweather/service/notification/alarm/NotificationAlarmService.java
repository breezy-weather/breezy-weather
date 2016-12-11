package wangdaye.com.geometricweather.service.notification.alarm;

import android.content.Context;

import wangdaye.com.geometricweather.basic.GeoAlarmService;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.utils.NotificationUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;

/**
 * Notification alarm service.
 * */

public class NotificationAlarmService extends GeoAlarmService {
    // data
    public static final int ALARM_CODE = 7;

    /** <br> life cycle. */

    public NotificationAlarmService() {
        super("NotificationAlarmService");
    }

    public NotificationAlarmService(String name) {
        super(name);
    }

    @Override
    protected Location readSettings() {
        return DatabaseHelper.getInstance(this).readLocationList().get(0);
    }

    @Override
    protected void doRefresh(Location location) {
        requestData(location);
        setAlarmIntent(this, getClass(), ALARM_CODE);
    }

    @Override
    public void updateView(Context context, Weather weather) {
        NotificationUtils.buildNotificationAndSendIt(context, weather);
    }
}
