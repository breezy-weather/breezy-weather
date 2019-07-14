package wangdaye.com.geometricweather.resource.provider;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.Size;
import androidx.core.content.res.ResourcesCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.resource.Constants;
import wangdaye.com.geometricweather.resource.XmlHelper;
import wangdaye.com.geometricweather.ui.image.MoonDrawable;
import wangdaye.com.geometricweather.ui.image.SunDrawable;
import wangdaye.com.geometricweather.utils.ValueUtils;

public class DefaultResourceProvider extends ResourceProvider {

    private Context context;
    private String providerName;
    @Nullable private Drawable iconDrawable;

    private Map<String, String> drawableFilter;
    private Map<String, String> animatorFilter;
    private Map<String, String> shortcutFilter;

    public DefaultResourceProvider() {
        context = GeometricWeather.getInstance();
        providerName = context.getString(R.string.geometric_weather);
        iconDrawable = context.getApplicationInfo().loadIcon(context.getPackageManager());

        Resources res = context.getResources();
        try {
            drawableFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_drawable_filter));
            animatorFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_animator_filter));
            shortcutFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_shortcut_filter));
        } catch (Exception e) {
            drawableFilter = new HashMap<>();
            animatorFilter = new HashMap<>();
            shortcutFilter = new HashMap<>();
        }
    }

    static boolean isDefaultIconProvider(@NonNull String packageName) {
        return packageName.equals(
                GeometricWeather.getInstance().getPackageName());
    }

    @NonNull
    private static String getFilterResource(Map<String, String> filter, String key) {
        String value = filter.get(key);
        if (TextUtils.isEmpty(value)) {
            return key;
        } else {
            return value;
        }
    }

    @Override
    public String getPackageName() {
        return context.getPackageName();
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    @Override
    public Drawable getProviderIcon() {
        return iconDrawable;
    }

    // weather icon.

    @NonNull
    @Override
    public Drawable getWeatherIcon(String weatherKind, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawable(getWeatherIconName(weatherKind, dayTime))
        );
    }

    @NonNull
    @Override
    public Uri getWeatherIconUri(String weatherKind, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawableUri(getWeatherIconName(weatherKind, dayTime))
        );
    }

    @Override
    @Size(3)
    public Drawable[] getWeatherIcons(String weatherKind, boolean dayTime) {
        return new Drawable[] {
                getDrawable(getWeatherIconName(weatherKind, dayTime, 1)),
                getDrawable(getWeatherIconName(weatherKind, dayTime, 2)),
                getDrawable(getWeatherIconName(weatherKind, dayTime, 3))
        };
    }

    @Nullable
    private Drawable getDrawable(@NonNull String resName) {
        try {
            return ResourcesCompat.getDrawable(
                    context.getResources(),
                    ValueUtils.nonNull(getResId(context, resName, "drawable")),
                    null
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String getWeatherIconName(String weatherKind, boolean daytime) {
        return getFilterResource(
                drawableFilter,
                innerGetWeatherIconName(weatherKind, daytime)
        );
    }

    private String getWeatherIconName(String weatherKind, boolean daytime,
                                      @IntRange(from = 1, to = 3) int index) {
        return getFilterResource(
                drawableFilter,
                innerGetWeatherIconName(weatherKind, daytime) + Constants.SEPARATOR + index
        );
    }

    private static String innerGetWeatherIconName(String weatherKind, boolean daytime) {
        return Constants.getResourcesName(weatherKind)
                + Constants.SEPARATOR + (daytime ? Constants.DAY : Constants.NIGHT);
    }

    // animator.

    @Override
    @Size(3)
    public Animator[] getWeatherAnimators(String weatherKind, boolean dayTime) {
        return new Animator[] {
                getAnimator(getWeatherAnimatorName(weatherKind, dayTime, 1)),
                getAnimator(getWeatherAnimatorName(weatherKind, dayTime, 2)),
                getAnimator(getWeatherAnimatorName(weatherKind, dayTime, 3))
        };
    }

    @Nullable
    private Animator getAnimator(@NonNull String resName) {
        try {
            return AnimatorInflater.loadAnimator(
                    context,
                    ValueUtils.nonNull(getResId(context, resName, "animator"))
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String getWeatherAnimatorName(String weatherKind, boolean daytime,
                                          @IntRange(from = 1, to = 3) int index) {
        return getFilterResource(
                animatorFilter,
                innerGetWeatherAnimatorName(weatherKind, daytime) + Constants.SEPARATOR + index
        );
    }

    private static String innerGetWeatherAnimatorName(String weatherKind, boolean daytime) {
        return Constants.getResourcesName(weatherKind)
                + Constants.SEPARATOR + (daytime ? Constants.DAY : Constants.NIGHT);
    }

    // minimal.

    @NonNull
    @Override
    public Drawable getMinimalLightIcon(String weatherKind, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawable(getMiniLightIconName(weatherKind, dayTime))
        );
    }

    @NonNull
    @Override
    public Uri getMinimalLightIconUri(String weatherKind, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawableUri(getMiniLightIconName(weatherKind, dayTime))
        );
    }

    @NonNull
    @Override
    public Drawable getMinimalGreyIcon(String weatherKind, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawable(getMiniGreyIconName(weatherKind, dayTime))
        );
    }

    @NonNull
    @Override
    public Uri getMinimalGreyIconUri(String weatherKind, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawableUri(getMiniGreyIconName(weatherKind, dayTime))
        );
    }

    @NonNull
    @Override
    public Drawable getMinimalDarkIcon(String weatherKind, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawable(getMiniDarkIconName(weatherKind, dayTime))
        );
    }

    @NonNull
    @Override
    public Uri getMinimalDarkIconUri(String weatherKind, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawableUri(getMiniDarkIconName(weatherKind, dayTime))
        );
    }

    @NonNull
    @Override
    public Drawable getMinimalXmlIcon(String weatherKind, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawable(getMiniXmlIconName(weatherKind, dayTime))
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public Icon getMinimalIcon(String weatherKind, boolean dayTime) {
        return Objects.requireNonNull(
                Icon.createWithResource(
                        context,
                        getMinimalXmlIconId(weatherKind, dayTime)
                )
        );
    }

    @DrawableRes
    public int getMinimalXmlIconId(String weatherKind, boolean dayTime) {
        return getResId(context, getMiniXmlIconName(weatherKind, dayTime), "drawable");
    }

    private String getMiniLightIconName(String weatherKind, boolean daytime) {
        return getFilterResource(
                drawableFilter,
                innerGetMiniIconName(weatherKind, daytime) + Constants.SEPARATOR + Constants.LIGHT
        );
    }

    private String getMiniGreyIconName(String weatherKind, boolean daytime) {
        return getFilterResource(
                drawableFilter,
                innerGetMiniIconName(weatherKind, daytime) + Constants.SEPARATOR + Constants.GREY
        );
    }

    private String getMiniDarkIconName(String weatherKind, boolean daytime) {
        return getFilterResource(
                drawableFilter,
                innerGetMiniIconName(weatherKind, daytime) + Constants.SEPARATOR + Constants.DARK
        );
    }

    private String getMiniXmlIconName(String weatherKind, boolean daytime) {
        return getFilterResource(
                drawableFilter,
                innerGetMiniIconName(weatherKind, daytime) + Constants.SEPARATOR + Constants.XML
        );
    }

    private static String innerGetMiniIconName(String weatherKind, boolean daytime) {
        return innerGetWeatherIconName(weatherKind, daytime)
                + Constants.SEPARATOR + Constants.MINI;
    }

    // shortcut.

    @NonNull
    @Override
    public Drawable getShortcutsIcon(String weatherKind, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawable(getShortcutsIconName(weatherKind, dayTime))
        );
    }

    @NonNull
    @Override
    public Drawable getShortcutsForegroundIcon(String weatherKind, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawable(getShortcutsForegroundIconName(weatherKind, dayTime))
        );
    }

    private String getShortcutsIconName(String weatherKind, boolean daytime) {
        return getFilterResource(
                shortcutFilter,
                innerGetShortcutsIconName(weatherKind, daytime)
        );
    }

    private String getShortcutsForegroundIconName(String weatherKind, boolean daytime) {
        return getFilterResource(
                shortcutFilter,
                getShortcutsIconName(weatherKind, daytime) + Constants.SEPARATOR + Constants.FOREGROUND
        );
    }

    private static String innerGetShortcutsIconName(String weatherKind, boolean daytime) {
        return Constants.getShortcutsName(weatherKind)
                + Constants.SEPARATOR + (daytime ? Constants.DAY : Constants.NIGHT);
    }

    // sun and moon.

    @NonNull
    @Override
    public Drawable getSunDrawable() {
        return new SunDrawable();
    }

    @NonNull
    @Override
    public Drawable getMoonDrawable() {
        return new MoonDrawable();
    }
}
