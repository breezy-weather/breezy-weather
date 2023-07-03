package org.breezyweather.common.utils.helpers;

import android.view.View;

import androidx.annotation.Nullable;

import org.breezyweather.BreezyWeather;
import org.breezyweather.common.basic.GeoActivity;
import org.breezyweather.common.snackbar.Snackbar;
import org.breezyweather.common.snackbar.SnackbarContainer;

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

        GeoActivity activity = BreezyWeather.getInstance().getTopActivity();
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

        Snackbar.make(container.getContainer(), content, Snackbar.LENGTH_LONG, container.getCardStyle())
                .setAction(action, l)
                .setCallback(callback)
                .show();
    }
}
