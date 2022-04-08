package wangdaye.com.geometricweather.main.fragments

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Animation
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.GeoActivity
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.Location.Companion.buildLocal
import wangdaye.com.geometricweather.common.ui.adapters.location.LocationAdapter
import wangdaye.com.geometricweather.common.ui.decotarions.ListDecoration
import wangdaye.com.geometricweather.common.utils.DisplayUtils
import wangdaye.com.geometricweather.common.utils.helpers.SnackbarHelper
import wangdaye.com.geometricweather.databinding.FragmentManagementBinding
import wangdaye.com.geometricweather.main.MainActivityViewModel
import wangdaye.com.geometricweather.main.adapters.LocationAdapterAnimWrapper
import wangdaye.com.geometricweather.main.widgets.LocationItemTouchCallback
import wangdaye.com.geometricweather.main.widgets.LocationItemTouchCallback.TouchReactor
import wangdaye.com.geometricweather.theme.ThemeManager

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
            !DisplayUtils.isLightColor(
                ThemeManager.getInstance(requireContext()).getThemeColor(
                    requireContext(),
                    R.attr.colorOnPrimaryContainer
                )
            ),
            true,
            !DisplayUtils.isDarkMode(requireContext())
        )
    }
}

open class ManagementFragment : MainModuleFragment(), TouchReactor {

    private lateinit var binding: FragmentManagementBinding
    protected lateinit var viewModel: MainActivityViewModel

    private lateinit var layout: LinearLayoutManager
    private lateinit var adapter: LocationAdapter
    private var adapterAnimWrapper: LocationAdapterAnimWrapper? = null
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var itemDecoration: ListDecoration? = null

    private var callback: Callback? = null

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

    override fun setSystemBarStyle() {
        // do nothing.
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateDayNightColors()

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
        updateDayNightColors()

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

        adapter = LocationAdapter(
            requireActivity(),
            ArrayList(),
            null,
            { _, formattedId ->  // on click.
                viewModel.setLocation(formattedId)
                parentFragmentManager.popBackStack()
            }
        ) { holder ->
            itemTouchHelper.startDrag(holder)
        }
        adapterAnimWrapper = LocationAdapterAnimWrapper(requireContext(), adapter)
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

    private fun updateDayNightColors() {
        val tm = ThemeManager.getInstance(requireContext())

        binding.recyclerView.setBackgroundColor(
            tm.getThemeColor(context = requireContext(), id = android.R.attr.colorBackground)
        )
        binding.appBar.setBackgroundColor(
            tm.getThemeColor(context = requireContext(), id = R.attr.colorPrimaryContainer)
        )
        binding.searchBar.setCardBackgroundColor(
            tm.getThemeColor(context = requireContext(), id = R.attr.colorSurface)
        )

        ImageViewCompat.setImageTintList(
            binding.searchIcon,
            ColorStateList.valueOf(
                tm.getThemeColor(context = requireContext(), id = R.attr.colorBodyText)
            )
        )
        ImageViewCompat.setImageTintList(
            binding.currentLocationButton,
            ColorStateList.valueOf(
                tm.getThemeColor(context = requireContext(), id = R.attr.colorBodyText)
            )
        )
        binding.title.setTextColor(
            tm.getThemeColor(context = requireContext(), id = R.attr.colorCaptionText)
        )

        if (itemDecoration == null) {
            itemDecoration = ListDecoration(
                requireContext(),
                tm.getThemeColor(context = requireContext(), id = R.attr.colorOutline)
            ).also {
                while (binding.recyclerView.itemDecorationCount > 0) {
                    binding.recyclerView.removeItemDecorationAt(0)
                }
                binding.recyclerView.addItemDecoration(it)
            }
        } else {
            itemDecoration?.color = tm.getThemeColor(
                context = requireContext(),
                id = R.attr.colorOutline
            )
            binding.recyclerView.invalidateItemDecorations()
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