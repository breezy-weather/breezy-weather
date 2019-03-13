package wangdaye.com.geometricweather.ui.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialogFragment;

/**
 * Running in background O dialog.
 * */
@RequiresApi(api = Build.VERSION_CODES.O)
public class RunningInBackgroundODialog extends GeoDialogFragment
        implements View.OnClickListener {

    private CoordinatorLayout container;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_running_in_background_o, null, false);
        this.initWidget(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    private void initWidget(View view) {
        this.container = view.findViewById(R.id.dialog_running_in_background_o_container);

        view.findViewById(R.id.dialog_running_in_background_o_setBtn).setOnClickListener(this);
        view.findViewById(R.id.dialog_running_in_background_o_doneBtn).setOnClickListener(this);
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_running_in_background_o_setBtn:
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getActivity().getPackageName());
                getActivity().startActivity(intent);
                break;

            case R.id.dialog_running_in_background_o_doneBtn:
                dismiss();
                break;
        }
    }
}
