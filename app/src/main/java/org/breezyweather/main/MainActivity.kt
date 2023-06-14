package org.breezyweather.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.bus.EventBus
import org.breezyweather.databinding.ActivityMainBinding
import org.breezyweather.main.fragments.HomeFragment
import org.breezyweather.main.fragments.ManagementFragment
import org.breezyweather.main.fragments.ModifyMainSystemBarMessage
import org.breezyweather.main.fragments.PushedManagementFragment
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.search.SearchActivity
import org.breezyweather.settings.SettingsChangedMessage

@AndroidEntryPoint
class MainActivity : GeoActivity(),
    HomeFragment.Callback,
    ManagementFragment.Callback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    companion object {
        const val SEARCH_ACTIVITY = 4

        const val ACTION_MAIN = "org.breezyweather.Main"
        const val KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID = "MAIN_ACTIVITY_LOCATION_FORMATTED_ID"

        const val ACTION_MANAGEMENT = "org.breezyweather.ACTION_MANAGEMENT"
        const val ACTION_SHOW_ALERTS = "org.breezyweather.ACTION_SHOW_ALERTS"

        const val ACTION_SHOW_DAILY_FORECAST = "org.breezyweather.ACTION_SHOW_DAILY_FORECAST"
        const val KEY_DAILY_INDEX = "DAILY_INDEX"

        private const val TAG_FRAGMENT_HOME = "fragment_main"
        private const val TAG_FRAGMENT_MANAGEMENT = "fragment_management"
    }

    private val backgroundUpdateObserver: Observer<Location> = Observer { location ->
        location.let {
            viewModel.updateLocationFromBackground(it)

            if (isActivityStarted
                && it.formattedId == viewModel.currentLocation.value?.location?.formattedId) {
                org.breezyweather.common.utils.helpers.SnackbarHelper.showSnackbar(getString(R.string.feedback_updated_in_background))
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
            org.breezyweather.common.utils.helpers.AsyncHelper.runOnIO {
                org.breezyweather.remoteviews.NotificationHelper.updateNotificationIfNecessary(this, viewModel.validLocationList.value.first)
            }
            refreshBackgroundViews(
                resetBackground = true,
                locationList = viewModel.validLocationList.value.first,
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SEARCH_ACTIVITY -> if (resultCode == RESULT_OK && data != null) {
                val location: Location? = data.getParcelableExtra(SearchActivity.KEY_LOCATION)
                if (location != null) {
                    viewModel.addLocation(location, null)
                    org.breezyweather.common.utils.helpers.SnackbarHelper.showSnackbar(getString(R.string.feedback_collect_succeed))
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

    override val snackbarContainer: org.breezyweather.common.snackbar.SnackbarContainer?
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

        val context = this
        // Start a coroutine in the lifecycle scope
        lifecycleScope.launch {
            // repeatOnLifecycle launches the block in a new coroutine every time the
            // lifecycle is in the STARTED state (or above) and cancels it when it's STOPPED.
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Trigger the flow and start listening for values.
                // Note that this happens when lifecycle is STARTED and stops
                // collecting when the lifecycle is STOPPED
                viewModel.validLocationList.collect {
                    // update notification immediately.
                    org.breezyweather.common.utils.helpers.AsyncHelper.runOnIO {
                        org.breezyweather.remoteviews.NotificationHelper.updateNotificationIfNecessary(
                            context,
                            it.first
                        )
                    }
                    refreshBackgroundViews(
                        resetBackground = false,
                        locationList = it.first,
                    )
                }
            }
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
                        org.breezyweather.common.utils.helpers.SnackbarHelper.showSnackbar(
                            getString(R.string.feedback_location_failed),
                            getString(R.string.help)
                        ) {
                            org.breezyweather.main.dialogs.LocationHelpDialog.show(this)
                        }
                    }
                    MainMessage.WEATHER_REQ_FAILED -> {
                        org.breezyweather.common.utils.helpers.SnackbarHelper.showSnackbar(
                            getString(R.string.feedback_get_weather_failed)
                        )
                    }
                    MainMessage.API_LIMIT_REACHED -> {
                        org.breezyweather.common.utils.helpers.SnackbarHelper.showSnackbar(
                            getString(R.string.feedback_api_limit_reached),
                            getString(R.string.help)
                        ) {
                            org.breezyweather.main.dialogs.ApiLimitReachedHelp.show(this)
                        }
                    }
                    MainMessage.API_UNAUTHORIZED -> {
                        org.breezyweather.common.utils.helpers.SnackbarHelper.showSnackbar(
                            getString(R.string.feedback_api_unauthorized),
                            getString(R.string.help)
                        ) {
                            org.breezyweather.main.dialogs.ApiUnauthorizedHelp.show(this)
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
        get() = viewModel.currentLocation.value?.daylight

    // control.

    private fun consumeIntentAction(intent: Intent) {
        val action = intent.action
        if (action.isNullOrEmpty()) {
            return
        }
        val formattedId = intent.getStringExtra(KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID)
        if (ACTION_SHOW_ALERTS == action) {
            org.breezyweather.common.utils.helpers.IntentHelper.startAlertActivity(this, formattedId)
            return
        }
        if (ACTION_SHOW_DAILY_FORECAST == action) {
            val index = intent.getIntExtra(KEY_DAILY_INDEX, 0)
            org.breezyweather.common.utils.helpers.IntentHelper.startDailyWeatherActivity(this, formattedId, index)
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
                lightTheme =  !org.breezyweather.common.utils.DisplayUtils.isDarkMode(this),
                id = android.R.attr.colorBackground
            )
        )
    }

    private val isOrWillManagementFragmentVisible: Boolean
        get() = binding.drawerLayout?.isUnfold
            ?: findManagementFragment()?.let { !it.isRemoving }
            ?: false

    private val isManagementFragmentVisible: Boolean
        get() = binding.drawerLayout?.isUnfold
            ?: findManagementFragment()?.isVisible
            ?: false

    fun setManagementFragmentVisibility(visible: Boolean) {
        val drawerLayout = binding.drawerLayout
        if (drawerLayout != null) {
            drawerLayout.isUnfold = visible
            return
        }
        if (visible == isOrWillManagementFragmentVisible) {
            return
        }
        if (!visible) {
            supportFragmentManager.popBackStack()
            return
        }

        val transaction = supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.fragment_manage_enter,
                R.anim.fragment_main_exit,
                R.anim.fragment_main_pop_enter,
                R.anim.fragment_manage_pop_exit,
            )
            .add(
                R.id.fragment,
                PushedManagementFragment.getInstance(),
                TAG_FRAGMENT_MANAGEMENT,
            )
            .addToBackStack(null)

        findHomeFragment()?.let {
            transaction.hide(it)
        }

        transaction.commit()
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

    private fun refreshBackgroundViews(resetBackground: Boolean, locationList: List<Location>?) {
        if (resetBackground) {
            org.breezyweather.common.utils.helpers.AsyncHelper.delayRunOnIO({
                org.breezyweather.background.polling.PollingManager.resetAllBackgroundTask(
                    this, false
                )
            }, 1000)
        }
        locationList?.let {
            if (it.isNotEmpty()) {
                org.breezyweather.common.utils.helpers.AsyncHelper.delayRunOnIO({
                    org.breezyweather.remoteviews.WidgetHelper.updateWidgetIfNecessary(this, it[0])
                    org.breezyweather.remoteviews.NotificationHelper.updateNotificationIfNecessary(this, it)
                    org.breezyweather.remoteviews.WidgetHelper.updateWidgetIfNecessary(this, it)
                }, 1000)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    org.breezyweather.common.utils.helpers.ShortcutsHelper.refreshShortcutsInNewThread(this, it)
                }
            }
        }
    }

    // interface.

    // main fragment callback.

    override fun onManageIconClicked() {
        setManagementFragmentVisibility(!isOrWillManagementFragmentVisible)
    }

    override fun onSettingsIconClicked() {
        org.breezyweather.common.utils.helpers.IntentHelper.startSettingsActivity(this)
    }

    // management fragment callback.

    override fun onSearchBarClicked() {
        org.breezyweather.common.utils.helpers.IntentHelper.startSearchActivityForResult(this, SEARCH_ACTIVITY)
    }

    override fun onSelectProviderActivityStarted() {
        org.breezyweather.common.utils.helpers.IntentHelper.startSelectProviderActivity(this)
    }
}