package wangdaye.com.geometricweather.background.polling.work;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import wangdaye.com.geometricweather.background.polling.work.worker.NormalUpdateWorker;
import wangdaye.com.geometricweather.background.polling.work.worker.TodayForecastUpdateWorker;
import wangdaye.com.geometricweather.background.polling.work.worker.TomorrowForecastUpdateWorker;

public class WorkerHelper {

    private static final long MINUTES_PER_HOUR = 60;
    private static final long BACKOFF_DELAY_MINUTES = 15;

    private static final String WORK_NAME_NORMAL_VIEW = "NORMAL_VIEW";
    private static final String WORK_NAME_TODAY_FORECAST = "TODAY_FORECAST";
    private static final String WORK_NAME_TOMORROW_FORECAST = "TOMORROW_FORECAST";

    public static void setNormalPollingWork(float pollingRate) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                NormalUpdateWorker.class,
                (long) (pollingRate * MINUTES_PER_HOUR),
                TimeUnit.MINUTES
        ).setBackoffCriteria(
                BackoffPolicy.LINEAR,
                BACKOFF_DELAY_MINUTES,
                TimeUnit.MINUTES
        ).setConstraints(
                new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
        ).build();

        WorkManager.getInstance().enqueueUniquePeriodicWork(
                WORK_NAME_NORMAL_VIEW,
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    public static void cancelNormalPollingWork() {
        WorkManager.getInstance().cancelUniqueWork(WORK_NAME_NORMAL_VIEW);
    }

    public static void setTodayForecastUpdateWork(String todayForecastTime, boolean nextDay) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(TodayForecastUpdateWorker.class)
                .setInitialDelay(
                        getForecastAlarmDelayInMinutes(todayForecastTime, nextDay),
                        TimeUnit.MINUTES
                ).setConstraints(
                        new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                ).build();

        WorkManager.getInstance().enqueueUniqueWork(
                WORK_NAME_TODAY_FORECAST,
                ExistingWorkPolicy.REPLACE,
                request
        );
    }

    public static void cancelTodayForecastUpdateWork() {
        WorkManager.getInstance().cancelUniqueWork(WORK_NAME_TODAY_FORECAST);
    }

    public static void setTomorrowForecastUpdateWork(String tomorrowForecastTime, boolean nextDay) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(TomorrowForecastUpdateWorker.class)
                .setInitialDelay(
                        getForecastAlarmDelayInMinutes(tomorrowForecastTime, nextDay),
                        TimeUnit.MINUTES
                ).setConstraints(
                        new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                ).build();

        WorkManager.getInstance().enqueueUniqueWork(
                WORK_NAME_TOMORROW_FORECAST,
                ExistingWorkPolicy.REPLACE,
                request
        );
    }

    public static void cancelTomorrowForecastUpdateWork() {
        WorkManager.getInstance().cancelUniqueWork(WORK_NAME_TOMORROW_FORECAST);
    }

    private static long getForecastAlarmDelayInMinutes(String time, boolean nextDay) {
        int[] realTimes = new int[]{
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE)
        };
        int[] setTimes = new int[]{
                Integer.parseInt(time.split(":")[0]),
                Integer.parseInt(time.split(":")[1])
        };
        int delay = (setTimes[0] - realTimes[0]) * 60 + (setTimes[1] - realTimes[1]);
        if (delay <= 0 || nextDay) {
            delay += 24 * 60;
        }
        return delay;
    }
}
