package wangdaye.com.geometricweather.settings.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Random;

import james.adaptiveicon.AdaptiveIcon;
import james.adaptiveicon.AdaptiveIconView;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialog;

/**
 * Adaptive icon dialog.
 * */
public class AdaptiveIconDialog extends GeoDialog {

    private String mTitle;
    private Drawable mForegroundDrawable;
    private Drawable mBackgroundDrawable;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_adaptive_icon, null, false);
        initWidget(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    private void initWidget(View view) {
        TextView titleView = view.findViewById(R.id.dialog_adaptive_icon_title);
        titleView.setText(mTitle);

        AdaptiveIconView iconView = view.findViewById(R.id.dialog_adaptive_icon_icon);
        iconView.setIcon(new AdaptiveIcon(mForegroundDrawable, mBackgroundDrawable, 0.5));
        iconView.setPath(new Random().nextInt(AdaptiveIconView.PATH_TEARDROP + 1));
    }

    public void setData(@NonNull String title,
                        @NonNull Drawable foregroundDrawable,
                        @Nullable Drawable backgroundDrawable) {
        mTitle = title;
        mForegroundDrawable = foregroundDrawable;
        mBackgroundDrawable = backgroundDrawable;
    }

    @Override
    public View getSnackbarContainer() {
        return requireDialog().findViewById(R.id.dialog_adaptive_icon_container);
    }
}
