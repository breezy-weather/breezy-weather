package wangdaye.com.geometricweather.resource.provider;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.resource.Constants;
import wangdaye.com.geometricweather.utils.ValueUtils;

public class ChronusResourceProvider extends ResourceProvider {

    private ResourceProvider defaultProvider;

    private Context context;
    private String providerName;
    @Nullable private Drawable iconDrawable;

    ChronusResourceProvider(@NonNull Context c, @NonNull String pkgName,
                            @NonNull ResourceProvider defaultProvider) {
        this.defaultProvider = defaultProvider;

        try {
            context = c.createPackageContext(
                    pkgName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

            PackageManager manager = context.getPackageManager();
            ApplicationInfo info = manager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
            providerName = manager.getApplicationLabel(info).toString();

            iconDrawable = context.getApplicationInfo().loadIcon(context.getPackageManager());
        } catch (Exception e) {
            buildDefaultInstance(c);
        }
    }

    private void buildDefaultInstance(@NonNull Context c) {
        context = c.getApplicationContext();
        providerName = c.getString(R.string.geometric_weather);
        iconDrawable = defaultProvider.getProviderIcon();
    }

    @NonNull
    static List<ChronusResourceProvider> getProviderList(@NonNull Context context,
                                                         @NonNull ResourceProvider defaultProvider) {
        List<ChronusResourceProvider> providerList = new ArrayList<>();

        List<ResolveInfo> infoList = context.getPackageManager().queryIntentActivities(
                new Intent(Intent.ACTION_MAIN).addCategory(Constants.CATEGORY_CHRONUS_ICON_PACK),
                PackageManager.GET_RESOLVED_FILTER
        );
        for (ResolveInfo info : infoList) {
            providerList.add(
                    new ChronusResourceProvider(
                            context,
                            info.activityInfo.applicationInfo.packageName,
                            defaultProvider
                    )
            );
        }

        return providerList;
    }

    static boolean isChronusIconProvider(@NonNull Context context, @NonNull String packageName) {
        List<ResolveInfo> infoList = context.getPackageManager().queryIntentActivities(
                new Intent(Intent.ACTION_MAIN).addCategory(Constants.CATEGORY_CHRONUS_ICON_PACK),
                PackageManager.GET_RESOLVED_FILTER
        );
        for (ResolveInfo info : infoList) {
            if (packageName.equals(info.activityInfo.applicationInfo.packageName)) {
                return true;
            }
        }

        return false;
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
            return getWeatherIcon(Weather.KIND_CLEAR, true);
        } else {
            return iconDrawable;
        }
    }

    // weather icon.

    @NonNull
    @Override
    public Drawable getWeatherIcon(String weatherKind, boolean dayTime) {
        try {
            return ValueUtils.nonNull(
                    getDrawable(getWeatherIconName(weatherKind, dayTime))
            );
        } catch (Exception ignore) {

        }

        return defaultProvider.getWeatherIcon(weatherKind, dayTime);
    }

    @NonNull
    @Override
    public Uri getWeatherIconUri(String weatherKind, boolean dayTime) {
        String resName = getWeatherIconName(weatherKind, dayTime);
        int resId = getResId(context, resName, "drawable");
        if (resId != 0) {
            return getDrawableUri(resName);
        } else {
            return defaultProvider.getWeatherIconUri(weatherKind, dayTime);
        }
    }

    @Override
    public Drawable[] getWeatherIcons(String weatherKind, boolean dayTime) {
        return new Drawable[] {getWeatherIcon(weatherKind, dayTime), null, null};
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

    String getWeatherIconName(String weatherKind, boolean daytime) {
        return "weather" + Constants.SEPARATOR + getWeatherIconCode(weatherKind, daytime);
    }

    private String getWeatherIconCode(String weatherKind, boolean daytime) {
        switch (weatherKind) {
            case Weather.KIND_CLEAR:
                return daytime ? "32" : "31";

            case Weather.KIND_PARTLY_CLOUDY:
                return daytime ? "30" : "29";

            case Weather.KIND_CLOUDY:
                return "26";

            case Weather.KIND_RAIN:
                return "11";

            case Weather.KIND_SNOW:
                return "16";

            case Weather.KIND_WIND:
                return "24";

            case Weather.KIND_FOG:
                return "20";

            case Weather.KIND_HAZE:
                return "19";

            case Weather.KIND_SLEET:
                return "5";

            case Weather.KIND_HAIL:
                return "17";

            case Weather.KIND_THUNDER:
                return daytime ? "37" : "47";

            case Weather.KIND_THUNDERSTORM:
                return daytime ? "38" : "45";
        }
        return "na";
    }

    // animator.

    @Override
    public Animator[] getWeatherAnimators(String weatherKind, boolean dayTime) {
        return new Animator[] {null, null, null};
    }

    // minimal icon.

    @NonNull
    @Override
    public Drawable getMinimalLightIcon(String weatherKind, boolean dayTime) {
        return defaultProvider.getMinimalLightIcon(weatherKind, dayTime);
    }

    @NonNull
    @Override
    public Uri getMinimalLightIconUri(String weatherKind, boolean dayTime) {
        return defaultProvider.getMinimalLightIconUri(weatherKind, dayTime);
    }

    @NonNull
    @Override
    public Drawable getMinimalGreyIcon(String weatherKind, boolean dayTime) {
        return defaultProvider.getMinimalGreyIcon(weatherKind, dayTime);
    }

    @NonNull
    @Override
    public Uri getMinimalGreyIconUri(String weatherKind, boolean dayTime) {
        return defaultProvider.getMinimalGreyIconUri(weatherKind, dayTime);
    }

    @NonNull
    @Override
    public Drawable getMinimalDarkIcon(String weatherKind, boolean dayTime) {
        return defaultProvider.getMinimalDarkIcon(weatherKind, dayTime);
    }

    @NonNull
    @Override
    public Uri getMinimalDarkIconUri(String weatherKind, boolean dayTime) {
        return defaultProvider.getMinimalDarkIconUri(weatherKind, dayTime);
    }

    @NonNull
    @Override
    public Drawable getMinimalXmlIcon(String weatherKind, boolean dayTime) {
        return defaultProvider.getMinimalXmlIcon(weatherKind, dayTime);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public Icon getMinimalIcon(String weatherKind, boolean dayTime) {
        return defaultProvider.getMinimalIcon(weatherKind, dayTime);
    }

    // shortcut.

    @NonNull
    @Override
    public Drawable getShortcutsIcon(String weatherKind, boolean dayTime) {
        return defaultProvider.getShortcutsIcon(weatherKind, dayTime);
    }

    @NonNull
    @Override
    public Drawable getShortcutsForegroundIcon(String weatherKind, boolean dayTime) {
        return defaultProvider.getShortcutsForegroundIcon(weatherKind, dayTime);
    }

    @NonNull
    @Override
    public Drawable getSunDrawable() {
        return getWeatherIcon(Weather.KIND_CLEAR, true);
    }

    @NonNull
    @Override
    public Drawable getMoonDrawable() {
        return getWeatherIcon(Weather.KIND_CLEAR, false);
    }
}
