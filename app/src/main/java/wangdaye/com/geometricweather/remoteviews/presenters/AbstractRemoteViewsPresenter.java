package wangdaye.com.geometricweather.remoteviews.presenters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.NotificationTextColor;
import wangdaye.com.geometricweather.common.basic.models.options.WidgetWeekIconMode;
import wangdaye.com.geometricweather.common.basic.models.options.unit.DistanceUnit;
import wangdaye.com.geometricweather.common.basic.models.options.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.common.basic.models.options.unit.PressureUnit;
import wangdaye.com.geometricweather.common.basic.models.options.unit.ProbabilityUnit;
import wangdaye.com.geometricweather.common.basic.models.options.unit.RelativeHumidityUnit;
import wangdaye.com.geometricweather.common.basic.models.options.unit.TemperatureUnit;
import wangdaye.com.geometricweather.common.basic.models.weather.Alert;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper;
import wangdaye.com.geometricweather.common.utils.helpers.LunarHelper;
import wangdaye.com.geometricweather.remoteviews.WidgetHelper;
import wangdaye.com.geometricweather.settings.ConfigStore;
import wangdaye.com.geometricweather.settings.SettingsManager;

// FIXME: all nullable values
public abstract class AbstractRemoteViewsPresenter {

    private static final int SUBTITLE_DAILY_ITEM_LENGTH = 5;

    public static WidgetConfig getWidgetConfig(Context context, String configStoreName) {
        WidgetConfig widgetConfig = new WidgetConfig();

        ConfigStore configStore = ConfigStore.getInstance(context, configStoreName);
        widgetConfig.viewStyle = configStore.getString(
                context.getString(R.string.key_view_type),
                "rectangle"
        );
        widgetConfig.cardStyle = configStore.getString(
                context.getString(R.string.key_card_style),
                "none"
        );
        widgetConfig.cardAlpha = configStore.getInt(
                context.getString(R.string.key_card_alpha),
                100
        );
        widgetConfig.textColor = configStore.getString(
                context.getString(R.string.key_text_color),
                "light"
        );
        widgetConfig.textSize = configStore.getInt(
                context.getString(R.string.key_text_size),
                100
        );
        widgetConfig.hideSubtitle = configStore.getBoolean(
                context.getString(R.string.key_hide_subtitle),
                false
        );
        widgetConfig.subtitleData = configStore.getString(
                context.getString(R.string.key_subtitle_data),
                "time"
        );
        widgetConfig.clockFont = configStore.getString(
                context.getString(R.string.key_clock_font),
                "light"
        );
        widgetConfig.hideLunar = configStore.getBoolean(
                context.getString(R.string.key_hide_lunar),
                false
        );
        widgetConfig.alignEnd = configStore.getBoolean(
                context.getString(R.string.key_align_end),
                false
        );

        return widgetConfig;
    }

