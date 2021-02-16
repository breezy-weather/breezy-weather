package wangdaye.com.geometricweather.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.polling.PollingManager;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.basic.models.options.DarkMode;
import wangdaye.com.geometricweather.basic.models.weather.Weather;
import wangdaye.com.geometricweather.databinding.ActivityMainBinding;
import wangdaye.com.geometricweather.main.dialogs.BackgroundLocationDialog;
import wangdaye.com.geometricweather.main.dialogs.LocationPermissionStatementDialog;
import wangdaye.com.geometricweather.main.fragments.MainFragment;
import wangdaye.com.geometricweather.main.fragments.ManagementFragment;
import wangdaye.com.geometricweather.main.models.LocationResource;
import wangdaye.com.geometricweather.main.models.PermissionsRequest;
import wangdaye.com.geometricweather.main.utils.StatementManager;
import wangdaye.com.geometricweather.remoteviews.NotificationUtils;
import wangdaye.com.geometricweather.remoteviews.WidgetUtils;
import wangdaye.com.geometricweather.search.SearchActivity;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.settings.activities.SelectProviderActivity;
import wangdaye.com.geometricweather.utils.helpters.AsyncHelper;
import wangdaye.com.geometricweather.utils.helpters.IntentHelper;
import wangdaye.com.geometricweather.utils.helpters.SnackbarHelper;
import wangdaye.com.geometricweather.utils.managers.ShortcutsManager;

/**
 * Main activity.
 * */

