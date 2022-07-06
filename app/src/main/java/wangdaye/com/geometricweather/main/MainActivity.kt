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
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.background.polling.PollingManager
import wangdaye.com.geometricweather.common.basic.GeoActivity
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.bus.EventBus
import wangdaye.com.geometricweather.common.snackbar.SnackbarContainer
import wangdaye.com.geometricweather.common.utils.DisplayUtils
import wangdaye.com.geometricweather.common.utils.helpers.AsyncHelper
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper
import wangdaye.com.geometricweather.common.utils.helpers.ShortcutsHelper
import wangdaye.com.geometricweather.common.utils.helpers.SnackbarHelper
import wangdaye.com.geometricweather.databinding.ActivityMainBinding
import wangdaye.com.geometricweather.main.dialogs.ApiKeyHelpDialog
import wangdaye.com.geometricweather.main.dialogs.LocationHelpDialog
import wangdaye.com.geometricweather.main.fragments.HomeFragment
import wangdaye.com.geometricweather.main.fragments.ManagementFragment
import wangdaye.com.geometricweather.main.fragments.ModifyMainSystemBarMessage
import wangdaye.com.geometricweather.main.fragments.PushedManagementFragment
import wangdaye.com.geometricweather.main.utils.MainThemeColorProvider
import wangdaye.com.geometricweather.remoteviews.NotificationHelper
import wangdaye.com.geometricweather.remoteviews.WidgetHelper
import wangdaye.com.geometricweather.search.SearchActivity
import wangdaye.com.geometricweather.settings.SettingsChangedMessage

