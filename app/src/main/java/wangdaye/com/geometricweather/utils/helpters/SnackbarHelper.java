package wangdaye.com.geometricweather.utils.helpters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;

/**
 * Snackbar helper.
 * */

public class SnackbarHelper {

    public static void showSnackbar(String content) {
        showSnackbar(content, null, null);
    }

    public static void showSnackbar(String content,
                                    @Nullable String action, @Nullable View.OnClickListener l) {
        showSnackbar(content, action, l, null);
    }

    public static void showSnackbar(String content,
                                    @Nullable String action, @Nullable View.OnClickListener l,
                                    @Nullable Snackbar.Callback callback) {
        if (action != null && l == null) {
            throw new RuntimeException("Must send a non null listener as parameter.");
        }

        GeoActivity activity = GeometricWeather.getInstance().getTopActivity();
        if (activity == null) {
            return;
        }

        if (callback == null) {
            callback = new Snackbar.Callback();
        }

        boolean[] fromActivity = new boolean[1];
        View container = activity.provideSnackbarContainer(fromActivity);

        setAppearanceAndShow(
                activity,
                Snackbar.make(container, content, Snackbar.LENGTH_LONG)
                        .setAction(action, l)
                        .setActionTextColor(ContextCompat.getColor(activity, R.color.colorTextAlert))
                        .addCallback(callback),
                fromActivity[0],
                content,
                action,
                l
        );
    }

    private static void setAppearanceAndShow(Context context, Snackbar snackbar, boolean inActivity,
                                             String content,
                                             @Nullable String action, @Nullable View.OnClickListener l) {
        Snackbar.SnackbarLayout view = (Snackbar.SnackbarLayout) snackbar.getView();
        view.removeAllViews();
        view.setPadding(0, 0, 0, 0);
        view.setBackgroundColor(Color.TRANSPARENT);

        View snackbarCard = LayoutInflater.from(context).inflate(
                inActivity ? R.layout.container_snackbar_card : R.layout.container_snackbar,
                view,
                true
        );

        TextView contentText = snackbarCard.findViewById(R.id.snackbar_text);
        contentText.setText(content);

        Button actionButton = snackbarCard.findViewById(R.id.snackbar_action);
        if (l != null) {
            actionButton.setText(action);
            actionButton.setOnClickListener(v -> {
                snackbar.dismiss();
                l.onClick(v);
            });
        } else {
            actionButton.setVisibility(View.GONE);
        }

        snackbar.show();
    }
}
