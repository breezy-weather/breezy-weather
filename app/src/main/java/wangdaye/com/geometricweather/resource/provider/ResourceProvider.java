package wangdaye.com.geometricweather.resource.provider;

import android.animation.Animator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.Size;

import wangdaye.com.geometricweather.utils.ResourceUtils;

public abstract class ResourceProvider {

    protected static int getResId(Context context, String resName, String type) {
        return ResourceUtils.getResId(context, resName, type);
    }

    public abstract String getPackageName();

    public abstract String getProviderName();

    public abstract Drawable getProviderIcon();

    // weather icon.

    @NonNull
    public abstract Drawable getWeatherIcon(String weatherKind, boolean dayTime);

    @Size(3)
    public abstract Drawable[] getWeatherIcons(String weatherKind, boolean dayTime);

    // animator.

    @Size(3)
    public abstract Animator[] getWeatherAnimators(String weatherKind, boolean dayTime);

    // minimal icon.

    @NonNull
    public abstract Drawable getMinimalLightIcon(String weatherKind, boolean dayTime);

    @NonNull
    public abstract Drawable getMinimalGreyIcon(String weatherKind, boolean dayTime);

    @NonNull
    public abstract Drawable getMinimalDarkIcon(String weatherKind, boolean dayTime);

    @NonNull
    public abstract Drawable getMinimalXmlIcon(String weatherKind, boolean dayTime);

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    public abstract Icon getMinimalIcon(String weatherKind, boolean dayTime);

    // shortcut.

    @NonNull
    public abstract Drawable getShortcutsIcon(String weatherKind, boolean dayTime);

    @NonNull
    public abstract Drawable getShortcutsForegroundIcon(String weatherKind, boolean dayTime);

    // sun and moon.

    @NonNull
    public abstract Drawable getSunDrawable();

    @NonNull
    public abstract Drawable getMoonDrawable();
}