public class MainActivity extends GeoActivity
        implements MainFragment.Callback, ManagementFragment.Callback,
        LocationPermissionStatementDialog.Callback, BackgroundLocationDialog.Callback {

    private ActivityMainBinding mBinding;
    private MainActivityViewModel mViewModel;

    private @Nullable String mPendingAction;
    private @Nullable HashMap<String, Object> mPendingExtraMap;

    public static final int SETTINGS_ACTIVITY = 1;
    public static final int CARD_MANAGE_ACTIVITY = 3;
    public static final int SEARCH_ACTIVITY = 4;
    public static final int SELECT_PROVIDER_ACTIVITY = 5;

    public static final String ACTION_MAIN = "com.wangdaye.geometricweather.Main";
    public static final String KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID
            = "MAIN_ACTIVITY_LOCATION_FORMATTED_ID";

    public static final String ACTION_UPDATE_WEATHER_IN_BACKGROUND
            = "com.wangdaye.geomtricweather.ACTION_UPDATE_WEATHER_IN_BACKGROUND";
    public static final String KEY_LOCATION = "LOCATION";

    public static final String ACTION_SHOW_ALERTS
            = "com.wangdaye.geomtricweather.ACTION_SHOW_ALERTS";

    public static final String ACTION_SHOW_DAILY_FORECAST
            = "com.wangdaye.geomtricweather.ACTION_SHOW_DAILY_FORECAST";
    public static final String KEY_DAILY_INDEX = "DAILY_INDEX";

    private static final String TAG_FRAGMENT_MAIN = "tag_fragment_main";
    private static final String TAG_FRAGMENT_MANAGEMENT = "tag_fragment_management";

    private final BroadcastReceiver backgroundUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = (Location) intent.getSerializableExtra(KEY_LOCATION);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pendingIntentAction(getIntent());

        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        initModel(savedInstanceState == null);
        initView();

        registerReceiver(
                backgroundUpdateReceiver,
                new IntentFilter(ACTION_UPDATE_WEATHER_IN_BACKGROUND)
        );
        refreshBackgroundViews(true, mViewModel.getValidLocationList(),
                false, false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        pendingIntentAction(intent);
        mViewModel.init(getLocationId(intent));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SETTINGS_ACTIVITY: {
                mViewModel.init();

                // update notification immediately.
                if (mViewModel.getValidLocationList() != null) {
                    AsyncHelper.runOnIO(() -> NotificationUtils.updateNotificationIfNecessary(
                            this, mViewModel.getValidLocationList()
                    ));
                }
                refreshBackgroundViews(true, mViewModel.getValidLocationList(),
                        true, true);
                break;
            }
            case CARD_MANAGE_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    mViewModel.init();
                }
                break;

            case SEARCH_ACTIVITY:
                if (resultCode == RESULT_OK && data != null) {
                    Location location = data.getParcelableExtra(SearchActivity.KEY_LOCATION);
                    mViewModel.addLocation(location);
                    SnackbarHelper.showSnackbar(getString(R.string.feedback_collect_succeed));
                }
                break;

            case SELECT_PROVIDER_ACTIVITY:
                if (resultCode == RESULT_OK && data != null) {
                    Location location = data.getParcelableExtra(SelectProviderActivity.KEY_LOCATION);
                    mViewModel.forceUpdateLocation(location);
                }
                break;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (SettingsOptionManager.getInstance(this).getDarkMode() == DarkMode.SYSTEM) {
            mViewModel.init();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(backgroundUpdateReceiver);
    }

    @Override
    public View getSnackbarContainer() {
        return mBinding.background;
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
        if (findMainFragment() == null && findManagementFragment() == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, MainFragment.class, null, TAG_FRAGMENT_MAIN)
                    .commit();
        }

        mViewModel.getCurrentLocation().observe(this, resource -> {
            if (resource == null) {
                return;
            }

            refreshBackgroundViews(
                    false, mViewModel.getValidLocationList(), resource.defaultLocation,
                    resource.event != LocationResource.Event.BACKGROUND_UPDATE);

            consumeIntentAction();
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
            if (needShowDialog
                    && !StatementManager.getInstance(this).isLocationPermissionDeclared()) {
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
                if (request.target.isUsable()) {
                    mViewModel.updateWeather(request.triggeredByUser, false);
                } else {
                    mViewModel.requestPermissionsFailed(request.target);
                }
                return;
            }
        }

        // check background location permissions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && !StatementManager.getInstance(this).isBackgroundLocationDeclared()
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            new BackgroundLocationDialog().show(getSupportFragmentManager(), null);
        }

        mViewModel.updateWeather(request.triggeredByUser, false);
    }

    private boolean isLocationPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                    || permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)
                    || permission.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        } else {
            return isForegroundLocationPermission(permission);
        }
    }

    private boolean isForegroundLocationPermission(String permission) {
        return permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                || permission.equals(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    // control.

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
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                R.anim.fragment_manange_enter,
                                0,
                                0,
                                R.anim.fragment_manange_pop_exit
                        )
                        .add(R.id.fragment, ManagementFragment.class, null, TAG_FRAGMENT_MANAGEMENT)
                        .addToBackStack(null)
                        .commit();
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
                    WidgetUtils.updateWidgetIfNecessary(this, locationList.get(0));
                    NotificationUtils.updateNotificationIfNecessary(this, locationList);
                }
                WidgetUtils.updateWidgetIfNecessary(this, locationList);
            }, 1000);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                ShortcutsManager.refreshShortcutsInNewThread(this, locationList);
            }
        }
    }

    private void pendingIntentAction(Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            mPendingAction = null;
            mPendingExtraMap = null;
            return;
        }

        if (action.equals(ACTION_SHOW_ALERTS)) {
            mPendingAction = ACTION_SHOW_ALERTS;
            mPendingExtraMap = new HashMap<>();
        } else if (action.equals(ACTION_SHOW_DAILY_FORECAST)) {
            mPendingAction = ACTION_SHOW_DAILY_FORECAST;

            mPendingExtraMap = new HashMap<>();
            mPendingExtraMap.put(KEY_DAILY_INDEX, intent.getIntExtra(KEY_DAILY_INDEX, 0));
        }
    }

    private void consumeIntentAction() {
        String action = mPendingAction;
        HashMap<String, Object> extraMap = mPendingExtraMap;
        mPendingAction = null;
        mPendingExtraMap = null;
        if (TextUtils.isEmpty(action) || extraMap == null) {
            return;
        }

        if (ACTION_SHOW_ALERTS.equals(action)) {
            Location location = mViewModel.getCurrentLocationValue();
            if (location != null) {
                Weather weather = location.getWeather();
                if (weather != null) {
                    IntentHelper.startAlertActivity(this, weather);
                }
            }
        } else if (ACTION_SHOW_DAILY_FORECAST.equals(action)) {
            String formattedId = mViewModel.getCurrentFormattedId();
            Integer index = (Integer) extraMap.get(KEY_DAILY_INDEX);
            if (formattedId != null && index != null) {
                IntentHelper.startDailyWeatherActivity(
                        this, mViewModel.getCurrentFormattedId(), index);
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
        StatementManager.getInstance(this).setLocationPermissionDeclared(this);

        PermissionsRequest request = mViewModel.getPermissionsRequestValue();
        if (request.permissionList.size() != 0 && request.target != null) {
            requestPermissions(request.permissionList.toArray(new String[0]), 0);
        }
    }

    // background location permissions callback.

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void requestBackgroundLocationPermission() {
        StatementManager.getInstance(this).setBackgroundLocationDeclared(this);

        List<String> permissionList = new ArrayList<>();
        permissionList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        requestPermissions(permissionList.toArray(new String[0]), 0);
    }
}