    public static boolean isLightWallpaper(Context context) {
        try {
            WallpaperManager manager = WallpaperManager.getInstance(context);
            if (manager == null) {
                return false;
            }

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
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
    public static int getCardBackgroundId(WidgetColor.ColorType cardColor) {
        switch (cardColor) {
            case AUTO:
                return R.drawable.widget_card_follow_system;
            case LIGHT:
                return R.drawable.widget_card_light;
            default:
                return R.drawable.widget_card_dark;
        }
    }

    public static boolean isWeekIconDaytime(WidgetWeekIconMode mode, boolean daytime) {
        switch (mode) {
            case DAY:
                return true;

            case NIGHT:
                return false;

            default:
                return daytime;
        }
    }

    @SuppressLint("InlinedApi")
    public static PendingIntent getWeatherPendingIntent(Context context,
                                                        @Nullable Location location, int requestCode) {
        return PendingIntent.getActivity(
                context,
                requestCode,
                IntentHelper.buildMainActivityIntent(location),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @SuppressLint("InlinedApi")
    public static PendingIntent getDailyForecastPendingIntent(Context context,
                                                              @Nullable Location location, int index,
                                                              int requestCode) {
        return PendingIntent.getActivity(
                context,
                requestCode,
                IntentHelper.buildMainActivityShowDailyForecastIntent(location, index),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @SuppressLint("InlinedApi")
    public static PendingIntent getAlarmPendingIntent(Context context, int requestCode) {
        return PendingIntent.getActivity(
                context,
                requestCode,
                new Intent(AlarmClock.ACTION_SHOW_ALARMS),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @SuppressLint("InlinedApi")
    public static PendingIntent getCalendarPendingIntent(Context context, int requestCode) {
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, System.currentTimeMillis());
        return PendingIntent.getActivity(
                context,
                requestCode,
                new Intent(Intent.ACTION_VIEW).setData(builder.build()),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @NonNull
    public static Bitmap drawableToBitmap(@NonNull Drawable drawable) {
        return DisplayUtils.drawableToBitmap(drawable);
    }

    // FIXME: Null safety
    public static String getCustomSubtitle(Context context, @Nullable String subtitle,
                                           @NonNull Location location, @NonNull Weather weather) {
        if (TextUtils.isEmpty(subtitle)) {
            return "";
        }
        TemperatureUnit temperatureUnit = SettingsManager.getInstance(context).getTemperatureUnit();
        PrecipitationUnit precipitationUnit = SettingsManager.getInstance(context).getPrecipitationUnit();
        PressureUnit pressureUnit = SettingsManager.getInstance(context).getPressureUnit();
        DistanceUnit distanceUnit = SettingsManager.getInstance(context).getDistanceUnit();

        subtitle = subtitle
                .replace("$cw$", (weather.getCurrent() != null
                        && weather.getCurrent().getWeatherText() != null)
                        ? weather.getCurrent().getWeatherText()
                        : "")
                .replace(
                        "$ct$", (weather.getCurrent() != null
                                && weather.getCurrent().getTemperature() != null
                                && weather.getCurrent().getTemperature().getTemperature() != null)
                                ? weather.getCurrent()
                                .getTemperature()
                                .getTemperature(context, temperatureUnit) + ""
                                : ""
                ).replace(
                        "$ctd$", (weather.getCurrent() != null
                                && weather.getCurrent().getTemperature() != null
                                && weather.getCurrent().getTemperature().getTemperature() != null)
                                ? weather.getCurrent()
                                .getTemperature()
                                .getShortTemperature(context, temperatureUnit) + ""
                                : ""
                ).replace(
                        "$at$", (weather.getCurrent() != null
                                && weather.getCurrent().getTemperature() != null
                                && weather.getCurrent().getTemperature().getFeelsLikeTemperature() != null)
                                ? weather.getCurrent()
                                .getTemperature()
                                .getFeelsLikeTemperature(context, temperatureUnit) + ""
                                : ""
                ).replace(
                        "$atd$", (weather.getCurrent() != null
                                && weather.getCurrent().getTemperature() != null
                                && weather.getCurrent().getTemperature().getFeelsLikeTemperature() != null)
                                ? weather.getCurrent()
                                .getTemperature()
                                .getShortFeelsLikeTemperature(context, temperatureUnit) + ""
                                : ""
                ).replace(
                        "$cwd$", (weather.getCurrent() != null
                                && weather.getCurrent().getWind() != null
                                && !TextUtils.isEmpty(weather.getCurrent().getWind().getLevel())
                                && !TextUtils.isEmpty(weather.getCurrent().getWind().getDirection()))
                                ? weather.getCurrent().getWind().getLevel()
                                + " ("
                                + weather.getCurrent().getWind().getDirection()
                                + ")" : ""
                ).replace("$cuv$", (weather.getCurrent() != null
                                && weather.getCurrent().getUV() != null
                                && !TextUtils.isEmpty(weather.getCurrent().getUV().getShortUVDescription()))
                                ? weather.getCurrent().getUV().getShortUVDescription()
                                : ""
                ).replace(
                        "$ch$",
                        RelativeHumidityUnit.PERCENT.getValueText(
                                context,
                                (int) WidgetHelper.getNonNullValue(
                                        weather.getCurrent().getRelativeHumidity(),
                                        0
                                )
                        )
                ).replace(
                        "$cps$",
                        pressureUnit.getValueText(
                                context,
                                WidgetHelper.getNonNullValue(
                                        weather.getCurrent().getPressure(),
                                        0
                                )
                        )
                ).replace(
                        "$cv$",
                        distanceUnit.getValueText(
                                context,
                                WidgetHelper.getNonNullValue(
                                        weather.getCurrent().getVisibility(),
                                        0
                                )
                        )
                ).replace(
                        "$cdp$",
                        temperatureUnit.getValueText(
                                context,
                                WidgetHelper.getNonNullValue(
                                        weather.getCurrent().getDewPoint(),
                                        0
                                )
                        )
                ).replace("$l$", location.getCityName(context))
                .replace("$lat$", String.valueOf(location.getLatitude()))
                .replace("$lon$", String.valueOf(location.getLongitude()))
                .replace("$ut$", DisplayUtils.getTime(context, weather.getBase().getUpdateDate(), location.getTimeZone()))
                .replace(
                        "$d$",
                        DisplayUtils.getFormattedDate(new Date(), location.getTimeZone(), context.getString(R.string.date_format_long))
                ).replace(
                        "$lc$",
                        LunarHelper.getLunarDate(new Date())
                ).replace(
                        "$w$",
                        DisplayUtils.getFormattedDate(new Date(), location.getTimeZone(), "EEEE")
                ).replace(
                        "$ws$",
                        DisplayUtils.getFormattedDate(new Date(), location.getTimeZone(), "EEE")
                ).replace("$dd$", weather.getCurrent().getDailyForecast() + "")
                .replace("$hd$", weather.getCurrent().getHourlyForecast() + "")
                .replace("$enter$", "\n");
        subtitle = replaceAlerts(context, subtitle, weather, location.getTimeZone());
        subtitle = replaceDaytimeWeatherSubtitle(subtitle, weather);
        subtitle = replaceNighttimeWeatherSubtitle(subtitle, weather);
        subtitle = replaceDaytimeTemperatureSubtitle(context, subtitle, weather, temperatureUnit);
        subtitle = replaceNighttimeTemperatureSubtitle(context, subtitle, weather, temperatureUnit);
        subtitle = replaceDaytimeDegreeTemperatureSubtitle(context, subtitle, weather, temperatureUnit);
        subtitle = replaceNighttimeDegreeTemperatureSubtitle(context, subtitle, weather, temperatureUnit);
        subtitle = replaceDaytimePrecipitationSubtitle(context, subtitle, weather);
        subtitle = replaceNighttimePrecipitationSubtitle(context, subtitle, weather);
        subtitle = replaceDaytimeWindSubtitle(subtitle, weather);
        subtitle = replaceNighttimeWindSubtitle(subtitle, weather);
        subtitle = replaceSunriseSubtitle(context, subtitle, weather, location.getTimeZone());
        subtitle = replaceSunsetSubtitle(context, subtitle, weather, location.getTimeZone());
        subtitle = replaceMoonriseSubtitle(context, subtitle, weather, location.getTimeZone());
        subtitle = replaceMoonsetSubtitle(context, subtitle, weather, location.getTimeZone());
        subtitle = replaceMoonPhaseSubtitle(context, subtitle, weather);
        return subtitle;
    }

    private static String replaceAlerts(Context context, @NonNull String subtitle, @NonNull Weather weather, TimeZone timeZone) {
        StringBuilder defaultBuilder = new StringBuilder();
        StringBuilder shortBuilder = new StringBuilder();
        List<Alert> currentAlertList = weather.getCurrentAlertList();
        for (int i = 0; i < currentAlertList.size(); i++) {
            defaultBuilder.append(currentAlertList.get(i).getDescription());
            if (currentAlertList.get(i).getStartDate() != null) {
                String startDateDay = DisplayUtils.getFormattedDate(currentAlertList.get(i).getStartDate(),
                        timeZone,
                        context.getString(R.string.date_format_long));
                defaultBuilder.append(", ")
                        .append(startDateDay)
                        .append(", ")
                        .append(DisplayUtils.getFormattedDate(currentAlertList.get(i).getStartDate(),
                                timeZone,
                                (DisplayUtils.is12Hour(context)) ? "h:mm aa" : "HH:mm"));
                if (currentAlertList.get(i).getEndDate() != null) {
                    defaultBuilder.append("-");
                    String endDateDay = DisplayUtils.getFormattedDate(currentAlertList.get(i).getEndDate(),
                            timeZone,
                            context.getString(R.string.date_format_long));
                    if (!startDateDay.equals(endDateDay)) {
                        defaultBuilder.append(endDateDay)
                                .append(", ");
                    }
                    defaultBuilder.append(DisplayUtils.getFormattedDate(currentAlertList.get(i).getEndDate(),
                            timeZone,
                            (DisplayUtils.is12Hour(context)) ? "h:mm aa" : "HH:mm"));
                }
            }
            shortBuilder.append(currentAlertList.get(i).getDescription());

            if (i != currentAlertList.size() - 1) {
                defaultBuilder.append("\n");
                shortBuilder.append("\n");
            }
        }
        return subtitle.replace("$al$", defaultBuilder.toString())
                .replace("$als$", shortBuilder.toString());
    }

    private static String replaceDaytimeWeatherSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i++) {
            subtitle = subtitle.replace(
                    "$" + i + "dw$",
                    weather.getDailyForecast().get(i).getDay().getWeatherText()
            );
        }
        return subtitle;
    }

    private static String replaceNighttimeWeatherSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i++) {
            subtitle = subtitle.replace(
                    "$" + i + "nw$",
                    weather.getDailyForecast().get(i).getNight().getWeatherText()
            );
        }
        return subtitle;
    }

    private static String replaceDaytimeTemperatureSubtitle(Context context, @NonNull String subtitle,
                                                            @NonNull Weather weather, TemperatureUnit unit) {

        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i++) {
            subtitle = subtitle.replace(
                    "$" + i + "dt$",
                    weather.getDailyForecast()
                            .get(i)
                            .getDay()
                            .getTemperature()
                            .getTemperature(context, unit) + ""
            );
        }
        return subtitle;
    }

    private static String replaceNighttimeTemperatureSubtitle(Context context, @NonNull String subtitle,
                                                              @NonNull Weather weather, TemperatureUnit unit) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i++) {
            subtitle = subtitle.replace(
                    "$" + i + "nt$",
                    weather.getDailyForecast()
                            .get(i)
                            .getNight()
                            .getTemperature()
                            .getTemperature(context, unit) + ""
            );
        }
        return subtitle;
    }

    private static String replaceDaytimeDegreeTemperatureSubtitle(Context context,
                                                                  @NonNull String subtitle,
                                                                  @NonNull Weather weather,
                                                                  TemperatureUnit unit) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i++) {
            subtitle = subtitle.replace(
                    "$" + i + "dtd$",
                    weather.getDailyForecast()
                            .get(i)
                            .getDay()
                            .getTemperature()
                            .getShortTemperature(context, unit) + ""
            );
        }
        return subtitle;
    }

