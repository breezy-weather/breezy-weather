package wangdaye.com.geometricweather.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import android.view.View;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;

/**
 * Notification utils.
 * */

public class SnackbarUtils {

    public static void showSnackbar(@NonNull GeoActivity activity, String txt) {
        Snackbar.make(activity.getSnackbarContainer(), txt, Snackbar.LENGTH_SHORT).show();
    }

    public static void showSnackbar(@NonNull GeoActivity activity, String txt, String action,
                                    @NonNull View.OnClickListener l) {
        showSnackbar(activity, txt, action, l, null);
    }

    public static void showSnackbar(@NonNull GeoActivity activity, String txt, String action,
                                    @NonNull View.OnClickListener l,
                                    @Nullable Snackbar.Callback callback) {
        if (callback == null) {
            callback = new Snackbar.Callback();
        }

        Snackbar.make(
                activity.getSnackbarContainer(),
                txt,
                Snackbar.LENGTH_LONG
        ).setAction(action, l)
                .setActionTextColor(ContextCompat.getColor(activity, R.color.colorTextAlert))
                .addCallback(callback)
                .show();
    }
}
