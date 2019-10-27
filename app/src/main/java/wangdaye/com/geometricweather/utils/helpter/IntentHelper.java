package wangdaye.com.geometricweather.utils.helpter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import android.view.View;

import java.util.ArrayList;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.polling.basic.AwakeForegroundUpdateService;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.wallpaper.material.MaterialLiveWallpaperService;
import wangdaye.com.geometricweather.settings.activity.AboutActivity;
import wangdaye.com.geometricweather.ui.activity.AlertActivity;
import wangdaye.com.geometricweather.settings.activity.SelectProviderActivity;
import wangdaye.com.geometricweather.main.MainActivity;
import wangdaye.com.geometricweather.ui.activity.ManageActivity;
import wangdaye.com.geometricweather.settings.activity.PreviewIconActivity;
import wangdaye.com.geometricweather.ui.activity.SearcActivity;
import wangdaye.com.geometricweather.settings.activity.SettingsActivity;
import wangdaye.com.geometricweather.utils.SnackbarUtils;

/**
 * Intent helper.
 * */

public class IntentHelper {

    public static void startMainActivity(Context context) {
        context.startActivity(
                new Intent("com.wangdaye.geometricweather.Main")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        );
    }

    public static Intent buildMainActivityIntent(@Nullable Location location) {
        String formattedId = "";
        if (location != null) {
            formattedId = location.getFormattedId();
        }

        return new Intent("com.wangdaye.geometricweather.Main")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(MainActivity.KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID, formattedId);
    }

    public static Intent buildAwakeUpdateActivityIntent() {
        return new Intent("com.wangdaye.geometricweather.UPDATE")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public static void startAlertActivity(GeoActivity activity, Weather weather) {
        Intent intent = new Intent(activity, AlertActivity.class);
        intent.putParcelableArrayListExtra(
                AlertActivity.KEY_ALERT_ACTIVITY_ALERT_LIST,
                (ArrayList<? extends Parcelable>) weather.getAlertList()
        );
        activity.startActivity(intent);
    }

    public static void startManageActivityForResult(GeoActivity activity) {
        activity.startActivityForResult(
                new Intent(activity, ManageActivity.class),
                MainActivity.MANAGE_ACTIVITY
        );
    }

    public static void startSearchActivityForResult(GeoActivity activity, View bar) {
        Intent intent = new Intent(activity, SearcActivity.class);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            activity.startActivityForResult(intent, ManageActivity.SEARCH_ACTIVITY);
            activity.overridePendingTransition(R.anim.activity_search_in, 0);
        } else {
            ActivityCompat.startActivityForResult(
                    activity,
                    intent,
                    ManageActivity.SEARCH_ACTIVITY,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            activity,
                            Pair.create(bar, activity.getString(R.string.transition_activity_search_bar))
                    ).toBundle()
            );
        }
    }

    public static void startSettingsActivityForResult(GeoActivity activity) {
        activity.startActivityForResult(
                new Intent(activity, SettingsActivity.class),
                MainActivity.SETTINGS_ACTIVITY
        );
    }

    public static void startSelectProviderActivity(Activity activity) {
        activity.startActivity(new Intent(activity, SelectProviderActivity.class));
    }

    public static void startSelectProviderActivityForResult(Activity activity, int requestCode) {
        activity.startActivityForResult(new Intent(activity, SelectProviderActivity.class), requestCode);
    }

    public static void startPreviewIconActivity(Activity activity, String packageName) {
        activity.startActivity(
                new Intent(activity, PreviewIconActivity.class).putExtra(
                        PreviewIconActivity.KEY_ICON_PREVIEW_ACTIVITY_PACKAGE_NAME,
                        packageName
                )
        );
    }

    public static void startAboutActivity(GeoActivity activity) {
        activity.startActivity(new Intent(activity, AboutActivity.class));
    }

    public static void startApplicationDetailsActivity(Context context) {
        startApplicationDetailsActivity(context, context.getPackageName());
    }

    public static void startApplicationDetailsActivity(Context context, String pkgName) {
        context.startActivity(
                new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.fromParts("package", pkgName, null))
        );
    }

    public static void startLocationSettingsActivity(Context context) {
        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    public static void startLiveWallpaperActivity(Context context) {
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(context, MaterialLiveWallpaperService.class)
        );
        if (isIntentAvailable(context, intent)) {
            context.startActivity(intent);
        } else {
            SnackbarUtils.showSnackbar(
                    context.getString(R.string.feedback_cannot_start_live_wallpaper_activity));
        }
    }

    public static void startAppStoreDetailsActivity(Context context) {
        startAppStoreDetailsActivity(context, context.getPackageName());
    }

    public static void startAppStoreDetailsActivity(Context context, String packageName) {
        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + packageName)
        );
        if (isIntentAvailable(context, intent)) {
            context.startActivity(intent);
        } else {
            SnackbarUtils.showSnackbar("Unavailable AppStore.");
        }
    }

    public static void startAppStoreSearchActivity(Context context, String query) {
        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://search?q=" + query)
        );
        if (isIntentAvailable(context, intent)) {
            context.startActivity(intent);
        } else {
            SnackbarUtils.showSnackbar("Unavailable AppStore.");
        }
    }

    public static void startWebViewActivity(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (isIntentAvailable(context, intent)) {
            context.startActivity(intent);
        } else {
            SnackbarUtils.showSnackbar("Unavailable internet browser.");
        }
    }

    public static void startEmailActivity(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
        if (isIntentAvailable(context, intent)) {
            context.startActivity(intent);
        } else {
            SnackbarUtils.showSnackbar("Unavailable e-mail.");
        }
    }

    public static void sendBackgroundUpdateBroadcast(Context context, Location location) {
        context.sendBroadcast(
                new Intent(MainActivity.ACTION_UPDATE_WEATHER_IN_BACKGROUND)
                        .putExtra(MainActivity.KEY_LOCATION_FORMATTED_ID, location.getFormattedId())
        );
    }

    public static void startAwakeForegroundUpdateService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(getAwakeForegroundUpdateServiceIntent(context));
        } else {
            context.startService(getAwakeForegroundUpdateServiceIntent(context));
        }
    }

    public static Intent getAwakeForegroundUpdateServiceIntent(Context context) {
        return new Intent(context, AwakeForegroundUpdateService.class);
    }

    @SuppressLint("WrongConstant")
    private static boolean isIntentAvailable(Context context, Intent intent) {
        return context.getPackageManager()
                .queryIntentActivities(intent, PackageManager.GET_ACTIVITIES)
                .size() > 0;
    }
}
