package wangdaye.com.geometricweather.manage.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import androidx.fragment.app.FragmentTransaction;
import android.view.View;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.main.MainActivity;
import wangdaye.com.geometricweather.manage.LocationManageFragment;

/**
 * Manage activity.
 * */

public class ManageActivity extends GeoActivity {

    private CoordinatorLayout container;
    private LocationManageFragment manageFragment;

    public static final int SEARCH_ACTIVITY = 1;
    public static final int SELECT_PROVIDER_ACTIVITY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        container = findViewById(R.id.activity_manage_container);

        manageFragment = new LocationManageFragment();
        manageFragment.setDrawerMode(false);
        manageFragment.setRequestCodes(SEARCH_ACTIVITY, SELECT_PROVIDER_ACTIVITY);
        manageFragment.setOnLocationListChangedListener(new LocationManageFragment.LocationManageCallback() {
            @Override
            public void onSelectedLocation(@NonNull String formattedId) {
                setResult(
                        RESULT_OK,
                        new Intent().putExtra(MainActivity.KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID, formattedId)
                );
                finish();
            }

            @Override
            public void onLocationListChanged() {
                setResult(RESULT_OK);
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
