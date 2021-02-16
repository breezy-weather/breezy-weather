package wangdaye.com.geometricweather.settings.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

import org.jetbrains.annotations.NotNull;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.settings.fragments.SettingsFragment;
import wangdaye.com.geometricweather.utils.helpters.IntentHelper;

/**
 * Settings activity.
 * */

public class SettingsActivity extends GeoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.activity_settings_toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.inflateMenu(R.menu.activity_settings);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_about) {
                IntentHelper.startAboutActivity(this);
            }
            return true;
        });

        SettingsFragment settingsFragment = new SettingsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_settings_container, settingsFragment)
                .commit();
    }

    @Override
    public View getSnackbarContainer() {
        return findViewById(R.id.activity_settings_container);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        // do nothing.
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