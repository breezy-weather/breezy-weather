package org.breezyweather.main.fragments

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
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.livedata.EqualtableLiveData
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.appearance.BackgroundAnimationMode
import org.breezyweather.common.extensions.isMotionReduced
import org.breezyweather.common.ui.widgets.SwipeSwitchLayout
import org.breezyweather.databinding.FragmentHomeBinding
import org.breezyweather.main.MainActivity
import org.breezyweather.main.MainActivityViewModel
import org.breezyweather.main.adapters.main.MainAdapter
import org.breezyweather.main.layouts.MainLayoutManager
import org.breezyweather.main.utils.MainModuleUtils
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.resource.ResourcesProviderFactory
import org.breezyweather.theme.resource.providers.ResourceProvider
import org.breezyweather.theme.weatherView.WeatherView
import org.breezyweather.theme.weatherView.WeatherViewController

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

    interface Callback {
        fun onManageIconClicked()
        fun onSettingsIconClicked()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        initModel()

        // attach weather view.
        weatherView = ThemeManager
            .getInstance(requireContext())
            .weatherThemeDelegate
            .getWeatherView(requireContext())
        (binding.switchLayout.parent as CoordinatorLayout).addView(
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
        ThemeManager
            .getInstance(requireContext())
            .weatherThemeDelegate
            .setSystemBarStyle(
                requireContext(),
                requireActivity().window,
                statusShader = scrollListener?.topOverlap == true,
                lightStatus = false,
                navigationShader = true,
                lightNavigation = false
            )
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

        binding.toolbar.setNavigationOnClickListener {
            callback?.onManageIconClicked()
        }
        binding.toolbar.inflateMenu(R.menu.activity_main)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_manage -> callback?.onManageIconClicked()
                R.id.action_settings -> callback?.onSettingsIconClicked()
            }
            true
        }

        binding.switchLayout.setOnSwitchListener(switchListener)
        binding.switchLayout.reset()
        binding.indicator.setSwitchView(binding.switchLayout)
        binding.indicator.setCurrentIndicatorColor(Color.WHITE)
        binding.indicator.setIndicatorColor(
            ColorUtils.setAlphaComponent(Color.WHITE, (0.5 * 255).toInt())
        )

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
            (requireActivity() as GeoActivity),
            binding.recyclerView,
            weatherView,
            null,
            (requireActivity() as MainActivity).sourceManager,
            resourceProvider!!,
            listAnimationEnabled,
            itemAnimationEnabled
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = MainLayoutManager()
        binding.recyclerView.addOnScrollListener(OnScrollListener().also { scrollListener = it })
        binding.recyclerView.setOnTouchListener(indicatorStateListener)

        viewModel.currentLocation.observe(viewLifecycleOwner) {
            updateViews(it?.location)
        }

        viewModel.loading.observe(viewLifecycleOwner) { setRefreshing(it) }

        viewModel.indicator.observe(viewLifecycleOwner) {
            binding.switchLayout.isEnabled = it.total > 1

            if (binding.switchLayout.totalCount != it.total
                || binding.switchLayout.position != it.index
            ) {
                binding.switchLayout.setData(it.index, it.total)
                binding.indicator.setSwitchView(binding.switchLayout)
            }

            binding.indicator.visibility = if (it.total > 1) View.VISIBLE else View.GONE
        }

        previewOffset.observe(viewLifecycleOwner) {
            binding.root.post {
                if (isFragmentViewCreated) {
                    updatePreviewSubviews()
                }
            }
        }
    }

    private fun updateDayNightColors() {
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(
            MainThemeColorProvider.getColor(
                location = viewModel.currentLocation.value?.location,
                id = com.google.android.material.R.attr.colorSurface
            )
        )
    }

    // control.
    fun updateViews(location: Location? = viewModel.currentLocation.value?.location) {
        ensureResourceProvider()
        updateContentViews(location = location)
        binding.root.post {
            if (isFragmentViewCreated) {
                updatePreviewSubviews()
            }
        }
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
                if (event.action == MotionEvent.ACTION_DOWN
                    && !binding.refreshLayout.isRefreshing
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
            (requireActivity() as GeoActivity),
            binding.recyclerView,
            weatherView,
            location,
            resourceProvider!!,
            listAnimationEnabled,
            itemAnimationEnabled
        )
        adapter!!.notifyDataSetChanged()

        scrollListener!!.postReset(binding.recyclerView)

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
        val daylight = location?.isDaylight ?: true
        val weatherKind = WeatherViewController.getWeatherKind(location?.weather)

        binding.toolbar.title = location?.getPlace(requireContext())

        val textColor = ThemeManager.getInstance(requireContext())
            .weatherThemeDelegate
            .getHeaderTextColor(requireContext())
        binding.refreshTimeText.setTextColor(textColor)
        location?.weather?.base?.updateDate?.let {
            binding.refreshTimeText.visibility = View.VISIBLE
            binding.refreshTimeText.setDate(it)
        } ?: run {
            binding.refreshTimeText.visibility = View.GONE
        }

        weatherView.setWeather(
            weatherKind,
            daylight,
            resourceProvider!!
        )
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

        private var mTopChanged: Boolean? = null
        var topOverlap = false
        private var mFirstCardMarginTop = 0
        private var mScrollY = 0
        private var mLastAppBarTranslationY = 0f

        fun postReset(recyclerView: RecyclerView) {
            recyclerView.post {
                if (!isFragmentViewCreated) {
                    return@post
                }
                mTopChanged = null
                topOverlap = false
                mFirstCardMarginTop = 0
                mScrollY = 0
                mLastAppBarTranslationY = 0f
                onScrolled(recyclerView, 0, 0)
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            mFirstCardMarginTop = if (recyclerView.childCount > 0) {
                recyclerView.getChildAt(0).top
            } else -1

            mScrollY = recyclerView.computeVerticalScrollOffset()
            mLastAppBarTranslationY = binding.appBar.translationY
            weatherView.onScroll(mScrollY)

            adapter?.onScroll()

            // set translation y of toolbar.
            if (adapter != null) {
                if (adapter!!.headerTop == -1 || mScrollY < (adapter!!.headerTop - binding.appBar.measuredHeight)) {
                    // Keep app bar on top until we reach top of temperature
                    binding.appBar.translationY = 0f
                } else if (mScrollY < adapter!!.headerTop) {
                    // Make the app bar disappear when we reach top of temperature
                    binding.appBar.translationY = (
                            adapter!!.headerTop
                                    - binding.appBar.measuredHeight
                                    - mScrollY
                            ).toFloat()
                } else {
                    // Make appbar completely disappear in other cases
                    binding.appBar.translationY = -binding.appBar.measuredHeight.toFloat()
                }
            }

            // set system bar style.
            mTopChanged = if (mFirstCardMarginTop <= 0) {
                (binding.appBar.translationY != 0f) != (mLastAppBarTranslationY != 0f)
            } else true
            topOverlap = binding.appBar.translationY != 0f
            if (mTopChanged!!) {
                checkToSetSystemBarStyle()
            }
        }
    }
}