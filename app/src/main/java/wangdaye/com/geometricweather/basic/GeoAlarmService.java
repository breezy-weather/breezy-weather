package wangdaye.com.geometricweather.basic;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.NotificationUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Widget service.
 * */

public abstract class GeoAlarmService extends Service
        implements WeatherHelper.OnRequestWeatherListener {
    // widget.
    private WeatherHelper weatherHelper;

    /** <br> life cycle. */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        List<Location> locationList = DatabaseHelper.getInstance(this).readLocationList();
        doRefresh(locationList.get(0));
        return START_NOT_STICKY;
    }

    protected abstract void doRefresh(Location location);

    public void requestData(Location location) {
        Weather weather = DatabaseHelper.getInstance(this).readWeather(location);
        if (weather != null) {

            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            String[] weatherDates = weather.base.date.split("-");
            String[] weatherTimes = weather.base.time.split(":");

            if (weatherDates[0].equals(String.valueOf(year))
                    && weatherDates[1].equals(String.valueOf(month))
                    && weatherDates[2].equals(String.valueOf(day))) {

                if (Math.abs((hour * 60 + minute)
                        - (Integer.parseInt(weatherTimes[0]) * 60 + Integer.parseInt(weatherTimes[1]))) > 60) {
                    requestWeatherSuccess(weather, location);
                    return;
                }
            }
        }

        initWeatherHelper();
        if(location.isLocal()) {
            if (location.isUsable()) {
                weatherHelper.requestWeather(this, location, this);
            } else {
                weatherHelper.requestWeather(this, Location.buildDefaultLocation(), this);
                Toast.makeText(
                        this,
                        getString(R.string.feedback_not_yet_location),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            weatherHelper.requestWeather(this, location, this);
        }
    }

    public static void setAlarmIntent(Context context, Class<?> cls, int requestCode) {
        Intent target = new Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                requestCode,
                target,
                PendingIntent.FLAG_UPDATE_CURRENT);

        int duration = (int) (1000 * 60 * 60 * 1.5);
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                .set(
                        AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + duration, 
                        pendingIntent);
    }
    
    public static void cancelAlarmIntent(Context context, Class<?> cls, int requestCode) {
        Intent target = new Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                requestCode,
                target,
                PendingIntent.FLAG_UPDATE_CURRENT);
        
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(pendingIntent);
    }

    public abstract void updateView(Context context, Location location, Weather weather);

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (weatherHelper != null) {
            weatherHelper.cancel();
        }
    }

    /** <br> widget. */

    private void initWeatherHelper() {
        if (weatherHelper == null) {
            weatherHelper = new WeatherHelper();
        } else {
            weatherHelper.cancel();
        }
    }

    /** <br> interface. */

    // request weather.

    @Override
    public void requestWeatherSuccess(Weather weather, Location requestLocation) {
        Weather oldResult = DatabaseHelper.getInstance(this).readWeather(requestLocation);
        DatabaseHelper.getInstance(this).writeWeather(requestLocation, weather);
        DatabaseHelper.getInstance(this).writeHistory(weather);
        NotificationUtils.checkAndSendAlert(this, weather, oldResult);
        updateView(this, requestLocation, weather);
    }

    @Override
    public void requestWeatherFailed(Location requestLocation) {
        Weather weather = DatabaseHelper.getInstance(this).readWeather(requestLocation);
        updateView(this, requestLocation, weather);
        Toast.makeText(
                this,
                getString(R.string.feedback_get_weather_failed),
                Toast.LENGTH_SHORT).show();
    }
}
