package wangdaye.com.geometricweather.view.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialogFragment;

/**
 * Initializing dialog.
 * */

public class InitializingDialog extends GeoDialogFragment {
    // widget
    private CoordinatorLayout container;
    private TextView textView;

    // data
    private String initTxt;

    /** <br> life cycle. */

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_initializing, null, false);

        initTxt = getString(R.string.feedback_initializing);
        container = (CoordinatorLayout) view.findViewById(R.id.dialog_initializing_container);
        textView = (TextView) view.findViewById(R.id.dialog_initializing_txt);
        setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    /** <br> UI. */

    public void setProcess(int process) {
        textView.setText(initTxt + " " + process + "%");
    }
}
