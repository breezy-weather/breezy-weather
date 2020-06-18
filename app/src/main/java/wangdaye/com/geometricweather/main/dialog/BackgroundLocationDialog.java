package wangdaye.com.geometricweather.main.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import wangdaye.com.geometricweather.R;

public class BackgroundLocationDialog extends DialogFragment {

    private OnSetButtonClickListener listener;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_background_location, null, false);
        view.findViewById(R.id.dialog_background_location_setButton).setOnClickListener(v -> {
            listener.onSetButtonClicked();
            dismiss();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    public interface OnSetButtonClickListener {
        void onSetButtonClicked();
    }

    public void setOnSetButtonClickListener(OnSetButtonClickListener l) {
        this.listener = l;
    }
}
