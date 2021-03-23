package wangdaye.com.geometricweather.background.polling.work.worker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import java.util.List;

import wangdaye.com.geometricweather.background.polling.PollingUpdateHelper;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.location.LocationHelper;
import wangdaye.com.geometricweather.remoteviews.NotificationHelper;
import wangdaye.com.geometricweather.common.utils.helpers.ShortcutsHelper;
import wangdaye.com.geometricweather.weather.WeatherHelper;

public abstract class AsyncUpdateWorker extends AsyncWorker
        implements PollingUpdateHelper.OnPollingUpdateListener {

    private final PollingUpdateHelper mPollingUpdateHelper;
    private final List<Location> mLocationList;

    private SettableFuture<Result> mFuture;
    private boolean mFailed;

    public AsyncUpdateWorker(@NonNull Context context,
                             @NonNull WorkerParameters workerParams,
                             LocationHelper locationHelper,
                             WeatherHelper weatherHelper) {
        super(context, workerParams);

        mLocationList = DatabaseHelper.getInstance(context).readLocationList();

        mPollingUpdateHelper = new PollingUpdateHelper(
                context, locationHelper, weatherHelper, mLocationList);
        mPollingUpdateHelper.setOnPollingUpdateListener(this);
    }

    @Override
    public void doAsyncWork(SettableFuture<Result> f) {
        mFuture = f;
        mFailed = false;

        mPollingUpdateHelper.pollingUpdate();
    }

    // control.

    public abstract void updateView(Context context, Location location);

    public abstract void updateView(Context context, List<Location> locationList);

    /**
     * Call {@link SettableFuture#set(Object)} here.
     * */
    public abstract void handleUpdateResult(SettableFuture<Result> future, boolean failed);

    // interface.

    // on polling update listener.

    @Override
    public void onUpdateCompleted(@NonNull Location location, @Nullable Weather old,
                                  boolean succeed, int index, int total) {
        for (int i = 0; i < mLocationList.size(); i ++) {
            if (mLocationList.get(i).equals(location)) {
                mLocationList.set(i, location);
                if (i == 0) {
                    updateView(getApplicationContext(), location);
                    if (succeed) {
                        NotificationHelper.checkAndSendAlert(getApplicationContext(), location, old);
                        NotificationHelper.checkAndSendPrecipitationForecast(getApplicationContext(), location, old);
                    } else {
                        mFailed = true;
                    }
                }
                return;
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onPollingCompleted() {
        updateView(getApplicationContext(), mLocationList);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutsHelper.refreshShortcutsInNewThread(getApplicationContext(), mLocationList);
        }
        handleUpdateResult(mFuture, mFailed);
    }
}
