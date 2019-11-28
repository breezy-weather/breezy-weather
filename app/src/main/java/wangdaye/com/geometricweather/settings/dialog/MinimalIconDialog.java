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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;

import wangdaye.com.geometricweather.R;

/**
 * Adaptive icon dialog.
 * */
public class MinimalIconDialog extends DialogFragment {

    private String title;
    private Drawable xmlIconDrawable;
    private Drawable lightDrawable;
    private Drawable greyDrawable;
    private Drawable darkDrawable;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_minimal_icon, null, false);
        this.initWidget(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    private void initWidget(View view) {
        if (getActivity() == null) {
            return;
        }

        AppCompatImageView xmlIcon = view.findViewById(R.id.dialog_minimal_icon_xmlIcon);
        xmlIcon.setImageDrawable(xmlIconDrawable);

        TextView titleView = view.findViewById(R.id.dialog_minimal_icon_title);
        titleView.setText(title);

        AppCompatImageView lightIconView = view.findViewById(R.id.dialog_minimal_icon_lightIcon);
        lightIconView.setImageDrawable(lightDrawable);

        AppCompatImageView greyIconView = view.findViewById(R.id.dialog_minimal_icon_greyIcon);
        greyIconView.setImageDrawable(greyDrawable);

        AppCompatImageView darkIconView = view.findViewById(R.id.dialog_minimal_icon_darkIcon);
        darkIconView.setImageDrawable(darkDrawable);
    }

    public void setData(@NonNull String title,
                        @NonNull Drawable xmlIconDrawable,
                        @NonNull Drawable lightDrawable,
                        @NonNull Drawable greyDrawable,
                        @NonNull Drawable darkDrawable) {
        this.title = title;
        this.xmlIconDrawable = xmlIconDrawable;
        this.lightDrawable = lightDrawable;
        this.greyDrawable = greyDrawable;
        this.darkDrawable = darkDrawable;
    }
}
