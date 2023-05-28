package wangdaye.com.geometricweather.main.fragments

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.GeoActivity
import wangdaye.com.geometricweather.common.basic.livedata.EqualtableLiveData
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.options.DarkMode
import wangdaye.com.geometricweather.common.basic.models.options.appearance.BackgroundAnimationMode
import wangdaye.com.geometricweather.common.ui.widgets.SwipeSwitchLayout
import wangdaye.com.geometricweather.common.ui.widgets.SwipeSwitchLayout.OnSwitchListener
import wangdaye.com.geometricweather.common.utils.DisplayUtils
import wangdaye.com.geometricweather.databinding.FragmentHomeBinding
import wangdaye.com.geometricweather.main.MainActivityViewModel
import wangdaye.com.geometricweather.main.adapters.main.MainAdapter
import wangdaye.com.geometricweather.main.layouts.MainLayoutManager
import wangdaye.com.geometricweather.main.utils.MainModuleUtils
import wangdaye.com.geometricweather.main.utils.MainThemeColorProvider
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.theme.ThemeManager
import wangdaye.com.geometricweather.theme.resource.ResourcesProviderFactory
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider
import wangdaye.com.geometricweather.theme.weatherView.WeatherView
import wangdaye.com.geometricweather.theme.weatherView.WeatherViewController

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

    private fun isBackgroundAnimationEnabled() = when (SettingsManager.getInstance(requireContext()).backgroundAnimationMode) {
        BackgroundAnimationMode.SYSTEM -> !DisplayUtils.isMotionReduced(requireContext())
        BackgroundAnimationMode.ENABLED -> true
        BackgroundAnimationMode.DISABLED -> false
    }

    override fun onResume() {
        super.onResume()
        weatherView.setDrawable(isBackgroundAnimationEnabled() && !isHidden)
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
        weatherView.setDrawable(isBackgroundAnimationEnabled() && !hidden)
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

        binding.toolbar.setNavigationOnClickListener {
            if (callback != null) {
                callback!!.onManageIconClicked()
            }
        }
        binding.toolbar.inflateMenu(R.menu.activity_main)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_manage -> if (callback != null) {
                    callback!!.onManageIconClicked()
                }
                R.id.action_settings -> if (callback != null) {
                    callback!!.onSettingsIconClicked()
                }
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
            .isListAnimationEnabled
        val itemAnimationEnabled = SettingsManager
            .getInstance(requireContext())
            .isItemAnimationEnabled
        adapter = MainAdapter(
            (requireActivity() as GeoActivity),
            binding.recyclerView,
            weatherView,
            null,
            resourceProvider!!,
            listAnimationEnabled,
            itemAnimationEnabled
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = MainLayoutManager()
        binding.recyclerView.addOnScrollListener(OnScrollListener().also { scrollListener = it })
        binding.recyclerView.setOnTouchListener(indicatorStateListener)

        viewModel.currentLocation.observe(viewLifecycleOwner) {
            updateViews(it.location)
        }

        viewModel.loading.observe(viewLifecycleOwner) { setRefreshing(it) }

        viewModel.indicator.observe(viewLifecycleOwner) {
            binding.switchLayout.isEnabled = it.total > 1

            if (binding.switchLayout.totalCount != it.total
                || binding.switchLayout.position != it.index) {
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
                location = viewModel.currentLocation.value!!.location,
                id = com.google.android.material.R.attr.colorSurface
            )
        )
    }

    // control.

    @JvmOverloads
    fun updateViews(location: Location = viewModel.currentLocation.value!!.location) {
        ensureResourceProvider()
        updateContentViews(location = location)
        binding.root.post {
            if (isFragmentViewCreated) {
                updatePreviewSubviews()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
    private fun updateContentViews(location: Location) {
        if (recyclerViewAnimator != null) {
            recyclerViewAnimator!!.cancel()
            recyclerViewAnimator = null
        }

        updateDayNightColors()

        binding.switchLayout.reset()

        if (location.weather == null) {
            adapter!!.setNullWeather()
            adapter!!.notifyDataSetChanged()
            binding.recyclerView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN
                    && !binding.refreshLayout.isRefreshing) {

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
            .isListAnimationEnabled
        val itemAnimationEnabled = SettingsManager
            .getInstance(requireContext())
            .isItemAnimationEnabled
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
            )
            recyclerViewAnimator!!.startDelay = 150
            recyclerViewAnimator!!.start()
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

    private fun updatePreviewSubviews() {
        val location = viewModel.getValidLocation(
            previewOffset.value!!
        )
        val daylight = location.isDaylight

        binding.toolbar.title = location.getCityName(requireContext())
        WeatherViewController.setWeatherCode(
            weatherView,
            location.weather,
            daylight,
            resourceProvider!!
        )
        binding.refreshLayout.setColorSchemeColors(
            ThemeManager
                .getInstance(requireContext())
                .weatherThemeDelegate
                .getThemeColors(
                    requireContext(),
                    WeatherViewController.getWeatherKind(location.weather),
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
    private val indicatorStateListener = OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_MOVE ->
                binding.indicator.setDisplayState(true)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                binding.indicator.setDisplayState(false)
        }
        false
    }

    // on swipe listener (swipe switch layout).

    private val switchListener: OnSwitchListener = object : OnSwitchListener {

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
                recyclerView.getChildAt(0).measuredHeight
            } else {
                -1
            }

            mScrollY = recyclerView.computeVerticalScrollOffset()
            mLastAppBarTranslationY = binding.appBar.translationY
            weatherView.onScroll(mScrollY)

            if (adapter != null) {
                adapter!!.onScroll()
            }

            // set translation y of toolbar.
            if (adapter != null && mFirstCardMarginTop > 0) {
                if (mFirstCardMarginTop >= binding.appBar.measuredHeight
                    + adapter!!.currentTemperatureTextHeight) {
                    when {
                        mScrollY < (mFirstCardMarginTop
                                - binding.appBar.measuredHeight
                                - adapter!!.currentTemperatureTextHeight) -> {
                            binding.appBar.translationY = 0f
                        }
                        mScrollY > mFirstCardMarginTop - binding.appBar.y -> {
                            binding.appBar.translationY = -binding.appBar.measuredHeight.toFloat()
                        }
                        else -> {
                            binding.appBar.translationY = (
                                    mFirstCardMarginTop
                                            - adapter!!.currentTemperatureTextHeight
                                            - mScrollY
                                            - binding.appBar.measuredHeight
                            ).toFloat()
                        }
                    }
                } else {
                    binding.appBar.translationY = -mScrollY.toFloat()
                }
            }

            // set system bar style.
            if (mFirstCardMarginTop <= 0) {
                mTopChanged = true
                topOverlap = false
            } else {
                mTopChanged = (binding.appBar.translationY != 0f) != (mLastAppBarTranslationY != 0f)
                topOverlap = binding.appBar.translationY != 0f
            }
            if (mTopChanged!!) {
                checkToSetSystemBarStyle()
            }
        }
    }
}