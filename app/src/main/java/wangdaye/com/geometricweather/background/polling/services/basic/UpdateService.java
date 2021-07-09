package wangdaye.com.geometricweather.background.polling.services.basic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import wangdaye.com.geometricweather.background.polling.PollingUpdateHelper;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.utils.helpers.AsyncHelper;
import wangdaye.com.geometricweather.common.utils.helpers.ShortcutsHelper;
import wangdaye.com.geometricweather.location.LocationHelper;
import wangdaye.com.geometricweather.remoteviews.NotificationHelper;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Update service.
 * */

public abstract class UpdateService extends Service
        implements PollingUpdateHelper.PollingResponder {

    private PollingUpdateHelper mPollingHelper;
    @Inject LocationHelper mLocationHelper;
    @Inject WeatherHelper mWeatherHelper;
    private AsyncHelper.Controller mDelayController;
    private boolean mFailed;

    @Override
    public void onCreate() {
        super.onCreate();

        mFailed = false;

        mPollingHelper = new PollingUpdateHelper(this, mLocationHelper, mWeatherHelper, this);
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
    public void responseSingleRequest(@NonNull Location location, @Nullable Weather old,
                                      boolean succeed, int index, int total) {
        if (index == 0) {
            updateView(this, location);
            if (succeed) {
                NotificationHelper.checkAndSendAlert(this, location, old);
                NotificationHelper.checkAndSendPrecipitationForecast(this, location, old);
            } else {
                mFailed = true;
            }
        }
    }

    @Override
    public void responsePolling(@NotNull List<? extends Location> locationList) {
        List<Location> list = new ArrayList<>(locationList);

        updateView(this, list);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutsHelper.refreshShortcutsInNewThread(this, list);
        }
        stopService(mFailed);
    }
}
