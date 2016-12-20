package wangdaye.com.geometricweather.view.dialog;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialogFragment;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Weather dialog.
 * */

public class WeatherDialog extends GeoDialogFragment
        implements View.OnClickListener {
    // widget
    private CoordinatorLayout container;

    // animator
    private AnimatorSet[] iconAnimatorSets;

    // data
    private Weather weather;
    private int position;
    private boolean daily;

    /** <br> life cycle. */

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

    /** <br> UI. */

    @SuppressLint("SetTextI18n")
    private void initWidget(View view) {
        boolean dayTime = TimeUtils.getInstance(getActivity()).getDayTime(getActivity(), weather, false).isDayTime();

        this.container = (CoordinatorLayout) view.findViewById(R.id.dialog_weather_container);

        view.findViewById(R.id.dialog_weather_container).setOnClickListener(this);

        ImageView[] weatherIcons = new ImageView[3];
        weatherIcons[0] = (ImageView) view.findViewById(R.id.dialog_weather_icon_1);
        weatherIcons[1] = (ImageView) view.findViewById(R.id.dialog_weather_icon_2);
        weatherIcons[2] = (ImageView) view.findViewById(R.id.dialog_weather_icon_3);
        int[] imageIds = WeatherHelper.getWeatherIcon(
                daily ? weather.dailyList.get(position).weatherKinds[0] : weather.hourlyList.get(position).weatherKind,
                TimeUtils.getInstance(getActivity()).isDayTime());
        for (int i = 0; i < weatherIcons.length; i ++) {
            if (imageIds[i] != 0) {
                weatherIcons[i].setImageResource(imageIds[i]);
                weatherIcons[i].setVisibility(View.VISIBLE);
            } else {
                weatherIcons[i].setVisibility(View.GONE);
            }
        }

        TextView weatherText = (TextView) view.findViewById(R.id.dialog_weather_text);
        if (daily) {
            String text = weather.dailyList.get(position).weathers[0]
                    + "  " + weather.dailyList.get(position).temps[1] + "/" + weather.dailyList.get(position).temps[0] + "°"
                    + "\n" + weather.dailyList.get(position).windDir + weather.dailyList.get(position).windLevel;
            weatherText.setText(text);
        } else {
            if (weather.base.cityId.matches("^CN[0-9]*$")) {
                String text = weather.hourlyList.get(position).weather + "  " + weather.hourlyList.get(position).temp + "°"
                        + "\n" + getString(R.string.precipitation) + "  " + weather.hourlyList.get(position).precipitation + "mm";
                weatherText.setText(text);
            } else {
                String text = weather.hourlyList.get(position).weather + "  " + weather.hourlyList.get(position).temp + "°"
                        + "\n" + getString(R.string.precipitation) + "  " + weather.hourlyList.get(position).precipitation + "%";
                weatherText.setText(text);
            }
        }
        
        TextView[] sunText = new TextView[] {
                (TextView) view.findViewById(R.id.dialog_weather_sunrise),
                (TextView) view.findViewById(R.id.dialog_weather_sunset)};
        if (daily) {
            sunText[0].setText(weather.dailyList.get(position).astros[0]);
            sunText[1].setText(weather.dailyList.get(position).astros[1]);
        } else {
            view.findViewById(R.id.dialog_weather_sunriseIcon).setVisibility(View.GONE);
            view.findViewById(R.id.dialog_weather_sunset_icon).setVisibility(View.GONE);
            sunText[0].setText(weather.hourlyList.get(position).time.replace("时", "").replace("h", "") + ":00");
            sunText[1].setVisibility(View.GONE);
        }

        Button done = (Button) view.findViewById(R.id.dialog_weather_button);
        if (dayTime) {
            done.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_3));
        } else {
            done.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_1));
        }
        done.setOnClickListener(this);

        int[] animatorIds = WeatherHelper.getAnimatorId(
                daily ? weather.dailyList.get(position).weatherKinds[0] : weather.hourlyList.get(position).weatherKind,
                TimeUtils.getInstance(getActivity()).isDayTime());
        this.iconAnimatorSets = new AnimatorSet[3];
        for (int i = 0; i < iconAnimatorSets.length; i ++) {
            if (animatorIds[i] != 0) {
                iconAnimatorSets[i] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), animatorIds[i]);
                iconAnimatorSets[i].setTarget(weatherIcons[i]);
            } else {
                iconAnimatorSets[i] = null;
            }
        }
    }

    /** <br> data. */

    public void setData(Weather weather, int position, boolean daily) {
        this.weather = weather;
        this.position = position;
        this.daily = daily;
    }

    /** <br> interface. */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_weather_button:
                dismiss();
                break;

            case R.id.dialog_weather_container:
                for (AnimatorSet a : iconAnimatorSets) {
                    if (a != null) {
                        a.start();
                    }
                }
                break;
        }
    }
}
