package wangdaye.com.geometricweather.management;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.view.View;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.main.MainActivity;
import wangdaye.com.geometricweather.management.search.SearchActivity;
import wangdaye.com.geometricweather.settings.activities.SelectProviderActivity;

/**
 * Manage activity.
 * */

public class ManagementActivity extends GeoActivity {

    private CoordinatorLayout mContainer;
    private ManagementFragment mManagementFragment;

    public static final int SEARCH_ACTIVITY = 1;
    public static final int SELECT_PROVIDER_ACTIVITY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);

        mContainer = findViewById(R.id.activity_management_container);

        mManagementFragment = (ManagementFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_management);
        if (mManagementFragment == null) {
            finish();
            return;
        }
        mManagementFragment.setDrawerMode(false);
        mManagementFragment.setRequestCodes(SEARCH_ACTIVITY, SELECT_PROVIDER_ACTIVITY);
        mManagementFragment.setOnLocationListChangedListener(new ManagementFragment.LocationManageCallback() {
            @Override
            public void onSelectedLocation(@NonNull String formattedId) {
                setResult(
                        RESULT_OK,
                        new Intent().putExtra(MainActivity.KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID, formattedId)
                );
                finish();
            }

            @Override
            public void onLocationListChanged(List<Location> locationList) {
                setResult(RESULT_OK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SEARCH_ACTIVITY:
                if (resultCode == RESULT_OK && data != null) {
                    Location location = data.getParcelableExtra(SearchActivity.KEY_LOCATION);
                    mManagementFragment.addLocation(location);
                }
                break;

            case SELECT_PROVIDER_ACTIVITY:
                if (resultCode == RESULT_OK && data != null) {
                    Location location = data.getParcelableExtra(SelectProviderActivity.KEY_LOCATION);
                    mManagementFragment.updateLocation(location);
                }
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
        return mContainer;
    }
}
