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

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import breezyweather.domain.location.model.Location
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.common.basic.BreezyActivity
import org.breezyweather.common.basic.livedata.EqualtableLiveData
import org.breezyweather.common.basic.models.options.appearance.BackgroundAnimationMode
import org.breezyweather.common.extensions.doOnApplyWindowInsets
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.extensions.isDarkMode
import org.breezyweather.common.extensions.isMotionReduced
import org.breezyweather.common.extensions.isTabletDevice
import org.breezyweather.common.extensions.isWidthHalfSizeable
import org.breezyweather.common.extensions.setSystemBarStyle
import org.breezyweather.common.extensions.uiModeManager
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.databinding.FragmentHomeBinding
import org.breezyweather.domain.location.model.getPlace
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.widgets.SwipeSwitchLayout
import org.breezyweather.ui.main.MainActivity
import org.breezyweather.ui.main.MainActivityViewModel
import org.breezyweather.ui.main.adapters.main.MainAdapter
import org.breezyweather.ui.main.adapters.main.ViewType
import org.breezyweather.ui.main.layouts.MainLayoutManager
import org.breezyweather.ui.main.utils.MainModuleUtils
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.ui.theme.weatherView.WeatherView
import org.breezyweather.ui.theme.weatherView.WeatherViewController

class HomeFragment : MainModuleFragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var weatherView: WeatherView

    private var adapter: MainAdapter? = null
    private var scrollListener: OnScrollListener? = null
    private var recyclerViewAnimator: Animator? = null
    private var resourceProvider: ResourceProvider? = null

    private val previewOffset = EqualtableLiveData(0)
    private var callback: Callback? = null
    private var lastCurrentLocation: Location? = null

    interface Callback {
        fun onEditIconClicked()
        fun onManageIconClicked()
        fun onOpenInOtherAppIconClicked()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        initModel()

        // attach weather view.
        weatherView = ThemeManager
            .getInstance(requireContext())
            .weatherThemeDelegate
            .getWeatherView(requireContext())
        (binding.switchLayout.parent.parent.parent as CoordinatorLayout).addView(
            weatherView as View,
            0,
            CoordinatorLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        initView()
        setCallback(requireActivity() as Callback)

        return binding.root
    }

    private fun isBackgroundAnimationEnabled() =
        when (SettingsManager.getInstance(requireContext()).backgroundAnimationMode) {
            BackgroundAnimationMode.SYSTEM -> !requireContext().isMotionReduced
            BackgroundAnimationMode.ENABLED -> true
            BackgroundAnimationMode.DISABLED -> false
        }

    override fun onResume() {
        super.onResume()
        weatherView.setDrawable(!isHidden)
    }

    override fun onPause() {
        super.onPause()
        weatherView.setDrawable(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        binding.recyclerView.clearOnScrollListeners()
        scrollListener = null
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        weatherView.setDrawable(!hidden)
    }

    override fun setSystemBarStyle() {
        val delegate = ThemeManager
            .getInstance(requireContext())
            .weatherThemeDelegate
        val location = if (::viewModel.isInitialized) viewModel.currentLocation.value?.location else null
        val isLightBackground = location?.let {
            delegate.isLightBackground(
                requireContext(),
                WeatherViewController.getWeatherKind(it),
                WeatherViewController.isDaylight(it)
            )
        } ?: false

        requireActivity().window.setSystemBarStyle(isLightBackground)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateDayNightColors()
        updateViews()
    }

    // init.

    private fun initModel() {
        viewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
    }

    @SuppressLint("ClickableViewAccessibility", "NonConstantResourceId", "NotifyDataSetChanged")
    private fun initView() {
        ensureResourceProvider()

        weatherView.setGravitySensorEnabled(
            SettingsManager.getInstance(requireContext()).isGravitySensorEnabled
        )

        weatherView.setDoAnimate(
            isBackgroundAnimationEnabled()
        )

        binding.appBar.doOnApplyWindowInsets { view, insets ->
            view.updatePadding(
                top = insets.top
            )
        }

        binding.toolbar.setNavigationOnClickListener {
            callback?.onManageIconClicked()
        }
        binding.toolbar.inflateMenu(R.menu.activity_main)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> callback?.onEditIconClicked()
                R.id.action_open_in_other_app -> callback?.onOpenInOtherAppIconClicked()
            }
            true
        }
        binding.toolbar.menu.findItem(R.id.action_edit).setVisible(false)
        binding.toolbar.menu.findItem(R.id.action_open_in_other_app).setVisible(false)
        // Needed to get the icon to show the correct color depending on dark mode
        binding.toolbar.overflowIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_more_vert)

        // Align refreshTimeText with toolbar (some devices, e.g. tablets, have an additional
        // start padding).
        (binding.refreshTimeText.layoutParams as ViewGroup.MarginLayoutParams).apply {
            marginStart = binding.toolbar.paddingStart +
                requireContext().resources.getDimensionPixelSize(R.dimen.normal_margin)
        }

        binding.switchLayout.setOnSwitchListener(switchListener)
        binding.switchLayout.reset()
        binding.indicator.setSwitchView(binding.switchLayout)
        binding.indicator.setCurrentIndicatorColor(Color.WHITE)
        binding.indicator.setIndicatorColor(
            ColorUtils.setAlphaComponent(Color.WHITE, (0.5 * 255).toInt())
        )
        binding.indicator.doOnApplyWindowInsets { view, insets ->
            view.updatePadding(
                bottom = insets.bottom
            )
        }

        binding.refreshLayout.setOnRefreshListener {
            viewModel.updateWithUpdatingChecking(
                triggeredByUser = true,
                checkPermissions = true
            )
        }

        val listAnimationEnabled = SettingsManager
            .getInstance(requireContext())
            .isCardsFadeInEnabled
        val itemAnimationEnabled = SettingsManager
            .getInstance(requireContext())
            .isElementsAnimationEnabled
        adapter = MainAdapter(
            (requireActivity() as MainActivity),
            binding.recyclerView,
            weatherView,
            null,
            resourceProvider!!,
            listAnimationEnabled,
            itemAnimationEnabled
        ).apply {
            itemTouchHelper.attachToRecyclerView(binding.recyclerView)
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = MainLayoutManager(requireContext()).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (
                        ViewType.isHalfSizeableBlock(binding.recyclerView.adapter!!.getItemViewType(position)) != true
                    ) {
                        2 // Full width
                    } else {
                        if (requireContext().isWidthHalfSizeable) {
                            1 // Half width
                        } else {
                            2 // Full width
                        }
                    }
                }
            }
        }
        binding.recyclerView.doOnApplyWindowInsets { view, insets ->
            view.updatePadding(bottom = insets.bottom)
        }
        binding.recyclerView.addOnScrollListener(OnScrollListener().also { scrollListener = it })
        binding.recyclerView.setOnTouchListener(indicatorStateListener)

        if (isAdded && context != null) {
            updatePreviewSubviews()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentLocation.collect {
                    if (it?.location != null) {
                        binding.toolbar.menu.findItem(R.id.action_edit).setVisible(true)
                        binding.toolbar.menu.findItem(R.id.action_open_in_other_app).setVisible(true)
                    }

                    // TODO: Dirty workaround to avoid recollecting on lifecycle resume
                    if (it?.location != lastCurrentLocation) {
                        updateViews(it?.location)
                        lastCurrentLocation = it?.location
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loading.collect {
                    setRefreshing(it)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.indicator.collect {
                    binding.switchLayout.isEnabled = it.total > 1

                    if (binding.switchLayout.totalCount != it.total ||
                        binding.switchLayout.position != it.index
                    ) {
                        binding.switchLayout.setData(it.index, it.total)
                        binding.indicator.setSwitchView(binding.switchLayout)
                    }

                    binding.indicator.visibility = if (it.total > 1) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun updateDayNightColors() {
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(
            requireContext().getThemeColor(com.google.android.material.R.attr.colorSurface)
        )
    }

    // control.
    fun updateViews(location: Location? = viewModel.currentLocation.value?.location) {
        ensureResourceProvider()
        updateDarkMode(location)
        updateContentViews(location = location)
        if (isAdded && context != null) {
            updatePreviewSubviews()
        }
    }

    private fun updateDarkMode(location: Location?) {
        val expectedLightTheme = ThemeManager.isLightTheme(requireContext(), location)
        LogHelper.log(msg = "Night mode: ${requireContext().uiModeManager?.nightMode}")
        LogHelper.log(msg = "Expected light theme for ${location?.city}: $expectedLightTheme")

        (activity as BreezyActivity).updateLocalNightMode(expectedLightTheme)
    }

    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
    private fun updateContentViews(location: Location?) {
        recyclerViewAnimator?.let {
            it.cancel()
            recyclerViewAnimator = null
        }

        updateDayNightColors()

        binding.switchLayout.reset()

        if (location?.weather == null) {
            adapter!!.setNullWeather()
            adapter!!.notifyDataSetChanged()
            binding.recyclerView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN &&
                    !binding.refreshLayout.isRefreshing
                ) {
                    viewModel.updateWithUpdatingChecking(
                        triggeredByUser = true,
                        checkPermissions = true
                    )
                }
                false
            }
            return
        }

        binding.recyclerView.setOnTouchListener(null)

        val listAnimationEnabled = SettingsManager
            .getInstance(requireContext())
            .isCardsFadeInEnabled
        val itemAnimationEnabled = SettingsManager
            .getInstance(requireContext())
            .isElementsAnimationEnabled
        adapter!!.update(
            (requireActivity() as MainActivity),
            binding.recyclerView,
            weatherView,
            location,
            resourceProvider!!,
            listAnimationEnabled,
            itemAnimationEnabled
        )
        adapter!!.notifyDataSetChanged()

        scrollListener!!.postReset(binding.recyclerView)

        // TODO: What's the purpose of this?
        if (!listAnimationEnabled) {
            binding.recyclerView.alpha = 0f
            recyclerViewAnimator = MainModuleUtils.getEnterAnimator(
                binding.recyclerView,
                0
            ).apply {
                startDelay = 150
            }.also { it.start() }
        }
    }

    private fun ensureResourceProvider() {
        val iconProvider = SettingsManager
            .getInstance(requireContext())
            .iconProvider
        if (resourceProvider == null || resourceProvider!!.packageName != iconProvider) {
            resourceProvider = ResourcesProviderFactory.newInstance
        }
    }

    private fun updatePreviewSubviews() {
        val location = viewModel.getValidLocation(previewOffset.value)
        val daylight = WeatherViewController.isDaylight(location)
        val weatherKind = WeatherViewController.getWeatherKind(location)

        // Show "current position" icon:
        // - On the left on mobile because it might not be visible on small displays otherwise
        // - On the right on tablet because on the left it would be confused with the action icon
        binding.toolbar.title =
            (if (location?.isCurrentPosition == true && !requireContext().isTabletDevice) "⊙ " else "") +
            (location?.getPlace(requireContext()) ?: "") +
            (if (location?.isCurrentPosition == true && requireContext().isTabletDevice) " ⊙" else "")

        location?.weather?.base?.refreshTime?.let {
            binding.refreshTimeText.visibility = View.VISIBLE
            binding.refreshTimeText.setDate(it)
        } ?: run {
            binding.refreshTimeText.visibility = View.GONE
        }

        weatherView.setWeather(weatherKind, daylight, requireContext().isDarkMode)
        binding.refreshLayout.setColorSchemeColors(
            ThemeManager
                .getInstance(requireContext())
                .weatherThemeDelegate
                .getThemeColors(
                    requireContext(),
                    weatherKind,
                    daylight
                )[0]
        )
    }

    private fun setRefreshing(b: Boolean) {
        binding.refreshLayout.post {
            if (isFragmentViewCreated) {
                binding.refreshLayout.isRefreshing = b
            }
        }
    }

    // interface.

    private fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    // on touch listener.

    @SuppressLint("ClickableViewAccessibility")
    private val indicatorStateListener = View.OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_MOVE ->
                binding.indicator.setDisplayState(true)

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                binding.indicator.setDisplayState(false)
        }
        false
    }

    // on swipe listener (swipe switch layout).

    private val switchListener = object : SwipeSwitchLayout.OnSwitchListener {

        override fun onSwiped(swipeDirection: Int, progress: Float) {
            binding.indicator.setDisplayState(progress != 0f)

            if (progress >= 1) {
                previewOffset.setValue(
                    if (swipeDirection == SwipeSwitchLayout.SWIPE_DIRECTION_LEFT) 1 else -1
                )
            } else {
                previewOffset.setValue(0)
            }
        }

        override fun onSwitched(swipeDirection: Int) {
            binding.indicator.setDisplayState(false)

            viewModel.offsetLocation(
                if (swipeDirection == SwipeSwitchLayout.SWIPE_DIRECTION_LEFT) 1 else -1
            )
            previewOffset.setValue(0)
        }
    }

    // on scroll changed listener.

    private inner class OnScrollListener : RecyclerView.OnScrollListener() {

        private var mScrollY = 0

        fun postReset(recyclerView: RecyclerView) {
            recyclerView.post {
                if (!isFragmentViewCreated) {
                    return@post
                }
                mScrollY = 0
                onScrolled(recyclerView, 0, 0)
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            mScrollY = recyclerView.computeVerticalScrollOffset()
            weatherView.onScroll(mScrollY)
            adapter?.onScroll()
        }
    }
}
