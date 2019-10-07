package wangdaye.com.geometricweather.main.ui.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.annotation.NonNull;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;

public class HeaderController extends AbstractMainItemController
        implements View.OnClickListener {

    private RelativeLayout container;
    private TextView temperature;
    private TextView weather;
    private TextView sensibleTemp;
    private TextView aqiOrWind;

    private WeatherView weatherView;

    public HeaderController(@NonNull Activity activity, @NonNull WeatherView weatherView,
                            @NonNull ResourceProvider provider, @NonNull MainColorPicker picker) {
        super(activity, activity.findViewById(R.id.container_main_header), provider, picker);

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

        if (location.getWeather() != null) {
            TemperatureUnit temperatureUnit = SettingsOptionManager.getInstance(context).getTemperatureUnit();
            temperature.setText(
                    location.getWeather().getCurrent().getTemperature().getShortTemperature(temperatureUnit));
            weather.setText(location.getWeather().getCurrent().getWeatherText());
            sensibleTemp.setText(
                    context.getString(R.string.feels_like)
                            + " "
                            + location.getWeather().getCurrent().getTemperature().getShortRealFeeTemperature(temperatureUnit)
            );

            if (location.getWeather().getCurrent().getAirQuality().getAqiText() == null) {
                aqiOrWind.setText(location.getWeather().getCurrent().getWind().getLevel());
            } else {
                aqiOrWind.setText(location.getWeather().getCurrent().getAirQuality().getAqiText());
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
