package wangdaye.com.geometricweather.theme.resource.providers;

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
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode;
import wangdaye.com.geometricweather.theme.resource.utils.Constants;
import wangdaye.com.geometricweather.theme.resource.utils.ResourceUtils;
import wangdaye.com.geometricweather.theme.resource.utils.XmlHelper;
import wangdaye.com.geometricweather.common.ui.images.MoonDrawable;
import wangdaye.com.geometricweather.common.ui.images.SunDrawable;

public class DefaultResourceProvider extends ResourceProvider {

    private final Context mContext;
    private final String mProviderName;
    @Nullable private final Drawable mIconDrawable;

    private Map<String, String> mDrawableFilter;
    private Map<String, String> mAnimatorFilter;
    private Map<String, String> mShortcutFilter;

    public DefaultResourceProvider() {
        mContext = GeometricWeather.getInstance();
        mProviderName = mContext.getString(R.string.geometric_weather);
        mIconDrawable = mContext.getApplicationInfo().loadIcon(mContext.getPackageManager());

        Resources res = mContext.getResources();
        try {
            mDrawableFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_drawable_filter));
            mAnimatorFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_animator_filter));
            mShortcutFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_shortcut_filter));
        } catch (Exception e) {
            mDrawableFilter = new HashMap<>();
            mAnimatorFilter = new HashMap<>();
            mShortcutFilter = new HashMap<>();
        }
    }

    public static boolean isDefaultIconProvider(@NonNull String packageName) {
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
        return mContext.getPackageName();
    }

    @Override
    public String getProviderName() {
        return mProviderName;
    }

    @Override
    public Drawable getProviderIcon() {
        return mIconDrawable;
    }

    // weather icon.

    @NonNull
    @Override
    public Drawable getWeatherIcon(WeatherCode code, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawable(getWeatherIconName(code, dayTime))
        );
    }

    @NonNull
    @Override
    public Uri getWeatherIconUri(WeatherCode code, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawableUri(getWeatherIconName(code, dayTime))
        );
    }

    @Override
    @Size(3)
    public Drawable[] getWeatherIcons(WeatherCode code, boolean dayTime) {
        return new Drawable[] {
                getDrawable(getWeatherIconName(code, dayTime, 1)),
                getDrawable(getWeatherIconName(code, dayTime, 2)),
                getDrawable(getWeatherIconName(code, dayTime, 3))
        };
    }

    @Nullable
    private Drawable getDrawable(@NonNull String resName) {
        try {
            return ResourcesCompat.getDrawable(
                    mContext.getResources(),
                    ResourceUtils.nonNull(getResId(mContext, resName, "drawable")),
                    null
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String getWeatherIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                mDrawableFilter,
                innerGetWeatherIconName(code, daytime)
        );
    }

    private String getWeatherIconName(WeatherCode code, boolean daytime,
                                      @IntRange(from = 1, to = 3) int index) {
        return getFilterResource(
                mDrawableFilter,
                innerGetWeatherIconName(code, daytime) + Constants.SEPARATOR + index
        );
    }

    private static String innerGetWeatherIconName(WeatherCode code, boolean daytime) {
        return Constants.getResourcesName(code)
                + Constants.SEPARATOR + (daytime ? Constants.DAY : Constants.NIGHT);
    }

    // animator.

    @Override
    @Size(3)
    public Animator[] getWeatherAnimators(WeatherCode code, boolean dayTime) {
        return new Animator[] {
                getAnimator(getWeatherAnimatorName(code, dayTime, 1)),
                getAnimator(getWeatherAnimatorName(code, dayTime, 2)),
                getAnimator(getWeatherAnimatorName(code, dayTime, 3))
        };
    }

    @Nullable
    private Animator getAnimator(@NonNull String resName) {
        try {
            return AnimatorInflater.loadAnimator(
                    mContext,
                    ResourceUtils.nonNull(getResId(mContext, resName, "animator"))
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String getWeatherAnimatorName(WeatherCode code, boolean daytime,
                                          @IntRange(from = 1, to = 3) int index) {
        return getFilterResource(
                mAnimatorFilter,
                innerGetWeatherAnimatorName(code, daytime) + Constants.SEPARATOR + index
        );
    }

    private static String innerGetWeatherAnimatorName(WeatherCode code, boolean daytime) {
        return Constants.getResourcesName(code)
                + Constants.SEPARATOR + (daytime ? Constants.DAY : Constants.NIGHT);
    }

    // minimal.

    @NonNull
    @Override
    public Drawable getMinimalLightIcon(WeatherCode code, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawable(getMiniLightIconName(code, dayTime))
        );
    }

    @NonNull
    @Override
    public Uri getMinimalLightIconUri(WeatherCode code, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawableUri(getMiniLightIconName(code, dayTime))
        );
    }

    @NonNull
    @Override
    public Drawable getMinimalGreyIcon(WeatherCode code, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawable(getMiniGreyIconName(code, dayTime))
        );
    }

    @NonNull
    @Override
    public Uri getMinimalGreyIconUri(WeatherCode code, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawableUri(getMiniGreyIconName(code, dayTime))
        );
    }

    @NonNull
    @Override
    public Drawable getMinimalDarkIcon(WeatherCode code, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawable(getMiniDarkIconName(code, dayTime))
        );
    }

    @NonNull
    @Override
    public Uri getMinimalDarkIconUri(WeatherCode code, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawableUri(getMiniDarkIconName(code, dayTime))
        );
    }

    @NonNull
    @Override
    public Drawable getMinimalXmlIcon(WeatherCode code, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawable(getMiniXmlIconName(code, dayTime))
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public Icon getMinimalIcon(WeatherCode code, boolean dayTime) {
        return Objects.requireNonNull(
                Icon.createWithResource(
                        mContext,
                        getMinimalXmlIconId(code, dayTime)
                )
        );
    }

    @DrawableRes
    public int getMinimalXmlIconId(WeatherCode code, boolean dayTime) {
        return getResId(mContext, getMiniXmlIconName(code, dayTime), "drawable");
    }

    private String getMiniLightIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                mDrawableFilter,
                innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.LIGHT
        );
    }

    private String getMiniGreyIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                mDrawableFilter,
                innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.GREY
        );
    }

    private String getMiniDarkIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                mDrawableFilter,
                innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.DARK
        );
    }

    private String getMiniXmlIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                mDrawableFilter,
                innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.XML
        );
    }

    private static String innerGetMiniIconName(WeatherCode code, boolean daytime) {
        return innerGetWeatherIconName(code, daytime)
                + Constants.SEPARATOR + Constants.MINI;
    }

    // shortcut.

    @NonNull
    @Override
    public Drawable getShortcutsIcon(WeatherCode code, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawable(getShortcutsIconName(code, dayTime))
        );
    }

    @NonNull
    @Override
    public Drawable getShortcutsForegroundIcon(WeatherCode code, boolean dayTime) {
        return Objects.requireNonNull(
                getDrawable(getShortcutsForegroundIconName(code, dayTime))
        );
    }

    private String getShortcutsIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                mShortcutFilter,
                innerGetShortcutsIconName(code, daytime)
        );
    }

    private String getShortcutsForegroundIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                mShortcutFilter,
                getShortcutsIconName(code, daytime) + Constants.SEPARATOR + Constants.FOREGROUND
        );
    }

    private static String innerGetShortcutsIconName(WeatherCode code, boolean daytime) {
        return Constants.getShortcutsName(code)
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
