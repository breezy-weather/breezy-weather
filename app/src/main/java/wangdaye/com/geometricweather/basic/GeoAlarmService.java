package wangdaye.com.geometricweather.basic;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.Toast;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Widget service.
 * */

public abstract class GeoAlarmService extends IntentService
        implements WeatherHelper.OnRequestWeatherListener {
    // widget.
    private WeatherHelper weatherHelper;

    /** <br> life cycle. */

    public GeoAlarmService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        doRefresh(
                readLocation(
                        readSettings()));
    }

    protected abstract String readSettings();

    protected Location readLocation(String locationName) {
        List<Location> locationList = DatabaseHelper.getInstance(this).readLocationList();
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationName.equals(getString(R.string.local)) && locationList.get(i).isLocal()) {
                return locationList.get(i);
            } else if (!locationName.equals(getString(R.string.local))
                    && locationList.get(i).city.equals(locationName)) {
                return locationList.get(i);
            }
        }
        return locationList.get(0);
    }

    protected abstract void doRefresh(Location location);

    public void requestData(Location location) {
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

    public void setAlarmIntent(Context context, Class<?> cls, int requestCode) {
        Intent target = new Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                requestCode,
                target,
                PendingIntent.FLAG_UPDATE_CURRENT);

        int duration = (int) (1000 * 60 * 60 * 1.5);
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
                .set(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
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
        DatabaseHelper.getInstance(this).writeWeather(requestLocation, weather);
        DatabaseHelper.getInstance(this).writeHistory(weather);
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
