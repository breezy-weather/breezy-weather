package wangdaye.com.geometricweather.theme.resource.providers;

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
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode;
import wangdaye.com.geometricweather.theme.resource.utils.Config;
import wangdaye.com.geometricweather.theme.resource.utils.Constants;
import wangdaye.com.geometricweather.theme.resource.utils.ResourceUtils;
import wangdaye.com.geometricweather.theme.resource.utils.XmlHelper;

public class IconPackResourcesProvider extends ResourceProvider {

    private final ResourceProvider mDefaultProvider;

    private Context mContext;
    private String mProviderName;
    @Nullable private Drawable mIconDrawable;

    private Config mConfig;
    private Map<String, String> mDrawableFilter;
    private Map<String, String> mAnimatorFilter;
    private Map<String, String> mShortcutFilter;
    private Map<String, String> mSunMoonFilter;

    public IconPackResourcesProvider(@NonNull Context c, @NonNull String pkgName,
                                     @NonNull ResourceProvider defaultProvider) {
        mDefaultProvider = defaultProvider;

        try {
            mContext = c.createPackageContext(
                    pkgName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

            PackageManager manager = mContext.getPackageManager();
            ApplicationInfo info = manager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
            mProviderName = manager.getApplicationLabel(info).toString();

            mIconDrawable = mContext.getApplicationInfo().loadIcon(mContext.getPackageManager());

            Resources res = mContext.getResources();

            int resId = getMetaDataResource(Constants.META_DATA_PROVIDER_CONFIG);
            if (resId != 0) {
                mConfig = XmlHelper.getConfig(res.getXml(resId));
            } else {
                mConfig = new Config();
            }

            resId = getMetaDataResource(Constants.META_DATA_DRAWABLE_FILTER);
            if (resId != 0) {
                mDrawableFilter = XmlHelper.getFilterMap(res.getXml(resId));
            } else {
                mDrawableFilter = new HashMap<>();
            }

            resId = getMetaDataResource(Constants.META_DATA_ANIMATOR_FILTER);
            if (resId != 0) {
                mAnimatorFilter = XmlHelper.getFilterMap(res.getXml(resId));
            } else {
                mAnimatorFilter = new HashMap<>();
            }

            resId = getMetaDataResource(Constants.META_DATA_SHORTCUT_FILTER);
            if (resId != 0) {
                mShortcutFilter = XmlHelper.getFilterMap(res.getXml(resId));
            } else {
                mShortcutFilter = new HashMap<>();
            }

            resId = getMetaDataResource(Constants.META_DATA_SUN_MOON_FILTER);
            if (resId != 0) {
                mSunMoonFilter = XmlHelper.getFilterMap(res.getXml(resId));
            } else {
                mSunMoonFilter = new HashMap<>();
            }
        } catch (Exception e) {
            buildDefaultInstance(c);
        }
    }

    private void buildDefaultInstance(@NonNull Context c) {
        mContext = c.getApplicationContext();
        mProviderName = c.getString(R.string.geometric_weather);
        mIconDrawable = mDefaultProvider.getProviderIcon();

        Resources res = mContext.getResources();
        try {
            mConfig = XmlHelper.getConfig(res.getXml(R.xml.icon_provider_config));
            mDrawableFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_drawable_filter));
            mAnimatorFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_animator_filter));
            mShortcutFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_shortcut_filter));
            mSunMoonFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_sun_moon_filter));
        } catch (Exception e) {
            mConfig = new Config();
            mDrawableFilter = new HashMap<>();
            mAnimatorFilter = new HashMap<>();
            mShortcutFilter = new HashMap<>();
            mSunMoonFilter = new HashMap<>();
        }
    }

    @NonNull
    public static List<IconPackResourcesProvider> getProviderList(@NonNull Context context,
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

    public static boolean isIconPackIconProvider(@NonNull Context context, @NonNull String packageName) {
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
            return mContext.getPackageManager().getApplicationInfo(
                    mContext.getPackageName(),
                    PackageManager.GET_META_DATA
            ).metaData.getInt(key);
        } catch (Exception e) {
            return 0;
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
        if (mIconDrawable == null) {
            return getWeatherIcon(WeatherCode.CLEAR, true);
        } else {
            return mIconDrawable;
        }
    }

    // weather icon.

    @Override
    @NonNull
    public Drawable getWeatherIcon(WeatherCode code, boolean dayTime) {
        try {
            if (mConfig.hasWeatherIcons) {
                return ResourceUtils.nonNull(
                        getDrawable(getWeatherIconName(code, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return mDefaultProvider.getWeatherIcon(code, dayTime);
    }

    @NonNull
    @Override
    public Uri getWeatherIconUri(WeatherCode code, boolean dayTime) {
        if (mConfig.hasWeatherIcons) {
            String resName = getWeatherIconName(code, dayTime);
            int resId = getResId(mContext, resName, "drawable");
            if (resId != 0) {
                return getDrawableUri(resName);
            }
        }

        return mDefaultProvider.getWeatherIconUri(code, dayTime);
    }

    @Override
    @Size(3)
    public Drawable[] getWeatherIcons(WeatherCode code, boolean dayTime) {
        if (mConfig.hasWeatherIcons) {
            if (mConfig.hasWeatherAnimators) {
                return new Drawable[] {
                        getDrawable(getWeatherIconName(code, dayTime, 1)),
                        getDrawable(getWeatherIconName(code, dayTime, 2)),
                        getDrawable(getWeatherIconName(code, dayTime, 3))
                };
            } else {
                return new Drawable[] {getWeatherIcon(code, dayTime), null, null};
            }
        }

        return mDefaultProvider.getWeatherIcons(code, dayTime);
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

    String getWeatherIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                mDrawableFilter,
                innerGetWeatherIconName(code, daytime)
        );
    }

    String getWeatherIconName(WeatherCode code, boolean daytime,
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
        if (mConfig.hasWeatherIcons) {
            if (mConfig.hasWeatherAnimators) {
                return new Animator[] {
                        getAnimator(getWeatherAnimatorName(code, dayTime, 1)),
                        getAnimator(getWeatherAnimatorName(code, dayTime, 2)),
                        getAnimator(getWeatherAnimatorName(code, dayTime, 3))
                };
            } else {
                return new Animator[] {null, null, null};
            }
        }

        return mDefaultProvider.getWeatherAnimators(code, dayTime);
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

    String getWeatherAnimatorName(WeatherCode code, boolean daytime,
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

    // minimal icon.

    @Override
    @NonNull
    public Drawable getMinimalLightIcon(WeatherCode code, boolean dayTime) {
        try {
            if (mConfig.hasMinimalIcons) {
                return ResourceUtils.nonNull(
                        getDrawable(getMiniLightIconName(code, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return mDefaultProvider.getMinimalLightIcon(code, dayTime);
    }

    @NonNull
    @Override
    public Uri getMinimalLightIconUri(WeatherCode code, boolean dayTime) {
        if (mConfig.hasMinimalIcons) {
            String resName = getMiniLightIconName(code, dayTime);
            int resId = getResId(mContext, resName, "drawable");
            if (resId != 0) {
                return getDrawableUri(resName);
            }
        }

        return mDefaultProvider.getMinimalLightIconUri(code, dayTime);
    }

    @Override
    @NonNull
    public Drawable getMinimalGreyIcon(WeatherCode code, boolean dayTime) {
        try {
            if (mConfig.hasMinimalIcons) {
                return ResourceUtils.nonNull(
                        getDrawable(getMiniGreyIconName(code, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return mDefaultProvider.getMinimalGreyIcon(code, dayTime);
    }

    @NonNull
    @Override
    public Uri getMinimalGreyIconUri(WeatherCode code, boolean dayTime) {
        if (mConfig.hasMinimalIcons) {
            String resName = getMiniGreyIconName(code, dayTime);
            int resId = getResId(mContext, resName, "drawable");
            if (resId != 0) {
                return getDrawableUri(resName);
            }
        }

        return mDefaultProvider.getMinimalGreyIconUri(code, dayTime);
    }

    @Override
    @NonNull
    public Drawable getMinimalDarkIcon(WeatherCode code, boolean dayTime) {
        try {
            if (mConfig.hasMinimalIcons) {
                return ResourceUtils.nonNull(
                        getDrawable(getMiniDarkIconName(code, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return mDefaultProvider.getMinimalDarkIcon(code, dayTime);
    }

    @NonNull
    @Override
    public Uri getMinimalDarkIconUri(WeatherCode code, boolean dayTime) {
        if (mConfig.hasMinimalIcons) {
            String resName = getMiniDarkIconName(code, dayTime);
            int resId = getResId(mContext, resName, "drawable");
            if (resId != 0) {
                return getDrawableUri(resName);
            }
        }

        return mDefaultProvider.getMinimalDarkIconUri(code, dayTime);
    }

    @Override
    @NonNull
    public Drawable getMinimalXmlIcon(WeatherCode code, boolean dayTime) {
        try {
            if (mConfig.hasMinimalIcons) {
                return ResourceUtils.nonNull(
                        getDrawable(getMiniXmlIconName(code, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return mDefaultProvider.getMinimalXmlIcon(code, dayTime);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public Icon getMinimalIcon(WeatherCode code, boolean dayTime) {
        try {
            if (mConfig.hasMinimalIcons) {
                return ResourceUtils.nonNull(
                        Icon.createWithResource(
                                mContext,
                                ResourceUtils.nonNull(getResId(
                                        mContext,
                                        getMiniXmlIconName(code, dayTime),
                                        "drawable"
                                ))
                        )
                );
            }
        } catch (Exception ignore) {

        }

        return mDefaultProvider.getMinimalIcon(code, dayTime);
    }

    String getMiniLightIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                mDrawableFilter,
                innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.LIGHT
        );
    }

    String getMiniGreyIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                mDrawableFilter,
                innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.GREY
        );
    }

    String getMiniDarkIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                mDrawableFilter,
                innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.DARK
        );
    }

    String getMiniXmlIconName(WeatherCode code, boolean daytime) {
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

    @Override
    @NonNull
    public Drawable getShortcutsIcon(WeatherCode code, boolean dayTime) {
        try {
            if (mConfig.hasShortcutIcons) {
                return ResourceUtils.nonNull(
                        getDrawable(getShortcutsIconName(code, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return mDefaultProvider.getShortcutsIcon(code, dayTime);
    }

    @Override
    @NonNull
    public Drawable getShortcutsForegroundIcon(WeatherCode code, boolean dayTime) {
        try {
            if (mConfig.hasShortcutIcons) {
                return ResourceUtils.nonNull(
                        getDrawable(getShortcutsForegroundIconName(code, dayTime))
                );
            }
        } catch (Exception ignore) {

        }

        return mDefaultProvider.getShortcutsForegroundIcon(code, dayTime);
    }

    String getShortcutsIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                mShortcutFilter,
                innerGetShortcutsIconName(code, daytime)
        );
    }

    String getShortcutsForegroundIconName(WeatherCode code, boolean daytime) {
        return getFilterResource(
                mShortcutFilter,
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
        if (mConfig.hasSunMoonDrawables) {
            try {
                return ResourceUtils.nonNull(
                        getReflectDrawable(getSunDrawableClassName())
                );
            } catch (Exception e) {
                return getWeatherIcon(WeatherCode.CLEAR, true);
            }
        }

        return mDefaultProvider.getSunDrawable();
    }

    @Override
    @NonNull
    public Drawable getMoonDrawable() {
        if (mConfig.hasSunMoonDrawables) {
            try {
                return ResourceUtils.nonNull(
                        getReflectDrawable(getMoonDrawableClassName())
                );
            } catch (Exception e) {
                return getWeatherIcon(WeatherCode.CLEAR, false);
            }
        }

        return mDefaultProvider.getMoonDrawable();
    }

    @Nullable
    private Drawable getReflectDrawable(@Nullable String className) {
        try {
            Class clazz = mContext.getClassLoader().loadClass(className);
            return (Drawable) clazz.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    String getSunDrawableClassName() {
        return mSunMoonFilter.get(Constants.RESOURCES_SUN);
    }

    @Nullable
    String getMoonDrawableClassName() {
        return mSunMoonFilter.get(Constants.RESOURCES_MOON);
    }
}
