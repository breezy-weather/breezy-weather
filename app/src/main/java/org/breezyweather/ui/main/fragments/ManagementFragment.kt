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

package org.breezyweather.ui.main.fragments

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import breezyweather.domain.location.model.Location
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.extensions.isDarkMode
import org.breezyweather.common.extensions.plus
import org.breezyweather.common.source.LocationPreset
import org.breezyweather.common.source.getName
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.common.utils.helpers.PermissionHelper
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.domain.location.model.getPlace
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.sources.SourceManager
import org.breezyweather.ui.common.composables.AlertDialogNoPadding
import org.breezyweather.ui.common.composables.NotificationCard
import org.breezyweather.ui.common.composables.SecondarySourcesPreference
import org.breezyweather.ui.common.decorations.Material3ListItemDecoration
import org.breezyweather.ui.common.widgets.Material3CardListItem
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.defaultCardListItemElevation
import org.breezyweather.ui.common.widgets.insets.BWCenterAlignedTopAppBar
import org.breezyweather.ui.main.MainActivity
import org.breezyweather.ui.main.MainActivityViewModel
import org.breezyweather.ui.main.adapters.LocationAdapterAnimWrapper
import org.breezyweather.ui.main.adapters.location.LocationAdapter
import org.breezyweather.ui.main.widgets.LocationItemTouchCallback
import org.breezyweather.ui.main.widgets.LocationItemTouchCallback.TouchReactor
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme
import org.breezyweather.ui.theme.compose.DayNightTheme
import org.breezyweather.ui.theme.compose.themeRipple
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import org.breezyweather.ui.theme.resource.providers.ResourceProvider

class PushedManagementFragment : ManagementFragment() {

    companion object {
        fun getInstance() = PushedManagementFragment()
    }

    override fun setSystemBarStyle() {
        ThemeManager
            .getInstance(requireContext())
            .weatherThemeDelegate
            .setSystemBarStyle(
                requireActivity().window,
                statusShader = false,
                lightStatus = !requireActivity().isDarkMode,
                lightNavigation = !requireActivity().isDarkMode
            )
    }
}

open class ManagementFragment : MainModuleFragment(), TouchReactor {

    protected lateinit var viewModel: MainActivityViewModel

    private lateinit var layout: LinearLayoutManager
    private lateinit var adapter: LocationAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterAnimWrapper: LocationAdapterAnimWrapper
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var resourceProvider: ResourceProvider? = null

    private var scrollOffset = 0f
    private var callback: Callback? = null

    interface Callback {
        fun onSearchBarClicked()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        initModel()
        initView()
        setCallback(requireActivity() as Callback)