@AndroidEntryPoint
class MainActivity : GeoActivity(),
    HomeFragment.Callback,
    ManagementFragment.Callback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    companion object {
        const val SEARCH_ACTIVITY = 4

        const val ACTION_MAIN = "com.wangdaye.geometricweather.Main"
        const val KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID = "MAIN_ACTIVITY_LOCATION_FORMATTED_ID"

        const val ACTION_MANAGEMENT = "com.wangdaye.geomtricweather.ACTION_MANAGEMENT"
        const val ACTION_SHOW_ALERTS = "com.wangdaye.geomtricweather.ACTION_SHOW_ALERTS"

        const val ACTION_SHOW_DAILY_FORECAST = "com.wangdaye.geomtricweather.ACTION_SHOW_DAILY_FORECAST"
        const val KEY_DAILY_INDEX = "DAILY_INDEX"

        private const val TAG_FRAGMENT_HOME = "fragment_main"
        private const val TAG_FRAGMENT_MANAGEMENT = "fragment_management"
    }

    private val backgroundUpdateObserver: Observer<Location> = Observer { location ->
        location?.let {
            viewModel.updateLocationFromBackground(it)

            if (isActivityStarted
                && it.formattedId == viewModel.currentLocation.value?.location?.formattedId) {
                SnackbarHelper.showSnackbar(getString(R.string.feedback_updated_in_background))
            }
        }
    }

    private val fragmentsLifecycleCallback = object : FragmentManager.FragmentLifecycleCallbacks() {

        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?
        ) {
            updateSystemBarStyle()
        }

        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
            updateSystemBarStyle()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        supportFragmentManager.registerFragmentLifecycleCallbacks(
            fragmentsLifecycleCallback, false
        )
        setContentView(binding.root)

        MainThemeColorProvider.bind(this)

        initModel(savedInstanceState == null)
        initView()

        consumeIntentAction(intent)

        EventBus.instance
            .with(Location::class.java)
            .observeForever(backgroundUpdateObserver)
        EventBus.instance.with(SettingsChangedMessage::class.java).observe(this) {
            viewModel.init()

            findHomeFragment()?.updateViews()

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
        EventBus.instance.with(ModifyMainSystemBarMessage::class.java).observe(this) {
            updateSystemBarStyle()
        }
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
            SEARCH_ACTIVITY -> if (resultCode == RESULT_OK && data != null) {
                val location: Location? = data.getParcelableExtra(SearchActivity.KEY_LOCATION)
                if (location != null) {
                    viewModel.addLocation(location, null)
                    SnackbarHelper.showSnackbar(getString(R.string.feedback_collect_succeed))
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateSystemBarStyle()
        updateDayNightColors()
    }

    override fun onStart() {
        super.onStart()
        viewModel.checkToUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentsLifecycleCallback)
        EventBus.instance
            .with(Location::class.java)
            .removeObserver(backgroundUpdateObserver)
    }

    override val snackbarContainer: SnackbarContainer?
        get() {
            if (binding.drawerLayout != null) {
                return super.snackbarContainer
            }

            val f = if (isManagementFragmentVisible) {
                findManagementFragment()
            } else {
                findHomeFragment()
            }

            return f?.snackbarContainer ?: super.snackbarContainer
        }

    // init.

    private fun initModel(newActivity: Boolean) {
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        if (!viewModel.checkIsNewInstance()) {
            return
        }
        if (newActivity) {
            viewModel.init(formattedId = getLocationId(intent))
        } else {
            viewModel.init()
        }
    }

    private fun getLocationId(intent: Intent?): String? {
        return intent?.getStringExtra(KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID)
    }

    @SuppressLint("ClickableViewAccessibility", "NonConstantResourceId")
    private fun initView() {
        binding.root.post {
            if (isActivityCreated) {
                updateDayNightColors()
            }
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
                || it == null
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
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.feedback_location_permissions_title)
                    .setMessage(R.string.feedback_location_permissions_statement)
                    .setPositiveButton(R.string.next) { _, _ ->
                        // mark declared.
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
                    .setCancelable(false)
                    .show()
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
                            LocationHelpDialog.show(this)
                        }
                    }
                    MainMessage.WEATHER_REQ_FAILED -> {
                        SnackbarHelper.showSnackbar(
                            getString(R.string.feedback_get_weather_failed)
                        )
                    }
                    MainMessage.API_LIMIT_REACHED -> {
                        SnackbarHelper.showSnackbar(
                            getString(R.string.feedback_api_limit_reached),
                            getString(R.string.help)
                        ) {
                            ApiKeyHelpDialog.show(this)
                        }
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

        grantResults.zip(permissions).firstOrNull { // result, permission
            it.first != PackageManager.PERMISSION_GRANTED
                    && isEssentialLocationPermission(permission = it.second)
        }?.let {
            // if the user denied an essential location permissions.
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

        // check background location permissions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            && !viewModel.statementManager.isBackgroundLocationDeclared
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.feedback_background_location_title)
                .setMessage(R.string.feedback_background_location_summary)
                .setPositiveButton(R.string.go_to_set) { _, _ ->
                    // mark background location permission declared.
                    viewModel.statementManager.setBackgroundLocationDeclared(this)
                    // request background location permission.
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        0
                    )
                }
                .setCancelable(false)
                .show()
        }
        viewModel.updateWithUpdatingChecking(
            request.triggeredByUser,
            false
        )
    }

    private fun isLocationPermission(
        permission: String
    ) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION
                || isEssentialLocationPermission(permission)
    } else {
        isEssentialLocationPermission(permission)
    }

    private fun isEssentialLocationPermission(permission: String): Boolean {
        return permission == Manifest.permission.ACCESS_COARSE_LOCATION
                || permission == Manifest.permission.ACCESS_FINE_LOCATION
    }

    private val isLocationPermissionsGranted: Boolean
        get() = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    val isDaylight
        get() = viewModel.currentLocation.value!!.daylight

    // control.

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

    private fun updateSystemBarStyle() {
        if (binding.drawerLayout != null) {
            findHomeFragment()?.setSystemBarStyle()
            return
        }

        if (isOrWillManagementFragmentVisible) {
            findManagementFragment()?.setSystemBarStyle()
        } else {
            findHomeFragment()?.setSystemBarStyle()
        }
    }

    private fun updateDayNightColors() {
        fitHorizontalSystemBarRootLayout.setBackgroundColor(
            MainThemeColorProvider.getColor(
                lightTheme =  !DisplayUtils.isDarkMode(this),
                id = android.R.attr.colorBackground
            )
        )
    }

    private val isOrWillManagementFragmentVisible: Boolean
        get() = if (binding.drawerLayout != null) {
            binding.drawerLayout!!.isUnfold
        } else {
            findManagementFragment()?.let { !it.isRemoving } ?: false
        }

    private val isManagementFragmentVisible: Boolean
        get() = if (binding.drawerLayout != null) {
            binding.drawerLayout!!.isUnfold
        } else {
            findManagementFragment()?.isVisible ?: false
        }

    fun setManagementFragmentVisibility(visible: Boolean) {
        if (binding.drawerLayout != null) {
            binding.drawerLayout!!.isUnfold = visible
        } else if (visible != isOrWillManagementFragmentVisible) {
            if (visible) {
                val transaction = supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(
                        R.anim.fragment_manange_enter,
                        R.anim.fragment_main_exit,
                        R.anim.fragment_main_pop_enter,
                        R.anim.fragment_manange_pop_exit
                    )
                    .add(
                        R.id.fragment,
                        PushedManagementFragment.getInstance(),
                        TAG_FRAGMENT_MANAGEMENT
                    )
                    .addToBackStack(null)

                findHomeFragment()?.let {
                    transaction.hide(it)
                }

                transaction.commit()
            } else {
                supportFragmentManager.popBackStack()
            }
        }
    }

    private fun findHomeFragment(): HomeFragment? {
        return if (binding.drawerLayout == null) {
            supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_HOME) as HomeFragment?
        } else {
            supportFragmentManager.findFragmentById(R.id.fragment_home) as HomeFragment?
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
            AsyncHelper.delayRunOnIO({
                PollingManager.resetAllBackgroundTask(
                    this, false
                )
            }, 1000)
        }
        if (updateRemoteViews && locationList != null && locationList.isNotEmpty()) {
            AsyncHelper.delayRunOnIO({
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
        setManagementFragmentVisibility(!isOrWillManagementFragmentVisible)
    }

    override fun onSettingsIconClicked() {
        IntentHelper.startSettingsActivity(this)
    }

    // management fragment callback.

    override fun onSearchBarClicked(searchBar: View) {
        IntentHelper.startSearchActivityForResult(this, searchBar, SEARCH_ACTIVITY)
    }

    override fun onSelectProviderActivityStarted() {
        IntentHelper.startSelectProviderActivity(this)
    }
}