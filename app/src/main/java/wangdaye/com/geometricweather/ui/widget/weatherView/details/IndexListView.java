package wangdaye.com.geometricweather.ui.widget.weatherView.details;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.ValueUtils;

/**
 * Index list view.
 * */

public class IndexListView extends FrameLayout {

    private RelativeLayout forecast;
    private TextView forecastTitle;
    private TextView forecastContent;

    private RelativeLayout briefing;
    private TextView briefingTitle;
    private TextView briefingContent;
    
    private RelativeLayout wind;
    private TextView windTitle;
    private TextView windContent;

    private RelativeLayout pm;
    private TextView pmTitle;
    private TextView pmContent;

    private RelativeLayout humidity;
    private TextView humidityTitle;
    private TextView humidityContent;

    private RelativeLayout uv;
    private TextView uvTitle;
    private TextView uvContent;

    public IndexListView(Context context) {
        super(context);
        this.initialize();
    }

    public IndexListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public IndexListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    // init.

    @SuppressLint("InflateParams")
    private void initialize() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.container_index, null);
        addView(view);

        forecast = (RelativeLayout) findViewById(R.id.container_details_forecast);
        forecastTitle = (TextView) findViewById(R.id.container_details_forecast_title);
        forecastContent = (TextView) findViewById(R.id.container_details_forecast_content);

        briefing = (RelativeLayout) findViewById(R.id.container_details_briefing);
        briefingTitle = (TextView) findViewById(R.id.container_details_briefing_title);
        briefingContent = (TextView) findViewById(R.id.container_details_briefing_content);
        
        wind = (RelativeLayout) findViewById(R.id.container_details_wind);
        windTitle = (TextView) findViewById(R.id.container_details_wind_title);
        windContent = (TextView) findViewById(R.id.container_details_wind_content);

        pm = (RelativeLayout) findViewById(R.id.container_details_pm);
        pmTitle = (TextView) findViewById(R.id.container_details_pm_title);
        pmContent = (TextView) findViewById(R.id.container_details_pm_content);

        humidity = (RelativeLayout) findViewById(R.id.container_details_humidity);
        humidityTitle = (TextView) findViewById(R.id.container_details_humidity_title);
        humidityContent = (TextView) findViewById(R.id.container_details_humidity_content);

        uv = (RelativeLayout) findViewById(R.id.container_details_uv);
        uvTitle = (TextView) findViewById(R.id.container_details_uv_title);
        uvContent = (TextView) findViewById(R.id.container_details_uv_content);

    }

    public void setData(Weather weather) {
        if (TextUtils.isEmpty(weather.index.simpleForecasts[1])) {
            forecast.setVisibility(GONE);
        } else {
            forecast.setVisibility(VISIBLE);
            forecastTitle.setText(weather.index.simpleForecasts[0]);
            forecastContent.setText(weather.index.simpleForecasts[1]);
        }

        if (TextUtils.isEmpty(weather.index.briefings[1])) {
            briefing.setVisibility(GONE);
        } else {
            briefing.setVisibility(VISIBLE);
            briefingTitle.setText(weather.index.briefings[0]);
            briefingContent.setText(weather.index.briefings[1]);
        }

        if (TextUtils.isEmpty(weather.index.winds[1])) {
            wind.setVisibility(GONE);
        } else {
            wind.setVisibility(VISIBLE);
            windTitle.setText(weather.index.winds[0]);
            windContent.setText(weather.index.winds[1]);
        }

        if (TextUtils.isEmpty(weather.index.aqis[1])) {
            pm.setVisibility(GONE);
        } else {
            pm.setVisibility(VISIBLE);
            pmTitle.setText(weather.index.aqis[0]);
            pmContent.setText(weather.index.aqis[1]);
        }

        if (TextUtils.isEmpty(weather.index.humidities[1])) {
            humidity.setVisibility(GONE);
        } else {
            humidity.setVisibility(VISIBLE);
            humidityTitle.setText(getContext().getString(R.string.sensible_temp) + " : "
                    + ValueUtils.buildCurrentTemp(
                    weather.realTime.sensibleTemp,
                    false,
                    GeometricWeather.getInstance().isFahrenheit()));
            humidityContent.setText(weather.index.humidities[1]);
        }

        if (TextUtils.isEmpty(weather.index.uvs[1])) {
            uv.setVisibility(GONE);
        } else {
            uv.setVisibility(VISIBLE);
            uvTitle.setText(weather.index.uvs[0]);
            uvContent.setText(weather.index.uvs[1]);
        }
    }
}
