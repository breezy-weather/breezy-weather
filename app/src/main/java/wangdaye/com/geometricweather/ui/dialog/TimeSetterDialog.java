package wangdaye.com.geometricweather.ui.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Calendar;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialogFragment;

/**
 * Time setter dialog.
 * */

public class TimeSetterDialog extends GeoDialogFragment
        implements View.OnClickListener, TimePicker.OnTimeChangedListener {

    private CoordinatorLayout container;
    private OnTimeChangedListener listener;

    private int hour;
    private int minute;
    private boolean today = true;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_time_setter, null, false);
        this.initData();
        this.initWidget(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    public void setModel(boolean today) {
        this.today = today;
    }

    private void initData() {
        Calendar calendar = Calendar.getInstance();
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
    }

    private void initWidget(View view) {
        this.container = (CoordinatorLayout) view.findViewById(R.id.dialog_time_setter_container);

        Button done = (Button) view.findViewById(R.id.dialog_time_setter_done);
        done.setOnClickListener(this);

        Button cancel = (Button) view.findViewById(R.id.dialog_time_setter_cancel);
        cancel.setOnClickListener(this);

        TimePicker timePicker = (TimePicker) view.findViewById(R.id.dialog_time_setter_time_picker);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(this);
    }

    // interface.

    // on time changed listener.

    public interface OnTimeChangedListener {
        void timeChanged();
    }

    public void setOnTimeChangedListener(OnTimeChangedListener l) {
        this.listener = l;
    }

    // on time changed listener.

    @Override
    public void onTimeChanged(TimePicker timePicker, int i, int i1) {
        this.hour = i;
        this.minute = i1;
    }

    // on click.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_time_setter_cancel:
                dismiss();
                break;

            case R.id.dialog_time_setter_done:
                String hourText;
                String minuteText;

                if (hour < 10) {
                    hourText = "0" + Integer.toString(hour);
                } else {
                    hourText = Integer.toString(hour);
                }

                if (minute < 10) {
                    minuteText = "0" + Integer.toString(minute);
                } else {
                    minuteText = Integer.toString(minute);
                }

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                if (today) {
                    editor.putString(getString(R.string.key_forecast_today_time), hourText + ":" + minuteText);
                } else {
                    editor.putString(getString(R.string.key_forecast_tomorrow_time), hourText + ":" + minuteText);
                }
                editor.apply();

                if (listener != null) {
                    listener.timeChanged();
                }

                dismiss();
                break;
        }
    }
}
