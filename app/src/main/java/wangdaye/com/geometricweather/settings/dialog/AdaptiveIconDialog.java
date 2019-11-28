package wangdaye.com.geometricweather.settings.dialog;

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
import androidx.fragment.app.DialogFragment;

import java.util.Random;

import james.adaptiveicon.AdaptiveIcon;
import james.adaptiveicon.AdaptiveIconView;
import wangdaye.com.geometricweather.R;

/**
 * Adaptive icon dialog.
 * */
public class AdaptiveIconDialog extends DialogFragment {

    private String title;
    private Drawable foregroundDrawable;
    private Drawable backgroundDrawable;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_adaptive_icon, null, false);
        this.initWidget(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    private void initWidget(View view) {
        TextView titleView = view.findViewById(R.id.dialog_adaptive_icon_title);
        titleView.setText(title);

        AdaptiveIconView iconView = view.findViewById(R.id.dialog_adaptive_icon_icon);
        iconView.setIcon(new AdaptiveIcon(foregroundDrawable, backgroundDrawable, 0.5));
        iconView.setPath(new Random().nextInt(AdaptiveIconView.PATH_TEARDROP + 1));
    }

    public void setData(@NonNull String title,
                        @NonNull Drawable foregroundDrawable,
                        @Nullable Drawable backgroundDrawable) {
        this.title = title;
        this.foregroundDrawable = foregroundDrawable;
        this.backgroundDrawable = backgroundDrawable;
    }
}
