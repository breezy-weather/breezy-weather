package wangdaye.com.geometricweather.main.dialogs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import java.text.SimpleDateFormat;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoDialog;
import wangdaye.com.geometricweather.common.basic.models.options.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.common.basic.models.options.unit.ProbabilityUnit;
import wangdaye.com.geometricweather.common.basic.models.options.unit.TemperatureUnit;
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode;
import wangdaye.com.geometricweather.common.ui.widgets.AnimatableIconView;
import wangdaye.com.geometricweather.main.utils.MainPalette;
import wangdaye.com.geometricweather.theme.resource.ResourceHelper;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.theme.resource.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsManager;

/**
 * Hourly weather dialog.
 * */

public class HourlyWeatherDialog extends GeoDialog {

    private AnimatableIconView mWeatherIcon;

    private static final String KEY_WEATHER = "weather";
    private static final String KEY_POSITION = "position";
    private static final String KEY_PALETTE = "palette";

    public static HourlyWeatherDialog getInstance(Weather weather,
                                                  int position,
                                                  MainPalette palette) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_WEATHER, weather);
        bundle.putInt(KEY_POSITION, position);
        bundle.putParcelable(KEY_PALETTE, palette);

        HourlyWeatherDialog dialog = new HourlyWeatherDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_weather_hourly, container, false);
        initWidget(view);
        return view;
    }

    @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
    private void initWidget(View view) {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }

        Weather weather = (Weather) bundle.getSerializable(KEY_WEATHER);
        int position = bundle.getInt(KEY_POSITION, 0);
        MainPalette palette = bundle.getParcelable(KEY_PALETTE);
        if (weather == null || palette == null) {
            return;
        }

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        Hourly hourly = weather.getHourlyForecast().get(position);

        CoordinatorLayout container = view.findViewById(R.id.dialog_weather_hourly_container);
        container.setBackgroundColor(palette.rootColor);

        TextView title = view.findViewById(R.id.dialog_weather_hourly_title);
        title.setText(hourly.getHour(getActivity()));
        title.setTextColor(palette.themeColors[0]);

        TextView subtitle = view.findViewById(R.id.dialog_weather_hourly_subtitle);
        subtitle.setText(new SimpleDateFormat(getString(R.string.date_format_widget_long)).format(hourly.getDate()));
        subtitle.setTextColor(palette.subtitleColor);

        view.findViewById(R.id.dialog_weather_hourly_weatherContainer).setOnClickListener(v ->
                mWeatherIcon.startAnimators());

        mWeatherIcon = view.findViewById(R.id.dialog_weather_hourly_icon);
        WeatherCode weatherCode = hourly.getWeatherCode();
        boolean daytime = hourly.isDaylight();
        mWeatherIcon.setAnimatableIcon(
                ResourceHelper.getWeatherIcons(provider, weatherCode, daytime),
                ResourceHelper.getWeatherAnimators(provider, weatherCode, daytime)
        );

        TextView weatherText = view.findViewById(R.id.dialog_weather_hourly_text);
        weatherText.setTextColor(palette.contentColor);

        SettingsManager settings = SettingsManager.getInstance(getActivity());
        TemperatureUnit temperatureUnit = settings.getTemperatureUnit();
        PrecipitationUnit precipitationUnit = settings.getPrecipitationUnit();

        StringBuilder builder = new StringBuilder(
                hourly.getWeatherText()
                        + ",  "
                        + hourly.getTemperature().getTemperature(requireActivity(), temperatureUnit)
        );
        if (hourly.getTemperature().getRealFeelTemperature() != null) {
            builder.append("\n")
                    .append(getString(R.string.feels_like))
                    .append(" ")
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
}
