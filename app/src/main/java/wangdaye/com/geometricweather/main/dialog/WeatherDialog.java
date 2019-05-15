package wangdaye.com.geometricweather.main.dialog;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialogFragment;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
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

    private Animator[][] iconAnimators;

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

        TextView title = view.findViewById(R.id.dialog_weather_title);
        if (daily) {
            title.setText(weather.dailyList.get(position).getDateInFormat(getString(R.string.date_format_long))
                    + " " + weather.dailyList.get(position).week);
        } else {
            title.setText(weather.hourlyList.get(position).time);
        }

        TextView subtitle = view.findViewById(R.id.dialog_weather_subtitle);
        if (daily && LanguageUtils.getLanguageCode(getActivity()).startsWith("zh")) {
            String[] dates = weather.dailyList.get(position).date.split("-");
            subtitle.setText(LunarHelper.getLunarDate(dates));
        } else {
            subtitle.setVisibility(View.GONE);
        }

        TextView phaseTitle = view.findViewById(R.id.dialog_weather_phaseText);
        MoonPhaseView phaseView = view.findViewById(R.id.dialog_weather_phaseView);
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

        AppCompatImageView[][] weatherIcons = new AppCompatImageView[2][3];
        weatherIcons[0][0] = view.findViewById(R.id.dialog_weather_icon_1_day);
        weatherIcons[0][1] = view.findViewById(R.id.dialog_weather_icon_2_day);
        weatherIcons[0][2] = view.findViewById(R.id.dialog_weather_icon_3_day);
        weatherIcons[1][0] = view.findViewById(R.id.dialog_weather_icon_1_night);
        weatherIcons[1][1] = view.findViewById(R.id.dialog_weather_icon_2_night);
        weatherIcons[1][2] = view.findViewById(R.id.dialog_weather_icon_3_night);
        if (daily) {
            Drawable[] daytimeDrawables = WeatherHelper.getWeatherIcons(
                    provider,
                    weather.dailyList.get(position).weatherKinds[0],
                    true
            );
            for (int i = 0; i < weatherIcons[0].length; i ++) {
                if (daytimeDrawables[i] != null) {
                    resetImageView(weatherIcons[0][i]);
                    weatherIcons[0][i].setImageDrawable(daytimeDrawables[i]);
                    weatherIcons[0][i].setVisibility(View.VISIBLE);
                } else {
                    weatherIcons[0][i].setVisibility(View.GONE);
                }
            }

            Drawable[] nighttimeDrawables = WeatherHelper.getWeatherIcons(
                    provider,
                    weather.dailyList.get(position).weatherKinds[1],
                    false
            );
            for (int i = 0; i < weatherIcons[0].length; i ++) {
                if (nighttimeDrawables[i] != null) {
                    resetImageView(weatherIcons[1][i]);
                    weatherIcons[1][i].setImageDrawable(nighttimeDrawables[i]);
                    weatherIcons[1][i].setVisibility(View.VISIBLE);
                } else {
                    weatherIcons[1][i].setVisibility(View.GONE);
                }
            }
        } else {
            Drawable[] drawables = WeatherHelper.getWeatherIcons(
                    provider,
                    weather.hourlyList.get(position).weatherKind,
                    weather.hourlyList.get(position).dayTime
            );
            for (int i = 0; i < weatherIcons[0].length; i ++) {
                if (drawables[i] != null) {
                    resetImageView(weatherIcons[0][i]);
                    weatherIcons[0][i].setImageDrawable(drawables[i]);
                    weatherIcons[0][i].setVisibility(View.VISIBLE);
                } else {
                    weatherIcons[0][i].setVisibility(View.GONE);
                }
            }
            weatherIcons[1][0].setVisibility(View.GONE);
            weatherIcons[1][1].setVisibility(View.GONE);
            weatherIcons[1][2].setVisibility(View.GONE);
        }

        TextView[] weatherTexts = new TextView[] {
                view.findViewById(R.id.dialog_weather_text_day),
                view.findViewById(R.id.dialog_weather_text_night)};
        if (daily) {
            String daytimeTxt = weather.dailyList.get(position).weathers[0] + "  "
                    + ValueUtils.buildCurrentTemp(
                            weather.dailyList.get(position).temps[0],
                            false,
                            GeometricWeather.getInstance().isFahrenheit()
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
                            GeometricWeather.getInstance().isFahrenheit()
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
                            GeometricWeather.getInstance().isFahrenheit()
                    ) + (
                            weather.hourlyList.get(position).precipitation >= 0
                                    ? "\n" + getString(R.string.precipitation) + " : " + weather.hourlyList.get(position).precipitation + "%"
                                    : ""
                    );
            weatherTexts[0].setText(text);
        }
        
        TextView[] sunMoonText = new TextView[] {
                view.findViewById(R.id.dialog_weather_sunrise_sunset),
                view.findViewById(R.id.dialog_weather_moonrise_moonset)
        };
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

        if (daily) {
            this.iconAnimators = new Animator[2][3];
            iconAnimators[0] = WeatherHelper.getWeatherAnimators(
                    provider,
                    weather.dailyList.get(position).weatherKinds[0],
                    true
            );
            iconAnimators[1] = WeatherHelper.getWeatherAnimators(
                    provider,
                    weather.dailyList.get(position).weatherKinds[1],
                    false
            );
            for (int i = 0; i < iconAnimators[0].length; i ++) {
                if (iconAnimators[0][i] != null) {
                    iconAnimators[0][i].setTarget(weatherIcons[0][i]);
                }
                if (iconAnimators[1][i] != null) {
                    iconAnimators[1][i].setTarget(weatherIcons[1][i]);
                }
            }
        } else {
            this.iconAnimators = new Animator[2][3];
            iconAnimators[0] = WeatherHelper.getWeatherAnimators(
                    provider,
                    weather.hourlyList.get(position).weatherKind,
                    weather.hourlyList.get(position).dayTime
            );
            for (int i = 0; i < iconAnimators[0].length; i ++) {
                if (iconAnimators[0][i] != null) {
                    iconAnimators[0][i].setTarget(weatherIcons[0][i]);
                }
            }
        }
    }

    public void setData(Weather weather, int position, boolean daily) {
        this.weather = weather;
        this.position = position;
        this.daily = daily;
    }

    private void resetImageView(@NonNull ImageView view) {
        view.setAlpha(1f);
        view.setRotation(0f);
        view.setTranslationX(0f);
        view.setTranslationY(0f);
        view.setScaleX(1f);
        view.setScaleY(1f);
    }

    // interface.

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_weather_weatherContainer_day:
                for (Animator a : iconAnimators[0]) {
                    if (a != null) {
                        a.start();
                    }
                }
                break;

            case R.id.dialog_weather_weatherContainer_night:
                for (Animator a : iconAnimators[1]) {
                    if (a != null) {
                        a.start();
                    }
                }
                break;
        }
    }
}
