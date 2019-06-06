package wangdaye.com.geometricweather.main.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialogFragment;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.MainColorPicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.AnimatableIconView;
import wangdaye.com.geometricweather.ui.widget.moon.MoonPhaseView;
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.utils.helpter.LunarHelper;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.weather.WeatherHelper;

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
            title.setText(weather.dailyList.get(position).getDateInFormat(getString(R.string.date_format_long))
                    + " " + weather.dailyList.get(position).week);
        } else {
            title.setText(weather.hourlyList.get(position).time);
        }
        title.setTextColor(colorPicker.getTextContentColor(getActivity()));

        TextView subtitle = view.findViewById(R.id.dialog_weather_subtitle);
        if (daily && LanguageUtils.getLanguageCode(getActivity()).startsWith("zh")) {
            String[] dates = weather.dailyList.get(position).date.split("-");
            subtitle.setText(LunarHelper.getLunarDate(dates));
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

        if (daily && !TextUtils.isEmpty(weather.dailyList.get(position).moonPhase)) {
            phaseTitle.setText(WeatherHelper.getMoonPhaseName(
                    getActivity(), weather.dailyList.get(position).moonPhase));
            phaseView.setSurfaceAngle(WeatherHelper.getMoonPhaseAngle(
                    weather.dailyList.get(position).moonPhase));
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
        String weatherKind;
        if (daily) {
            weatherKind = weather.dailyList.get(position).weatherKinds[0];
            weatherIcons[0].setAnimatableIcon(
                    WeatherHelper.getWeatherIcons(provider, weatherKind, true),
                    WeatherHelper.getWeatherAnimators(provider, weatherKind, true)
            );

            weatherKind = weather.dailyList.get(position).weatherKinds[1];
            weatherIcons[1].setAnimatableIcon(
                    WeatherHelper.getWeatherIcons(provider, weatherKind, false),
                    WeatherHelper.getWeatherAnimators(provider, weatherKind, false)
            );
        } else {
            weatherKind = weather.hourlyList.get(position).weatherKind;
            boolean daytime = weather.hourlyList.get(position).dayTime;
            weatherIcons[0].setAnimatableIcon(
                    WeatherHelper.getWeatherIcons(provider, weatherKind, daytime),
                    WeatherHelper.getWeatherAnimators(provider, weatherKind, daytime)
            );

            view.findViewById(R.id.dialog_weather_weatherContainer_night).setVisibility(View.GONE);
        }

        TextView[] weatherTexts = new TextView[] {
                view.findViewById(R.id.dialog_weather_text_day),
                view.findViewById(R.id.dialog_weather_text_night)
        };
        weatherTexts[0].setTextColor(colorPicker.getTextContentColor(getActivity()));
        weatherTexts[1].setTextColor(colorPicker.getTextContentColor(getActivity()));
        if (daily) {
            String daytimeTxt = weather.dailyList.get(position).weathers[0] + "  "
                    + ValueUtils.buildCurrentTemp(
                            weather.dailyList.get(position).temps[0],
                            false,
                            SettingsOptionManager.getInstance(getActivity()).isFahrenheit()
                    ) + "\n"
                    + getString(R.string.wind) + " : " + weather.dailyList.get(position).windDirs[0]
                    + (TextUtils.isEmpty(weather.dailyList.get(position).windSpeeds[0])
                            ? ""
                            : " " + weather.dailyList.get(position).windSpeeds[0])
                    + " (" + weather.dailyList.get(position).windLevels[0] + ") "
                    + WeatherHelper.getWindArrows(weather.dailyList.get(position).windDegrees[0])
                    + (
                            weather.dailyList.get(position).precipitations[0] >= 0
                                    ? "\n" + getString(R.string.precipitation) + " : " + weather.dailyList.get(position).precipitations[0] + "%"
                                    : ""
                    );
            weatherTexts[0].setText(daytimeTxt);

            String nighttimeTxt = weather.dailyList.get(position).weathers[1] + "  "
                    + ValueUtils.buildCurrentTemp(
                            weather.dailyList.get(position).temps[1],
                            false,
                            SettingsOptionManager.getInstance(getActivity()).isFahrenheit()
                    ) + "\n"
                    + getString(R.string.wind) + " : " + weather.dailyList.get(position).windDirs[1]
                    + (
                            TextUtils.isEmpty(weather.dailyList.get(position).windSpeeds[1])
                                    ? ""
                                    : " " + weather.dailyList.get(position).windSpeeds[1]
                    ) + " (" + weather.dailyList.get(position).windLevels[1] + ") "
                    + WeatherHelper.getWindArrows(weather.dailyList.get(position).windDegrees[1])
                    + (
                            weather.dailyList.get(position).precipitations[1] >= 0
                                    ? "\n" + getString(R.string.precipitation) + " : " + weather.dailyList.get(position).precipitations[1] + "%"
                                    : ""
                    );
            weatherTexts[1].setText(nighttimeTxt);
        } else {
            String text = weather.hourlyList.get(position).weather + "  "
                    + ValueUtils.buildCurrentTemp(
                            weather.hourlyList.get(position).temp,
                            false,
                            SettingsOptionManager.getInstance(getActivity()).isFahrenheit()
                    ) + (
                            weather.hourlyList.get(position).precipitation >= 0
                                    ? "\n" + getString(R.string.precipitation) + " : " + weather.hourlyList.get(position).precipitation + "%"
                                    : ""
                    );
            weatherTexts[0].setText(text);
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
            sunMoonText[0].setText(
                    weather.dailyList.get(position).astros[0] + "↑"
                            + " / "
                            + weather.dailyList.get(position).astros[1] + "↓"
            );
            if (!TextUtils.isEmpty(weather.dailyList.get(position).astros[2])) {
                sunMoonText[1].setText(
                        weather.dailyList.get(position).astros[2] + "↑"
                                + " / "
                                + weather.dailyList.get(position).astros[3] + "↓"
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

    public void setData(Weather weather, int position, boolean daily) {
        this.weather = weather;
        this.position = position;
        this.daily = daily;
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
