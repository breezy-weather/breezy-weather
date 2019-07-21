package wangdaye.com.geometricweather.remoteviews.presenter;

import android.Manifest;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.text.TextUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.ui.widget.weatherView.circularSkyView.CircularSkyWeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.resource.ResourceUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public abstract class AbstractRemoteViewsPresenter {

    private static final int SUBTITLE_DAILY_ITEM_LENGTH = 5;

    public static class WidgetConfig {
        public String viewStyle;
        public String cardStyle;
        public int cardAlpha;
        public String textColor;
        public int textSize;
        public boolean hideSubtitle;
        public String subtitleData;
        public String clockFont;
    }

    public static class WidgetColor {
        public boolean showCard;
        public boolean darkCard;
        public boolean darkText;

        public WidgetColor(Context context, boolean dayTime, String cardStyle, String textColor) {
            showCard = !cardStyle.equals("none");
            darkCard = cardStyle.equals("dark") || (cardStyle.equals("auto") && !dayTime);

            darkText = showCard
                    ? !darkCard // light card.
                    : textColor.equals("dark") || (textColor.equals("auto") && isLightWallpaper(context));
        }
    }

    public static WidgetConfig getWidgetConfig(Context context, String sharedPreferencesName) {
        WidgetConfig config = new WidgetConfig();

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                sharedPreferencesName,
                Context.MODE_PRIVATE
        );
        config.viewStyle = sharedPreferences.getString(
                context.getString(R.string.key_view_type),
                "rectangle"
        );
        config.cardStyle = sharedPreferences.getString(
                context.getString(R.string.key_card_style),
                "none"
        );
        config.cardAlpha = sharedPreferences.getInt(
                context.getString(R.string.key_card_alpha),
                100
        );
        config.textColor = sharedPreferences.getString(
                context.getString(R.string.key_text_color),
                "light"
        );
        config.textSize = sharedPreferences.getInt(
                context.getString(R.string.key_text_size),
                100
        );
        config.hideSubtitle = sharedPreferences.getBoolean(
                context.getString(R.string.key_hide_subtitle),
                false
        );
        config.subtitleData = sharedPreferences.getString(
                context.getString(R.string.key_subtitle_data),
                "time"
        );
        config.clockFont = sharedPreferences.getString(
                context.getString(R.string.key_clock_font),
                "light"
        );

        return config;
    }

    public static boolean isLightWallpaper(Context context) {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        try {
            WallpaperManager manager = WallpaperManager.getInstance(context);
            if (manager == null) {
                return false;
            }

            Drawable drawable = manager.getDrawable();
            if (!(drawable instanceof BitmapDrawable)) {
                return false;
            }

            return DisplayUtils.isLightColor(
                    DisplayUtils.bitmapToColorInt(((BitmapDrawable) drawable).getBitmap())
            );
        } catch (Exception ignore) {
            return false;
        }
    }

    @DrawableRes
    public static int getCardBackgroundId(Context context, boolean darkCard, int cardAlpha) {
        int resId;
        if (darkCard) {
            resId = ResourceUtils.getResId(
                    context,
                    "widget_card_dark_" + cardAlpha,
                    "drawable"
            );
            if (resId != 0) {
                return resId;
            }
            return R.drawable.widget_card_dark_100;
        } else {
            resId = ResourceUtils.getResId(
                    context,
                    "widget_card_light_" + cardAlpha,
                    "drawable"
            );
            if (resId != 0) {
                return resId;
            }
            return R.drawable.widget_card_light_100;
        }
    }

    public static PendingIntent getWeatherPendingIntent(Context context,
                                                        @Nullable Location location, int requestCode) {
        return PendingIntent.getActivity(
                context,
                requestCode,
                IntentHelper.buildMainActivityIntent(location),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getRefreshPendingIntent(Context context, int requestCode) {
        return PendingIntent.getService(
                context,
                requestCode,
                IntentHelper.getAwakeForegroundUpdateServiceIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getAlarmPendingIntent(Context context, int requestCode) {
        return PendingIntent.getActivity(
                context,
                requestCode,
                new Intent(AlarmClock.ACTION_SHOW_ALARMS),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getCalendarPendingIntent(Context context, int requestCode) {
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, System.currentTimeMillis());
        return PendingIntent.getActivity(
                context,
                requestCode,
                new Intent(Intent.ACTION_VIEW).setData(builder.build()),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @NonNull
    public static Bitmap drawableToBitmap(@NonNull Drawable drawable) {
        return DisplayUtils.drawableToBitmap(drawable);
    }

    public static String getCustomSubtitle(Context context, @Nullable String subtitle,
                                    @NonNull Location location, @NonNull Weather weather) {
        if (TextUtils.isEmpty(subtitle)) {
            return "";
        }
        boolean fahrenheit = SettingsOptionManager.getInstance(context).isFahrenheit();
        subtitle = subtitle.replace("$cw$", weather.realTime.weather)
                .replace("$ct$", ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit))
                .replace("$ctd$", ValueUtils.buildAbbreviatedCurrentTemp(weather.realTime.temp, fahrenheit))
                .replace("$at$", ValueUtils.buildCurrentTemp(weather.realTime.sensibleTemp, false, fahrenheit))
                .replace("$atd$", ValueUtils.buildAbbreviatedCurrentTemp(weather.realTime.sensibleTemp, fahrenheit))
                .replace("$cp$", weather.hourlyList.get(0).precipitation + "%")
                .replace(
                        "$cwd$",
                        weather.realTime.windLevel
                                + " (" + weather.realTime.windDir + weather.realTime.windSpeed + ")"
                ).replace("$l$", weather.base.city)
                .replace("$lat$", location.lat)
                .replace("$lon$", location.lon)
                .replace("$ut$", weather.base.time)
                .replace("$dd$", weather.index.simpleForecast)
                .replace("$hd$", weather.index.briefing)
                .replace("$h$", weather.index.humidity.replaceAll( "[^\\d]", "") + "%");
        subtitle = replaceDaytimeWeatherSubtitle(subtitle, weather);
        subtitle = replaceNighttimeWeatherSubtitle(subtitle, weather);
        subtitle = replaceDaytimeTemperatureSubtitle(subtitle, weather, fahrenheit);
        subtitle = replaceNighttimeTemperatureSubtitle(subtitle, weather, fahrenheit);
        subtitle = replaceDaytimeDegreeTemperatureSubtitle(subtitle, weather, fahrenheit);
        subtitle = replaceNighttimeDegreeTemperatureSubtitle(subtitle, weather, fahrenheit);
        subtitle = replaceDaytimePrecipitationSubtitle(subtitle, weather);
        subtitle = replaceNighttimePrecipitationSubtitle(subtitle, weather);
        subtitle = replaceDaytimeWindSubtitle(subtitle, weather);
        subtitle = replaceNighttimeWindSubtitle(subtitle, weather);
        subtitle = replaceSunriseSubtitle(subtitle, weather);
        subtitle = replaceSunsetSubtitle(subtitle, weather);
        subtitle = replaceMoonriseSubtitle(subtitle, weather);
        subtitle = replaceMoonsetSubtitle(subtitle, weather);
        subtitle = replaceMoonPhaseSubtitle(subtitle, weather);
        return subtitle;
    }

    private static String replaceDaytimeWeatherSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace("$" + i + "dw$", weather.dailyList.get(i).weathers[0]);
        }
        return subtitle;
    }

    private static String replaceNighttimeWeatherSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace("$" + i + "nw$", weather.dailyList.get(i).weathers[1]);
        }
        return subtitle;
    }

    private static String replaceDaytimeTemperatureSubtitle(@NonNull String subtitle, @NonNull Weather weather,
                                                     boolean fahrenheit) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "dt$",
                    ValueUtils.buildCurrentTemp(weather.dailyList.get(i).temps[0], false, fahrenheit)
            );
        }
        return subtitle;
    }

    private static String replaceNighttimeTemperatureSubtitle(@NonNull String subtitle, @NonNull Weather weather,
                                                       boolean fahrenheit) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "nt$",
                    ValueUtils.buildCurrentTemp(weather.dailyList.get(i).temps[1], false, fahrenheit)
            );
        }
        return subtitle;
    }

    private static String replaceDaytimeDegreeTemperatureSubtitle(@NonNull String subtitle, @NonNull Weather weather,
                                                           boolean fahrenheit) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "dtd$",
                    ValueUtils.buildAbbreviatedCurrentTemp(weather.dailyList.get(i).temps[0], fahrenheit)
            );
        }
        return subtitle;
    }

    private static String replaceNighttimeDegreeTemperatureSubtitle(@NonNull String subtitle, @NonNull Weather weather,
                                                             boolean fahrenheit) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "ntd$",
                    ValueUtils.buildAbbreviatedCurrentTemp(weather.dailyList.get(i).temps[1], fahrenheit)
            );
        }
        return subtitle;
    }

    private static String replaceDaytimePrecipitationSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "dp$",
                    weather.dailyList.get(i).precipitations[0] + "%"
            );
        }
        return subtitle;
    }

    private static String replaceNighttimePrecipitationSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "np$",
                    weather.dailyList.get(i).precipitations[1] + "%"
            );
        }
        return subtitle;
    }

    private static String replaceDaytimeWindSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "dwd$",
                    weather.dailyList.get(i).windLevels[0]
                            + " (" + weather.dailyList.get(i).windDirs[0] + weather.dailyList.get(i).windSpeeds[0] + ")"
            );
        }
        return subtitle;
    }

    private static String replaceNighttimeWindSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "nwd$",
                    weather.dailyList.get(i).windLevels[1]
                            + " (" + weather.dailyList.get(i).windDirs[1] + weather.dailyList.get(i).windSpeeds[1] + ")"
            );
        }
        return subtitle;
    }

    private static String replaceSunriseSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace("$" + i + "sr$", weather.dailyList.get(i).astros[0]);
        }
        return subtitle;
    }

    private static String replaceSunsetSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace("$" + i + "ss$", weather.dailyList.get(i).astros[1]);
        }
        return subtitle;
    }

    private static String replaceMoonriseSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace("$" + i + "mr$", weather.dailyList.get(i).astros[2]);
        }
        return subtitle;
    }

    private static String replaceMoonsetSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace("$" + i + "ms$", weather.dailyList.get(i).astros[3]);
        }
        return subtitle;
    }

    private static String replaceMoonPhaseSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace("$" + i + "mp$", weather.dailyList.get(i).moonPhase);
        }
        return subtitle;
    }

    @NonNull @Size(3) @ColorInt
    protected static int[] getWeatherColors(Context context, @NonNull Weather weather, boolean dayTime,
                                         ResourceProvider provider) {
        WeatherView weatherView;
        String uiStyle = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.key_ui_style), "material");
        switch (uiStyle) {
            case "material":
                weatherView = new MaterialWeatherView(context);
                break;

            default: // circular
                weatherView = new CircularSkyWeatherView(context);
                break;
        }
        WeatherViewController.setWeatherViewWeatherKind(weatherView, weather, dayTime, provider);
        return weatherView.getThemeColors(dayTime);
    }
}
