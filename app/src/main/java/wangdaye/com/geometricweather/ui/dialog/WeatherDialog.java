package wangdaye.com.geometricweather.ui.dialog;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialogFragment;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.utils.helpter.LunarHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Weather dialog.
 * */

public class WeatherDialog extends GeoDialogFragment
        implements View.OnClickListener {

    private CoordinatorLayout container;

    private AnimatorSet[][] iconAnimatorSets;

    private Weather weather;
    private int position;
    private boolean daily;

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_weather, null, false);
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
            String dates[] = weather.dailyList.get(position).date.split("-");
            subtitle.setText(LunarHelper.getLunarDate(dates));
        } else {
            subtitle.setVisibility(View.GONE);
        }

        view.findViewById(R.id.dialog_weather_weatherContainer_day).setOnClickListener(this);
        view.findViewById(R.id.dialog_weather_weatherContainer_night).setOnClickListener(this);

        ImageView[][] weatherIcons = new ImageView[2][3];
        weatherIcons[0][0] = view.findViewById(R.id.dialog_weather_icon_1_day);
        weatherIcons[0][1] = view.findViewById(R.id.dialog_weather_icon_2_day);
        weatherIcons[0][2] = view.findViewById(R.id.dialog_weather_icon_3_day);
        weatherIcons[1][0] = view.findViewById(R.id.dialog_weather_icon_1_night);
        weatherIcons[1][1] = view.findViewById(R.id.dialog_weather_icon_2_night);
        weatherIcons[1][2] = view.findViewById(R.id.dialog_weather_icon_3_night);
        if (daily) {
            int[] daytimeImageIds = WeatherHelper.getWeatherIcon(
                    weather.dailyList.get(position).weatherKinds[0], true);
            for (int i = 0; i < weatherIcons[0].length; i ++) {
                if (daytimeImageIds[i] != 0) {
                    weatherIcons[0][i].setImageResource(daytimeImageIds[i]);
                    weatherIcons[0][i].setVisibility(View.VISIBLE);
                } else {
                    weatherIcons[0][i].setVisibility(View.GONE);
                }
            }
            int[] nighttimeImageIds = WeatherHelper.getWeatherIcon(
                    weather.dailyList.get(position).weatherKinds[1], false);
            for (int i = 0; i < weatherIcons[0].length; i ++) {
                if (nighttimeImageIds[i] != 0) {
                    weatherIcons[1][i].setImageResource(nighttimeImageIds[i]);
                    weatherIcons[1][i].setVisibility(View.VISIBLE);
                } else {
                    weatherIcons[1][i].setVisibility(View.GONE);
                }
            }
        } else {
            int[] imageIds = WeatherHelper.getWeatherIcon(
                    weather.hourlyList.get(position).weatherKind,
                    weather.hourlyList.get(position).dayTime);
            for (int i = 0; i < weatherIcons[0].length; i ++) {
                if (imageIds[i] != 0) {
                    weatherIcons[0][i].setImageResource(imageIds[i]);
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
                    + ValueUtils.buildCurrentTemp(weather.dailyList.get(position).temps[0], false, GeometricWeather.getInstance().isFahrenheit()) + "\n"
                    + getString(R.string.wind) + " : " + weather.dailyList.get(position).windDirs[0]
                    + (TextUtils.isEmpty(weather.dailyList.get(position).windSpeeds[0]) ? "" : " " + weather.dailyList.get(position).windSpeeds[0])
                    + " (" + weather.dailyList.get(position).windLevels[0] + ") "
                    + WeatherHelper.getWindArrows(weather.dailyList.get(position).windDegrees[0])
                    + (weather.dailyList.get(position).precipitations[0] >= 0 ? "\n" + getString(R.string.precipitation) + " : " + weather.dailyList.get(position).precipitations[0] + "%" : "");
            weatherTexts[0].setText(daytimeTxt);

            String nighttimeTxt = weather.dailyList.get(position).weathers[1] + "  "
                    + ValueUtils.buildCurrentTemp(weather.dailyList.get(position).temps[1], false, GeometricWeather.getInstance().isFahrenheit()) + "\n"
                    + getString(R.string.wind) + " : " + weather.dailyList.get(position).windDirs[1]
                    + (TextUtils.isEmpty(weather.dailyList.get(position).windSpeeds[1]) ? "" : " " + weather.dailyList.get(position).windSpeeds[1])
                    + " (" + weather.dailyList.get(position).windLevels[1] + ") "
                    + WeatherHelper.getWindArrows(weather.dailyList.get(position).windDegrees[1])
                    + (weather.dailyList.get(position).precipitations[1] >= 0 ? "\n" + getString(R.string.precipitation) + " : " + weather.dailyList.get(position).precipitations[1] + "%" : "");
            weatherTexts[1].setText(nighttimeTxt);
        } else {
            String text = weather.hourlyList.get(position).weather + "  "
                    + ValueUtils.buildCurrentTemp(weather.hourlyList.get(position).temp, false, GeometricWeather.getInstance().isFahrenheit())
                    + (weather.hourlyList.get(position).precipitation >= 0 ? "\n" + getString(R.string.precipitation) + " : " + weather.hourlyList.get(position).precipitation + "%" : "");
            weatherTexts[0].setText(text);
        }
        
        TextView[] sunText = new TextView[] {
                view.findViewById(R.id.dialog_weather_sunrise),
                view.findViewById(R.id.dialog_weather_sunset)};
        if (daily) {
            sunText[0].setText(weather.dailyList.get(position).astros[0]);
            sunText[1].setText(weather.dailyList.get(position).astros[1]);
        } else {
            view.findViewById(R.id.dialog_weather_sunriseIcon).setVisibility(View.GONE);
            view.findViewById(R.id.dialog_weather_sunset_icon).setVisibility(View.GONE);
            sunText[0].setVisibility(View.GONE);
            sunText[1].setVisibility(View.GONE);
        }

        Button done = view.findViewById(R.id.dialog_weather_button);
        boolean dayTime = daily
                ? TimeManager.getInstance(getActivity()).isDayTime() : weather.hourlyList.get(position).dayTime;
        if (dayTime) {
            done.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_3));
        } else {
            done.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_1));
        }
        done.setOnClickListener(this);

        if (daily) {
            int[] daytimeAnimatorIds = WeatherHelper.getAnimatorId(
                    weather.dailyList.get(position).weatherKinds[0], true);
            int[] nighttimeAnimatorIds = WeatherHelper.getAnimatorId(
                    weather.dailyList.get(position).weatherKinds[1], false);
            this.iconAnimatorSets = new AnimatorSet[2][3];
            for (int i = 0; i < iconAnimatorSets[0].length; i ++) {
                if (daytimeAnimatorIds[i] != 0) {
                    iconAnimatorSets[0][i] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), daytimeAnimatorIds[i]);
                    iconAnimatorSets[0][i].setTarget(weatherIcons[0][i]);
                } else {
                    iconAnimatorSets[0][i] = null;
                }
                if (nighttimeAnimatorIds[i] != 0) {
                    iconAnimatorSets[1][i] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), nighttimeAnimatorIds[i]);
                    iconAnimatorSets[1][i].setTarget(weatherIcons[1][i]);
                } else {
                    iconAnimatorSets[1][i] = null;
                }
            }
        } else {
            int[] animatorIds = WeatherHelper.getAnimatorId(
                    weather.hourlyList.get(position).weatherKind, weather.hourlyList.get(position).dayTime);
            this.iconAnimatorSets = new AnimatorSet[2][3];
            for (int i = 0; i < iconAnimatorSets[0].length; i ++) {
                if (animatorIds[i] != 0) {
                    iconAnimatorSets[0][i] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), animatorIds[i]);
                    iconAnimatorSets[0][i].setTarget(weatherIcons[0][i]);
                } else {
                    iconAnimatorSets[0][i] = null;
                }
            }
        }
    }

    public void setData(Weather weather, int position, boolean daily) {
        this.weather = weather;
        this.position = position;
        this.daily = daily;
    }

    // interface.

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_weather_button:
                dismiss();
                break;

            case R.id.dialog_weather_weatherContainer_day:
                for (AnimatorSet a : iconAnimatorSets[0]) {
                    if (a != null) {
                        a.start();
                    }
                }
                break;

            case R.id.dialog_weather_weatherContainer_night:
                for (AnimatorSet a : iconAnimatorSets[1]) {
                    if (a != null) {
                        a.start();
                    }
                }
                break;
        }
    }
}
