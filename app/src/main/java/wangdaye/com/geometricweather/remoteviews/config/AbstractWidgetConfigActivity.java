package wangdaye.com.geometricweather.remoteviews.config;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;

import org.jetbrains.annotations.NotNull;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Abstract widget config activity.
 * */

public abstract class AbstractWidgetConfigActivity extends GeoActivity
        implements WeatherHelper.OnRequestWeatherListener {

    private ImageView wallpaper;

    private Location locationNow;
    private WeatherHelper weatherHelper;

    private boolean destroyed;

    private final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();

        initData();
        initView();
        updateHostView();

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

    @Override
    @Nullable
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context,
                             @NonNull AttributeSet attrs) {
        if (name.equals("ImageView")) {
            return new ImageView(context, attrs);
        }
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull String name, @NonNull Context context,
                             @NonNull AttributeSet attrs) {
        if (name.equals("ImageView")) {
            return new ImageView(context, attrs);
        }
        return super.onCreateView(name, context, attrs);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyed = true;
        weatherHelper.cancel();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        // do nothing.
    }

    public abstract void setContentView();

    public void initData() {
        locationNow = DatabaseHelper.getInstance(this).readLocationList().get(0);
        locationNow.weather = DatabaseHelper.getInstance(this).readWeather(locationNow);

        weatherHelper = new WeatherHelper();

        destroyed = false;
    }

    public abstract void initView();

    public final void updateHostView() {
        getWidgetContainer().removeAllViews();

        View view = getRemoteViews().apply(getApplicationContext(), getWidgetContainer());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        getWidgetContainer().addView(view, params);
    }

    public abstract ViewGroup getWidgetContainer();

    public abstract RemoteViews getRemoteViews();

    public Location getLocationNow() {
        return locationNow;
    }

    // interface.

    // on request weather listener.

    @Override
    public void requestWeatherSuccess(@Nullable Weather weather, @Nullable History history,
                                      @NonNull Location requestLocation) {
        if (destroyed) {
            return;
        }
        if (weather == null) {
            requestWeatherFailed(requestLocation);
        } else {
            locationNow.weather = weather;
            updateHostView();
        }
    }

    @Override
    public void requestWeatherFailed(@NonNull Location requestLocation) {
        if (destroyed) {
            return;
        }
        locationNow.weather = DatabaseHelper.getInstance(this).readWeather(requestLocation);
        updateHostView();
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
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
            );
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
