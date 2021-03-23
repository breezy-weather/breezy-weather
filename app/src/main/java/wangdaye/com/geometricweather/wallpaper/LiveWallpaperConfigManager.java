package wangdaye.com.geometricweather.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;

public class LiveWallpaperConfigManager {

    private final String mWeatherKind;
    private final String mDayNightType;

    private static final String SP_LIVE_WALLPAPER_CONFIG = "live_wallpaper_config";
    private static final String KEY_WEATHER_KIND = "weather_kind";
    private static final String KEY_DAY_NIGHT_TYPE = "day_night_type";

    private LiveWallpaperConfigManager(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                SP_LIVE_WALLPAPER_CONFIG, Context.MODE_PRIVATE);
        mWeatherKind = sharedPreferences.getString(KEY_WEATHER_KIND, "auto");
        mDayNightType = sharedPreferences.getString(KEY_DAY_NIGHT_TYPE, "auto");
    }

    public static LiveWallpaperConfigManager getInstance(Context context) {
        return new LiveWallpaperConfigManager(context);
    }

    public String getWeatherKind() {
        return mWeatherKind;
    }

    public String getDayNightType() {
        return mDayNightType;
    }

    public static void update(Context context, String weatherKind, String dayNightType) {
        context.getSharedPreferences(SP_LIVE_WALLPAPER_CONFIG, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_WEATHER_KIND, weatherKind)
                .putString(KEY_DAY_NIGHT_TYPE, dayNightType)
                .apply();
    }
}
