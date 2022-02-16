package wangdaye.com.geometricweather.common.utils.helpers;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.snackbar.Snackbar;
import wangdaye.com.geometricweather.common.snackbar.SnackbarContainer;

public class SnackbarHelper {

    public static void showSnackbar(String content) {
        showSnackbar(content, null, null);
    }

    public static void showSnackbar(GeoActivity activity, String content) {
        showSnackbar(activity, content, null, null);
    }

    public static void showSnackbar(String content,
                                    @Nullable String action, @Nullable View.OnClickListener l) {
        showSnackbar(content, action, l, null);
    }

    public static void showSnackbar(GeoActivity activity, String content,
                                    @Nullable String action, @Nullable View.OnClickListener l) {
        showSnackbar(activity, content, action, l, null);
    }

    public static void showSnackbar(String content,
                                    @Nullable String action, @Nullable View.OnClickListener l,
                                    @Nullable Snackbar.Callback callback) {

        GeoActivity activity = GeometricWeather.getInstance().getTopActivity();
        if (activity != null) {
            showSnackbar(activity, content, action, l, callback);
        }
    }

    public static void showSnackbar(GeoActivity activity, String content,
                                    @Nullable String action, @Nullable View.OnClickListener l,
                                    @Nullable Snackbar.Callback callback) {
        if (action != null && l == null) {
            throw new RuntimeException("Must send a non null listener as parameter.");
        }

        if (callback == null) {
            callback = new Snackbar.Callback();
        }

        SnackbarContainer container = activity.provideSnackbarContainer();

        Snackbar.make(container.container, content, Snackbar.LENGTH_LONG, container.cardStyle)
                .setAction(action, l)
                .setActionTextColor(ContextCompat.getColor(activity, R.color.colorTextAlert))
                .setCallback(callback)
                .show();
    }
}
