package wangdaye.com.geometricweather.utils;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Job schedule utils.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerUtils {

    public static void schedule(Context context, Class cls, int scheduleCode) {
        cancel(context, scheduleCode);
        ((JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(
                new JobInfo.Builder(scheduleCode, new ComponentName(context.getPackageName(), cls.getName()))
                        .setPeriodic((long) (1000 * 60 * 60 * 1.5))
                        .build());
    }

    static void scheduleForecastMission(Context context, Class cls, int scheduleCode, boolean today) {
        ((JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(
                new JobInfo.Builder(scheduleCode, new ComponentName(context.getPackageName(), cls.getName()))
                        .setMinimumLatency(NotificationUtils.calcForecastDuration(context, today, true))
                        .build());
    }

    public static void cancel(Context context, int scheduleCode) {
        ((JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(scheduleCode);
    }
}
