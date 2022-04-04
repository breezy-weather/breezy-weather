package wangdaye.com.geometricweather.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.background.polling.PollingManager
import wangdaye.com.geometricweather.common.basic.GeoActivity
import wangdaye.com.geometricweather.common.basic.GeoFragment
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.bus.DataBus
import wangdaye.com.geometricweather.common.snackbar.SnackbarContainer
import wangdaye.com.geometricweather.common.utils.helpers.AsyncHelper
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper
import wangdaye.com.geometricweather.common.utils.helpers.ShortcutsHelper
import wangdaye.com.geometricweather.common.utils.helpers.SnackbarHelper
import wangdaye.com.geometricweather.databinding.ActivityMainBinding
import wangdaye.com.geometricweather.main.dialogs.BackgroundLocationDialog
import wangdaye.com.geometricweather.main.dialogs.LocationHelpDialog
import wangdaye.com.geometricweather.main.dialogs.LocationPermissionStatementDialog
import wangdaye.com.geometricweather.main.fragments.MainFragment
import wangdaye.com.geometricweather.main.fragments.ManagementFragment
import wangdaye.com.geometricweather.main.utils.DayNightColorWrapper.Companion.bind
import wangdaye.com.geometricweather.main.utils.DayNightColorWrapper.Companion.updateAll
import wangdaye.com.geometricweather.main.utils.MainModuleUtils
import wangdaye.com.geometricweather.remoteviews.NotificationHelper
import wangdaye.com.geometricweather.remoteviews.WidgetHelper
import wangdaye.com.geometricweather.search.SearchActivity
import wangdaye.com.geometricweather.settings.activities.SelectProviderActivity
import wangdaye.com.geometricweather.theme.ThemeManager

