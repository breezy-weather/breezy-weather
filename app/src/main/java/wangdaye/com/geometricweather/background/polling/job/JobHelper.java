package wangdaye.com.geometricweather.background.polling.job;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;

import java.util.Calendar;

/**
 * Job helper.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobHelper {

    private static final int HOUR = 1000 * 60 * 60;
    private static final int MINUTE = 1000 * 60;

    private static final int JOB_ID_NORMAL_VIEW = 1;
    private static final int JOB_ID_TODAY_FORECAST = 2;
    private static final int JOB_ID_TOMORROW_FORECAST = 3;

    public static void setJobForNormalView(Context context, float pollingRate) {
        JobInfo.Builder builder = new JobInfo.Builder(
                JOB_ID_NORMAL_VIEW,
                new ComponentName(context, JobNormalUpdateService.class)
        ).setBackoffCriteria(15 * MINUTE, JobInfo.BACKOFF_POLICY_LINEAR)
                .setPeriodic((long) (pollingRate * HOUR))
                .setPersisted(true);
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler != null) {
            scheduler.schedule(builder.build());
        }
    }

    public static void cancelNormalViewJob(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler != null) {
            scheduler.cancel(JOB_ID_NORMAL_VIEW);
        }
    }

    public static void setJobForTodayForecast(Context context, String todayForecastTime) {
        JobInfo.Builder builder = new JobInfo.Builder(
                JOB_ID_TODAY_FORECAST,
                new ComponentName(context, JobTodayForecastUpdateService.class)
        ).setMinimumLatency(getForecastAlarmDelay(todayForecastTime));
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler != null) {
            scheduler.schedule(builder.build());
        }
    }

    public static void cancelTodayForecastJob(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler != null) {
            scheduler.cancel(JOB_ID_TODAY_FORECAST);
        }
    }

    public static void setJobForTomorrowForecast(Context context, String TomorrowForecastTime) {
        JobInfo.Builder builder = new JobInfo.Builder(
                JOB_ID_TOMORROW_FORECAST,
                new ComponentName(context, JobTomorrowForecastUpdateService.class)
        ).setMinimumLatency(getForecastAlarmDelay(TomorrowForecastTime));
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler != null) {
            scheduler.schedule(builder.build());
        }
    }

    public static void cancelTomorrowForecastJob(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler != null) {
            scheduler.cancel(JOB_ID_TOMORROW_FORECAST);
        }
    }

    private static long getForecastAlarmDelay(String time) {
        int realTimes[] = new int[] {
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE)
        };
        int setTimes[] = new int[] {
                Integer.parseInt(time.split(":")[0]),
                Integer.parseInt(time.split(":")[1])
        };
        int duration = (setTimes[0] - realTimes[0]) * HOUR + (setTimes[1] - realTimes[1]) * MINUTE;
        if (duration <= 0) {
            duration += 24 * HOUR;
        }
        return duration;
    }
}
