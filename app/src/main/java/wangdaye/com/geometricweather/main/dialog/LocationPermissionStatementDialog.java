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

public class LocationPermissionStatementDialog extends GeoDialog {

    private OnNextButtonClickListener mListener;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_location_permission_statement, null, false);
        view.findViewById(R.id.dialog_location_permission_statement_nextButton).setOnClickListener(v -> {
            dismiss();
            mListener.onNextButtonClicked();
        });

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    @Override
    public View getSnackbarContainer() {
        return requireDialog().findViewById(R.id.dialog_location_permission_statement_container);
    }

    public interface OnNextButtonClickListener {
        void onNextButtonClicked();
    }

    public void setOnSetButtonClickListener(OnNextButtonClickListener l) {
        mListener = l;
    }
}
