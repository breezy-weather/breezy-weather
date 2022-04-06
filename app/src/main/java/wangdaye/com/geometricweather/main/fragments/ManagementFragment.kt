package wangdaye.com.geometricweather.main.fragments

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Animation
import androidx.core.graphics.ColorUtils
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.GeoActivity
import wangdaye.com.geometricweather.common.basic.GeoFragment
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.Location.Companion.buildLocal
import wangdaye.com.geometricweather.common.ui.adapters.location.LocationAdapter
import wangdaye.com.geometricweather.common.ui.decotarions.ListDecoration
import wangdaye.com.geometricweather.common.utils.DisplayUtils
import wangdaye.com.geometricweather.common.utils.helpers.SnackbarHelper
import wangdaye.com.geometricweather.databinding.FragmentManagementBinding
import wangdaye.com.geometricweather.main.MainActivityViewModel
import wangdaye.com.geometricweather.main.adapters.LocationAdapterAnimWrapper
import wangdaye.com.geometricweather.main.utils.DayNightColorWrapper
import wangdaye.com.geometricweather.main.utils.MainModuleUtils
import wangdaye.com.geometricweather.main.widgets.LocationItemTouchCallback
import wangdaye.com.geometricweather.main.widgets.LocationItemTouchCallback.TouchReactor
import wangdaye.com.geometricweather.theme.ThemeManager.Companion.getInstance

class PushedManagementFragment: ManagementFragment() {

    companion object {
        fun getInstance(controlSystemBar: Boolean) = PushedManagementFragment().apply {
            arguments = Bundle().apply {
                putBoolean(KEY_CONTROL_SYSTEM_BAR, controlSystemBar)
            }
        }
    }
}
class SplitManagementFragment: ManagementFragment()

open class ManagementFragment : GeoFragment(), TouchReactor {

    private lateinit var binding: FragmentManagementBinding
    private lateinit var viewModel: MainActivityViewModel

    private lateinit var layout: LinearLayoutManager
    private lateinit var adapter: LocationAdapter
    private var adapterAnimWrapper: LocationAdapterAnimWrapper? = null
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var itemDecoration: ListDecoration? = null

    private var colorAnimator: ValueAnimator? = null

    private var callback: Callback? = null

    companion object {
        const val KEY_CONTROL_SYSTEM_BAR = "control_system_bar"
    }

    interface Callback {
        fun onSearchBarClicked(searchBar: View)
        fun onSelectProviderActivityStarted()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentManagementBinding.inflate(layoutInflater, container, false)

        initModel()
        initView()

        setCallback(requireActivity() as Callback)

        arguments?.let {
            val controlSystemBar = it.getBoolean(KEY_CONTROL_SYSTEM_BAR, false)
            if (controlSystemBar) {
                DisplayUtils.setSystemBarStyle(
                    requireContext(),
                    requireActivity().window,
                    false,
                    false,
                    true,
                    MainModuleUtils.isMainLightTheme(
                        requireContext(),
                        getInstance(requireContext()).isDaylight
                    )
                )
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        itemDecoration?.let {
            binding.recyclerView.removeItemDecoration(it)
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (enter && nextAnim != 0 && adapterAnimWrapper != null) {
            adapterAnimWrapper!!.setLastPosition(-1)
        }
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

    private fun initModel() {
        viewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
    }

    private fun initView() {
        binding.searchBar.setOnClickListener {
            if (callback != null) {
                callback!!.onSearchBarClicked(binding.searchBar)
            }
        }
        binding.searchBar.transitionName = getString(R.string.transition_activity_search_bar)

        binding.currentLocationButton.setOnClickListener {
            viewModel.addLocation(buildLocal(), null)
            SnackbarHelper.showSnackbar(getString(R.string.feedback_collect_succeed))
        }

        adapterAnimWrapper = LocationAdapterAnimWrapper(
            requireContext(),
            LocationAdapter(
                requireActivity(),
                ArrayList(),
                null,
                { _, formattedId ->  // on click.
                    viewModel.setLocation(formattedId)
                    parentFragmentManager.popBackStack()
                }
            ) { holder ->
                itemTouchHelper.startDrag(holder)
            }.also {
                adapter = it
            }
        )
        adapterAnimWrapper!!.setLastPosition(Int.MAX_VALUE)
        binding.recyclerView.adapter = adapterAnimWrapper
        binding.recyclerView.layoutManager = LinearLayoutManager(
            requireActivity(),
            RecyclerView.VERTICAL,
            false
        ).also { layout = it }
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) {
                    adapterAnimWrapper!!.setScrolled()
                }
            }
        })

