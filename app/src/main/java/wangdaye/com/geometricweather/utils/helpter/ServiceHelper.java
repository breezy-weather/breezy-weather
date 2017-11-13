package wangdaye.com.geometricweather.utils.helpter;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.service.PollingService;
import wangdaye.com.geometricweather.service.ProtectService;
import wangdaye.com.geometricweather.service.NormalUpdateService;
import wangdaye.com.geometricweather.service.TodayForecastUpdateService;
import wangdaye.com.geometricweather.service.TomorrowForecastUpdateService;
import wangdaye.com.geometricweather.utils.ValueUtils;

/**
 * Service helper.
 * */

public class ServiceHelper {

    public static void resetNormalService(Context context, boolean checkIfRunning, boolean forceRefresh) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String refreshRate = sharedPreferences.getString(context.getString(R.string.key_refresh_rate), "1:30");
        boolean backgroundFree = sharedPreferences.getBoolean(context.getString(R.string.key_background_free), false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            JobHelper.setJobForNormalView(context, ValueUtils.getRefreshRateScale(refreshRate));
        } else {
            if (backgroundFree) {
                stopNormalService(context);
                if (forceRefresh) {
                    context.startService(new Intent(context, NormalUpdateService.class));
                } else {
                    AlarmHelper.setAlarmForNormalView(context, ValueUtils.getRefreshRateScale(refreshRate));
                }
            } else {
                resetPollingService(context, checkIfRunning, forceRefresh);
            }
        }
    }

    private static void stopNormalService(Context context) {
        resetPollingService(context, false, false);
        context.stopService(new Intent(context, NormalUpdateService.class));
        AlarmHelper.cancelNormalViewAlarm(context);
    }

    private static void resetPollingService(Context context, boolean checkIsRunning, boolean forceRefresh) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        boolean backgroundFree = sharedPreferences.getBoolean(context.getString(R.string.key_background_free), false);
        String refreshRate = sharedPreferences.getString(context.getString(R.string.key_refresh_rate), "1:30");

        // polling service.
        Intent polling = new Intent(context, PollingService.class);
        polling.putExtra(PollingService.KEY_IS_REFRESH, true);
        polling.putExtra(PollingService.KEY_WORKING, !backgroundFree);
        polling.putExtra(PollingService.KEY_POLLING_RATE, ValueUtils.getRefreshRateScale(refreshRate));
        polling.putExtra(PollingService.KEY_FORCE_REFRESH, forceRefresh);
        boolean pollingExist = checkIsRunning && isExist(context, PollingService.class);

        // protect service.
        Intent protect = new Intent(context, ProtectService.class);
        protect.putExtra(ProtectService.KEY_IS_REFRESH, true);
        protect.putExtra(ProtectService.KEY_WORKING, !backgroundFree);
        boolean protectExist = checkIsRunning && isExist(context, ProtectService.class);

        if (!pollingExist) {
            context.startService(polling);
        }
        if (!protectExist) {
            context.startService(protect);
        }
    }

    private static boolean isExist(Context context, Class cls) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            List<ActivityManager.RunningServiceInfo> serviceList = manager.getRunningServices(Integer.MAX_VALUE);
            int myUid = android.os.Process.myUid();
            for (ActivityManager.RunningServiceInfo runningServiceInfo : serviceList) {
                if (runningServiceInfo.uid == myUid
                        && runningServiceInfo.service.getClassName().equals(cls.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void resetForecastService(Context context, boolean today) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean openTodayForecast = sharedPreferences.getBoolean(
                context.getString(R.string.key_forecast_today),
                false);
        boolean openTomorrowForecast = sharedPreferences.getBoolean(
                context.getString(R.string.key_forecast_tomorrow),
                false);
        String todayForecastTime = sharedPreferences.getString(
                context.getString(R.string.key_forecast_today_time),
                GeometricWeather.DEFAULT_TODAY_FORECAST_TIME);
        String tomorrowForecastTime = sharedPreferences.getString(
                context.getString(R.string.key_forecast_tomorrow_time),
                GeometricWeather.DEFAULT_TOMORROW_FORECAST_TIME);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (today) {
                if (openTodayForecast) {
                    if (isForecastTime(todayForecastTime)) {
                        try {
                            context.startService(new Intent(context, TodayForecastUpdateService.class));
                        } catch (Exception ignore) {

                        }
                    }
                    JobHelper.setJobForTodayForecast(context, todayForecastTime);
                } else {
                    JobHelper.cancelTodayForecastJob(context);
                }
            } else {
                if (openTomorrowForecast) {
                    if (isForecastTime(tomorrowForecastTime)) {
                        try {
                            context.startService(new Intent(context, TomorrowForecastUpdateService.class));
                        } catch (Exception ignore) {

                        }
                    }
                    JobHelper.setJobForTomorrowForecast(context, tomorrowForecastTime);
                } else {
                    JobHelper.cancelTomorrowForecastJob(context);
                }
            }
        } else {
            stopForecastService(context, today);
            if (today && openTodayForecast) {
                if (isForecastTime(todayForecastTime)) {
                    context.startService(new Intent(context, TodayForecastUpdateService.class));
                } else {
                    AlarmHelper.setAlarmForTodayForecast(context, todayForecastTime);
                }
            } else if (!today && openTomorrowForecast) {
                if (isForecastTime(tomorrowForecastTime)) {
                    context.startService(new Intent(context, TomorrowForecastUpdateService.class));
                } else {
                    AlarmHelper.setAlarmForTomorrowForecast(context, tomorrowForecastTime);
                }
            }
        }
    }

    private static void stopForecastService(Context context, boolean today) {
        if (today) {
            context.stopService(new Intent(context, TodayForecastUpdateService.class));
            AlarmHelper.cancelTodayForecastAlarm(context);
        } else {
            context.stopService(new Intent(context, TomorrowForecastUpdateService.class));
            AlarmHelper.cancelTomorrowForecastAlarm(context);
        }
    }

    private static boolean isForecastTime(String time) {
        int realTimes[] = new int[] {
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE)};
        int setTimes[] = new int[]{
                Integer.parseInt(time.split(":")[0]),
                Integer.parseInt(time.split(":")[1])};
        return realTimes[0] == setTimes[0] && realTimes[1] == setTimes[1];
    }
}
