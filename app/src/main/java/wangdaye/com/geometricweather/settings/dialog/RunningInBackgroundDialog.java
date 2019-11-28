package wangdaye.com.geometricweather.settings.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;

import wangdaye.com.geometricweather.R;

/**
 * Running in background dialog.
 * */
public class RunningInBackgroundDialog extends DialogFragment {

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_running_in_background, null, false);
        view.findViewById(R.id.dialog_running_in_background_doneBtn).setOnClickListener(v -> dismiss());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }
}
