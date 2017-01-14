package wangdaye.com.geometricweather.view.widget.weatherView.details;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
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
    // widget
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
/*
    private RelativeLayout exercise;
    private TextView exerciseTitle;
    private TextView exerciseContent;

    private RelativeLayout cold;
    private TextView coldTitle;
    private TextView coldContent;

    private RelativeLayout washCar;
    private TextView washCarTitle;
    private TextView washCarContent;
*/
    /** <br> life cycle. */

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public IndexListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize();
    }

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
/*
        exercise = (RelativeLayout) findViewById(R.id.container_details_exercise);
        exerciseTitle = (TextView) findViewById(R.id.container_details_exercise_title);
        exerciseContent = (TextView) findViewById(R.id.container_details_exercise_content);

        washCar = (RelativeLayout) findViewById(R.id.container_details_wash_car);
        washCarTitle = (TextView) findViewById(R.id.container_details_wash_car_title);
        washCarContent = (TextView) findViewById(R.id.container_details_wash_car_content);

        cold = (RelativeLayout) findViewById(R.id.container_details_cold);
        coldTitle = (TextView) findViewById(R.id.container_details_cold_title);
        coldContent = (TextView) findViewById(R.id.container_details_cold_content);
*/
    }

    /** <br> data. */

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
/*
        if (TextUtils.isEmpty(weather.index.exercises[1])) {
            exercise.setVisibility(GONE);
        } else {
            exercise.setVisibility(VISIBLE);
            exerciseTitle.setText(weather.index.exercises[0]);
            exerciseContent.setText(weather.index.exercises[1]);
        }

        if (TextUtils.isEmpty(weather.index.colds[1])) {
            cold.setVisibility(GONE);
        } else {
            cold.setVisibility(VISIBLE);
            coldTitle.setText(weather.index.colds[0]);
            coldContent.setText(weather.index.colds[1]);
        }

        if (TextUtils.isEmpty(weather.index.carWashes[1])) {
            washCar.setVisibility(GONE);
        } else {
            washCar.setVisibility(VISIBLE);
            washCarTitle.setText(weather.index.carWashes[0]);
            washCarContent.setText(weather.index.carWashes[1]);
        }
*/
    }
}
