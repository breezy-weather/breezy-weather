package wangdaye.com.geometricweather.resource.provider;

import android.animation.Animator;
import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

public abstract class ResourceProvider {

    public static int getResId(Context context, String resName, String type) {
        try {
            return context.getClassLoader()
                    .loadClass(context.getPackageName() + ".R$" + type)
                    .getField(resName)
                    .getInt(null);
        } catch (Exception e) {
            return 0;
        }
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

    public abstract int getMinimalXmlIconId(String weatherKind, boolean dayTime);

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
