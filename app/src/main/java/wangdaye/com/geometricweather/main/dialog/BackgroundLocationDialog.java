package wangdaye.com.geometricweather.main.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialog;

public class BackgroundLocationDialog extends GeoDialog {

    private OnSetButtonClickListener mListener;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_background_location, null, false);
        view.findViewById(R.id.dialog_background_location_setButton).setOnClickListener(v -> {
            dismiss();
            mListener.onSetButtonClicked();
        });

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    @Override
    public View getSnackbarContainer() {
        return requireDialog().findViewById(R.id.dialog_background_location_container);
    }

    public interface OnSetButtonClickListener {
        void onSetButtonClicked();
    }

    public void setOnSetButtonClickListener(OnSetButtonClickListener l) {
        mListener = l;
    }
}
