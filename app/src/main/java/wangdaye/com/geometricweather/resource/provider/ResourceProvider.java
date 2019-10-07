package wangdaye.com.geometricweather.resource.provider;

import android.animation.Animator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.Size;

import wangdaye.com.geometricweather.basic.model.weather.WeatherCode;
import wangdaye.com.geometricweather.resource.ResourceUtils;

public abstract class ResourceProvider {

    protected static int getResId(Context context, String resName, String type) {
        return ResourceUtils.getResId(context, resName, type);
    }

    @NonNull
    protected Uri getDrawableUri(String resName) {
        return ResourceUtils.getDrawableUri(getPackageName(), "drawable", resName);
    }

    public abstract String getPackageName();

    public abstract String getProviderName();

    public abstract Drawable getProviderIcon();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ResourceProvider) {
            return ((ResourceProvider) obj).getPackageName().equals(getPackageName());
        }
        return false;
    }

    // weather icon.

    @NonNull
    public abstract Drawable getWeatherIcon(WeatherCode code, boolean dayTime);

    @NonNull
    public abstract Uri getWeatherIconUri(WeatherCode code, boolean dayTime);

    @Size(3)
    public abstract Drawable[] getWeatherIcons(WeatherCode code, boolean dayTime);

    // animator.

    @Size(3)
    public abstract Animator[] getWeatherAnimators(WeatherCode code, boolean dayTime);

    // minimal icon.

    @NonNull
    public abstract Drawable getMinimalLightIcon(WeatherCode code, boolean dayTime);

    @NonNull
    public abstract Uri getMinimalLightIconUri(WeatherCode code, boolean dayTime);

    @NonNull
    public abstract Drawable getMinimalGreyIcon(WeatherCode code, boolean dayTime);

    @NonNull
    public abstract Uri getMinimalGreyIconUri(WeatherCode code, boolean dayTime);

    @NonNull
    public abstract Drawable getMinimalDarkIcon(WeatherCode code, boolean dayTime);

    @NonNull
    public abstract Uri getMinimalDarkIconUri(WeatherCode code, boolean dayTime);

    @NonNull
    public abstract Drawable getMinimalXmlIcon(WeatherCode code, boolean dayTime);

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    public abstract Icon getMinimalIcon(WeatherCode code, boolean dayTime);

    // shortcut.

    @NonNull
    public abstract Drawable getShortcutsIcon(WeatherCode code, boolean dayTime);

    @NonNull
    public abstract Drawable getShortcutsForegroundIcon(WeatherCode code, boolean dayTime);

    // sun and moon.

    @NonNull
    public abstract Drawable getSunDrawable();

    @NonNull
    public abstract Drawable getMoonDrawable();
}
