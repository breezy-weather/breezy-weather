package wangdaye.com.geometricweather.main.ui.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialogFragment;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

/**
 * Daily weather dialog.
 * */

public class DailyWeatherDialog extends GeoDialogFragment {

    private CoordinatorLayout container;
    private MainColorPicker colorPicker;

    private Weather weather;
    private int position;

    @ColorInt private int weatherColor;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_weather_daily, null, false);
        this.initWidget(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    @SuppressLint("SetTextI18n")
    private void initWidget(View view) {
        if (getActivity() == null) {
            return;
        }

        Daily daily = weather.getDailyForecast().get(position);

        this.container = view.findViewById(R.id.dialog_weather_daily_container);
        container.setBackgroundColor(colorPicker.getRootColor(getActivity()));

        TextView title = view.findViewById(R.id.dialog_weather_daily_title);
        title.setText(daily.getDate(getString(R.string.date_format_widget_long)));
        title.setTextColor(weatherColor);

        TextView subtitle = view.findViewById(R.id.dialog_weather_daily_subtitle);
        if (SettingsOptionManager.getInstance(getActivity()).getLanguage().getCode().startsWith("zh")) {
            subtitle.setText(daily.getLunar());
        } else {
            subtitle.setVisibility(View.GONE);
        }
        subtitle.setTextColor(colorPicker.getTextSubtitleColor(getActivity()));


    }

    public void setData(Weather weather, int position, @ColorInt int weatherColor) {
        this.weather = weather;
        this.position = position;
        this.weatherColor = weatherColor;
    }

    public void setColorPicker(@NonNull MainColorPicker colorPicker) {
        this.colorPicker = colorPicker;
    }
}
