package wangdaye.com.geometricweather.background.polling.job;

import android.app.job.JobParameters;
import android.content.Context;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.remoteviews.NotificationUtils;
import wangdaye.com.geometricweather.remoteviews.WidgetUtils;

/**
 * Job normal update service.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobNormalUpdateService extends JobUpdateService {

    @Override
    public void updateView(Context context, Location location,
                           @Nullable Weather weather, @Nullable History history) {
        WidgetUtils.refreshWidgetIfNecessary(context, location, weather, history);
        NotificationUtils.refreshNotificationIfNecessary(context, weather);
    }

    @Override
    public void setDelayTask(JobParameters jobParameters, boolean failed) {
        jobFinished(jobParameters, failed);
    }
}
