package wangdaye.com.geometricweather.basic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.widget.ImageView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Geometric weather widget configuration activity.
 * */

public abstract class GeoWidgetConfigActivity extends GeoActivity
        implements WeatherHelper.OnRequestWeatherListener {

    private Location locationNow;
    private WeatherHelper weatherHelper;

    private ImageView wallpaper;

    private boolean fahrenheit;
    private boolean destroyed;

    private final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 0;

    @Override
    protected void onStart() {
        super.onStart();
        if (!isStarted()) {
            setStarted();
            initData();
            initWidget();

            if (locationNow.isLocal()) {
                if (locationNow.isUsable()) {
                    weatherHelper.requestWeather(this, locationNow, this);
                } else {
                    weatherHelper.requestWeather(this, Location.buildDefaultLocation(), this);
                }
            } else {
                weatherHelper.requestWeather(this, locationNow, this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyed = true;
        weatherHelper.cancel();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // do nothing.
    }

    public void initData() {
        this.locationNow = DatabaseHelper.getInstance(this).readLocationList().get(0);
        this.weatherHelper = new WeatherHelper();
        this.fahrenheit = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.key_fahrenheit), false);
        this.destroyed = false;
    }

    public abstract void initWidget();

    public abstract void refreshWidgetView(Weather weather);

    public Location getLocationNow() {
        return locationNow;
    }

    public boolean isFahrenheit() {
        return fahrenheit;
    }

    // interface.

    // on request weather listener.

    @Override
    public void requestWeatherSuccess(Weather weather, Location requestLocation) {
        if (destroyed) {
            return;
        }
        if (weather == null) {
            requestWeatherFailed(requestLocation);
        } else {
            locationNow.weather = weather;
            refreshWidgetView(weather);
            DatabaseHelper.getInstance(this).writeWeather(requestLocation, weather);
            DatabaseHelper.getInstance(this).writeHistory(weather);
        }
    }

    @Override
    public void requestWeatherFailed(Location requestLocation) {
        if (destroyed) {
            return;
        }
        Weather weather = DatabaseHelper.getInstance(this).readWeather(requestLocation);
        locationNow.weather = weather;
        refreshWidgetView(weather);
        SnackbarUtils.showSnackbar(getString(R.string.feedback_get_weather_failed));
    }

    public void bindWallpaper(ImageView imageView) {
        bindWallpaper(imageView, true);
    }

    private void bindWallpaper(ImageView imageView, boolean requestPermissionIfFailed) {
        try {
            WallpaperManager manager = WallpaperManager.getInstance(this);
            if (manager != null) {
                Drawable drawable = manager.getDrawable();
                if (drawable != null) {
                    imageView.setImageDrawable(drawable);
                }
            }
        } catch (Exception ignore) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requestPermissionIfFailed) {
                requestReadExternalStoragePermission(imageView);
            }
        }
    }

    // permission.

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestReadExternalStoragePermission(ImageView imageView) {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            wallpaper = imageView;
            this.requestPermissions(
                    new String[] {
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            bindWallpaper(imageView, false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permission, @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permission, grantResult);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE:
                bindWallpaper(wallpaper, false);
                break;
        }
    }
}
