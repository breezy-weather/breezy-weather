package wangdaye.com.geometricweather.daily;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

/**
 * Daily weather activity.
 * */

public class DailyWeatherActivity extends GeoActivity {

    private CoordinatorLayout container;

    private @Nullable Weather weather;
    private int position;

    public static final String KEY_FORMATTED_LOCATION_ID = "FORMATTED_LOCATION_ID";
    public static final String KEY_CURRENT_DAILY_INDEX = "CURRENT_DAILY_INDEX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_daily);
        initData();
        initWidget();
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    private void initData() {
        Location location;
        String formattedId = getIntent().getStringExtra(KEY_FORMATTED_LOCATION_ID);
        if (TextUtils.isEmpty(formattedId)) {
            location = DatabaseHelper.getInstance(this).readLocationList().get(0);
        } else {
            location = DatabaseHelper.getInstance(this).readLocation(formattedId);
        }

        if (location != null) {
            weather = DatabaseHelper.getInstance(this).readWeather(location);
        }
        position = getIntent().getIntExtra(KEY_CURRENT_DAILY_INDEX, 0);
    }

    private void initWidget() {
        if (weather == null) {
            finish();
        }

        Daily daily = weather.getDailyForecast().get(position);

        container = findViewById(R.id.activity_weather_daily_container);

        Toolbar toolbar = findViewById(R.id.activity_weather_daily_toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView title = findViewById(R.id.activity_weather_daily_title);
        title.setText(daily.getDate(getString(R.string.date_format_widget_long)));

        TextView subtitle = findViewById(R.id.activity_weather_daily_subtitle);
        if (SettingsOptionManager.getInstance(this).getLanguage().getCode().startsWith("zh")) {
            subtitle.setText(daily.getLunar());
        } else {
            subtitle.setVisibility(View.GONE);
        }

        RecyclerView recyclerView = findViewById(R.id.activity_weather_daily_recyclerView);

        DailyWeatherAdapter dailyWeatherAdapter = new DailyWeatherAdapter(this, daily, 3);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        gridLayoutManager.setSpanSizeLookup(dailyWeatherAdapter.spanSizeLookup);
        recyclerView.setAdapter(dailyWeatherAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
    }
}
