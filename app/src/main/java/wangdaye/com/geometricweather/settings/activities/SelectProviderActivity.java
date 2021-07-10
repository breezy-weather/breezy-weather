package wangdaye.com.geometricweather.settings.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

import org.jetbrains.annotations.NotNull;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.settings.fragments.ServiceProviderSettingsFragment;

/**
 * Select provider activity.
 * */

public class SelectProviderActivity extends GeoActivity {

    public static final String KEY_LOCATION = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_settings);

        initToolbar();

        ServiceProviderSettingsFragment f = new ServiceProviderSettingsFragment();
        f.setOnWeatherSourceChangedListener(location ->
                setResult(RESULT_OK, new Intent().putExtra(KEY_LOCATION, location))
        );
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_settings_container, f)
                .commit();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        // do nothing.
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.activity_settings_toolbar);
        toolbar.setTitle(getString(R.string.settings_title_service_provider));
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    public void pushFragment(PreferenceFragmentCompat f, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, 0, android.R.anim.fade_in, 0)
                .replace(R.id.activity_settings_container, f)
                .addToBackStack(tag)
                .commit();
    }
}