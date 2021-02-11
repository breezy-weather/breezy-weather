package wangdaye.com.geometricweather.main.dialogs;

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

import java.text.SimpleDateFormat;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialog;
import wangdaye.com.geometricweather.basic.models.options.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.basic.models.options.unit.ProbabilityUnit;
import wangdaye.com.geometricweather.basic.models.options.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.models.weather.Hourly;
import wangdaye.com.geometricweather.basic.models.weather.Weather;
import wangdaye.com.geometricweather.basic.models.weather.WeatherCode;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.resource.providers.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widgets.AnimatableIconView;
import wangdaye.com.geometricweather.utils.managers.ThemeManager;

/**
 * Hourly weather dialog.
 * */

public class HourlyWeatherDialog extends GeoDialog {

    private AnimatableIconView mWeatherIcon;

    private Weather mWeather;
    private int mPosition;

    private @ColorInt int mWeatherColor;

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

        Hourly hourly = mWeather.getHourlyForecast().get(mPosition);

        CoordinatorLayout container = view.findViewById(R.id.dialog_weather_hourly_container);
        container.setBackgroundColor(ThemeManager.getInstance(requireActivity()).getRootColor(getActivity()));

        TextView title = view.findViewById(R.id.dialog_weather_hourly_title);
        title.setText(hourly.getHour(getActivity()));
        title.setTextColor(mWeatherColor);

        TextView subtitle = view.findViewById(R.id.dialog_weather_hourly_subtitle);
        subtitle.setText(new SimpleDateFormat(getString(R.string.date_format_widget_long)).format(hourly.getDate()));
        subtitle.setTextColor(ThemeManager.getInstance(requireActivity()).getTextSubtitleColor(getActivity()));

        view.findViewById(R.id.dialog_weather_hourly_weatherContainer).setOnClickListener(v -> mWeatherIcon.startAnimators());

        mWeatherIcon = view.findViewById(R.id.dialog_weather_hourly_icon);
        WeatherCode weatherCode = hourly.getWeatherCode();
        boolean daytime = hourly.isDaylight();
        mWeatherIcon.setAnimatableIcon(
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
        mWeather = weather;
        mPosition = position;
        mWeatherColor = weatherColor;
    }

    @Override
    public View getSnackbarContainer() {
        return requireDialog().findViewById(R.id.dialog_weather_hourly_container);
    }
}
