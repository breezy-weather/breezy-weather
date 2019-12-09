package wangdaye.com.geometricweather.remoteviews.presenter;

import android.Manifest;
import android.annotation.SuppressLint;
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

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.option.unit.DistanceUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.PressureUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.ProbabilityUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.RelativeHumidityUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Base;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.remoteviews.WidgetUtils;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.resource.ResourceUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.helpter.LunarHelper;

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

    @SuppressLint("SimpleDateFormat")
    public static String getCustomSubtitle(Context context, @Nullable String subtitle,
                                           @NonNull Location location, @NonNull Weather weather) {
        if (TextUtils.isEmpty(subtitle)) {
            return "";
        }
        TemperatureUnit temperatureUnit = SettingsOptionManager.getInstance(context).getTemperatureUnit();
        PrecipitationUnit precipitationUnit = SettingsOptionManager.getInstance(context).getPrecipitationUnit();
        PressureUnit pressureUnit = SettingsOptionManager.getInstance(context).getPressureUnit();
        DistanceUnit distanceUnit = SettingsOptionManager.getInstance(context).getDistanceUnit();

        subtitle = subtitle
                .replace("$cw$", weather.getCurrent().getWeatherText())
                .replace(
                        "$ct$",
                        weather.getCurrent()
                                .getTemperature()
                                .getTemperature(temperatureUnit) + ""
                ).replace(
                        "$ctd$",
                        weather.getCurrent()
                                .getTemperature()
                                .getShortTemperature(temperatureUnit) + ""
                ).replace(
                        "$at$",
                        weather.getCurrent()
                                .getTemperature()
                                .getRealFeelTemperature(temperatureUnit) + ""
                ).replace(
                        "$atd$",
                        weather.getCurrent()
                                .getTemperature()
                                .getShortRealFeeTemperature(temperatureUnit) + ""
                ).replace(
                        "$cpb$",
                        ProbabilityUnit.PERCENT.getProbabilityText(
                                WidgetUtils.getNonNullValue(
                                        weather.getCurrent()
                                                .getPrecipitationProbability()
                                                .getTotal(),
                                        0
                                )
                        )
                ).replace(
                        "$cp$",
                        precipitationUnit.getPrecipitationText(
                                WidgetUtils.getNonNullValue(
                                        weather.getCurrent()
                                                .getPrecipitation()
                                                .getTotal(),
                                        0
                                )
                        )
                ).replace(
                        "$cwd$",
                        weather.getCurrent().getWind().getLevel()
                                + " ("
                                + weather.getCurrent().getWind().getDirection()
                                + ")"
                ).replace("$cuv$", weather.getCurrent().getUV().getShortUVDescription())
                .replace(
                        "$ch$",
                        RelativeHumidityUnit.PERCENT.getRelativeHumidityText(
                                WidgetUtils.getNonNullValue(
                                        weather.getCurrent().getRelativeHumidity(),
                                        0
                                )
                        )
                ).replace("$cps$", pressureUnit.getPressureText(
                        WidgetUtils.getNonNullValue(weather.getCurrent().getPressure(), 0))
                ).replace("$cv$", distanceUnit.getDistanceText(
                        WidgetUtils.getNonNullValue(weather.getCurrent().getVisibility(), 0))
                ).replace("$cdp$", temperatureUnit.getTemperatureText(
                        WidgetUtils.getNonNullValue(weather.getCurrent().getDewPoint(), 0))
                ).replace("$l$", location.getCityName(context))
                .replace("$lat$", String.valueOf(location.getLatitude()))
                .replace("$lon$", String.valueOf(location.getLongitude()))
                .replace("$ut$", Base.getTime(context, weather.getBase().getUpdateDate()))
                .replace(
                        "$d$",
                        new SimpleDateFormat(context.getString(R.string.date_format_long)).format(new Date())
                ).replace(
                        "$lc$",
                        LunarHelper.getLunarDate(new Date())
                ).replace(
                        "$w$",
                        new SimpleDateFormat("EEEE").format(new Date())
                ).replace(
                        "$ws$",
                        new SimpleDateFormat("EEE").format(new Date())
                ).replace("$dd$", weather.getCurrent().getDailyForecast() + "")
                .replace("$hd$", weather.getCurrent().getHourlyForecast() + "");
        subtitle = replaceDaytimeWeatherSubtitle(subtitle, weather);
        subtitle = replaceNighttimeWeatherSubtitle(subtitle, weather);
        subtitle = replaceDaytimeTemperatureSubtitle(subtitle, weather, temperatureUnit);
        subtitle = replaceNighttimeTemperatureSubtitle(subtitle, weather, temperatureUnit);
        subtitle = replaceDaytimeDegreeTemperatureSubtitle(subtitle, weather, temperatureUnit);
        subtitle = replaceNighttimeDegreeTemperatureSubtitle(subtitle, weather, temperatureUnit);
        subtitle = replaceDaytimePrecipitationSubtitle(subtitle, weather);
        subtitle = replaceNighttimePrecipitationSubtitle(subtitle, weather);
        subtitle = replaceDaytimeWindSubtitle(subtitle, weather);
        subtitle = replaceNighttimeWindSubtitle(subtitle, weather);
        subtitle = replaceSunriseSubtitle(context, subtitle, weather);
        subtitle = replaceSunsetSubtitle(context, subtitle, weather);
        subtitle = replaceMoonriseSubtitle(context, subtitle, weather);
        subtitle = replaceMoonsetSubtitle(context, subtitle, weather);
        subtitle = replaceMoonPhaseSubtitle(context, subtitle, weather);
        return subtitle;
    }

    private static String replaceDaytimeWeatherSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "dw$",
                    weather.getDailyForecast().get(i).day().getWeatherText()
            );
        }
        return subtitle;
    }

    private static String replaceNighttimeWeatherSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "nw$",
                    weather.getDailyForecast().get(i).night().getWeatherText()
            );
        }
        return subtitle;
    }

    private static String replaceDaytimeTemperatureSubtitle(@NonNull String subtitle, @NonNull Weather weather,
                                                            TemperatureUnit unit) {

        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "dt$",
                    weather.getDailyForecast()
                            .get(i)
                            .day()
                            .getTemperature()
                            .getTemperature(unit) + ""
            );
        }
        return subtitle;
    }

    private static String replaceNighttimeTemperatureSubtitle(@NonNull String subtitle, @NonNull Weather weather,
                                                              TemperatureUnit unit) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "nt$",
                    weather.getDailyForecast()
                            .get(i)
                            .night()
                            .getTemperature()
                            .getTemperature(unit) + ""
            );
        }
        return subtitle;
    }

    private static String replaceDaytimeDegreeTemperatureSubtitle(@NonNull String subtitle, @NonNull Weather weather,
                                                                  TemperatureUnit unit) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "dtd$",
                    weather.getDailyForecast()
                            .get(i)
                            .day()
                            .getTemperature()
                            .getShortTemperature(unit) + ""
            );
        }
        return subtitle;
    }

    private static String replaceNighttimeDegreeTemperatureSubtitle(@NonNull String subtitle, @NonNull Weather weather,
                                                                    TemperatureUnit unit) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "ntd$",
                    weather.getDailyForecast()
                            .get(i)
                            .night()
                            .getTemperature()
                            .getShortTemperature(unit) + ""
            );
        }
        return subtitle;
    }

    private static String replaceDaytimePrecipitationSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "dp$",
                    ProbabilityUnit.PERCENT.getProbabilityText(
                            WidgetUtils.getNonNullValue(
                                    weather.getDailyForecast()
                                            .get(i)
                                            .day()
                                            .getPrecipitationProbability()
                                            .getTotal(),
                                    0
                            )
                    )
            );
        }
        return subtitle;
    }

    private static String replaceNighttimePrecipitationSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "np$",
                    ProbabilityUnit.PERCENT.getProbabilityText(
                            WidgetUtils.getNonNullValue(
                                    weather.getDailyForecast()
                                            .get(i)
                                            .night()
                                            .getPrecipitationProbability()
                                            .getTotal(),
                                    0
                            )
                    )
            );
        }
        return subtitle;
    }

    private static String replaceDaytimeWindSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "dwd$",
                    weather.getDailyForecast().get(i).day().getWind().getLevel()
                            + " ("
                            + weather.getDailyForecast().get(i).day().getWind().getDirection()
                            + ")"
            );
        }
        return subtitle;
    }

    private static String replaceNighttimeWindSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "nwd$",
                    weather.getDailyForecast().get(i).night().getWind().getLevel()
                            + " ("
                            + weather.getDailyForecast().get(i).night().getWind().getDirection()
                            + ")"
            );
        }
        return subtitle;
    }

    private static String replaceSunriseSubtitle(Context context, @NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "sr$",
                    weather.getDailyForecast().get(i).sun().getRiseTime(context) + ""
            );
        }
        return subtitle;
    }

    private static String replaceSunsetSubtitle(Context context, @NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "ss$",
                    weather.getDailyForecast().get(i).sun().getSetTime(context) + ""
            );
        }
        return subtitle;
    }

    private static String replaceMoonriseSubtitle(Context context, @NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "mr$",
                    weather.getDailyForecast().get(i).moon().getRiseTime(context) + ""
            );
        }
        return subtitle;
    }

    private static String replaceMoonsetSubtitle(Context context, @NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "ms$",
                    weather.getDailyForecast().get(i).moon().getSetTime(context) + ""
            );
        }
        return subtitle;
    }

    private static String replaceMoonPhaseSubtitle(Context context, @NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i ++) {
            subtitle = subtitle.replace(
                    "$" + i + "mp$",
                    weather.getDailyForecast().get(i).getMoonPhase().getMoonPhase(context) + ""
            );
        }
        return subtitle;
    }
}
