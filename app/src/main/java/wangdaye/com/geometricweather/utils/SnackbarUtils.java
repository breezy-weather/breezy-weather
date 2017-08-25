package wangdaye.com.geometricweather.utils;

import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.ui.widget.verticalScrollView.SwipeSwitchLayout;

/**
 * Notification utils.
 * */

public class SnackbarUtils {

    public static void showSnackbar(String txt) {
        GeoActivity activity = GeometricWeather.getInstance().getTopActivity();
        if (activity != null) {
            View view = activity.provideSnackbarContainer();
            if (view instanceof SwipeSwitchLayout) {
                SwipeSwitchLayout switchView = (SwipeSwitchLayout) view;
                Snackbar.make(
                        switchView,
                        txt,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(
                        view,
                        txt,
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    public static void showSnackbar(String txt, String action, View.OnClickListener l) {
        GeoActivity activity = GeometricWeather.getInstance().getTopActivity();
        if (activity != null) {
            View view = activity.provideSnackbarContainer();
            if (view instanceof SwipeSwitchLayout) {
                SwipeSwitchLayout switchView = (SwipeSwitchLayout) view;
                Snackbar.make(
                        switchView,
                        txt,
                        Snackbar.LENGTH_LONG)
                        .setAction(action, l)
                        .setActionTextColor(
                                ContextCompat.getColor(
                                        GeometricWeather.getInstance(),
                                        R.color.colorTextAlert))
                        .show();
            } else {
                Snackbar.make(
                        view,
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
    }
}
