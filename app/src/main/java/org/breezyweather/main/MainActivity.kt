/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.core.view.updatePadding
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import breezyweather.domain.location.model.Location
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.breezyweather.Migrations
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.bus.EventBus
import org.breezyweather.common.extensions.doOnApplyWindowInsets
import org.breezyweather.common.extensions.hasPermission
import org.breezyweather.common.extensions.isDarkMode
import org.breezyweather.common.snackbar.SnackbarContainer
import org.breezyweather.common.ui.composables.AlertDialogNoPadding
import org.breezyweather.common.ui.composables.LocationPreference
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.databinding.ActivityMainBinding
import org.breezyweather.main.fragments.HomeFragment
import org.breezyweather.main.fragments.ManagementFragment
import org.breezyweather.main.fragments.ModifyMainSystemBarMessage
import org.breezyweather.main.fragments.PushedManagementFragment
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.search.SearchActivity
import org.breezyweather.settings.SettingsChangedMessage
import org.breezyweather.sources.SourceManager
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.compose.DayNightTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : GeoActivity(),
    HomeFragment.Callback,
    ManagementFragment.Callback {

    @Inject
    lateinit var sourceManager: SourceManager

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    private val _dialogPerLocationSettingsOpen = MutableStateFlow(false)
    val dialogPerLocationSettingsOpen = _dialogPerLocationSettingsOpen.asStateFlow()
    private var dialogPerLocationAlert: AlertDialog? = null

    companion object {
        const val SEARCH_ACTIVITY = 4

        const val ACTION_MAIN = "org.breezyweather.Main"
        const val KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID = "MAIN_ACTIVITY_LOCATION_FORMATTED_ID"
        const val KEY_MAIN_ACTIVITY_ALERT_ID = "MAIN_ACTIVITY_ALERT_ID"

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

            // TODO: Leads to annoying popup, disabling for now
            /*if (isActivityStarted &&
                it.formattedId == viewModel.currentLocation.value?.location?.formattedId) {
                SnackbarHelper.showSnackbar(getString(R.string.message_updated_in_background))
            }*/
        }
    }

    fun updateLocation(location: Location) {
        if (!viewModel.initCompleted.value) {
            return
        }

        // Only updates are coming here (no location added or deleted)
        // If we don't find the formattedId in the current list, it means main source was changed
        // for currently focused location
        // TODO: This shouldn't be the case anymore, as only WeatherUpdateJob comes here
        val oldLocation = viewModel.validLocationList.value.firstOrNull {
            it.formattedId == location.formattedId
        } ?: viewModel.currentLocation.value?.location

        if (viewModel.currentLocation.value?.location?.formattedId == (oldLocation?.formattedId ?: location.formattedId)) {
            viewModel.cancelRequest()
        }
        viewModel.updateLocation(location, oldLocation)
    }

    fun deleteLocation(location: Location) {
        if (locationListSize() > 1) {
            val position: Int = viewModel.validLocationList.value.indexOfFirst {
                it.formattedId == location.formattedId
            }
            if (position >= 0) {
                viewModel.deleteLocation(position)
                SnackbarHelper.showSnackbar(
                    this.getString(R.string.location_message_deleted)
                )
            }
        } else {
            SnackbarHelper.showSnackbar(
                this.getString(R.string.location_message_list_cannot_be_empty)
            )
        }
    }

    fun locationListSize(): Int {
        return viewModel.locationListSize()
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
        installSplashScreen()

        val isLaunch = savedInstanceState == null

        super.onCreate(savedInstanceState)

        if (isLaunch) {
            Migrations.upgrade(applicationContext)
        }

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
            .observeForever(backgroundUpdateObserver) // Only comes from WeatherUpdateJob
        EventBus.instance.with(SettingsChangedMessage::class.java).observe(this) {
            // Force refresh but with latest location used
            viewModel.init(viewModel.currentLocation.value?.location?.formattedId)

            findHomeFragment()?.updateViews()

            refreshBackgroundViews(viewModel.validLocationList.value)
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
                    SnackbarHelper.showSnackbar(getString(R.string.location_message_added))
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateSystemBarStyle()
        updateDayNightColors()
    }

    override fun onPause() {
        super.onPause()
        dialogPerLocationAlert?.dismiss()
    }

    override fun onStart() {
        super.onStart()
        viewModel.checkToUpdate()

        binding.root.doOnApplyWindowInsets { view, insets ->
            if (this.getResources().configuration.orientation == 2) {
                // Apply root insets in landscape mode for a consistent look across different
                // device types and navigation modes.
                view.updatePadding(
                    left = insets.left,
                    right = insets.right
                )
            } else {
                view.updatePadding(
                    left = 0,
                    right = 0
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentsLifecycleCallback)
        EventBus.instance
            .with(Location::class.java)
            .removeObserver(backgroundUpdateObserver)
    }

    override val snackbarContainer: SnackbarContainer
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
        if (!viewModel.checkIsNewInstance()) return
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

        // Start a coroutine in the lifecycle scope
        lifecycleScope.launch {
            // repeatOnLifecycle launches the block in a new coroutine every time the
            // lifecycle is in the STARTED state (or above) and cancels it when it's STOPPED.
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Trigger the flow and start listening for values.
                // Note that this happens when lifecycle is STARTED and stops
                // collecting when the lifecycle is STOPPED
                viewModel.validLocationList.collect {
                    if (it.isEmpty()) {
                        setManagementFragmentVisibility(true)
                    }
                    refreshBackgroundViews(it)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.locationPermissionsRequest.collect {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        it != null &&
                        it.permissionList.isNotEmpty() &&
                        it.consume()
                    ) {
                        // only show dialog if we need request basic location permissions.
                        var showLocationPermissionDialog = false
                        for (permission in it.permissionList) {
                            if (isLocationPermission(permission)) {
                                showLocationPermissionDialog = true
                                break
                            }
                        }

                        if (showLocationPermissionDialog && !viewModel.statementManager.isLocationPermissionDialogAlreadyShown) {
                            dialogPerLocationAlert = MaterialAlertDialogBuilder(this@MainActivity)
                                .setTitle(R.string.dialog_permissions_location_title)
                                .setMessage(R.string.dialog_permissions_location_content)
                                .setPositiveButton(R.string.action_next) { _, _ ->
                                    // mark declared.
                                    viewModel.statementManager.setLocationPermissionDialogAlreadyShown()

                                    val request = viewModel.locationPermissionsRequest.value
                                    if (request != null &&
                                        request.permissionList.isNotEmpty() &&
                                        request.target != null
                                    ) {
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
                }
            }
        }
        viewModel.snackbarError.observe(this) { error ->
            if (error != null) {
                val shortMessage = if (!error.source.isNullOrEmpty()) {
                    "${error.source}${getString(R.string.colon_separator)}${getString(error.error.shortMessage)}"
                } else getString(error.error.shortMessage)
                error.error.showDialogAction?.let { showDialogAction ->
                    SnackbarHelper.showSnackbar(
                        content = shortMessage,
                        action = getString(error.error.actionButtonMessage)
                    ) {
                        showDialogAction(this)
                    }
                } ?: SnackbarHelper.showSnackbar(shortMessage)
            }
        }

        initPerLocationSettingsView()
    }

    private fun initPerLocationSettingsView() {
        binding.perLocationSettings.setContent {
            val validLocation = viewModel.currentLocation.collectAsState()

            BreezyWeatherTheme(
                lightTheme = MainThemeColorProvider.isLightTheme(this, validLocation.value?.daylight)
            ) {
                PerLocationSettingsDialog(location = validLocation.value?.location)
            }
        }
    }

    @Composable
    fun PerLocationSettingsDialog(
        location: Location?
    ) {
        val dialogPerLocationSettingsOpenState = dialogPerLocationSettingsOpen.collectAsState()
        if (dialogPerLocationSettingsOpenState.value) {
            location?.let {
                val dialogDeleteLocationOpenState = remember { mutableStateOf(false) }
                AlertDialogNoPadding(
                    onDismissRequest = {
                        _dialogPerLocationSettingsOpen.value = false
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.action_edit_location),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    },
                    text = {
                        LocationPreference(this, it) { newLocation: Location? ->
                            if (newLocation != null) {
                                updateLocation(newLocation)
                            }
                            _dialogPerLocationSettingsOpen.value = false
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                _dialogPerLocationSettingsOpen.value = false
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.action_close),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    },
                    dismissButton = if (locationListSize() > 1) {
                        {
                            TextButton(
                                onClick = {
                                    dialogDeleteLocationOpenState.value = true
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.action_delete),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    } else null
                )

                if (dialogDeleteLocationOpenState.value) {
                    AlertDialog(
                        onDismissRequest = {
                            dialogDeleteLocationOpenState.value = false
                        },
                        title = {
                            Text(
                                text = stringResource(R.string.location_delete_location_dialog_title),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        },
                        text = {
                            Text(
                                text = if (it.city.isNotEmpty()) {
                                    stringResource(
                                        R.string.location_delete_location_dialog_message,
                                        it.city
                                    )
                                } else {
                                    stringResource(R.string.location_delete_location_dialog_message_no_name)
                                },
                                color = DayNightTheme.colors.bodyColor,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    dialogDeleteLocationOpenState.value = false
                                    _dialogPerLocationSettingsOpen.value = false
                                    deleteLocation(it)
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.action_confirm),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    dialogDeleteLocationOpenState.value = false
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.action_cancel),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    )
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

        val request = viewModel.locationPermissionsRequest.value
        if (request == null || request.permissionList.isEmpty() || request.target == null) {
            return
        }

        grantResults.zip(permissions).firstOrNull { // result, permission
            it.first != PackageManager.PERMISSION_GRANTED &&
                isEssentialLocationPermission(permission = it.second)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            !viewModel.statementManager.isBackgroundLocationPermissionDialogAlreadyShown &&
            !this.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_permissions_location_background_title)
                .setMessage(R.string.dialog_permissions_location_background_content)
                .setPositiveButton(R.string.action_set) { _, _ ->
                    // mark background location permission declared.
                    viewModel.statementManager.setBackgroundLocationPermissionDialogAlreadyShown()
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
        permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION ||
            isEssentialLocationPermission(permission)
    } else isEssentialLocationPermission(permission)

    private fun isEssentialLocationPermission(permission: String): Boolean {
        return permission == Manifest.permission.ACCESS_COARSE_LOCATION ||
            permission == Manifest.permission.ACCESS_FINE_LOCATION
    }

    private val isLocationPermissionsGranted: Boolean
        get() = this.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ||
            this.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    val isDaylight: Boolean
        get() = viewModel.currentLocation.value?.daylight ?: true

    // control.

    private fun consumeIntentAction(intent: Intent) {
        val action = intent.action
        if (action.isNullOrEmpty()) return
        val formattedId = intent.getStringExtra(KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID)
        if (ACTION_SHOW_ALERTS == action) {
            val alertId = intent.getStringExtra(KEY_MAIN_ACTIVITY_ALERT_ID)
            if (!alertId.isNullOrEmpty()) {
                IntentHelper.startAlertActivity(this, formattedId, alertId)
            } else {
                IntentHelper.startAlertActivity(this, formattedId)
            }
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
        if (this.getResources().configuration.orientation == 2) {
            // Set a black background to keep the background of the system bars black when root
            // insets are applied in landscape mode.
            binding.root.setBackgroundColor(Color.BLACK)
        } else {
            binding.root.setBackgroundColor(
                MainThemeColorProvider.getColor(
                    lightTheme = !this.isDarkMode,
                    id = android.R.attr.colorBackground
                )
            )
        }
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
        if (visible == isOrWillManagementFragmentVisible) return
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

    private fun refreshBackgroundViews(locationList: List<Location>?) {
        viewModel.refreshBackgroundViews(this, locationList)
    }

    // interface.

    // main fragment callback.
    override fun onEditIconClicked() {
        _dialogPerLocationSettingsOpen.value = true
        initPerLocationSettingsView()
    }

    override fun onManageIconClicked() {
        setManagementFragmentVisibility(!isOrWillManagementFragmentVisible)
    }

    override fun onSettingsIconClicked() {
        IntentHelper.startSettingsActivity(this)
    }

    // management fragment callback.

    override fun onSearchBarClicked() {
        IntentHelper.startSearchActivityForResult(this, SEARCH_ACTIVITY)
    }
}
