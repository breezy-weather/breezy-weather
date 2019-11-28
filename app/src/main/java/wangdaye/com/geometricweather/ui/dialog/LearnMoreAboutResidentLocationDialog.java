package wangdaye.com.geometricweather.ui.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import wangdaye.com.geometricweather.R;

/**
 * Learn more about resident location dialog.
 * */
public class LearnMoreAboutResidentLocationDialog extends DialogFragment {

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_resident_location, null, false);
        builder.setView(view);
        return builder.create();
    }
}
