package wangdaye.com.geometricweather.background.polling.basic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.remoteviews.NotificationUtils;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.background.polling.PollingUpdateHelper;
import wangdaye.com.geometricweather.utils.manager.ShortcutsManager;

/**
 * Update service.
 * */

public abstract class UpdateService extends Service
        implements PollingUpdateHelper.OnPollingUpdateListener {

    private PollingUpdateHelper helper;
    private List<Location> locationList;

    private boolean failed;

    @Override
    public void onCreate() {
        super.onCreate();
        failed = false;
        locationList = DatabaseHelper.getInstance(this).readLocationList();
        helper = new PollingUpdateHelper(this, locationList);
        helper.setOnPollingUpdateListener(this);
        helper.pollingUpdate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    public abstract void updateView(Context context, Location location);

    public abstract void handlePollingResult(boolean failed);

    public abstract void stopService();

    // interface.

    // on polling update listener.

    @Override
    public void onUpdateCompleted(@NonNull Location location, @Nullable Weather old,
                                  boolean succeed, int index, int total) {
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).equals(location)) {
                locationList.set(i, location);
                if (i == 0) {
                    updateView(this, location);
                    if (succeed) {
                        NotificationUtils.checkAndSendAlert(this, location, old);
                        NotificationUtils.checkAndSendPrecipitationForecast(this, location, old);
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
        handlePollingResult(failed);
        stopService();
    }
}
