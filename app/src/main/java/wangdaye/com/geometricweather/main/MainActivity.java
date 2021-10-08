package wangdaye.com.geometricweather.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.polling.PollingManager;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.GeoFragment;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.DarkMode;
import wangdaye.com.geometricweather.common.snackbar.SnackbarContainer;
import wangdaye.com.geometricweather.common.utils.helpers.AsyncHelper;
import wangdaye.com.geometricweather.common.utils.helpers.BusHelper;
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper;
import wangdaye.com.geometricweather.common.utils.helpers.ShortcutsHelper;
import wangdaye.com.geometricweather.common.utils.helpers.SnackbarHelper;
import wangdaye.com.geometricweather.databinding.ActivityMainBinding;
import wangdaye.com.geometricweather.main.dialogs.BackgroundLocationDialog;
import wangdaye.com.geometricweather.main.dialogs.LocationPermissionStatementDialog;
import wangdaye.com.geometricweather.main.fragments.MainFragment;
import wangdaye.com.geometricweather.main.fragments.ManagementFragment;
import wangdaye.com.geometricweather.main.models.LocationResource;
import wangdaye.com.geometricweather.main.models.PermissionsRequest;
import wangdaye.com.geometricweather.main.utils.MainThemeManager;
import wangdaye.com.geometricweather.remoteviews.NotificationHelper;
import wangdaye.com.geometricweather.remoteviews.WidgetHelper;
import wangdaye.com.geometricweather.search.SearchActivity;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.settings.activities.SelectProviderActivity;

/**
 * Main activity.
 * */

