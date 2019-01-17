package wangdaye.com.geometricweather.ui.activity.main.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.utils.ValueUtils;

public class HeaderController extends AbstractMainItemController
        implements View.OnClickListener {

    private RelativeLayout container;
    private TextView temperature;
    private TextView weather;
    private TextView sensibleTemp;
    private TextView aqiOrWind;

    private WeatherView weatherView;

    public HeaderController(@NonNull Activity activity, @NonNull WeatherView weatherView) {
        super(activity, activity.findViewById(R.id.container_main_header));

        this.container = view.findViewById(R.id.container_main_header);
        this.temperature = view.findViewById(R.id.container_main_header_tempTxt);
        this.weather = view.findViewById(R.id.container_main_header_weatherTxt);
        this.sensibleTemp = view.findViewById(R.id.container_main_header_sensibleTempTxt);
        this.aqiOrWind = view.findViewById(R.id.container_main_header_aqiOrWindTxt);
        
        this.weatherView = weatherView;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(@NonNull Location location) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) container.getLayoutParams();
        params.height = weatherView.getFirstCardMarginTop();
        container.setLayoutParams(params);
        container.setOnClickListener(this);

        if (location.weather != null) {
            temperature.setText(
                    ValueUtils.buildAbbreviatedCurrentTemp(
                            location.weather.realTime.temp,
                            GeometricWeather.getInstance().isFahrenheit()));
            weather.setText(location.weather.realTime.weather);
            sensibleTemp.setText(
                    context.getString(R.string.feels_like) + " "
                            + ValueUtils.buildAbbreviatedCurrentTemp(
                            location.weather.realTime.sensibleTemp, GeometricWeather.getInstance().isFahrenheit()));

            if (location.weather.aqi == null) {
                aqiOrWind.setText(location.weather.realTime.windLevel);
            } else {
                aqiOrWind.setText(location.weather.aqi.quality);
            }
        }
    }

    public int getCurrentTemperatureHeight() {
        return temperature.getMeasuredHeight();
    }

    // interface.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.container_main_header:
                weatherView.onClick();
                break;
        }
    }
}
