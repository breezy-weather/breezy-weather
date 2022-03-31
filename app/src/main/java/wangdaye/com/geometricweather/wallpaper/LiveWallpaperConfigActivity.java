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
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Arrays;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.snackbar.SnackbarContainer;

public class LiveWallpaperConfigActivity extends GeoActivity {

    protected String mWeatherKindValueNow;
    protected String[] mWeatherKinds;
    protected String[] mWeatherKindValues;

    protected String mDayNightTypeValueNow;
    protected String[] mDayNightTypes;
    protected String[] mDayNightTypeValues;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_wallpaper_config);
        initData();
        initView();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // do nothing.
    }

    @Override
    public SnackbarContainer getSnackbarContainer() {
        return new SnackbarContainer(this,
                findViewById(R.id.activity_live_wallpaper_config_container), true);
    }

    public void initData() {
        LiveWallpaperConfigManager configManager = LiveWallpaperConfigManager.getInstance(this);
        Resources res = getResources();

        mWeatherKindValueNow = configManager.getWeatherKind();
        mWeatherKinds = res.getStringArray(R.array.live_wallpaper_weather_kinds);
        mWeatherKindValues = res.getStringArray(R.array.live_wallpaper_weather_kind_values);

        mDayNightTypeValueNow = configManager.getDayNightType();
        mDayNightTypes = res.getStringArray(R.array.live_wallpaper_day_night_types);
        mDayNightTypeValues = res.getStringArray(R.array.live_wallpaper_day_night_type_values);
    }

    public void initView() {
        MaterialToolbar toolbar = findViewById(R.id.activity_live_wallpaper_config_toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());

        AppCompatSpinner weatherKindSpinner = findViewById(R.id.activity_live_wallpaper_config_weatherKindSpinner);
        weatherKindSpinner.setOnItemSelectedListener(new WeatherKindSpinnerSelectedListener());
        weatherKindSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mWeatherKinds)
        );
        weatherKindSpinner.setSelection(Arrays.binarySearch(mWeatherKindValues, mWeatherKindValueNow));

        AppCompatSpinner dayNightTypeSpinner = findViewById(R.id.activity_live_wallpaper_config_dayNightTypeSpinner);
        dayNightTypeSpinner.setOnItemSelectedListener(new DayNightTypeSpinnerSelectedListener());
        dayNightTypeSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mDayNightTypes)
        );
        dayNightTypeSpinner.setSelection(Arrays.binarySearch(mDayNightTypeValues, mDayNightTypeValueNow));

        Button doneButton = findViewById(R.id.activity_live_wallpaper_config_doneButton);
        doneButton.setOnClickListener(v -> {
            LiveWallpaperConfigManager.update(
                    this, mWeatherKindValueNow, mDayNightTypeValueNow);
            finish();
        });
    }

    // interface.

    // on item selected listener.

    private class WeatherKindSpinnerSelectedListener implements AppCompatSpinner.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            mWeatherKindValueNow = mWeatherKindValues[i];
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // do nothing.
        }
    }

    private class DayNightTypeSpinnerSelectedListener implements AppCompatSpinner.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            mDayNightTypeValueNow = mDayNightTypeValues[i];
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // do nothing.
        }
    }
}
