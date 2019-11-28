package wangdaye.com.geometricweather.settings.dialog;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.DialogFragment;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.widget.AnimatableIconView;

/**
 * Animatable icon dialog.
 * */
public class AnimatableIconDialog extends DialogFragment {

    private String title;
    @Size(3) private Drawable[] iconDrawables;
    @Size(3) private Animator[] iconAnimators;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_animatable_icon, null, false);
        this.initWidget(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    private void initWidget(View view) {
        TextView titleView = view.findViewById(R.id.dialog_animatable_icon_title);
        titleView.setText(title);

        AnimatableIconView iconView = view.findViewById(R.id.dialog_animatable_icon_icon);
        iconView.setAnimatableIcon(iconDrawables, iconAnimators);

        CoordinatorLayout container = view.findViewById(R.id.dialog_animatable_icon_container);
        container.setOnClickListener(v -> iconView.startAnimators());
    }

    public void setData(@NonNull String title,
                        @NonNull @Size(3) Drawable[] iconDrawables,
                        @NonNull @Size(3) Animator[] iconAnimators) {
        this.title = title;
        this.iconDrawables = iconDrawables;
        this.iconAnimators = iconAnimators;
    }
}
