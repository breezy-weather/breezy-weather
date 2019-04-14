package wangdaye.com.geometricweather.background.service.job;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.remoteviews.NotificationUtils;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.background.PollingUpdateHelper;
import wangdaye.com.geometricweather.utils.manager.ShortcutsManager;

/**
 * Job update service.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public abstract class JobUpdateService extends JobService
        implements PollingUpdateHelper.OnPollingUpdateListener {

    private PollingUpdateHelper helper;
    private List<Location> locationList;
    private JobParameters parameters;
    private boolean failed;

    @Override
    public void onCreate() {
        super.onCreate();
        parameters = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        parameters = null;
        if (helper != null) {
            helper.setOnPollingUpdateListener(null);
            helper.cancel();
            helper = null;
        }
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        if (parameters == null) {
            parameters = jobParameters;
            failed = false;
            locationList = DatabaseHelper.getInstance(this).readLocationList();
            helper = new PollingUpdateHelper(this, locationList);
            helper.setOnPollingUpdateListener(this);
            helper.pollingUpdate();
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        parameters = null;
        if (helper != null) {
            helper.setOnPollingUpdateListener(null);
            helper.cancel();
            helper = null;
        }
        return false;
    }

    // control.

    public abstract void updateView(Context context, Location location,
                                    @Nullable Weather weather, @Nullable History history);

    // call jobFinish() in here.
    public abstract void setDelayTask(JobParameters jobParameters, boolean failed);

    // interface.

    // on polling updateRotation listener.

    @Override
    public void onUpdateCompleted(Location location, Weather weather, Weather old, boolean succeed) {
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).equals(location)) {
                location.weather = weather;
                location.history = DatabaseHelper.getInstance(this).readHistory(weather);
                locationList.set(i, location);
                if (i == 0) {
                    updateView(this, location, location.weather, location.history);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setDelayTask(parameters, failed);
        }
    }
}
