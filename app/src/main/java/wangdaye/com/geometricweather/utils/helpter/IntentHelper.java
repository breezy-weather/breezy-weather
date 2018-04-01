package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.View;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.ArrayList;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.ui.activity.AboutActivity;
import wangdaye.com.geometricweather.ui.activity.AlertActivity;
import wangdaye.com.geometricweather.ui.activity.MainActivity;
import wangdaye.com.geometricweather.ui.activity.ManageActivity;
import wangdaye.com.geometricweather.ui.activity.SearcActivity;
import wangdaye.com.geometricweather.ui.activity.SettingsActivity;

/**
 * Intent helper.
 * */

public class IntentHelper {

    public static void startMainActivity(Context context) {
        Intent intent = new Intent("com.wangdaye.geometricweather.Main")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static Intent buildMainActivityIntent(Context context, @Nullable Location location) {
        String locationName;
        if (location == null) {
            locationName = "";
        } else {
            locationName= location.isLocal() ? context.getString(R.string.local) : location.city;
        }
        return new Intent("com.wangdaye.geometricweather.Main")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(MainActivity.KEY_MAIN_ACTIVITY_LOCATION, locationName);
    }

    public static Intent buildMainActivityIntent(@NotNull String cityName) {
        return new Intent("com.wangdaye.geometricweather.Main")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(MainActivity.KEY_MAIN_ACTIVITY_LOCATION, cityName);
    }

    public static void startAlertActivity(GeoActivity activity, Weather weather) {
        Intent intent = new Intent(activity, AlertActivity.class);
        intent.putParcelableArrayListExtra(
                AlertActivity.KEY_ALERT_ACTIVITY_ALERT_LIST,
                (ArrayList<? extends Parcelable>) weather.alertList);
        activity.startActivity(intent);
    }

    public static void startManageActivityForResult(GeoActivity activity) {
        activity.startActivityForResult(
                new Intent(activity, ManageActivity.class),
                MainActivity.MANAGE_ACTIVITY);
    }

    public static void startSearchActivityForResult(GeoActivity geoActivity, View bar) {
        Intent intent = new Intent(geoActivity, SearcActivity.class);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            geoActivity.startActivityForResult(intent, ManageActivity.SEARCH_ACTIVITY);
            geoActivity.overridePendingTransition(R.anim.activity_search_in, 0);
        } else {
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(
                            geoActivity,
                            Pair.create(bar, geoActivity.getString(R.string.transition_activity_search_bar)));
            ActivityCompat.startActivityForResult(
                    geoActivity,
                    intent,
                    ManageActivity.SEARCH_ACTIVITY,
                    options.toBundle());
        }
    }

    public static void startSettingsActivityForResult(GeoActivity activity) {
        activity.startActivityForResult(
                new Intent(activity, SettingsActivity.class),
                MainActivity.SETTINGS_ACTIVITY);
    }

    public static void startAboutActivity(GeoActivity activity) {
        activity.startActivity(new Intent(activity, AboutActivity.class));
    }

    public static void startApplicationDetailsActivity(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        context.startActivity(intent);
    }
}
