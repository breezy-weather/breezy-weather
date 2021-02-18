package wangdaye.com.geometricweather.background.polling.basic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;

import wangdaye.com.geometricweather.background.polling.PollingUpdateHelper;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.utils.helpters.AsyncHelper;
import wangdaye.com.geometricweather.common.utils.helpters.ShortcutsHelper;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.location.LocationHelper;
import wangdaye.com.geometricweather.remoteviews.NotificationHelper;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Update service.
 * */

public abstract class UpdateService extends Service
        implements PollingUpdateHelper.OnPollingUpdateListener {

    private PollingUpdateHelper mPollingHelper;
    @Inject LocationHelper mLocationHelper;
    @Inject WeatherHelper mWeatherHelper;
    private List<Location> mLocationList;
    private AsyncHelper.Controller mDelayController;
    private boolean mFailed;

    @Override
    public void onCreate() {
        super.onCreate();

        mFailed = false;

        mLocationList = DatabaseHelper.getInstance(this).readLocationList();

        mPollingHelper = new PollingUpdateHelper(
                this, mLocationHelper, mWeatherHelper, mLocationList);
        mPollingHelper.setOnPollingUpdateListener(this);
        mPollingHelper.pollingUpdate();

        mDelayController = AsyncHelper.delayRunOnIO(() -> stopService(true), 30 * 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDelayController != null) {
            mDelayController.cancel();
            mDelayController = null;
        }
        if (mPollingHelper != null) {
            mPollingHelper.setOnPollingUpdateListener(null);
            mPollingHelper.cancel();
            mPollingHelper = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // control.

    public abstract void updateView(Context context, Location location);

    public abstract void updateView(Context context, List<Location> locationList);

    public abstract void handlePollingResult(boolean updateSucceed);

    public void stopService(boolean updateFailed) {
        handlePollingResult(updateFailed);
        stopSelf();
    }

    // interface.

    // on polling update listener.

    @Override
    public void onUpdateCompleted(@NonNull Location location, @Nullable Weather old,
                                  boolean succeed, int index, int total) {
        for (int i = 0; i < mLocationList.size(); i ++) {
            if (mLocationList.get(i).equals(location)) {
                mLocationList.set(i, location);
                if (i == 0) {
                    updateView(this, location);
                    if (succeed) {
                        NotificationHelper.checkAndSendAlert(this, location, old);
                        NotificationHelper.checkAndSendPrecipitationForecast(this, location, old);
                    } else {
                        mFailed = true;
                    }
                }
                return;
            }
        }
    }

    @Override
    public void onPollingCompleted() {
        updateView(this, mLocationList);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutsHelper.refreshShortcutsInNewThread(this, mLocationList);
        }
        stopService(mFailed);
    }
}
