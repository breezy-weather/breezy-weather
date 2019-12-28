package wangdaye.com.geometricweather.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import androidx.fragment.app.FragmentTransaction;
import android.view.View;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.main.MainActivity;
import wangdaye.com.geometricweather.main.fragment.LocationManageFragment;

/**
 * Manage activity.
 * */

public class ManageActivity extends GeoActivity {

    private CoordinatorLayout container;
    private LocationManageFragment manageFragment;

    public static final int SEARCH_ACTIVITY = 1;
    public static final int SELECT_PROVIDER_ACTIVITY = 2;

    public static final String KEY_CURRENT_FORMATTED_ID = "CURRENT_FORMATTED_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        String currentFormattedId = getIntent().getStringExtra(KEY_CURRENT_FORMATTED_ID);

        container = findViewById(R.id.activity_manage_container);

        manageFragment = new LocationManageFragment();
        manageFragment.setData(SEARCH_ACTIVITY, SELECT_PROVIDER_ACTIVITY, currentFormattedId);
        manageFragment.setOnLocationListChangedListener(new LocationManageFragment.LocationManageCallback() {
            @Override
            public void onSelectedLocation(@NonNull String formattedId) {
                finish();
            }

            @Override
            public void onLocationListChanged(@Nullable String formattedId) {
                setResult(
                        RESULT_OK,
                        new Intent().putExtra(MainActivity.KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID, formattedId)
                );
            }
        });

        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.activity_manage_container, manageFragment)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SEARCH_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    manageFragment.addLocation();
                }
                break;

            case SELECT_PROVIDER_ACTIVITY:
                manageFragment.resetLocationList();
                break;
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // do nothing.
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }
}
