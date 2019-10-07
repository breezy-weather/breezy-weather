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
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.remoteviews.NotificationUtils;
import wangdaye.com.geometricweather.utils.manager.ShortcutsManager;

public abstract class AsyncUpdateWorker extends AsyncWorker
        implements PollingUpdateHelper.OnPollingUpdateListener {

    private PollingUpdateHelper helper;
    private List<Location> locationList;

    private SettableFuture<Result> future;
    private boolean failed;

    public AsyncUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        locationList = DatabaseHelper.getInstance(context).readLocationList();

        helper = new PollingUpdateHelper(context, locationList);
        helper.setOnPollingUpdateListener(this);
    }

    @Override
    public void doAsyncWork(SettableFuture<Result> f) {
        future = f;
        failed = false;

        helper.pollingUpdate();
    }

    // control.

    public abstract void updateView(Context context, Location location);

    /**
     * Call {@link SettableFuture#set(Object)} here.
     * */
    public abstract void handleUpdateResult(SettableFuture<Result> future, boolean failed);

    // interface.

    // on polling update listener.

    @Override
    public void onUpdateCompleted(@NonNull Location location, @Nullable Weather old,
                                  boolean succeed, int index, int total) {
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).equals(location)) {
                locationList.set(i, location);
                if (i == 0) {
                    updateView(getApplicationContext(), location);
                    if (succeed) {
                        NotificationUtils.checkAndSendAlert(
                                getApplicationContext(), location, old);
                        NotificationUtils.checkAndSendPrecipitationForecast(
                                getApplicationContext(), location, old);
                    } else {
                        failed = true;
                    }
                }
                return;
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onPollingCompleted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutsManager.refreshShortcutsInNewThread(getApplicationContext(), locationList);
        }
        handleUpdateResult(future, failed);
    }
}
