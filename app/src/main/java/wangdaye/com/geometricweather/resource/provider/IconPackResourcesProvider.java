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

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.resource.Config;
import wangdaye.com.geometricweather.resource.Constants;
import wangdaye.com.geometricweather.resource.XmlHelper;

public class IconPackResourcesProvider extends ResourceProvider {

    private ResourceProvider defaultProvider;

    private Context context;
    private String providerName;

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

        Resources res = context.getResources();
        try {
            config = XmlHelper.getConfig(res.getXml(R.xml.provider_config));
            drawableFilter = XmlHelper.getFilterMap(res.getXml(R.xml.drawable_filter));
            animatorFilter = XmlHelper.getFilterMap(res.getXml(R.xml.animator_filter));
            shortcutFilter = XmlHelper.getFilterMap(res.getXml(R.xml.shortcut_filter));
            sunMoonFilter = XmlHelper.getFilterMap(res.getXml(R.xml.sun_moon_filter));
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

    @NonNull
    private static String getFilterResource(Map<String, String> filter, String key) {
        try {
            return Objects.requireNonNull(filter.get(key));
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
        try {
            return context.getPackageManager()
                    .getApplicationIcon(context.getPackageName());
        } catch (Exception e) {
            return getWeatherIcon(Weather.KIND_CLEAR, true);
        }
    }

    // weather icon.

    @Override
    @NonNull
    public Drawable getWeatherIcon(String weatherKind, boolean dayTime) {
        try {
            if (config.hasWeatherIcons) {
                return Objects.requireNonNull(
                        getDrawable(getWeatherIconName(weatherKind, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return defaultProvider.getWeatherIcon(weatherKind, dayTime);
    }

    @Override
    @Size(3)
    public Drawable[] getWeatherIcons(String weatherKind, boolean dayTime) {
        if (config.hasWeatherIcons) {
            if (config.hasWeatherAnimators) {
                return new Drawable[] {
                        getDrawable(getWeatherIconName(weatherKind, dayTime, 1)),
                        getDrawable(getWeatherIconName(weatherKind, dayTime, 2)),
                        getDrawable(getWeatherIconName(weatherKind, dayTime, 3))
                };
            } else {
                return new Drawable[] {getWeatherIcon(weatherKind, dayTime), null, null};
            }
        }

        return defaultProvider.getWeatherIcons(weatherKind, dayTime);
    }

    @Nullable
    private Drawable getDrawable(@NonNull String resName) {
        try {
            return ResourcesCompat.getDrawable(
                    context.getResources(),
                    getResId(context, resName, "drawable"),
                    // res.getIdentifier(resName, "drawable", context.getPackageName()),
                    null
            );
        } catch (Exception e) {
            return null;
        }
    }

    String getWeatherIconName(String weatherKind, boolean daytime) {
        return getFilterResource(
                drawableFilter,
                innerGetWeatherIconName(weatherKind, daytime)
        );
    }

    String getWeatherIconName(String weatherKind, boolean daytime,
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
        if (config.hasWeatherIcons) {
            if (config.hasWeatherAnimators) {
                return new Animator[] {
                        getAnimator(getWeatherAnimatorName(weatherKind, dayTime, 1)),
                        getAnimator(getWeatherAnimatorName(weatherKind, dayTime, 2)),
                        getAnimator(getWeatherAnimatorName(weatherKind, dayTime, 3))
                };
            } else {
                return new Animator[] {null, null, null};
            }
        }

        return defaultProvider.getWeatherAnimators(weatherKind, dayTime);
    }

    @Nullable
    private Animator getAnimator(@NonNull String resName) {
        try {
            return AnimatorInflater.loadAnimator(
                    context,
                    getResId(context, resName, "animator")
                    /*
                    context.getResources().getIdentifier(
                            resName,
                            "animator",
                            context.getPackageName()
                    )*/
            );
        } catch (Exception e) {
            return null;
        }
    }

    String getWeatherAnimatorName(String weatherKind, boolean daytime,
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

    // minimal icon.

    @Override
    @NonNull
    public Drawable getMinimalLightIcon(String weatherKind, boolean dayTime) {
        try {
            if (config.hasMinimalIcons) {
                return Objects.requireNonNull(
                        getDrawable(getMiniLightIconName(weatherKind, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return defaultProvider.getMinimalLightIcon(weatherKind, dayTime);
    }

    @Override
    @NonNull
    public Drawable getMinimalGreyIcon(String weatherKind, boolean dayTime) {
        try {
            if (config.hasMinimalIcons) {
                return Objects.requireNonNull(
                        getDrawable(getMiniGreyIconName(weatherKind, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return defaultProvider.getMinimalGreyIcon(weatherKind, dayTime);
    }

    @Override
    @NonNull
    public Drawable getMinimalDarkIcon(String weatherKind, boolean dayTime) {
        try {
            if (config.hasMinimalIcons) {
                return Objects.requireNonNull(
                        getDrawable(getMiniDarkIconName(weatherKind, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return defaultProvider.getMinimalDarkIcon(weatherKind, dayTime);
    }

    @Override
    @NonNull
    public Drawable getMinimalXmlIcon(String weatherKind, boolean dayTime) {
        try {
            if (config.hasMinimalIcons) {
                return Objects.requireNonNull(
                        getDrawable(getMiniXmlIconName(weatherKind, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return defaultProvider.getMinimalXmlIcon(weatherKind, dayTime);
    }

    @Override
    public int getMinimalXmlIconId(String weatherKind, boolean dayTime) {
        int id = getResId(context, getMiniXmlIconName(weatherKind, dayTime), "drawable");
        if (id != 0) {
            return id;
        }

        return defaultProvider.getMinimalXmlIconId(weatherKind, dayTime);
    }

    String getMiniLightIconName(String weatherKind, boolean daytime) {
        return getFilterResource(
                drawableFilter,
                innerGetMiniIconName(weatherKind, daytime) + Constants.SEPARATOR + Constants.LIGHT
        );
    }

    String getMiniGreyIconName(String weatherKind, boolean daytime) {
        return getFilterResource(
                drawableFilter,
                innerGetMiniIconName(weatherKind, daytime) + Constants.SEPARATOR + Constants.GREY
        );
    }

    String getMiniDarkIconName(String weatherKind, boolean daytime) {
        return getFilterResource(
                drawableFilter,
                innerGetMiniIconName(weatherKind, daytime) + Constants.SEPARATOR + Constants.DARK
        );
    }

    String getMiniXmlIconName(String weatherKind, boolean daytime) {
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

    @Override
    @NonNull
    public Drawable getShortcutsIcon(String weatherKind, boolean dayTime) {
        try {
            if (config.hasShortcutIcons) {
                return Objects.requireNonNull(
                        getDrawable(getShortcutsIconName(weatherKind, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return defaultProvider.getShortcutsIcon(weatherKind, dayTime);
    }

    @Override
    @NonNull
    public Drawable getShortcutsForegroundIcon(String weatherKind, boolean dayTime) {
        try {
            if (config.hasShortcutIcons) {
                return Objects.requireNonNull(
                        getDrawable(getShortcutsForegroundIconName(weatherKind, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return defaultProvider.getShortcutsForegroundIcon(weatherKind, dayTime);
    }

    String getShortcutsIconName(String weatherKind, boolean daytime) {
        return getFilterResource(
                shortcutFilter,
                innerGetShortcutsIconName(weatherKind, daytime)
        );
    }

    String getShortcutsForegroundIconName(String weatherKind, boolean daytime) {
        return getFilterResource(
                shortcutFilter,
                innerGetShortcutsIconName(weatherKind, daytime) + Constants.SEPARATOR + Constants.FOREGROUND
        );
    }

    private static String innerGetShortcutsIconName(String weatherKind, boolean daytime) {
        return Constants.getShortcutsName(weatherKind)
                + Constants.SEPARATOR + (daytime ? Constants.DAY : Constants.NIGHT);
    }

    // sun and moon.

    @Override
    @NonNull
    public Drawable getSunDrawable() {
        if (config.hasSunMoonDrawables) {
            try {
                return Objects.requireNonNull(
                        getReflectDrawable(getSunDrawableClassName())
                );
            } catch (Exception e) {
                return getWeatherIcon(Weather.KIND_CLEAR, true);
            }
        }

        return defaultProvider.getSunDrawable();
    }

    @Override
    @NonNull
    public Drawable getMoonDrawable() {
        if (config.hasSunMoonDrawables) {
            try {
                return Objects.requireNonNull(
                        getReflectDrawable(getMoonDrawableClassName())
                );
            } catch (Exception e) {
                return getWeatherIcon(Weather.KIND_CLEAR, false);
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