    private static String replaceNighttimeDegreeTemperatureSubtitle(Context context,
                                                                    @NonNull String subtitle,
                                                                    @NonNull Weather weather,
                                                                    TemperatureUnit unit) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i++) {
            subtitle = subtitle.replace(
                    "$" + i + "ntd$",
                    weather.getDailyForecast()
                            .get(i)
                            .getNight()
                            .getTemperature()
                            .getShortTemperature(context, unit) + ""
            );
        }
        return subtitle;
    }

    private static String replaceDaytimePrecipitationSubtitle(Context context,
                                                              @NonNull String subtitle,
                                                              @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i++) {
            subtitle = subtitle.replace(
                    "$" + i + "dp$",
                    ProbabilityUnit.PERCENT.getValueText(
                            context,
                            (int) WidgetHelper.getNonNullValue(
                                    weather.getDailyForecast()
                                            .get(i)
                                            .getDay()
                                            .getPrecipitationProbability()
                                            .getTotal(),
                                    0
                            )
                    )
            );
        }
        return subtitle;
    }

    private static String replaceNighttimePrecipitationSubtitle(Context context,
                                                                @NonNull String subtitle,
                                                                @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i++) {
            subtitle = subtitle.replace(
                    "$" + i + "np$",
                    ProbabilityUnit.PERCENT.getValueText(
                            context,
                            (int) WidgetHelper.getNonNullValue(
                                    weather.getDailyForecast()
                                            .get(i)
                                            .getNight()
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
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i++) {
            subtitle = subtitle.replace(
                    "$" + i + "dwd$",
                    weather.getDailyForecast().get(i).getDay().getWind().getLevel()
                            + " ("
                            + weather.getDailyForecast().get(i).getDay().getWind().getDirection()
                            + ")"
            );
        }
        return subtitle;
    }

    private static String replaceNighttimeWindSubtitle(@NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i++) {
            subtitle = subtitle.replace(
                    "$" + i + "nwd$",
                    weather.getDailyForecast().get(i).getNight().getWind().getLevel()
                            + " ("
                            + weather.getDailyForecast().get(i).getNight().getWind().getDirection()
                            + ")"
            );
        }
        return subtitle;
    }

    private static String replaceSunriseSubtitle(
            Context context,
            @NonNull String subtitle,
            @NonNull Weather weather,
            @NonNull TimeZone timeZone
    ) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i++) {
            subtitle = subtitle.replace(
                    "$" + i + "sr$",
                    weather.getDailyForecast().get(i).getSun().getRiseTime(context, timeZone) + ""
            );
        }
        return subtitle;
    }

    private static String replaceSunsetSubtitle(
            Context context,
            @NonNull String subtitle,
            @NonNull Weather weather,
            @NonNull TimeZone timeZone
    ) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i++) {
            subtitle = subtitle.replace(
                    "$" + i + "ss$",
                    weather.getDailyForecast().get(i).getSun().getSetTime(context, timeZone) + ""
            );
        }
        return subtitle;
    }

    private static String replaceMoonriseSubtitle(
            Context context,
            @NonNull String subtitle,
            @NonNull Weather weather,
            @NonNull TimeZone timeZone
    ) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i++) {
            subtitle = subtitle.replace(
                    "$" + i + "mr$",
                    weather.getDailyForecast().get(i).getMoon().getRiseTime(context, timeZone) + ""
            );
        }
        return subtitle;
    }

    private static String replaceMoonsetSubtitle(
            Context context,
            @NonNull String subtitle,
            @NonNull Weather weather,
            @NonNull TimeZone timeZone
    ) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i++) {
            subtitle = subtitle.replace(
                    "$" + i + "ms$",
                    weather.getDailyForecast().get(i).getMoon().getSetTime(context, timeZone) + ""
            );
        }
        return subtitle;
    }

    private static String replaceMoonPhaseSubtitle(Context context, @NonNull String subtitle, @NonNull Weather weather) {
        for (int i = 0; i < SUBTITLE_DAILY_ITEM_LENGTH; i++) {
            subtitle = subtitle.replace(
                    "$" + i + "mp$",
                    weather.getDailyForecast().get(i).getMoonPhase().getMoonPhase(context) + ""
            );
        }
        return subtitle;
    }

    public static class WidgetConfig {
        public String viewStyle;
        public String cardStyle;
        public int cardAlpha;
        public String textColor;
        public int textSize;
        public boolean hideSubtitle;
        public String subtitleData;
        public String clockFont;
        public boolean hideLunar;
        public boolean alignEnd;
    }

    public static class WidgetColor {

        public final boolean showCard;
        public final ColorType cardColor;
        @ColorInt
        public final int textColor;
        public final boolean darkText;

        public WidgetColor(Context context, String cardStyle, String textColor) {
            this.showCard = !cardStyle.equals("none");
            this.cardColor = cardStyle.equals("auto")
                    ? ColorType.AUTO
                    : (cardStyle.equals("light") ? ColorType.LIGHT : ColorType.DARK);

            if (showCard) {
                if (cardColor == ColorType.AUTO) {
                    this.textColor = Color.TRANSPARENT;
                    this.darkText = false;
                } else if (cardColor == ColorType.LIGHT) {
                    this.textColor = ContextCompat.getColor(context, R.color.colorTextDark);
                    this.darkText = true;
                } else {
                    this.textColor = ContextCompat.getColor(context, R.color.colorTextLight);
                    this.darkText = false;
                }
            } else if (textColor.equals("dark")
                    || (textColor.equals("auto") && isLightWallpaper(context))) {
                this.textColor = ContextCompat.getColor(context, R.color.colorTextDark);
                this.darkText = true;
            } else {
                this.textColor = ContextCompat.getColor(context, R.color.colorTextLight);
                this.darkText = false;
            }
        }

        public NotificationTextColor getMinimalIconColor() {
            if (showCard) {
                if (cardColor == ColorType.AUTO) {
                    return NotificationTextColor.GREY;
                } else if (cardColor == ColorType.LIGHT) {
                    return NotificationTextColor.DARK;
                } else {
                    return NotificationTextColor.LIGHT;
                }
            } else if (darkText) {
                return NotificationTextColor.DARK;
            } else {
                return NotificationTextColor.LIGHT;
            }
        }

        enum ColorType {
            LIGHT, DARK, AUTO
        }
    }
}
