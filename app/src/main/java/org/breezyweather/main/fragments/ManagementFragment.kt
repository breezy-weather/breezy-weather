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

package org.breezyweather.main.fragments

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
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
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.extensions.isDarkMode
import org.breezyweather.common.extensions.plus
import org.breezyweather.common.extensions.setSystemBarStyle
import org.breezyweather.common.ui.composables.NotificationCard
import org.breezyweather.common.ui.composables.SecondarySourcesPreference
import org.breezyweather.common.ui.decorations.Material3ListItemDecoration
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.insets.BWCenterAlignedTopAppBar
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.main.MainActivity
import org.breezyweather.main.MainActivityViewModel
import org.breezyweather.main.adapters.LocationAdapterAnimWrapper
import org.breezyweather.main.adapters.location.LocationAdapter
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.main.widgets.LocationItemTouchCallback
import org.breezyweather.main.widgets.LocationItemTouchCallback.TouchReactor
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.compose.DayNightTheme
import org.breezyweather.theme.resource.ResourcesProviderFactory
import org.breezyweather.theme.resource.providers.ResourceProvider

class PushedManagementFragment : ManagementFragment() {

    companion object {
        fun getInstance() = PushedManagementFragment()
    }

    override fun setSystemBarStyle() {
        requireActivity().window.setSystemBarStyle(
            false,
            !requireContext().isDarkMode,
            true,
            !requireContext().isDarkMode
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
        container: ViewGroup?, savedInstanceState: Bundle?
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

        val dialogChooseWeatherSourcesOpenState = viewModel.dialogChooseWeatherSourcesOpen.collectAsState()
        val selectedLocationState = viewModel.selectedLocation.collectAsState()
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
                                callback?.onSearchBarClicked()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = stringResource(R.string.action_add_new_location),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                Column {
                    if (validLocationListState.value.firstOrNull { it.isCurrentPosition } == null) {
                        FloatingActionButton(
                            onClick = {
                                viewModel.openChooseWeatherSourcesDialog(null)
                            },
                        ) {
                            Icon(
                                Icons.Outlined.MyLocation,
                                stringResource(R.string.action_add_current_location)
                            )
                        }
                        //Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
                    }
                    // TODO: Uncomment spacer and FAB when map selection is implemented
                    /*FloatingActionButton(
                        onClick = { /* TODO */ },
                    ) {
                        Icon(Icons.Outlined.Map, stringResource(R.string.action_add_from_map))
                    }*/
                }
            }
        ) { paddings ->
            if (validLocationListState.value.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(paddings)
                ) {
                    if (!viewModel.statementManager.isPostNotificationDialogAlreadyShown &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !notificationDismissed
                    ) {
                        val notificationPermissionState = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
                        if (notificationPermissionState.status != PermissionStatus.Granted) {
                            NotificationCard(
                                title = stringResource(R.string.dialog_permissions_notification_title),
                                summary = stringResource(R.string.dialog_permissions_notification_content),
                                onClick = {
                                    viewModel.statementManager.setPostNotificationDialogAlreadyShown()
                                    notificationDismissed = true
                                    requireActivity().requestPermissions(
                                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                        0
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
                    AndroidView(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clipToBounds(),
                        factory = {
                            recyclerView
                        }
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            paddings
                                + PaddingValues(horizontal = dimensionResource(R.dimen.normal_margin))
                                + PaddingValues(top = dimensionResource(R.dimen.large_margin))
                        ),
                ) {
                    Text(
                        text = stringResource(R.string.location_none_added_yet_instructions),
                        color = DayNightTheme.colors.bodyColor,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        if (dialogChooseWeatherSourcesOpenState.value) {
            SecondarySourcesPreference(
                (requireActivity() as MainActivity).sourceManager,
                selectedLocationState.value ?: Location(isCurrentPosition = true)
            ) { newLocation: Location? ->
                viewModel.closeChooseWeatherSourcesDialog()

                if (newLocation != null) {
                    // If coming from an existing location
                    if (selectedLocationState.value != null) {
                        // If main source was changed, we need to check first that it doesn't create
                        // a duplicate
                        if (selectedLocationState.value!!.weatherSource != newLocation.weatherSource) {
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
                        viewModel.addLocation(newLocation, null)
                        SnackbarHelper.showSnackbar(getString(R.string.location_message_added))
                    }
                }
            }
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
                { formattedId ->  // on click.
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
                updateAppBarColor()

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

    private fun updateDayNightColors() {
        val lightTheme = !requireContext().isDarkMode

        updateAppBarColor()

        recyclerView.setBackgroundColor(
            MainThemeColorProvider.getColor(
                lightTheme = lightTheme,
                id = com.google.android.material.R.attr.colorSurfaceVariant
            )
        )
    }

    private fun updateAppBarColor() {}

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
}
