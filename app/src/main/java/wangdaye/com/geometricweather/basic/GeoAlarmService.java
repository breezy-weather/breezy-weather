package wangdaye.com.geometricweather.basic;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.Toast;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.utils.WidgetUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.LocationHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Widget service.
 * */

public abstract class GeoAlarmService extends IntentService
        implements LocationHelper.OnRequestLocationListener, WeatherHelper.OnRequestWeatherListener {

    /** <br> life cycle. */

    public GeoAlarmService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Location location = readSettings();
        if (location == null) {
            location = new Location(getString(R.string.local), null);
        }
        doRefresh(location);
    }

    protected abstract Location readSettings();

    protected abstract void doRefresh(Location location);

    public void requestData(Location location) {
        if(location.name.equals(getString(R.string.local))) {
            new LocationHelper(this).requestLocation(this, this);
        } else {
            location.realName = location.name;
            DatabaseHelper.getInstance(this).insertLocation(location);
            new WeatherHelper().requestWeather(this, location, this);
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

    public abstract void updateView(Context context, Weather weather);

    /** <br> interface. */

    // request name.

    @Override
    public void requestLocationSuccess(String locationName) {
        Location location = new Location(getString(R.string.local), locationName);
        DatabaseHelper.getInstance(this).insertLocation(location);
        new WeatherHelper().requestWeather(this, location, this);
    }

    @Override
    public void requestLocationFailed() {
        Location location = DatabaseHelper.getInstance(this).searchLocation(getString(R.string.local));
        new WeatherHelper().requestWeather(this, location, this);
        Toast.makeText(
                this,
                getString(R.string.feedback__location_failed),
                Toast.LENGTH_SHORT).show();
    }

    // request weather.

    @Override
    public void requestWeatherSuccess(Weather weather, String locationName) {
        DatabaseHelper.getInstance(this).insertWeather(weather);
        DatabaseHelper.getInstance(this).insertHistory(weather);
        updateView(this, weather);
    }

    @Override
    public void requestWeatherFailed(String locationName) {
        Toast.makeText(
                this,
                getString(R.string.feedback_get_weather_failed),
                Toast.LENGTH_SHORT).show();
        Weather weather = DatabaseHelper.getInstance(this).searchWeather(locationName);
        updateView(this, weather);
    }
}
