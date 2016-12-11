package wangdaye.com.geometricweather.view.widget.weatherView;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Weather;

/**
 * Life info view.
 * */

public class LifeInfoView extends FrameLayout {
    // widget
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

    private RelativeLayout dress;
    private TextView dressTitle;
    private TextView dressContent;

    private RelativeLayout cold;
    private TextView coldTitle;
    private TextView coldContent;

    private RelativeLayout aqi;
    private TextView aqiTitle;
    private TextView aqiContent;

    private RelativeLayout washCar;
    private TextView washCarTitle;
    private TextView washCarContent;

    private RelativeLayout exercise;
    private TextView exerciseTitle;
    private TextView exerciseContent;

    /** <br> life cycle. */

    public LifeInfoView(Context context) {
        super(context);
        this.initialize();
    }

    public LifeInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public LifeInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LifeInfoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize();
    }

    @SuppressLint("InflateParams")
    private void initialize() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.container_life_info, null);
        addView(view);

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

        dress = (RelativeLayout) findViewById(R.id.container_details_dress);
        dressTitle = (TextView) findViewById(R.id.container_details_dress_title);
        dressContent = (TextView) findViewById(R.id.container_details_dress_content);

        cold = (RelativeLayout) findViewById(R.id.container_details_cold);
        coldTitle = (TextView) findViewById(R.id.container_details_cold_title);
        coldContent = (TextView) findViewById(R.id.container_details_cold_content);

        aqi = (RelativeLayout) findViewById(R.id.container_details_aqi);
        aqiTitle = (TextView) findViewById(R.id.container_details_aqi_title);
        aqiContent = (TextView) findViewById(R.id.container_details_aqi_content);

        washCar = (RelativeLayout) findViewById(R.id.container_details_wash_car);
        washCarTitle = (TextView) findViewById(R.id.container_details_wash_car_title);
        washCarContent = (TextView) findViewById(R.id.container_details_wash_car_content);

        exercise = (RelativeLayout) findViewById(R.id.container_details_exercise);
        exerciseTitle = (TextView) findViewById(R.id.container_details_exercise_title);
        exerciseContent = (TextView) findViewById(R.id.container_details_exercise_content);
    }

    /** <br> data. */

    public void setData(Weather weather) {
        if (TextUtils.isEmpty(weather.life.winds[1])) {
            wind.setVisibility(GONE);
        } else {
            wind.setVisibility(VISIBLE);
            windTitle.setText(weather.life.winds[0]);
            windContent.setText(weather.life.winds[1]);
        }

        if (TextUtils.isEmpty(weather.life.pms[1])) {
            pm.setVisibility(GONE);
        } else {
            pm.setVisibility(VISIBLE);
            pmTitle.setText(weather.life.pms[0]);
            pmContent.setText(weather.life.pms[1]);
        }

        if (TextUtils.isEmpty(weather.life.hums[1])) {
            humidity.setVisibility(GONE);
        } else {
            humidity.setVisibility(VISIBLE);
            humidityTitle.setText(weather.life.hums[0]);
            humidityContent.setText(weather.life.hums[1]);
        }

        if (TextUtils.isEmpty(weather.life.uvs[1])) {
            uv.setVisibility(GONE);
        } else {
            uv.setVisibility(VISIBLE);
            uvTitle.setText(weather.life.uvs[0]);
            uvContent.setText(weather.life.uvs[1]);
        }

        if (TextUtils.isEmpty(weather.life.dresses[1])) {
            dress.setVisibility(GONE);
        } else {
            dress.setVisibility(VISIBLE);
            dressTitle.setText(weather.life.dresses[0]);
            dressContent.setText(weather.life.dresses[1]);
        }

        if (TextUtils.isEmpty(weather.life.colds[1])) {
            cold.setVisibility(GONE);
        } else {
            cold.setVisibility(VISIBLE);
            coldTitle.setText(weather.life.colds[0]);
            coldContent.setText(weather.life.colds[1]);
        }

        if (TextUtils.isEmpty(weather.life.airs[1])) {
            aqi.setVisibility(GONE);
        } else {
            aqi.setVisibility(VISIBLE);
            aqiTitle.setText(weather.life.airs[0]);
            aqiContent.setText(weather.life.airs[1]);
        }

        if (TextUtils.isEmpty(weather.life.washCars[1])) {
            washCar.setVisibility(GONE);
        } else {
            washCar.setVisibility(VISIBLE);
            washCarTitle.setText(weather.life.washCars[0]);
            washCarContent.setText(weather.life.washCars[1]);
        }

        if (TextUtils.isEmpty(weather.life.sports[1])) {
            exercise.setVisibility(GONE);
        } else {
            exercise.setVisibility(VISIBLE);
            exerciseTitle.setText(weather.life.sports[0]);
            exerciseContent.setText(weather.life.sports[1]);
        }
    }
}
