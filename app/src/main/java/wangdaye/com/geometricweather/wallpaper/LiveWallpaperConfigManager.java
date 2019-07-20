package wangdaye.com.geometricweather.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;

public class LiveWallpaperConfigManager {

    private String weatherKind;
    private String dayNightType;

    private static final String SP_LIVE_WALLPAPER_CONFIG = "live_wallpaper_config";
    private static final String KEY_WEATHER_KIND = "weather_kind";
    private static final String KEY_DAY_NIGHT_TYPE = "day_night_type";

    private LiveWallpaperConfigManager(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                SP_LIVE_WALLPAPER_CONFIG, Context.MODE_PRIVATE);
        weatherKind = sharedPreferences.getString(KEY_WEATHER_KIND, "auto");
        dayNightType = sharedPreferences.getString(KEY_DAY_NIGHT_TYPE, "auto");
    }

    public static LiveWallpaperConfigManager getInstance(Context context) {
        return new LiveWallpaperConfigManager(context);
    }

    public String getWeatherKind() {
        return weatherKind;
    }

    public String getDayNightType() {
        return dayNightType;
    }

    public static void update(Context context, String weatherKind, String dayNightType) {
        context.getSharedPreferences(SP_LIVE_WALLPAPER_CONFIG, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_WEATHER_KIND, weatherKind)
                .putString(KEY_DAY_NIGHT_TYPE, dayNightType)
                .apply();
    }
}
