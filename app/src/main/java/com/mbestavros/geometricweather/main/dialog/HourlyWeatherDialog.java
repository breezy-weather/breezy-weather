package com.mbestavros.geometricweather.main.dialog;

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
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;

import com.mbestavros.geometricweather.R;
import com.mbestavros.geometricweather.basic.model.option.unit.PrecipitationUnit;
import com.mbestavros.geometricweather.basic.model.option.unit.ProbabilityUnit;
import com.mbestavros.geometricweather.basic.model.option.unit.TemperatureUnit;
import com.mbestavros.geometricweather.basic.model.weather.Hourly;
import com.mbestavros.geometricweather.basic.model.weather.Weather;
import com.mbestavros.geometricweather.basic.model.weather.WeatherCode;
import com.mbestavros.geometricweather.resource.ResourceHelper;
import com.mbestavros.geometricweather.resource.provider.ResourceProvider;
import com.mbestavros.geometricweather.resource.provider.ResourcesProviderFactory;
import com.mbestavros.geometricweather.settings.SettingsOptionManager;
import com.mbestavros.geometricweather.ui.widget.AnimatableIconView;
import com.mbestavros.geometricweather.utils.manager.ThemeManager;

/**
 * Hourly weather dialog.
 * */

public class HourlyWeatherDialog extends DialogFragment {

    private AnimatableIconView weatherIcon;

    private Weather weather;
    private int position;

    @ColorInt private int weatherColor;

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_weather_hourly, null, false);
        initWidget(view);
        return new AlertDialog.Builder(getActivity()).setView(view).create();
    }

    @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
    private void initWidget(View view) {
        if (getActivity() == null) {
            return;
        }

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        Hourly hourly = weather.getHourlyForecast().get(position);

        CoordinatorLayout container = view.findViewById(R.id.dialog_weather_hourly_container);
        container.setBackgroundColor(ThemeManager.getInstance(requireActivity()).getRootColor(getActivity()));

        TextView title = view.findViewById(R.id.dialog_weather_hourly_title);
        title.setText(hourly.getHour(getActivity()));
        title.setTextColor(weatherColor);

        TextView subtitle = view.findViewById(R.id.dialog_weather_hourly_subtitle);
        subtitle.setText(new SimpleDateFormat(getString(R.string.date_format_widget_long)).format(hourly.getDate()));
        subtitle.setTextColor(ThemeManager.getInstance(requireActivity()).getTextSubtitleColor(getActivity()));

        view.findViewById(R.id.dialog_weather_hourly_weatherContainer).setOnClickListener(v -> weatherIcon.startAnimators());

        this.weatherIcon = view.findViewById(R.id.dialog_weather_hourly_icon);
        WeatherCode weatherCode = hourly.getWeatherCode();
        boolean daytime = hourly.isDaylight();
        weatherIcon.setAnimatableIcon(
                ResourceHelper.getWeatherIcons(provider, weatherCode, daytime),
                ResourceHelper.getWeatherAnimators(provider, weatherCode, daytime)
        );

        TextView weatherText = view.findViewById(R.id.dialog_weather_hourly_text);
        weatherText.setTextColor(ThemeManager.getInstance(requireActivity()).getTextContentColor(getActivity()));

        SettingsOptionManager settings = SettingsOptionManager.getInstance(getActivity());
        TemperatureUnit temperatureUnit = settings.getTemperatureUnit();
        PrecipitationUnit precipitationUnit = settings.getPrecipitationUnit();

        StringBuilder builder = new StringBuilder(
                hourly.getWeatherText()
                        + "  "
                        + hourly.getTemperature().getTemperature(requireActivity(), temperatureUnit)
        );
        if (hourly.getTemperature().getRealFeelTemperature() != null) {
            builder.append("\n")
                    .append(getString(R.string.feels_like))
                    .append(hourly.getTemperature().getRealFeelTemperature(requireActivity(), temperatureUnit));
        }
        if (hourly.getPrecipitation().getTotal() != null) {
            Float p = hourly.getPrecipitation().getTotal();
            builder.append("\n")
                    .append(getString(R.string.precipitation))
                    .append(" : ")
                    .append(precipitationUnit.getPrecipitationText(requireActivity(), p));
        }
        if (hourly.getPrecipitationProbability().getTotal() != null
                && hourly.getPrecipitationProbability().getTotal() > 0) {
            Float p = hourly.getPrecipitationProbability().getTotal();
            builder.append("\n")
                    .append(getString(R.string.precipitation_probability))
                    .append(" : ")
                    .append(ProbabilityUnit.PERCENT.getProbabilityText(requireActivity(), p));
        }
        weatherText.setText(builder.toString());
    }

    public void setData(Weather weather, int position, @ColorInt int weatherColor) {
        this.weather = weather;
        this.position = position;
        this.weatherColor = weatherColor;
    }
}
