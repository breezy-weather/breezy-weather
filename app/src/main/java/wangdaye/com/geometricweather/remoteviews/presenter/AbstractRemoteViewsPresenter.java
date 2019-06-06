package wangdaye.com.geometricweather.remoteviews.presenter;

import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.CalendarContract;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.ResourceUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.background.ServiceHelper;

public abstract class AbstractRemoteViewsPresenter {

    public static class WidgetConfig {
        public String viewStyle;
        public String cardStyle;
        public int cardAlpha;
        public String textColor;
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
                    ? !darkCard
                    : textColor.equals("dark") || (textColor.equals("auto") && !isLightWallpaper(context));
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
        Drawable drawable = WallpaperManager.getInstance(context).getDrawable();
        if (!(drawable instanceof BitmapDrawable)) {
            return false;
        }

        return DisplayUtils.isLightColor(
                context,
                DisplayUtils.bitmapToColorInt(((BitmapDrawable) drawable).getBitmap())
        );
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
                IntentHelper.buildMainActivityIntent(context, location),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getRefreshPendingIntent(Context context, int requestCode) {
        return PendingIntent.getService(
                context,
                requestCode,
                ServiceHelper.getAwakePollingUpdateServiceIntent(context),
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
}
