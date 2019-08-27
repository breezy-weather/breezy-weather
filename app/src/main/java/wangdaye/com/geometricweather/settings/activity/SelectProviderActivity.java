package wangdaye.com.geometricweather.settings.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.settings.fragment.ServiceProviderSettingsFragment;

/**
 * Select provider activity.
 * */

public class SelectProviderActivity extends GeoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_settings);
        initToolbar();
        ServiceProviderSettingsFragment f = new ServiceProviderSettingsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_settings_container, f)
                .commit();
    }

    @Override
    public View getSnackbarContainer() {
        return findViewById(R.id.activity_settings_container);
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
}