        registerDayNightColors()

        itemTouchHelper = ItemTouchHelper(
            LocationItemTouchCallback(
                requireActivity() as GeoActivity,
                viewModel,
                this
            )
        )
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        viewModel.totalLocationList.observe(viewLifecycleOwner) {
            adapter.update(it.locationList, it.selectedId)
            setCurrentLocationButtonEnabled(it.locationList)
        }
    }

    private fun registerDayNightColors() {
        DayNightColorWrapper.bind(
            binding.recyclerView,
            arrayOf(
                android.R.attr.colorBackground,
                R.attr.colorSurface,
                R.attr.colorOutline,
                android.R.attr.colorPrimary,
            )
        ) { colors, animated ->
            if (!animated) {
                binding.appBar.setBackgroundColor(colors[3])
                binding.recyclerView.setBackgroundColor(colors[0])
                binding.searchBar.setCardBackgroundColor(colors[1])

                itemDecoration = ListDecoration(requireContext(), colors[2])

                while (binding.recyclerView.itemDecorationCount > 0) {
                    binding.recyclerView.removeItemDecorationAt(0)
                }
                binding.recyclerView.addItemDecoration(itemDecoration!!)
            }

            colorAnimator?.cancel()

            val progress = FloatArray(1)
            val oldColors = intArrayOf(
                (binding.recyclerView.background as? ColorDrawable)?.color ?: Color.TRANSPARENT,
                binding.searchBar.cardBackgroundColor.defaultColor,
                itemDecoration!!.color,
                (binding.appBar.background as? ColorDrawable)?.color ?: Color.TRANSPARENT,
            )
            colorAnimator = ValueAnimator.ofFloat(0f, 1f)
            colorAnimator!!.addUpdateListener { animation ->
                progress[0] = animation.animatedValue as Float

                binding.recyclerView.setBackgroundColor(
                    DisplayUtils.blendColor(
                        ColorUtils.setAlphaComponent(colors[0], (255 * progress[0]).toInt()),
                        oldColors[0]
                    )
                )
                binding.searchBar.setCardBackgroundColor(
                    DisplayUtils.blendColor(
                        ColorUtils.setAlphaComponent(colors[1], (255 * progress[0]).toInt()),
                        oldColors[1]
                    )
                )
                itemDecoration!!.color = DisplayUtils.blendColor(
                    ColorUtils.setAlphaComponent(colors[2], (255 * progress[0]).toInt()),
                    oldColors[2]
                )
                binding.appBar.setBackgroundColor(
                    DisplayUtils.blendColor(
                        ColorUtils.setAlphaComponent(colors[3], (255 * progress[0]).toInt()),
                        oldColors[3]
                    )
                )
            }
            colorAnimator!!.duration = 500 // same as 2 * changeDuration of default item animator.
            colorAnimator!!.start()

            val firstHolderPosition = layout.findFirstVisibleItemPosition()
            adapter.notifyItemRangeChanged(
                firstHolderPosition,
                layout.findLastVisibleItemPosition() - firstHolderPosition + 1
            )
        }
        DayNightColorWrapper.bind(
            binding.searchIcon,
            R.attr.colorBodyText
        ) { color, _ ->
            ImageViewCompat.setImageTintList(binding.searchIcon, ColorStateList.valueOf(color))
        }
        DayNightColorWrapper.bind(
            binding.currentLocationButton,
            R.attr.colorBodyText
        ) { color, _ ->
            ImageViewCompat.setImageTintList(binding.currentLocationButton, ColorStateList.valueOf(color))
        }
        DayNightColorWrapper.bind(
            binding.title,
            R.attr.colorCaptionText
        ) { color, _ ->
            binding.title.setTextColor(ColorStateList.valueOf(color))
        }
    }

    private fun setCurrentLocationButtonEnabled(list: List<Location>) {
        var enabled = list.isNotEmpty()
        for (i in list.indices) {
            if (list[i].isCurrentPosition) {
                enabled = false
                break
            }
        }

        binding.currentLocationButton.isEnabled = enabled
        binding.currentLocationButton.alpha = if (enabled) 1f else .5f
    }

    fun prepareReenterTransition() {
        postponeEnterTransition()

        binding.searchBar.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    binding.searchBar.viewTreeObserver.removeOnPreDrawListener(this)
                    startPostponedEnterTransition()
                    return true
                }
            }
        )
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
}