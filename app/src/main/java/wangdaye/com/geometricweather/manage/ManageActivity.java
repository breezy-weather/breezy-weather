package wangdaye.com.geometricweather.manage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.view.View;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.main.MainActivity;

/**
 * Manage activity.
 * */

public class ManageActivity extends GeoActivity {

    private CoordinatorLayout mContainer;
    private ManageFragment mManageFragment;

    public static final int SEARCH_ACTIVITY = 1;
    public static final int SELECT_PROVIDER_ACTIVITY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        mContainer = findViewById(R.id.activity_manage_container);

        mManageFragment = (ManageFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_manage);
        if (mManageFragment == null) {
            finish();
        } else {
            mManageFragment.setDrawerMode(false);
            mManageFragment.setRequestCodes(SEARCH_ACTIVITY, SELECT_PROVIDER_ACTIVITY);
            mManageFragment.setOnLocationListChangedListener(new ManageFragment.LocationManageCallback() {
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
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SEARCH_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    mManageFragment.readAppendLocation();
                }
                break;

            case SELECT_PROVIDER_ACTIVITY:
                mManageFragment.resetLocationList(null);
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
