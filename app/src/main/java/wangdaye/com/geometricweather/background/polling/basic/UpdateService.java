package wangdaye.com.geometricweather.background.polling.basic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
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
    private Disposable disposable;
    private boolean failed;

    @Override
    public void onCreate() {
        super.onCreate();
        failed = false;
        locationList = DatabaseHelper.getInstance(this).readLocationList();
        helper = new PollingUpdateHelper(this, locationList);
        helper.setOnPollingUpdateListener(this);
        helper.pollingUpdate();

        disposable = Observable.timer(30, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> stopService(true))
                .subscribe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
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
        stopService(failed);
    }
}
