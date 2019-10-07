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
import wangdaye.com.geometricweather.basic.model.weather.WeatherCode;
import wangdaye.com.geometricweather.resource.Constants;
import wangdaye.com.geometricweather.resource.ResourceUtils;

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
            return getWeatherIcon(WeatherCode.CLEAR, true);
        } else {
            return iconDrawable;
        }
    }

    // weather icon.

    @NonNull
    @Override
    public Drawable getWeatherIcon(WeatherCode code, boolean dayTime) {
        try {
            return ResourceUtils.nonNull(
                    getDrawable(getWeatherIconName(code, dayTime))
            );
        } catch (Exception ignore) {

        }

        return defaultProvider.getWeatherIcon(code, dayTime);
    }

    @NonNull
    @Override
    public Uri getWeatherIconUri(WeatherCode code, boolean dayTime) {
        String resName = getWeatherIconName(code, dayTime);
        int resId = getResId(context, resName, "drawable");
        if (resId != 0) {
            return getDrawableUri(resName);
        } else {
            return defaultProvider.getWeatherIconUri(code, dayTime);
        }
    }

    @Override
    public Drawable[] getWeatherIcons(WeatherCode code, boolean dayTime) {
        return new Drawable[] {getWeatherIcon(code, dayTime), null, null};
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
        return "weather" + Constants.SEPARATOR + getWeatherIconCode(code, daytime);
    }

    private String getWeatherIconCode(WeatherCode code, boolean daytime) {
        switch (code) {
            case CLEAR:
                return daytime ? "32" : "31";

            case PARTLY_CLOUDY:
                return daytime ? "30" : "29";

            case CLOUDY:
                return "26";

            case RAIN:
                return "11";

            case SNOW:
                return "16";

            case WIND:
                return "24";

            case FOG:
                return "20";

            case HAZE:
                return "19";

            case SLEET:
                return "5";

            case HAIL:
                return "17";

            case THUNDER:
                return daytime ? "37" : "47";

            case THUNDERSTORM:
                return daytime ? "38" : "45";
        }
        return "na";
    }

    // animator.

    @Override
    public Animator[] getWeatherAnimators(WeatherCode code, boolean dayTime) {
        return new Animator[] {null, null, null};
    }

    // minimal icon.

    @NonNull
    @Override
    public Drawable getMinimalLightIcon(WeatherCode code, boolean dayTime) {
        return defaultProvider.getMinimalLightIcon(code, dayTime);
    }

    @NonNull
    @Override
    public Uri getMinimalLightIconUri(WeatherCode code, boolean dayTime) {
        return defaultProvider.getMinimalLightIconUri(code, dayTime);
    }

    @NonNull
    @Override
    public Drawable getMinimalGreyIcon(WeatherCode code, boolean dayTime) {
        return defaultProvider.getMinimalGreyIcon(code, dayTime);
    }

    @NonNull
    @Override
    public Uri getMinimalGreyIconUri(WeatherCode code, boolean dayTime) {
        return defaultProvider.getMinimalGreyIconUri(code, dayTime);
    }

    @NonNull
    @Override
    public Drawable getMinimalDarkIcon(WeatherCode code, boolean dayTime) {
        return defaultProvider.getMinimalDarkIcon(code, dayTime);
    }

    @NonNull
    @Override
    public Uri getMinimalDarkIconUri(WeatherCode code, boolean dayTime) {
        return defaultProvider.getMinimalDarkIconUri(code, dayTime);
    }

    @NonNull
    @Override
    public Drawable getMinimalXmlIcon(WeatherCode code, boolean dayTime) {
        return defaultProvider.getMinimalXmlIcon(code, dayTime);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public Icon getMinimalIcon(WeatherCode code, boolean dayTime) {
        return defaultProvider.getMinimalIcon(code, dayTime);
    }

    // shortcut.

    @NonNull
    @Override
    public Drawable getShortcutsIcon(WeatherCode code, boolean dayTime) {
        return defaultProvider.getShortcutsIcon(code, dayTime);
    }

    @NonNull
    @Override
    public Drawable getShortcutsForegroundIcon(WeatherCode code, boolean dayTime) {
        return defaultProvider.getShortcutsForegroundIcon(code, dayTime);
    }

    @NonNull
    @Override
    public Drawable getSunDrawable() {
        return getWeatherIcon(WeatherCode.CLEAR, true);
    }

    @NonNull
    @Override
    public Drawable getMoonDrawable() {
        return getWeatherIcon(WeatherCode.CLEAR, false);
    }
}
