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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalUriHandler
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
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.extensions.isDarkMode
import org.breezyweather.common.extensions.plus
import org.breezyweather.common.extensions.setSystemBarStyle
import org.breezyweather.common.ui.composables.NotificationCard
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
import org.breezyweather.settings.preference.composables.RadioButton
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

        val totalLocationListState = viewModel.totalLocationList.collectAsState()
        var notificationDismissed by remember { mutableStateOf(false) }

        val dialogCurrentLocationProviderOpenState = viewModel.dialogChooseCurrentLocationWeatherSourceOpen.collectAsState()
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
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                Column {
                    if (totalLocationListState.value.first.firstOrNull { it.isCurrentPosition } == null) {
                        FloatingActionButton(
                            onClick = {
                                viewModel.openChooseCurrentLocationWeatherSourceDialog()
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
            if (totalLocationListState.value.first.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(paddings)
                ) {
                    if (!viewModel.statementManager.isPostNotificationDialogAlreadyShown
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        && !notificationDismissed
                    ) {
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

        if (dialogCurrentLocationProviderOpenState.value) {
            val uriHandler = LocalUriHandler.current
            AlertDialog(
                onDismissRequest = { viewModel.closeChooseCurrentLocationWeatherSourceDialog() },
                title = {
                    Text(
                        text = stringResource(R.string.settings_weather_sources_current_location),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val currentLocation = totalLocationListState.value.first
                            .firstOrNull { it.isCurrentPosition }
                        items((requireActivity() as MainActivity).sourceManager.getConfiguredWeatherSources()) { source ->
                            RadioButton(
                                selected = if (currentLocation != null) source.id == currentLocation.weatherSource else false,
                                onClick = {
                                    viewModel.closeChooseCurrentLocationWeatherSourceDialog()

                                    // TODO: Contains code to change existing current location
                                    // However, not yet called from the swipe event
                                    if (currentLocation != null) {
                                        viewModel.updateLocation(
                                            currentLocation.copy(
                                                weatherSource = source.id
                                                // Should we clean old weather data?
                                            )
                                        )
                                    } else {
                                        viewModel.addLocation(
                                            Location(
                                                weatherSource = source.id,
                                                isCurrentPosition = true
                                            ),
                                            null
                                        )
                                        SnackbarHelper.showSnackbar(getString(R.string.location_message_added))
                                    }
                                },
                                text = source.name,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.closeChooseCurrentLocationWeatherSourceDialog() }
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { uriHandler.openUri("https://github.com/breezy-weather/breezy-weather/blob/main/docs/SOURCES.md") }
                    ) {
                        Text(
                            text = stringResource(R.string.action_help_me_choose),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
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
        lifecycleScope.launch {
            // repeatOnLifecycle launches the block in a new coroutine every time the
            // lifecycle is in the STARTED state (or above) and cancels it when it's STOPPED.
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Trigger the flow and start listening for values.
                // Note that this happens when lifecycle is STARTED and stops
                // collecting when the lifecycle is STOPPED
                viewModel.totalLocationList.collect {
                    // New value received
                    adapter.update(it.first, it.second)
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

    private fun updateAppBarColor() {

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
}