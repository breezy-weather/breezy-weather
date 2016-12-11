package wangdaye.com.geometricweather.utils;

import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;

/**
 * Notification utils.
 * */

public class SnackbarUtils {

    public static void showSnackbar(String txt) {
        Snackbar.make(
                GeometricWeather.getInstance().getTopActivity().provideSnackbarContainer(),
                txt,
                Snackbar.LENGTH_SHORT).show();
    }

    public static void showSnackbar(String txt, String action, View.OnClickListener l) {
        Snackbar.make(
                GeometricWeather.getInstance().getTopActivity().provideSnackbarContainer(),
                txt,
                Snackbar.LENGTH_LONG)
                .setAction(action, l)
                .setActionTextColor(
                        ContextCompat.getColor(
                                GeometricWeather.getInstance(),
                                R.color.colorTextAlert))
                .show();
    }
}
