package wangdaye.com.geometricweather.main.ui.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialogFragment;
import wangdaye.com.geometricweather.basic.model.option.unit.ProbabilityUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.SpeedUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.basic.model.weather.WeatherCode;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.AnimatableIconView;
import wangdaye.com.geometricweather.ui.widget.moon.MoonPhaseView;

/**
 * Weather dialog.
 * */

public class WeatherDialog extends GeoDialogFragment
        implements View.OnClickListener {

    private CoordinatorLayout container;
    private AnimatableIconView[] weatherIcons;
    private MainColorPicker colorPicker;

    private Weather weather;
    private int position;
    private boolean daily;

    @ColorInt private int weatherColor;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_weather, null, false);
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

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        this.container = view.findViewById(R.id.dialog_weather_container);
        container.setBackgroundColor(colorPicker.getRootColor(getActivity()));

        TextView title = view.findViewById(R.id.dialog_weather_title);
        if (daily) {
            title.setText(
                    weather.getDailyForecast().get(position).getLongDate(getActivity())
                            + " "
                            + weather.getDailyForecast().get(position).getWeek(getActivity())
            );
        } else {
            title.setText(weather.getHourlyForecast().get(position).getHour(getActivity()));
        }
        title.setTextColor(weatherColor);

        TextView subtitle = view.findViewById(R.id.dialog_weather_subtitle);
        if (daily && SettingsOptionManager.getInstance(getActivity()).getLanguage().getCode().startsWith("zh")) {
            subtitle.setText(weather.getDailyForecast().get(position).getLunar());
        } else {
            subtitle.setVisibility(View.GONE);
        }
        subtitle.setTextColor(colorPicker.getTextSubtitleColor(getActivity()));

        TextView phaseTitle = view.findViewById(R.id.dialog_weather_phaseText);
        phaseTitle.setTextColor(colorPicker.getTextContentColor(getActivity()));

        MoonPhaseView phaseView = view.findViewById(R.id.dialog_weather_phaseView);
        phaseView.setColor(
                ContextCompat.getColor(getActivity(), R.color.colorTextContent_dark),
                ContextCompat.getColor(getActivity(), R.color.colorTextContent_light),
                colorPicker.getTextContentColor(getActivity())
        );

        if (daily && weather.getDailyForecast().get(position).getMoonPhase().isValid()) {
            phaseTitle.setText(
                    weather.getDailyForecast().get(position).getMoonPhase().getMoonPhase(getActivity())
            );
            Integer angle = weather.getDailyForecast().get(position).getMoonPhase().getAngle();
            phaseView.setSurfaceAngle(angle == null ? 0 : angle);
        } else {
            phaseTitle.setVisibility(View.GONE);
            phaseView.setVisibility(View.GONE);
        }

        view.findViewById(R.id.dialog_weather_weatherContainer_day).setOnClickListener(this);
        view.findViewById(R.id.dialog_weather_weatherContainer_night).setOnClickListener(this);

        this.weatherIcons = new AnimatableIconView[] {
                view.findViewById(R.id.dialog_weather_icon_day),
                view.findViewById(R.id.dialog_weather_icon_night)
        };
        WeatherCode weatherCode;
        if (daily) {
            weatherCode = weather.getDailyForecast().get(position).day().getWeatherCode();
            weatherIcons[0].setAnimatableIcon(
                    ResourceHelper.getWeatherIcons(provider, weatherCode, true),
                    ResourceHelper.getWeatherAnimators(provider, weatherCode, true)
            );

            weatherCode = weather.getDailyForecast().get(position).night().getWeatherCode();
            weatherIcons[1].setAnimatableIcon(
                    ResourceHelper.getWeatherIcons(provider, weatherCode, false),
                    ResourceHelper.getWeatherAnimators(provider, weatherCode, false)
            );
        } else {
            weatherCode = weather.getHourlyForecast().get(position).getWeatherCode();
            boolean daytime = weather.getHourlyForecast().get(position).isDaylight();
            weatherIcons[0].setAnimatableIcon(
                    ResourceHelper.getWeatherIcons(provider, weatherCode, daytime),
                    ResourceHelper.getWeatherAnimators(provider, weatherCode, daytime)
            );

            view.findViewById(R.id.dialog_weather_weatherContainer_night).setVisibility(View.GONE);
        }

        TextView[] weatherTexts = new TextView[] {
                view.findViewById(R.id.dialog_weather_text_day),
                view.findViewById(R.id.dialog_weather_text_night)
        };
        weatherTexts[0].setTextColor(colorPicker.getTextContentColor(getActivity()));
        weatherTexts[1].setTextColor(colorPicker.getTextContentColor(getActivity()));

        SettingsOptionManager settings = SettingsOptionManager.getInstance(getActivity());
        TemperatureUnit temperatureUnit = settings.getTemperatureUnit();
        SpeedUnit speedUnit = settings.getSpeedUnit();

        if (daily) {
            StringBuilder builder = new StringBuilder(
                    weather.getDailyForecast().get(position).day().getWeatherText()
                            + "  "
                            + weather.getDailyForecast().get(position).day().getTemperature().getTemperature(temperatureUnit)
                            + "\n"
                            + getString(R.string.wind)
                            + " : "
                            + weather.getDailyForecast().get(position).day().getWind().getWindDescription(speedUnit)
            );
            if (weather.getDailyForecast().get(position).day().getPrecipitationProbability().getTotal() != null) {
                Float p = weather.getDailyForecast().get(position).day().getPrecipitationProbability().getTotal();
                builder.append("\n")
                        .append(getString(R.string.precipitation))
                        .append(" : ")
                        .append(ProbabilityUnit.PERCENT.getProbabilityText(p == null ? 0 : p));
            }
            weatherTexts[0].setText(builder.toString());

            builder = new StringBuilder(
                    weather.getDailyForecast().get(position).night().getWeatherText()
                            + "  "
                            + weather.getDailyForecast().get(position).night().getTemperature().getTemperature(temperatureUnit)
                            + "\n"
                            + getString(R.string.wind)
                            + " : "
                            + weather.getDailyForecast().get(position).night().getWind().getWindDescription(speedUnit)
            );
            if (weather.getDailyForecast().get(position).night().getPrecipitationProbability().getTotal() != null) {
                Float p = weather.getDailyForecast().get(position).night().getPrecipitationProbability().getTotal();
                builder.append("\n")
                        .append(getString(R.string.precipitation))
                        .append(" : ")
                        .append(ProbabilityUnit.PERCENT.getProbabilityText(p == null ? 0 : p));
            }
            weatherTexts[1].setText(builder.toString());
        } else {
            StringBuilder builder = new StringBuilder(
                    weather.getHourlyForecast().get(position).getWeatherText()
                            + "  "
                            + weather.getHourlyForecast().get(position).getTemperature().getTemperature(temperatureUnit)
            );
            if (weather.getHourlyForecast().get(position).getPrecipitationProbability().getTotal() != null) {
                Float p = weather.getHourlyForecast().get(position).getPrecipitationProbability().getTotal();
                builder.append("\n")
                        .append(getString(R.string.precipitation))
                        .append(" : ")
                        .append(ProbabilityUnit.PERCENT.getProbabilityText(p == null ? 0 : p));
            }
            weatherTexts[0].setText(builder.toString());
        }

        ((AppCompatImageView) view.findViewById(R.id.dialog_weather_sun_icon)).setSupportImageTintList(
                ColorStateList.valueOf(colorPicker.getTextSubtitleColor(getActivity()))
        );
        ((AppCompatImageView) view.findViewById(R.id.dialog_weather_moon_icon)).setSupportImageTintList(
                ColorStateList.valueOf(colorPicker.getTextSubtitleColor(getActivity()))
        );

        TextView[] sunMoonText = new TextView[] {
                view.findViewById(R.id.dialog_weather_sunrise_sunset),
                view.findViewById(R.id.dialog_weather_moonrise_moonset)
        };
        sunMoonText[0].setTextColor(colorPicker.getTextSubtitleColor(getActivity()));
        sunMoonText[1].setTextColor(colorPicker.getTextSubtitleColor(getActivity()));
        if (daily) {
            if (weather.getDailyForecast().get(position).sun().isValid()) {
                sunMoonText[0].setText(
                        weather.getDailyForecast().get(position).sun().getRiseTime(getActivity())
                                + "↑"
                                + " / "
                                + weather.getDailyForecast().get(position).sun().getSetTime(getActivity())
                                + "↓"
                );
            } else {
                view.findViewById(R.id.dialog_weather_sunContainer).setVisibility(View.GONE);
            }
            if (weather.getDailyForecast().get(position).moon().isValid()) {
                sunMoonText[1].setText(
                        weather.getDailyForecast().get(position).moon().getRiseTime(getActivity())
                                + "↑"
                                + " / "
                                + weather.getDailyForecast().get(position).moon().getSetTime(getActivity())
                                + "↓"
                );
            } else {
                view.findViewById(R.id.dialog_weather_moonContainer).setVisibility(View.GONE);
            }
        } else {
            view.findViewById(R.id.dialog_weather_sun_icon).setVisibility(View.GONE);
            view.findViewById(R.id.dialog_weather_moon_icon).setVisibility(View.GONE);
            sunMoonText[0].setVisibility(View.GONE);
            sunMoonText[1].setVisibility(View.GONE);
        }
    }

    public void setData(Weather weather, int position, boolean daily, @ColorInt int weatherColor) {
        this.weather = weather;
        this.position = position;
        this.daily = daily;
        this.weatherColor = weatherColor;
    }

    public void setColorPicker(@NonNull MainColorPicker colorPicker) {
        this.colorPicker = colorPicker;
    }

    // interface.

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_weather_weatherContainer_day:
                weatherIcons[0].startAnimators();
                break;

            case R.id.dialog_weather_weatherContainer_night:
                weatherIcons[1].startAnimators();
                break;
        }
    }
}