        return ComposeView(requireContext()).apply {
            setContent {
                BreezyWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                    ContentView()
                }
            }
        }
    }

    @Composable
    private fun ContentView() {
        ensureResourceProvider()

        val validLocationListState = viewModel.validLocationList.collectAsState()
        var notificationDismissed by remember { mutableStateOf(false) }
        var notificationAppUpdateCheckDismissed by remember { mutableStateOf(false) }

        val dialogChooseWeatherSourcesOpenState = viewModel.dialogChooseWeatherSourcesOpen.collectAsState()
        val selectedLocationState = viewModel.selectedLocation.collectAsState()

        val dialogChooseDebugLocationOpenState = viewModel.dialogChooseDebugLocationOpen.collectAsState()

        /*
         * We should add a scroll behavior to make the top bar change color when scrolling, but
         * as we mix ComposeView and XML views, this leads to stuttering in scrolling.
         * Implement it later once we replace the RecyclerView as a LazyList. It’s not an easy
         * task as we need to implement drag & drop and swipe left/right
         */
        Material3Scaffold(
            topBar = {
                BWCenterAlignedTopAppBar(
                    title = stringResource(R.string.locations),
                    onBackPressed = {
                        (requireActivity() as MainActivity).setManagementFragmentVisibility(false)
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                activity?.let { IntentHelper.startSettingsActivity(it) }
                            }
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_settings),
                                contentDescription = stringResource(R.string.action_settings),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                )
            },
            floatingActionButton = {
                if (validLocationListState.value.isNotEmpty()) {
                    Column {
                        if (BreezyWeather.instance.debugMode) {
                            FloatingActionButton(
                                onClick = {
                                    viewModel.openChooseDebugLocationDialog()
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.BugReport,
                                    stringResource(R.string.action_add_debug_location)
                                )
                            }
                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
                        }
                        if (validLocationListState.value.firstOrNull { it.isCurrentPosition } == null) {
                            FloatingActionButton(
                                onClick = {
                                    viewModel.openChooseWeatherSourcesDialog(null)
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.MyLocation,
                                    stringResource(R.string.action_add_current_location)
                                )
                            }
                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
                        }
                        FloatingActionButton(
                            onClick = {
                                callback?.onSearchBarClicked()
                            }
                        ) {
                            Icon(
                                Icons.Outlined.Add,
                                stringResource(R.string.action_add_new_location)
                            )
                        }
                    }
                }
            }
        ) { paddings ->
            if (validLocationListState.value.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            // Do not set a horizontal padding as this adds too much padding in
                            // landscape mode.
                            top = paddings.calculateTopPadding(),
                            bottom = paddings.calculateBottomPadding(),
                            start = dimensionResource(R.dimen.normal_margin),
                            end = dimensionResource(R.dimen.normal_margin)
                        )
                ) {
                    if (!viewModel.statementManager.isPostNotificationDialogAlreadyShown &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !notificationDismissed
                    ) {
                        val notificationPermissionState =
                            rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
                        if (notificationPermissionState.status != PermissionStatus.Granted) {
                            NotificationCard(
                                title = stringResource(R.string.dialog_permissions_notification_title),
                                summary = stringResource(R.string.dialog_permissions_notification_content),
                                onClick = {
                                    viewModel.statementManager.setPostNotificationDialogAlreadyShown()
                                    notificationDismissed = true

                                    PermissionHelper.requestPermissionWithFallback(
                                        activity = requireActivity(),
                                        permission = Manifest.permission.POST_NOTIFICATIONS,
                                        fallback = {
                                            IntentHelper.startNotificationSettingsActivity(requireActivity())
                                        }
                                    )
                                },
                                onClose = {
                                    viewModel.statementManager.setPostNotificationDialogAlreadyShown()
                                    notificationDismissed = true
                                    /*
                                     * We could turn off alert notification from SettingsManager, but
                                     * it’s best not to, as the user can still enable notification
                                     * permission again from Android settings, and there is a
                                     * permission check before sending any notification even if
                                     * preference is enabled.
                                     */
                                }
                            )
                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                        }
                    }
                    if (!viewModel.statementManager.isAppUpdateCheckDialogAlreadyShown &&
                        !notificationAppUpdateCheckDismissed
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val notificationPermissionState =
                                rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
                            if (notificationPermissionState.status == PermissionStatus.Granted) {
                                NotificationCard(
                                    title = stringResource(R.string.dialog_app_update_check_title),
                                    summary = stringResource(R.string.dialog_app_update_check_content),
                                    onClick = {
                                        viewModel.statementManager.setAppUpdateCheckDialogAlreadyShown()
                                        notificationAppUpdateCheckDismissed = true
                                        SettingsManager.getInstance(requireContext()).isAppUpdateCheckEnabled = true
                                    },
                                    onClose = {
                                        viewModel.statementManager.setAppUpdateCheckDialogAlreadyShown()
                                        notificationAppUpdateCheckDismissed = true
                                    }
                                )
                                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                            }
                        } else {
                            NotificationCard(
                                title = stringResource(R.string.dialog_app_update_check_title),
                                summary = stringResource(R.string.dialog_app_update_check_content),
                                onClick = {
                                    viewModel.statementManager.setAppUpdateCheckDialogAlreadyShown()
                                    notificationAppUpdateCheckDismissed = true
                                    SettingsManager.getInstance(requireContext()).isAppUpdateCheckEnabled = true
                                },
                                onClose = {
                                    viewModel.statementManager.setAppUpdateCheckDialogAlreadyShown()
                                    notificationAppUpdateCheckDismissed = true
                                }
                            )
                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                        }
                    }
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .clipToBounds(),
                        factory = {
                            recyclerView
                        }
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            // Do not set a horizontal padding as this adds too much padding in
                            // landscape mode.
                            PaddingValues(
                                top = paddings.calculateTopPadding(),
                                bottom = paddings.calculateBottomPadding() + dimensionResource(R.dimen.large_margin)
                            ) + PaddingValues(horizontal = dimensionResource(R.dimen.normal_margin))
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.large_margin))
                    ) {
                        Button(
                            onClick = {
                                callback?.onSearchBarClicked()
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.action_add_new_location),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.openChooseWeatherSourcesDialog(null)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MyLocation,
                                contentDescription = stringResource(R.string.location_current),
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Text(
                                text = stringResource(R.string.action_add_current_location),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                        if (BreezyWeather.instance.debugMode) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.openChooseDebugLocationDialog()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.BugReport,
                                    contentDescription = stringResource(R.string.settings_debug),
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                                Text(
                                    text = stringResource(R.string.action_add_debug_location),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        if (dialogChooseWeatherSourcesOpenState.value) {
            SecondarySourcesPreference(
                sourceManager = (requireActivity() as MainActivity).sourceManager,
                location = selectedLocationState.value
                    ?: LocationPreset.getLocationWithPresetApplied(Location(isCurrentPosition = true)),
                onClose = { newLocation: Location? ->
                    viewModel.closeChooseWeatherSourcesDialog()

                    if (newLocation != null) {
                        // If coming from an existing location
                        if (selectedLocationState.value != null) {
                            // If main source was changed, we need to check first that it doesn't create
                            // a duplicate
                            if (selectedLocationState.value!!.forecastSource != newLocation.forecastSource) {
                                if (viewModel.locationExists(newLocation)) {
                                    SnackbarHelper.showSnackbar(getString(R.string.location_message_already_exists))
                                } else {
                                    viewModel.updateLocation(newLocation, selectedLocationState.value!!)
                                    SnackbarHelper.showSnackbar(getString(R.string.location_message_updated))
                                }
                            } else {
                                viewModel.updateLocation(newLocation, selectedLocationState.value!!)
                                SnackbarHelper.showSnackbar(getString(R.string.location_message_updated))
                            }
                        } else {
                            if (viewModel.locationExists(newLocation)) {
                                SnackbarHelper.showSnackbar(getString(R.string.location_message_already_exists))
                            } else {
                                viewModel.addLocation(newLocation, null)
                                SnackbarHelper.showSnackbar(getString(R.string.location_message_added))
                            }
                        }
                    }
                },
                locationExists = { loc: Location ->
                    viewModel.locationExists(loc)
                }
            )
        }

        if (BreezyWeather.instance.debugMode && dialogChooseDebugLocationOpenState.value) {
            DebugLocationScreen(
                sourceManager = (requireActivity() as MainActivity).sourceManager,
                onClose = { addedLocation: Location? ->
                    viewModel.closeChooseDebugLocationDialog()

                    if (addedLocation != null) {
                        if (viewModel.locationExists(addedLocation)) {
                            SnackbarHelper.showSnackbar(getString(R.string.location_message_already_exists))
                        } else {
                            viewModel.addLocation(addedLocation, null)
                            SnackbarHelper.showSnackbar(getString(R.string.location_message_added))
                        }
                    }
                }
            )
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (enter && nextAnim != 0) {
            adapterAnimWrapper.setLastPosition(-1)
        }
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

    override fun setSystemBarStyle() {
        // do nothing.
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val firstHolderPosition = layout.findFirstVisibleItemPosition()
        adapter.notifyItemRangeChanged(
            firstHolderPosition,
            layout.findLastVisibleItemPosition() - firstHolderPosition + 1
        )
    }

    private fun initModel() {
        viewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
    }

    private fun initView() {
        adapter =
            LocationAdapter(
                requireActivity(),
                ArrayList(),
                null,
                (requireActivity() as MainActivity).sourceManager,
                mClickListener = { formattedId ->
                    viewModel.setLocation(formattedId)
                    parentFragmentManager.popBackStack()
                }
            ) { holder ->
                itemTouchHelper.startDrag(holder)
            }
        adapterAnimWrapper = LocationAdapterAnimWrapper(
            requireContext(),
            adapter
        ).apply {
            setLastPosition(Int.MAX_VALUE)
        }
        recyclerView = RecyclerView(requireContext())
        recyclerView.adapter = adapterAnimWrapper
        recyclerView.layoutManager = LinearLayoutManager(
            requireActivity(),
            RecyclerView.VERTICAL,
            false
        ).also { layout = it }
        while (recyclerView.itemDecorationCount > 0) {
            recyclerView.removeItemDecorationAt(0)
        }
        recyclerView.addItemDecoration(
            Material3ListItemDecoration(
                requireContext()
            )
        )
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                scrollOffset = recyclerView.computeVerticalScrollOffset().toFloat()

                if (dy != 0) {
                    adapterAnimWrapper.setScrolled()
                }
            }
        })

        itemTouchHelper = ItemTouchHelper(
            LocationItemTouchCallback(
                requireActivity() as GeoActivity,
                viewModel,
                this
            )
        )
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Start a coroutine in the lifecycle scope
        // FIXME: Race condition
        lifecycleScope.launch {
            // repeatOnLifecycle launches the block in a new coroutine every time the
            // lifecycle is in the STARTED state (or above) and cancels it when it's STOPPED.
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Trigger the flow and start listening for values.
                // Note that this happens when lifecycle is STARTED and stops
                // collecting when the lifecycle is STOPPED
                viewModel.validLocationList.collect {
                    // New value received
                    adapter.update(it, viewModel.currentLocation.value?.location?.formattedId)
                }
            }
        }
        lifecycleScope.launch {
            // repeatOnLifecycle launches the block in a new coroutine every time the
            // lifecycle is in the STARTED state (or above) and cancels it when it's STOPPED.
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Trigger the flow and start listening for values.
                // Note that this happens when lifecycle is STARTED and stops
                // collecting when the lifecycle is STOPPED
                viewModel.currentLocation.collect {
                    // New value received
                    adapter.update(viewModel.validLocationList.value, it?.location?.formattedId)
                }
            }
        }
    }

    fun prepareReenterTransition() {
        // TODO
        postponeEnterTransition()
        startPostponedEnterTransition()
    }

    // interface.
    private fun setCallback(l: Callback?) {
        callback = l
    }

    // location item touch reactor.
    override fun resetViewHolderAt(position: Int) {
        adapter.notifyItemChanged(position)
    }

    override fun reorderByDrag(from: Int, to: Int) {
        adapter.update(from, to)
    }

    private fun ensureResourceProvider() {
        val iconProvider = SettingsManager
            .getInstance(requireContext())
            .iconProvider
        if (resourceProvider == null || resourceProvider!!.packageName != iconProvider) {
            resourceProvider = ResourcesProviderFactory.newInstance
        }
    }

    @Composable
    fun DebugLocationScreen(
        sourceManager: SourceManager,
        onClose: ((location: Location?) -> Unit),
        modifier: Modifier = Modifier,
    ) {
        val context = LocalContext.current

        AlertDialogNoPadding(
            modifier = modifier,
            onDismissRequest = {
                onClose(null)
            },
            title = {
                Text(
                    text = stringResource(R.string.action_add_debug_location),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(sourceManager.getWeatherSources().filter { it.testingLocations.isNotEmpty() }) {
                        val enabled = !viewModel.locationExists(it.testingLocations[0])

                        Material3CardListItem(
                            elevation = if (enabled) defaultCardListItemElevation else 0.dp
                        ) {
                            ListItem(
                                tonalElevation = if (enabled) defaultCardListItemElevation else 0.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(if (enabled) 1f else 0.5f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = themeRipple(),
                                        onClick = { onClose(it.testingLocations[0]) },
                                        enabled = enabled
                                    ),
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                ),
                                headlineContent = {
                                    Text(
                                        it.getName(context),
                                        fontWeight = FontWeight.Bold,
                                        color = DayNightTheme.colors.titleColor
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        it.testingLocations[0].getPlace(context),
                                        color = DayNightTheme.colors.bodyColor
                                    )
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClose(null)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_close),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        )
    }
}
