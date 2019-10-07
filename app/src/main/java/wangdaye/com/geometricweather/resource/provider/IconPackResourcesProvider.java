package wangdaye.com.geometricweather.resource.provider;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.Size;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.weather.WeatherCode;
import wangdaye.com.geometricweather.resource.Config;
import wangdaye.com.geometricweather.resource.Constants;
import wangdaye.com.geometricweather.resource.ResourceUtils;
import wangdaye.com.geometricweather.resource.XmlHelper;

public class IconPackResourcesProvider extends ResourceProvider {

    private ResourceProvider defaultProvider;

    private Context context;
    private String providerName;
    @Nullable private Drawable iconDrawable;

    private Config config;
    private Map<String, String> drawableFilter;
    private Map<String, String> animatorFilter;
    private Map<String, String> shortcutFilter;
    private Map<String, String> sunMoonFilter;

    IconPackResourcesProvider(@NonNull Context c, @NonNull String pkgName,
                              @NonNull ResourceProvider defaultProvider) {
        this.defaultProvider = defaultProvider;

        try {
            context = c.createPackageContext(
                    pkgName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

            PackageManager manager = context.getPackageManager();
            ApplicationInfo info = manager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
            providerName = manager.getApplicationLabel(info).toString();

            iconDrawable = context.getApplicationInfo().loadIcon(context.getPackageManager());

            Resources res = context.getResources();

            int resId = getMetaDataResource(Constants.META_DATA_PROVIDER_CONFIG);
            if (resId != 0) {
                config = XmlHelper.getConfig(res.getXml(resId));
            } else {
                config = new Config();
            }

            resId = getMetaDataResource(Constants.META_DATA_DRAWABLE_FILTER);
            if (resId != 0) {
                drawableFilter = XmlHelper.getFilterMap(res.getXml(resId));
            } else {
                drawableFilter = new HashMap<>();
            }

            resId = getMetaDataResource(Constants.META_DATA_ANIMATOR_FILTER);
            if (resId != 0) {
                animatorFilter = XmlHelper.getFilterMap(res.getXml(resId));
            } else {
                animatorFilter = new HashMap<>();
            }

            resId = getMetaDataResource(Constants.META_DATA_SHORTCUT_FILTER);
            if (resId != 0) {
                shortcutFilter = XmlHelper.getFilterMap(res.getXml(resId));
            } else {
                shortcutFilter = new HashMap<>();
            }

            resId = getMetaDataResource(Constants.META_DATA_SUN_MOON_FILTER);
            if (resId != 0) {
                sunMoonFilter = XmlHelper.getFilterMap(res.getXml(resId));
            } else {
                sunMoonFilter = new HashMap<>();
            }
        } catch (Exception e) {
            buildDefaultInstance(c);
        }
    }

    private void buildDefaultInstance(@NonNull Context c) {
        context = c.getApplicationContext();
        providerName = c.getString(R.string.geometric_weather);
        iconDrawable = defaultProvider.getProviderIcon();

        Resources res = context.getResources();
        try {
            config = XmlHelper.getConfig(res.getXml(R.xml.icon_provider_config));
            drawableFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_drawable_filter));
            animatorFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_animator_filter));
            shortcutFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_shortcut_filter));
            sunMoonFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_sun_moon_filter));
        } catch (Exception e) {
            config = new Config();
            drawableFilter = new HashMap<>();
            animatorFilter = new HashMap<>();
            shortcutFilter = new HashMap<>();
            sunMoonFilter = new HashMap<>();
        }
    }

    @NonNull
    static List<IconPackResourcesProvider> getProviderList(@NonNull Context context,
                                                           @NonNull ResourceProvider defaultProvider) {
        List<IconPackResourcesProvider> providerList = new ArrayList<>();

        List<ResolveInfo> infoList = context.getPackageManager().queryIntentActivities(
                new Intent(Constants.ACTION_ICON_PROVIDER),
                PackageManager.GET_RESOLVED_FILTER
        );
        for (ResolveInfo info : infoList) {
            providerList.add(
                    new IconPackResourcesProvider(
                            context,
                            info.activityInfo.applicationInfo.packageName,
                            defaultProvider
                    )
            );
        }

        return providerList;
    }

    static boolean isIconPackIconProvider(@NonNull Context context, @NonNull String packageName) {
        List<ResolveInfo> infoList = context.getPackageManager().queryIntentActivities(
                new Intent(Constants.ACTION_ICON_PROVIDER),
                PackageManager.GET_RESOLVED_FILTER
        );
        for (ResolveInfo info : infoList) {
            if (packageName.equals(info.activityInfo.applicationInfo.packageName)) {
                return true;
            }
        }

        return false;
    }

    @NonNull
    private static String getFilterResource(Map<String, String> filter, String key) {
        try {
            return ResourceUtils.nonNull(filter.get(key));
        } catch (Exception e) {
            return key;
        }
    }

    private int getMetaDataResource(String key) {
        try {
            return context.getPackageManager().getApplicationInfo(
                    context.getPackageName(),
                    PackageManager.GET_META_DATA
            ).metaData.getInt(key);
        } catch (Exception e) {
            return 0;
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
        if (iconDrawable == null) {
            return getWeatherIcon(WeatherCode.CLEAR, true);
        } else {
            return iconDrawable;
        }
    }

    // weather icon.

    @Override
    @NonNull
    public Drawable getWeatherIcon(WeatherCode code, boolean dayTime) {
        try {
            if (config.hasWeatherIcons) {
                return ResourceUtils.nonNull(
                        getDrawable(getWeatherIconName(code, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return defaultProvider.getWeatherIcon(code, dayTime);
    }

    @NonNull
    @Override
    public Uri getWeatherIconUri(WeatherCode code, boolean dayTime) {
        if (config.hasWeatherIcons) {
            String resName = getWeatherIconName(code, dayTime);
            int resId = getResId(context, resName, "drawable");
            if (resId != 0) {
                return getDrawableUri(resName);
            }
        }

        return defaultProvider.getWeatherIconUri(code, dayTime);
    }

    @Override
    @Size(3)
    public Drawable[] getWeatherIcons(WeatherCode code, boolean dayTime) {
        if (config.hasWeatherIcons) {
            if (config.hasWeatherAnimators) {
                return new Drawable[] {
                        getDrawable(getWeatherIconName(code, dayTime, 1)),
                        getDrawable(getWeatherIconName(code, dayTime, 2)),
                        getDrawable(getWeatherIconName(code, dayTime, 3))
                };
            } else {
                return new Drawable[] {getWeatherIcon(code, dayTime), null, null};
            }
        }

        return defaultProvider.getWeatherIcons(code, dayTime);
    }

    @Nullable
    private Drawable getDrawable(@NonNull String resName) {
        try {
            return ResourcesCompat.getDrawable(
                    context.getResources(),
                    ResourceUtils.nonNull(getResId(context, resName, "drawable")),
                    null
            );
        } catch (Exception e) {
            return null;
        }
    }

    String getWeatherIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                drawableFilter,
                innerGetWeatherIconName(code, daytime)
        );
    }

    String getWeatherIconName(WeatherCode code, boolean daytime,
                                     @IntRange(from = 1, to = 3) int index) {
        return getFilterResource(
                drawableFilter,
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
        if (config.hasWeatherIcons) {
            if (config.hasWeatherAnimators) {
                return new Animator[] {
                        getAnimator(getWeatherAnimatorName(code, dayTime, 1)),
                        getAnimator(getWeatherAnimatorName(code, dayTime, 2)),
                        getAnimator(getWeatherAnimatorName(code, dayTime, 3))
                };
            } else {
                return new Animator[] {null, null, null};
            }
        }

        return defaultProvider.getWeatherAnimators(code, dayTime);
    }

    @Nullable
    private Animator getAnimator(@NonNull String resName) {
        try {
            return AnimatorInflater.loadAnimator(
                    context,
                    ResourceUtils.nonNull(getResId(context, resName, "animator"))
            );
        } catch (Exception e) {
            return null;
        }
    }

    String getWeatherAnimatorName(WeatherCode code, boolean daytime,
                                          @IntRange(from = 1, to = 3) int index) {
        return getFilterResource(
                animatorFilter,
                innerGetWeatherAnimatorName(code, daytime) + Constants.SEPARATOR + index
        );
    }

    private static String innerGetWeatherAnimatorName(WeatherCode code, boolean daytime) {
        return Constants.getResourcesName(code)
                + Constants.SEPARATOR + (daytime ? Constants.DAY : Constants.NIGHT);
    }

    // minimal icon.

    @Override
    @NonNull
    public Drawable getMinimalLightIcon(WeatherCode code, boolean dayTime) {
        try {
            if (config.hasMinimalIcons) {
                return ResourceUtils.nonNull(
                        getDrawable(getMiniLightIconName(code, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return defaultProvider.getMinimalLightIcon(code, dayTime);
    }

    @NonNull
    @Override
    public Uri getMinimalLightIconUri(WeatherCode code, boolean dayTime) {
        if (config.hasMinimalIcons) {
            String resName = getMiniLightIconName(code, dayTime);
            int resId = getResId(context, resName, "drawable");
            if (resId != 0) {
                return getDrawableUri(resName);
            }
        }

        return defaultProvider.getMinimalLightIconUri(code, dayTime);
    }

    @Override
    @NonNull
    public Drawable getMinimalGreyIcon(WeatherCode code, boolean dayTime) {
        try {
            if (config.hasMinimalIcons) {
                return ResourceUtils.nonNull(
                        getDrawable(getMiniGreyIconName(code, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return defaultProvider.getMinimalGreyIcon(code, dayTime);
    }

    @NonNull
    @Override
    public Uri getMinimalGreyIconUri(WeatherCode code, boolean dayTime) {
        if (config.hasMinimalIcons) {
            String resName = getMiniGreyIconName(code, dayTime);
            int resId = getResId(context, resName, "drawable");
            if (resId != 0) {
                return getDrawableUri(resName);
            }
        }

        return defaultProvider.getMinimalGreyIconUri(code, dayTime);
    }

    @Override
    @NonNull
    public Drawable getMinimalDarkIcon(WeatherCode code, boolean dayTime) {
        try {
            if (config.hasMinimalIcons) {
                return ResourceUtils.nonNull(
                        getDrawable(getMiniDarkIconName(code, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return defaultProvider.getMinimalDarkIcon(code, dayTime);
    }

    @NonNull
    @Override
    public Uri getMinimalDarkIconUri(WeatherCode code, boolean dayTime) {
        if (config.hasMinimalIcons) {
            String resName = getMiniDarkIconName(code, dayTime);
            int resId = getResId(context, resName, "drawable");
            if (resId != 0) {
                return getDrawableUri(resName);
            }
        }

        return defaultProvider.getMinimalDarkIconUri(code, dayTime);
    }

    @Override
    @NonNull
    public Drawable getMinimalXmlIcon(WeatherCode code, boolean dayTime) {
        try {
            if (config.hasMinimalIcons) {
                return ResourceUtils.nonNull(
                        getDrawable(getMiniXmlIconName(code, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return defaultProvider.getMinimalXmlIcon(code, dayTime);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public Icon getMinimalIcon(WeatherCode code, boolean dayTime) {
        try {
            if (config.hasMinimalIcons) {
                return ResourceUtils.nonNull(
                        Icon.createWithResource(
                                context,
                                ResourceUtils.nonNull(getResId(
                                        context,
                                        getMiniXmlIconName(code, dayTime),
                                        "drawable"
                                ))
                        )
                );
            }
        } catch (Exception ignore) {

        }

        return defaultProvider.getMinimalIcon(code, dayTime);
    }

    String getMiniLightIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                drawableFilter,
                innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.LIGHT
        );
    }

    String getMiniGreyIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                drawableFilter,
                innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.GREY
        );
    }

    String getMiniDarkIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                drawableFilter,
                innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.DARK
        );
    }

    String getMiniXmlIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                drawableFilter,
                innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.XML
        );
    }

    private static String innerGetMiniIconName(WeatherCode code, boolean daytime) {
        return innerGetWeatherIconName(code, daytime)
                + Constants.SEPARATOR + Constants.MINI;
    }

    // shortcut.

    @Override
    @NonNull
    public Drawable getShortcutsIcon(WeatherCode code, boolean dayTime) {
        try {
            if (config.hasShortcutIcons) {
                return ResourceUtils.nonNull(
                        getDrawable(getShortcutsIconName(code, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return defaultProvider.getShortcutsIcon(code, dayTime);
    }

    @Override
    @NonNull
    public Drawable getShortcutsForegroundIcon(WeatherCode code, boolean dayTime) {
        try {
            if (config.hasShortcutIcons) {
                return ResourceUtils.nonNull(
                        getDrawable(getShortcutsForegroundIconName(code, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return defaultProvider.getShortcutsForegroundIcon(code, dayTime);
    }

    String getShortcutsIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                shortcutFilter,
                innerGetShortcutsIconName(code, daytime)
        );
    }

    String getShortcutsForegroundIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                shortcutFilter,
                innerGetShortcutsIconName(code, daytime) + Constants.SEPARATOR + Constants.FOREGROUND
        );
    }

    private static String innerGetShortcutsIconName(WeatherCode code, boolean daytime) {
        return Constants.getShortcutsName(code)
                + Constants.SEPARATOR + (daytime ? Constants.DAY : Constants.NIGHT);
    }

    // sun and moon.

    @Override
    @NonNull
    public Drawable getSunDrawable() {
        if (config.hasSunMoonDrawables) {
            try {
                return ResourceUtils.nonNull(
                        getReflectDrawable(getSunDrawableClassName())
                );
            } catch (Exception e) {
                return getWeatherIcon(WeatherCode.CLEAR, true);
            }
        }

        return defaultProvider.getSunDrawable();
    }

    @Override
    @NonNull
    public Drawable getMoonDrawable() {
        if (config.hasSunMoonDrawables) {
            try {
                return ResourceUtils.nonNull(
                        getReflectDrawable(getMoonDrawableClassName())
                );
            } catch (Exception e) {
                return getWeatherIcon(WeatherCode.CLEAR, false);
            }
        }

        return defaultProvider.getMoonDrawable();
    }

    @Nullable
    private Drawable getReflectDrawable(@Nullable String className) {
        try {
            Class clazz = context.getClassLoader().loadClass(className);
            return (Drawable) clazz.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    String getSunDrawableClassName() {
        return sunMoonFilter.get(Constants.RESOURCES_SUN);
    }

    @Nullable
    String getMoonDrawableClassName() {
        return sunMoonFilter.get(Constants.RESOURCES_MOON);
    }
}
