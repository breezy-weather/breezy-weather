package wangdaye.com.geometricweather.ui.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialogFragment;

/**
 * Learn more about geocoder dialog.
 * */
public class LearnMoreAboutGeocoderDialog extends GeoDialogFragment
        implements View.OnClickListener {

    private CoordinatorLayout container;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_learn_more_about_geocoder, null, false);
        this.initWidget(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    private void initWidget(View view) {
        this.container = view.findViewById(R.id.dialog_learn_more_about_geocoder_container);

        view.findViewById(R.id.dialog_learn_more_about_geocoder_doneBtn).setOnClickListener(this);
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_learn_more_about_geocoder_doneBtn:
                dismiss();
                break;
        }
    }
}