@AndroidEntryPoint
class MainActivity : GeoActivity(),
    MainFragment.Callback,
    ManagementFragment.Callback,
    LocationPermissionStatementDialog.Callback,
    BackgroundLocationDialog.Callback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    private var isLightTheme = false

    companion object {
        const val SETTINGS_ACTIVITY = 1
        const val CARD_MANAGE_ACTIVITY = 3
        const val SEARCH_ACTIVITY = 4
        const val SELECT_PROVIDER_ACTIVITY = 5

        const val ACTION_MAIN = "com.wangdaye.geometricweather.Main"
        const val KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID = "MAIN_ACTIVITY_LOCATION_FORMATTED_ID"

        const val ACTION_MANAGEMENT = "com.wangdaye.geomtricweather.ACTION_MANAGEMENT"
        const val ACTION_SHOW_ALERTS = "com.wangdaye.geomtricweather.ACTION_SHOW_ALERTS"

        const val ACTION_SHOW_DAILY_FORECAST = "com.wangdaye.geomtricweather.ACTION_SHOW_DAILY_FORECAST"
        const val KEY_DAILY_INDEX = "DAILY_INDEX"

        private const val TAG_FRAGMENT_MAIN = "fragment_main"
        private const val TAG_FRAGMENT_MANAGEMENT = "fragment_management"
    }

    private val backgroundUpdateObserver: Observer<Location> = Observer { location ->
        if (location == null) {
            return@Observer
        }

        viewModel.updateLocationFromBackground(location)

        if (isForeground
            && location.formattedId == viewModel.currentLocation.value?.formattedId) {
            SnackbarHelper.showSnackbar(getString(R.string.feedback_updated_in_background))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initModel(savedInstanceState == null)
        initView()

        DataBus.instance
            .with(Location::class.java)
            .observeForever(backgroundUpdateObserver)

        consumeIntentAction(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        consumeIntentAction(getIntent())
    }

    override fun onActivityReenter(resultCode: Int, data: Intent) {
        super.onActivityReenter(resultCode, data)
        if (resultCode == SEARCH_ACTIVITY) {
            val f = findManagementFragment()
            f?.prepareReenterTransition()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SETTINGS_ACTIVITY -> {
                findMainFragment()?.updateViews()

                // update notification immediately.
                AsyncHelper.runOnIO {
                    NotificationHelper.updateNotificationIfNecessary(
                        this,
                        viewModel.validLocationList.value!!.locationList
                    )
                }
                refreshBackgroundViews(
                    resetBackground = true,
                    locationList = viewModel.validLocationList.value!!.locationList,
                    defaultLocationChanged = true,
                    updateRemoteViews = true
                )
            }
            CARD_MANAGE_ACTIVITY -> if (resultCode == RESULT_OK) {
                findMainFragment()?.updateViews()
            }
            SEARCH_ACTIVITY -> if (resultCode == RESULT_OK && data != null) {
                val location: Location? = data.getParcelableExtra(SearchActivity.KEY_LOCATION)
                if (location != null) {
                    viewModel.addLocation(location, null)
                    SnackbarHelper.showSnackbar(getString(R.string.feedback_collect_succeed))
                }
            }
            SELECT_PROVIDER_ACTIVITY -> if (resultCode == RESULT_OK && data != null) {
                val location: Location? = data.getParcelableExtra(SelectProviderActivity.KEY_LOCATION)
                if (location != null) {
                    viewModel.updateLocation(location)
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        checkToUpdateDayNightColors()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkToUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        DataBus.instance
            .with(Location::class.java)
            .removeObserver(backgroundUpdateObserver)
    }

    override fun getSnackbarContainer(): SnackbarContainer {
        if (binding.drawerLayout != null) {
            return super.getSnackbarContainer()
        }

        val f: GeoFragment? = if (isManagementFragmentVisible) {
            findManagementFragment()
        } else {
            findMainFragment()
        }

        return if (f != null) {
            f.snackbarContainer
        } else {
            super.getSnackbarContainer()
        }
    }

    // init.
    private fun initModel(newActivity: Boolean) {
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        if (!viewModel.checkIsNewInstance()) {
            return
        }
        if (newActivity) {
            viewModel.init(getLocationId(intent))
        } else {
            viewModel.init()
        }
        isLightTheme = MainModuleUtils.isMainLightTheme(
            this,
            viewModel.currentLocation.value!!.isDaylight
        )
    }

    private fun getLocationId(intent: Intent?): String? {
        return intent?.getStringExtra(KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID)
    }

    @SuppressLint("ClickableViewAccessibility", "NonConstantResourceId")
    private fun initView() {
        if (binding.fragmentDrawer != null) {
            bind(binding.fragmentDrawer!!, android.R.attr.colorBackground) { color, _ ->
                binding.fragmentDrawer!!.setBackgroundColor(color)
            }
        }
        if (binding.fragmentMain != null) {
            bind(binding.fragmentMain!!, android.R.attr.colorBackground) { color, _ ->
                binding.fragmentMain!!.setBackgroundColor(color)
            }
        }
        if (binding.fragment != null) {
            bind(binding.fragment!!, android.R.attr.colorBackground) { color, _ ->
                binding.fragment!!.setBackgroundColor(color)
            }
        }

        ThemeManager.getInstance(this).homeUIMode.observe(this) {
            delegate.localNightMode = it
        }

        viewModel.currentLocation.observe(this) {
            checkToUpdateDayNightColors()
        }
        viewModel.validLocationList.observe(this) {
            // update notification immediately.
            AsyncHelper.runOnIO {
                NotificationHelper.updateNotificationIfNecessary(
                    this,
                    it.locationList
                )
            }
            refreshBackgroundViews(
                resetBackground = false,
                locationList = it.locationList,
                defaultLocationChanged = true,
                updateRemoteViews = true
            )
        }
        viewModel.permissionsRequest.observe(this) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || it.permissionList.isEmpty()
                || !it.consume()) {
                return@observe
            }

            // only show dialog if we need request basic location permissions.
            var needShowDialog = false
            for (permission in it.permissionList) {
                if (isLocationPermission(permission)) {
                    needShowDialog = true
                    break
                }
            }
            if (needShowDialog && !viewModel.statementManager.isLocationPermissionDeclared) {
                // only show dialog once.
                val dialog = LocationPermissionStatementDialog()
                dialog.isCancelable = false
                dialog.show(supportFragmentManager, null)
            } else {
                requestPermissions(it.permissionList.toTypedArray(), 0)
            }
        }
        viewModel.mainMessage.observe(this) {
            it?. let { msg ->
                when (msg) {
                    MainMessage.LOCATION_FAILED -> {
                        SnackbarHelper.showSnackbar(
                            getString(R.string.feedback_location_failed),
                            getString(R.string.help)
                        ) {
                            LocationHelpDialog
                                .getInstance()
                                .show(supportFragmentManager, null)
                        }
                    }
                    MainMessage.WEATHER_REQ_FAILED -> {
                        SnackbarHelper.showSnackbar(
                            getString(R.string.feedback_get_weather_failed)
                        )
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val request = viewModel.permissionsRequest.value
        if (request == null
            || request.permissionList.isEmpty()
            || request.target == null) {
            return
        }

        var i = 0
        while (i < permissions.size && i < grantResults.size) {
            if (isForegroundLocationPermission(permissions[i])
                && grantResults[i] != PackageManager.PERMISSION_GRANTED
            ) {
                // denied basic location permissions.
                if (request.target.isUsable || isLocationPermissionsGranted) {
                    viewModel.updateWithUpdatingChecking(
                        request.triggeredByUser,
                        false
                    )
                } else {
                    viewModel.cancelRequest()
                }
                return
            }
            i++
        }

        // check background location permissions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !viewModel.statementManager.isBackgroundLocationDeclared
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            BackgroundLocationDialog().show(supportFragmentManager, null)
        }
        viewModel.updateWithUpdatingChecking(
            request.triggeredByUser,
            false
        )
    }

    private fun isLocationPermission(permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permission == Manifest.permission.ACCESS_COARSE_LOCATION || isForegroundLocationPermission(
                permission
            )
        } else {
            isForegroundLocationPermission(permission)
        }
    }

    private fun isForegroundLocationPermission(permission: String): Boolean {
        return permission == Manifest.permission.ACCESS_COARSE_LOCATION || permission == Manifest.permission.ACCESS_FINE_LOCATION
    }

    private val isLocationPermissionsGranted: Boolean
        get() = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    // control.
    private fun checkToUpdateDayNightColors() {
        val lightTheme = MainModuleUtils.isMainLightTheme(
            this,
            viewModel.currentLocation.value!!.isDaylight
        )
        if (isLightTheme != lightTheme) {
            isLightTheme = lightTheme
            updateAll()
        }
    }

    private fun consumeIntentAction(intent: Intent) {
        val action = intent.action
        if (TextUtils.isEmpty(action)) {
            return
        }
        val formattedId = intent.getStringExtra(KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID)
        if (ACTION_SHOW_ALERTS == action) {
            IntentHelper.startAlertActivity(this, formattedId)
            return
        }
        if (ACTION_SHOW_DAILY_FORECAST == action) {
            val index = intent.getIntExtra(KEY_DAILY_INDEX, 0)
            IntentHelper.startDailyWeatherActivity(this, formattedId, index)
            return
        }
        if (ACTION_MANAGEMENT == action) {
            setManagementFragmentVisibility(true)
        }
    }

    private val isManagementFragmentVisible: Boolean
        get() = if (binding.drawerLayout != null) {
            binding.drawerLayout!!.isUnfold
        } else {
            val f: Fragment? = findManagementFragment()
            f != null && f.isVisible
        }

    fun setManagementFragmentVisibility(visible: Boolean) {
        if (binding.drawerLayout != null) {
            binding.drawerLayout!!.isUnfold = visible
        } else if (visible != isManagementFragmentVisible) {
            if (visible) {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.setCustomAnimations(
                    R.anim.fragment_manange_enter,
                    R.anim.fragment_main_exit,
                    R.anim.fragment_main_pop_enter,
                    R.anim.fragment_manange_pop_exit
                ).add(R.id.fragment, ManagementFragment.getInstance(true), TAG_FRAGMENT_MANAGEMENT)
                    .addToBackStack(null)
                val main: Fragment? = findMainFragment()
                if (main != null) {
                    transaction.hide(main)
                }
                transaction.commit()
            } else {
                supportFragmentManager.popBackStack()
            }
        }
    }

    private fun findMainFragment(): MainFragment? {
        return if (binding.drawerLayout == null) {
            supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_MAIN) as MainFragment?
        } else {
            supportFragmentManager.findFragmentById(R.id.fragment_main) as MainFragment?
        }
    }

    private fun findManagementFragment(): ManagementFragment? {
        return if (binding.drawerLayout == null) {
            supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_MANAGEMENT) as ManagementFragment?
        } else {
            supportFragmentManager.findFragmentById(R.id.fragment_drawer) as ManagementFragment?
        }
    }

    private fun refreshBackgroundViews(
        resetBackground: Boolean, locationList: List<Location>?,
        defaultLocationChanged: Boolean, updateRemoteViews: Boolean
    ) {
        if (resetBackground) {
            AsyncHelper.delayRunOnIO<Any>({
                PollingManager.resetAllBackgroundTask(
                    this, false
                )
            }, 1000)
        }
        if (updateRemoteViews && locationList != null && locationList.isNotEmpty()) {
            AsyncHelper.delayRunOnIO<Any>({
                if (defaultLocationChanged) {
                    WidgetHelper.updateWidgetIfNecessary(this, locationList[0])
                    NotificationHelper.updateNotificationIfNecessary(this, locationList)
                }
                WidgetHelper.updateWidgetIfNecessary(this, locationList)
            }, 1000)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                ShortcutsHelper.refreshShortcutsInNewThread(this, locationList)
            }
        }
    }

    // interface.
    // main fragment callback.
    override fun onManageIconClicked() {
        setManagementFragmentVisibility(!isManagementFragmentVisible)
    }

    override fun onSettingsIconClicked() {
        IntentHelper.startSettingsActivityForResult(this, SETTINGS_ACTIVITY)
    }

    // management fragment callback.
    override fun onSearchBarClicked(searchBar: View) {
        IntentHelper.startSearchActivityForResult(this, searchBar, SEARCH_ACTIVITY)
    }

    override fun onSelectProviderActivityStarted() {
        IntentHelper.startSelectProviderActivityForResult(this, SELECT_PROVIDER_ACTIVITY)
    }

    // location permissions statement callback.
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun requestLocationPermissions() {
        viewModel.statementManager.setLocationPermissionDeclared(this)

        val request = viewModel.permissionsRequest.value
        if (request != null
            && request.permissionList.isNotEmpty()
            && request.target != null) {
            requestPermissions(
                request.permissionList.toTypedArray(),
                0
            )
        }
    }

    // background location permissions callback.
    @RequiresApi(api = Build.VERSION_CODES.Q)
    override fun requestBackgroundLocationPermission() {
        viewModel.statementManager.setBackgroundLocationDeclared(this)
        val permissionList: MutableList<String> = ArrayList()
        permissionList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        requestPermissions(permissionList.toTypedArray(), 0)
    }
}