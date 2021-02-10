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

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialog;

/**
 * Adaptive icon dialog.
 * */
public class MinimalIconDialog extends GeoDialog {

    private String mTitle;
    private Drawable mXmlIconDrawable;
    private Drawable mLightDrawable;
    private Drawable mGreyDrawable;
    private Drawable mDarkDrawable;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_minimal_icon, null, false);
        initWidget(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    private void initWidget(View view) {
        if (getActivity() == null) {
            return;
        }

        AppCompatImageView xmlIcon = view.findViewById(R.id.dialog_minimal_icon_xmlIcon);
        xmlIcon.setImageDrawable(mXmlIconDrawable);

        TextView titleView = view.findViewById(R.id.dialog_minimal_icon_title);
        titleView.setText(mTitle);

        AppCompatImageView lightIconView = view.findViewById(R.id.dialog_minimal_icon_lightIcon);
        lightIconView.setImageDrawable(mLightDrawable);

        AppCompatImageView greyIconView = view.findViewById(R.id.dialog_minimal_icon_greyIcon);
        greyIconView.setImageDrawable(mGreyDrawable);

        AppCompatImageView darkIconView = view.findViewById(R.id.dialog_minimal_icon_darkIcon);
        darkIconView.setImageDrawable(mDarkDrawable);
    }

    public void setData(@NonNull String title,
                        @NonNull Drawable xmlIconDrawable,
                        @NonNull Drawable lightDrawable,
                        @NonNull Drawable greyDrawable,
                        @NonNull Drawable darkDrawable) {
        mTitle = title;
        mXmlIconDrawable = xmlIconDrawable;
        mLightDrawable = lightDrawable;
        mGreyDrawable = greyDrawable;
        mDarkDrawable = darkDrawable;
    }

    @Override
    public View getSnackbarContainer() {
        return requireDialog().findViewById(R.id.dialog_minimal_icon_container);
    }
}