@AndroidEntryPoint
public class MainActivity extends GeoActivity
        implements MainFragment.Callback, ManagementFragment.Callback,
        LocationPermissionStatementDialog.Callback, BackgroundLocationDialog.Callback {

    private ActivityMainBinding mBinding;
    private MainActivityViewModel mViewModel;

    private final Observer<Location> mBackgroundUpdateObserver = new Observer<Location>() {

        @Override
        public void onChanged(Location location) {
            if (location == null) {
                return;
            }

            mViewModel.updateLocationFromBackground(location);
            if (isForeground()
                    && location.getFormattedId().equals(mViewModel.getCurrentFormattedId())) {
                SnackbarHelper.showSnackbar(getString(R.string.feedback_updated_in_background));
            }
        }
    };

    public static final int SETTINGS_ACTIVITY = 1;
    public static final int CARD_MANAGE_ACTIVITY = 3;
    public static final int SEARCH_ACTIVITY = 4;
    public static final int SELECT_PROVIDER_ACTIVITY = 5;

    public static final String ACTION_MAIN = "com.wangdaye.geometricweather.Main";
    public static final String KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID
            = "MAIN_ACTIVITY_LOCATION_FORMATTED_ID";

    public static final String ACTION_MANAGEMENT
            = "com.wangdaye.geomtricweather.ACTION_MANAGEMENT";

    public static final String ACTION_SHOW_ALERTS
            = "com.wangdaye.geomtricweather.ACTION_SHOW_ALERTS";

    public static final String ACTION_SHOW_DAILY_FORECAST
            = "com.wangdaye.geomtricweather.ACTION_SHOW_DAILY_FORECAST";
    public static final String KEY_DAILY_INDEX = "DAILY_INDEX";

    private static final String TAG_FRAGMENT_MAIN = "fragment_main";
    private static final String TAG_FRAGMENT_MANAGEMENT = "fragment_management";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        initModel(savedInstanceState == null);
        initView();

        BusHelper.observeLocationChangedForever(mBackgroundUpdateObserver);

        refreshBackgroundViews(true, mViewModel.getValidLocationList(),
                false, false);

        consumeIntentAction(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        consumeIntentAction(getIntent());
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        if (resultCode == SEARCH_ACTIVITY) {
            ManagementFragment f = findManagementFragment();
            if (f != null) {
                f.prepareReenterTransition();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SETTINGS_ACTIVITY:
                mViewModel.init();
                // update notification immediately.
                if (mViewModel.getValidLocationList() != null) {
                    AsyncHelper.runOnIO(() -> NotificationHelper.updateNotificationIfNecessary(
                            this, mViewModel.getValidLocationList()
                    ));
                }
                refreshBackgroundViews(true, mViewModel.getValidLocationList(),
                        true, true);
                break;

            case CARD_MANAGE_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    mViewModel.init();
                }
                break;

            case SEARCH_ACTIVITY:
                if (resultCode == RESULT_OK && data != null) {
                    Location location = data.getParcelableExtra(SearchActivity.KEY_LOCATION);
                    if (location != null) {
                        mViewModel.addLocation(location);
                        SnackbarHelper.showSnackbar(getString(R.string.feedback_collect_succeed));
                    }
                }
                break;

            case SELECT_PROVIDER_ACTIVITY:
                if (resultCode == RESULT_OK && data != null) {
                    Location location = data.getParcelableExtra(SelectProviderActivity.KEY_LOCATION);
                    if (location != null) {
                        mViewModel.forceUpdateLocation(location);
                    }
                }
                break;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (SettingsManager.getInstance(this).getDarkMode() == DarkMode.SYSTEM) {
            mViewModel.init();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewModel.checkWhetherToChangeTheme();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusHelper.cancelObserveLocationChanged(mBackgroundUpdateObserver);
    }

    @Override
    public SnackbarContainer getSnackbarContainer() {
        if (mBinding.drawerLayout != null) {
            return super.getSnackbarContainer();
        }
        GeoFragment f;
        if (isManagementFragmentVisible()) {
            f = findManagementFragment();
        } else {
            f = findMainFragment();
        }
        if (f != null) {
            return f.getSnackbarContainer();
        }

        return super.getSnackbarContainer();
    }

    // init.

    private void initModel(boolean newActivity) {
        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        if (!mViewModel.checkIsNewInstance()) {
            return;
        }

        if (newActivity) {
            mViewModel.init(getLocationId(getIntent()));
        } else {
            mViewModel.init();
        }
    }

    @Nullable
    private String getLocationId(@Nullable Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.getStringExtra(KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID);
    }

    @SuppressLint({"ClickableViewAccessibility", "NonConstantResourceId"})
    private void initView() {
        mViewModel.getCurrentLocation().observe(this, resource -> {
            if (resource == null) {
                return;
            }

            setDarkMode(resource.data.isDaylight());
            MainThemeManager manager = mViewModel.getThemeManager();
            if (mBinding.fragmentDrawer != null) {
                mBinding.fragmentDrawer.setBackgroundColor(manager.getRootColor(this));
            }
            if (mBinding.fragmentMain != null) {
                mBinding.fragmentMain.setBackgroundColor(manager.getRootColor(this));
            }
            if (mBinding.fragment != null) {
                mBinding.fragment.setBackgroundColor(manager.getRootColor(this));
            }

            refreshBackgroundViews(
                    false, mViewModel.getValidLocationList(), resource.defaultLocation,
                    resource.event != LocationResource.Event.BACKGROUND_UPDATE_CURRENT
                            && resource.event != LocationResource.Event.BACKGROUND_UPDATE_OTHERS);
        });

        mViewModel.getPermissionsRequest().observe(this, request -> {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                    || request.permissionList.size() == 0
                    || !request.consume()) {
                return;
            }

            // only show dialog if we need request basic location permissions.
            boolean needShowDialog = false;
            for (String permission : request.permissionList) {
                if (isLocationPermission(permission)) {
                    needShowDialog = true;
                    break;
                }
            }
            if (needShowDialog && !mViewModel.getStatementManager().isLocationPermissionDeclared()) {
                // only show dialog once.
                LocationPermissionStatementDialog dialog = new LocationPermissionStatementDialog();
                dialog.setCancelable(false);
                dialog.show(getSupportFragmentManager(), null);
            } else {
                requestPermissions(request.permissionList.toArray(new String[0]), 0);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionsRequest request = mViewModel.getPermissionsRequestValue();
        if (request.permissionList.size() == 0 || request.target == null) {
            return;
        }

        for (int i = 0; i < permissions.length && i < grantResults.length; i++) {
            if (isForegroundLocationPermission(permissions[i])
                    && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                // denied basic location permissions.
                if (request.target.isUsable() || isLocationPermissionsGranted()) {
                    mViewModel.updateWeather(request.triggeredByUser, false);
                } else {
                    mViewModel.requestPermissionsFailed(request.target);
                }
                return;
            }
        }

        // check background location permissions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && !mViewModel.getStatementManager().isBackgroundLocationDeclared()
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new BackgroundLocationDialog().show(getSupportFragmentManager(), null);
        }

        mViewModel.updateWeather(request.triggeredByUser, false);
    }

    private boolean isLocationPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                    || isForegroundLocationPermission(permission);
        } else {
            return isForegroundLocationPermission(permission);
        }
    }

    private boolean isForegroundLocationPermission(String permission) {
        return permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                || permission.equals(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private boolean isLocationPermissionsGranted() {
        return ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    // control.

    private void consumeIntentAction(Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }

        String formattedId = intent.getStringExtra(KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID);

        if (ACTION_SHOW_ALERTS.equals(action)) {
            IntentHelper.startAlertActivity(this, formattedId);
            return;
        }

        if (ACTION_SHOW_DAILY_FORECAST.equals(action)) {
            int index = intent.getIntExtra(KEY_DAILY_INDEX, 0);
            IntentHelper.startDailyWeatherActivity(this, formattedId, index);
            return;
        }

        if (ACTION_MANAGEMENT.equals(action)) {
            setManagementFragmentVisibility(true);
        }
    }

    private void setDarkMode(boolean dayTime) {
        if (SettingsManager.getInstance(this).getDarkMode() == DarkMode.AUTO) {
            int mode = dayTime ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
            getDelegate().setLocalNightMode(mode);
            AppCompatDelegate.setDefaultNightMode(mode);
        } else if (SettingsManager.getInstance(this).getDarkMode() == DarkMode.SYSTEM) {
            int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            getDelegate().setLocalNightMode(mode);
            AppCompatDelegate.setDefaultNightMode(mode);
        }
    }

    private boolean isManagementFragmentVisible() {
        if (mBinding.drawerLayout != null) {
            return mBinding.drawerLayout.isUnfold();
        } else {
            Fragment f = findManagementFragment();
            return f != null && f.isVisible();
        }
    }

    public void setManagementFragmentVisibility(boolean visible) {
        if (mBinding.drawerLayout != null) {
            mBinding.drawerLayout.setUnfold(visible);
        } else if (visible != isManagementFragmentVisible()) {
            if (visible) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(
                                R.anim.fragment_manange_enter,
                                R.anim.fragment_main_exit,
                                R.anim.fragment_main_pop_enter,
                                R.anim.fragment_manange_pop_exit
                        ).add(R.id.fragment, ManagementFragment.getInstance(true), TAG_FRAGMENT_MANAGEMENT)
                        .addToBackStack(null);
                Fragment main = findMainFragment();
                if (main != null) {
                    transaction.hide(main);
                }
                transaction.commit();
            } else {
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    @Nullable
    private MainFragment findMainFragment() {
        if (mBinding.drawerLayout == null) {
            return (MainFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MAIN);
        } else {
            return (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_main);
        }
    }

    @Nullable
    private ManagementFragment findManagementFragment() {
        if (mBinding.drawerLayout == null) {
            return (ManagementFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MANAGEMENT);
        } else {
            return (ManagementFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_drawer);
        }
    }

    private void refreshBackgroundViews(boolean resetBackground, @Nullable List<Location> locationList,
                                        boolean defaultLocationChanged, boolean updateRemoteViews) {
        if (resetBackground) {
            AsyncHelper.delayRunOnIO(() -> PollingManager.resetAllBackgroundTask(
                    this, false
            ), 1000);
        }

        if (updateRemoteViews && locationList != null && locationList.size() > 0) {
            AsyncHelper.delayRunOnIO(() -> {
                if (defaultLocationChanged) {
                    WidgetHelper.updateWidgetIfNecessary(this, locationList.get(0));
                    NotificationHelper.updateNotificationIfNecessary(this, locationList);
                }
                WidgetHelper.updateWidgetIfNecessary(this, locationList);
            }, 1000);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                ShortcutsHelper.refreshShortcutsInNewThread(this, locationList);
            }
        }
    }

    // interface.

    // main fragment callback.

    @Override
    public void onManageIconClicked() {
        setManagementFragmentVisibility(!isManagementFragmentVisible());
    }

    @Override
    public void onSettingsIconClicked() {
        IntentHelper.startSettingsActivityForResult(this, SETTINGS_ACTIVITY);
    }

    // management fragment callback.

    @Override
    public void onSearchBarClicked(View searchBar) {
        IntentHelper.startSearchActivityForResult(this, searchBar, SEARCH_ACTIVITY);
    }

    @Override
    public void onSelectProviderActivityStarted() {
        IntentHelper.startSelectProviderActivityForResult(this, SELECT_PROVIDER_ACTIVITY);
    }

    // location permissions statement callback.

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void requestLocationPermissions() {
        mViewModel.getStatementManager().setLocationPermissionDeclared(this);

        PermissionsRequest request = mViewModel.getPermissionsRequestValue();
        if (request.permissionList.size() != 0 && request.target != null) {
            requestPermissions(request.permissionList.toArray(new String[0]), 0);
        }
    }

    // background location permissions callback.

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void requestBackgroundLocationPermission() {
        mViewModel.getStatementManager().setBackgroundLocationDeclared(this);

        List<String> permissionList = new ArrayList<>();
        permissionList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        requestPermissions(permissionList.toArray(new String[0]), 0);
    }
}