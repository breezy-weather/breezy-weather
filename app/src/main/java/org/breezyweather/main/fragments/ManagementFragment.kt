package org.breezyweather.main.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
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
import org.breezyweather.common.basic.models.Location.Companion.buildLocal
import org.breezyweather.common.ui.decorations.Material3ListItemDecoration
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.generateCollapsedScrollBehavior
import org.breezyweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.common.utils.DisplayUtils
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.main.MainActivityViewModel
import org.breezyweather.main.adapters.LocationAdapterAnimWrapper
import org.breezyweather.main.adapters.location.LocationAdapter
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.main.widgets.LocationItemTouchCallback
import org.breezyweather.main.widgets.LocationItemTouchCallback.TouchReactor
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.resource.ResourcesProviderFactory
import org.breezyweather.theme.resource.providers.ResourceProvider

class PushedManagementFragment: ManagementFragment() {

    companion object {
        @JvmStatic
        fun getInstance() = PushedManagementFragment()
    }

    override fun setSystemBarStyle() {
        DisplayUtils.setSystemBarStyle(
            requireContext(),
            requireActivity().window,
            false,
            !DisplayUtils.isDarkMode(requireContext()),
            true,
            !DisplayUtils.isDarkMode(requireContext())
        )
    }
}

open class ManagementFragment : MainModuleFragment(), TouchReactor {

    protected lateinit var viewModel: MainActivityViewModel

    private lateinit var layout: LinearLayoutManager
    private lateinit var adapter: LocationAdapter
    private lateinit var recyclerView: RecyclerView
    private var adapterAnimWrapper: LocationAdapterAnimWrapper? = null
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var resourceProvider: ResourceProvider? = null

    private var scrollOffset = 0f
    private var callback: Callback? = null

    interface Callback {
        fun onSearchBarClicked()
        fun onSelectProviderActivityStarted()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        initModel()
        initView()
        setCallback(requireActivity() as Callback)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
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
        val scrollBehavior = generateCollapsedScrollBehavior()

        val totalLocationListState = viewModel.totalLocationList.collectAsState()
        Material3Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                FitStatusBarTopAppBar(
                    title = stringResource(R.string.locations),
                    onBackPressed = { /* FIXME */ },
                    actions = {
                        IconButton(
                            onClick = {
                                if (callback != null) {
                                    callback!!.onSearchBarClicked()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = stringResource(R.string.feedback_search_location),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            floatingActionButton = {
                Column {
                    if (totalLocationListState.value.first.firstOrNull { it.isCurrentPosition } == null) {
                        FloatingActionButton(
                            onClick = {
                                viewModel.addLocation(buildLocal(requireContext()), null)
                                SnackbarHelper.showSnackbar(getString(R.string.feedback_collect_succeed))
                            },
                        ) {
                            Icon(Icons.Outlined.MyLocation, "My current position")
                        }
                        //Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
                    }
                    // TODO: Uncomment spacer and FAB when map selection is implemented
                    /*FloatingActionButton(
                        onClick = { /* TODO */ },
                    ) {
                        Icon(Icons.Outlined.Map, "Choose from map")
                    }*/
                }
            }
        ) { paddings ->
            AndroidView(
                modifier = Modifier.padding(paddings),
                factory = {
                    recyclerView
                }
            )
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (enter && nextAnim != 0 && adapterAnimWrapper != null) {
            adapterAnimWrapper!!.setLastPosition(-1)
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
        )
        adapterAnimWrapper!!.setLastPosition(Int.MAX_VALUE)
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
                    adapterAnimWrapper!!.setScrolled()
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
        val lightTheme = !DisplayUtils.isDarkMode(requireContext())

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

    override fun startSelectProviderActivityBySwipe() {
        if (callback != null) {
            callback!!.onSelectProviderActivityStarted()
        }
    }

    private fun ensureResourceProvider() {
        val iconProvider = SettingsManager
            .getInstance(requireContext())
            .iconProvider
        if (resourceProvider == null
            || resourceProvider!!.packageName != iconProvider) {
            resourceProvider = ResourcesProviderFactory.getNewInstance()
        }
    }
}