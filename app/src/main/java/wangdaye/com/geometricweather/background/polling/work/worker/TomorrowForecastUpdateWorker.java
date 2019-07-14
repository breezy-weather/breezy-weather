package wangdaye.com.geometricweather.background.polling.work.worker;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import wangdaye.com.geometricweather.background.polling.PollingManager;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.remoteviews.presenter.notification.ForecastNotificationIMP;

public class TomorrowForecastUpdateWorker extends AsyncUpdateWorker {

    public TomorrowForecastUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public void updateView(Context context, Location location,
                           @Nullable Weather weather, @Nullable History history) {
        if (ForecastNotificationIMP.isEnable(context, false)) {
            ForecastNotificationIMP.buildForecastAndSendIt(context, weather, false);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void handleUpdateResult(SettableFuture<Result> future, boolean failed) {
        future.set(failed ? Result.failure() : Result.success());
        PollingManager.resetTomorrowForecastBackgroundTask(
                getApplicationContext(), false, true);
    }
}
