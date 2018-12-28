package wangdaye.com.geometricweather.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.ui.fragment.SettingsFragment;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

/**
 * Settings activity.
 * */

public class SettingsActivity extends GeoActivity
        implements View.OnClickListener, Toolbar.OnMenuItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_settings);
    }

    @Override
    public View getSnackbarContainer() {
        return findViewById(R.id.activity_settings_container);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isStarted()) {
            setStarted();
            initToolbar();
            SettingsFragment settingsFragment = new SettingsFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_settings_container, settingsFragment)
                    .commit();
        }
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
    protected void onSaveInstanceState(Bundle outState) {
        // do nothing.
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.activity_settings_toolbar);
        toolbar.setTitle(getString(R.string.action_settings));
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(this);
        toolbar.inflateMenu(R.menu.activity_settings);
        toolbar.setOnMenuItemClickListener(this);
    }

    public void pushFragment(PreferenceFragmentCompat f, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, 0, android.R.anim.fade_in, 0)
                .replace(R.id.activity_settings_container, f)
                .addToBackStack(tag)
                .commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case -1:
                finish();
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_about:
                IntentHelper.startAboutActivity(this);
                break;
        }
        return true;
    }
}