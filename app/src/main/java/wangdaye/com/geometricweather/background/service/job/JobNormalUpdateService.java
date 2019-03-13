package wangdaye.com.geometricweather.background.service.job;

import android.app.job.JobParameters;
import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;

import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.remote.utils.NormalNotificationUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetClockDayDetailsUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetClockDayHorizontalUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetClockDayVerticalUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetClockDayWeekUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetDayUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetDayWeekUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetTextUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetWeekUtils;

/**
 * Job normal update service.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobNormalUpdateService extends JobUpdateService {

    @Override
    public void updateView(Context context, Location location, Weather weather) {
        if (WidgetDayUtils.isEnable(context)) {
            WidgetDayUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetWeekUtils.isEnable(context)) {
            WidgetWeekUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetDayWeekUtils.isEnable(context)) {
            WidgetDayWeekUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetClockDayHorizontalUtils.isEnable(context)) {
            WidgetClockDayHorizontalUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetClockDayDetailsUtils.isEnable(context)) {
            WidgetClockDayDetailsUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetClockDayVerticalUtils.isEnable(context)) {
            WidgetClockDayVerticalUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetClockDayWeekUtils.isEnable(context)) {
            WidgetClockDayWeekUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetTextUtils.isEnable(context)) {
            WidgetTextUtils.refreshWidgetView(context, location, weather);
        }
        if (NormalNotificationUtils.isEnable(context)) {
            NormalNotificationUtils.buildNotificationAndSendIt(context, weather);
        }
    }

    @Override
    public void setDelayTask(JobParameters jobParameters, boolean failed) {
        jobFinished(jobParameters, failed);
    }
}
