package wangdaye.com.geometricweather.wallpaper;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import java.util.Arrays;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;

public class LiveWallpaperConfigActivity extends GeoActivity {
    
    protected CoordinatorLayout container;

    protected String weatherKindValueNow;
    protected String[] weatherKinds;
    protected String[] weatherKindValues;

    protected String dayNightTypeValueNow;
    protected String[] dayNightTypes;
    protected String[] dayNightTypeValues;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_wallpaper_config);
        initData();
        initView();
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // do nothing.
    }

    public void initData() {
        LiveWallpaperConfigManager configManager = LiveWallpaperConfigManager.getInstance(this);
        Resources res = getResources();

        this.weatherKindValueNow = configManager.getWeatherKind();
        this.weatherKinds = res.getStringArray(R.array.live_wallpaper_weather_kinds);
        this.weatherKindValues = res.getStringArray(R.array.live_wallpaper_weather_kind_values);

        this.dayNightTypeValueNow = configManager.getDayNightType();
        this.dayNightTypes = res.getStringArray(R.array.live_wallpaper_day_night_types);
        this.dayNightTypeValues = res.getStringArray(R.array.live_wallpaper_day_night_type_values);
    }

    public void initView() {
        Toolbar toolbar = findViewById(R.id.activity_live_wallpaper_config_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(view -> finish());

        this.container = findViewById(R.id.activity_live_wallpaper_config_container);
        
        AppCompatSpinner weatherKindSpinner = findViewById(R.id.activity_live_wallpaper_config_weatherKindSpinner);
        weatherKindSpinner.setOnItemSelectedListener(new WeatherKindSpinnerSelectedListener());
        weatherKindSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, weatherKinds)
        );
        weatherKindSpinner.setSelection(Arrays.binarySearch(weatherKindValues, weatherKindValueNow));

        AppCompatSpinner dayNightTypeSpinner = findViewById(R.id.activity_live_wallpaper_config_dayNightTypeSpinner);
        dayNightTypeSpinner.setOnItemSelectedListener(new DayNightTypeSpinnerSelectedListener());
        dayNightTypeSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dayNightTypes)
        );
        dayNightTypeSpinner.setSelection(Arrays.binarySearch(dayNightTypeValues, dayNightTypeValueNow));

        Button doneButton = findViewById(R.id.activity_live_wallpaper_config_doneButton);
        doneButton.setOnClickListener(v -> {
            LiveWallpaperConfigManager.update(
                    this, weatherKindValueNow, dayNightTypeValueNow);
            finish();
        });
    }

    // interface.

    // on item selected listener.

    private class WeatherKindSpinnerSelectedListener implements AppCompatSpinner.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            weatherKindValueNow = weatherKindValues[i];
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // do nothing.
        }
    }

    private class DayNightTypeSpinnerSelectedListener implements AppCompatSpinner.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            dayNightTypeValueNow = dayNightTypeValues[i];
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // do nothing.
        }
    }
}
