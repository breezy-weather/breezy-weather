package wangdaye.com.geometricweather.basic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.NotificationUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.PollingUpdateHelper;
import wangdaye.com.geometricweather.utils.manager.ShortcutsManager;

/**
 * Alarm update service.
 * */

public abstract class UpdateService extends Service
        implements PollingUpdateHelper.OnPollingUpdateListener {

    private PollingUpdateHelper helper;

    private List<Location> locationList;
    private boolean refreshing;

    private boolean failed;

    @Override
    public void onCreate() {
        super.onCreate();
        this.refreshing = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (!refreshing) {
            refreshing = true;
            failed = false;
            locationList = DatabaseHelper.getInstance(this).readLocationList();
            helper = new PollingUpdateHelper(this, locationList);
            helper.setOnPollingUpdateListener(this);
            helper.pollingUpdate();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        refreshing = false;
        if (helper != null) {
            helper.setOnPollingUpdateListener(null);
            helper.cancel();
            helper = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // control.

    public abstract void updateView(Context context, Location location, @Nullable Weather weather);

    public abstract void setDelayTask(boolean failed);

    // interface.

    // on polling updateRotation listener.

    @Override
    public void onUpdateCompleted(Location location, Weather weather, Weather old, boolean succeed) {
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).equals(location)) {
                location.weather = weather;
                locationList.set(i, location);
                if (i == 0) {
                    updateView(this, location, weather);
                    if (succeed) {
                        NotificationUtils.checkAndSendAlert(this, weather, old);
                    } else {
                        failed = true;
                    }
                }
                return;
            }
        }
    }

    @Override
    public void onPollingCompleted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutsManager.refreshShortcutsInNewThread(this, locationList);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setDelayTask(failed);
        }
        stopSelf();
    }
}
