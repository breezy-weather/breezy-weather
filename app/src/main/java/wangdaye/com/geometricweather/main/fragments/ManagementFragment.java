package wangdaye.com.geometricweather.main.fragments;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.ImageViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.GeoFragment;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.ui.adapters.location.LocationAdapter;
import wangdaye.com.geometricweather.common.ui.decotarions.ListDecoration;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.common.utils.helpers.SnackbarHelper;
import wangdaye.com.geometricweather.databinding.FragmentManagementBinding;
import wangdaye.com.geometricweather.main.MainActivityViewModel;
import wangdaye.com.geometricweather.main.adapters.LocationAdapterAnimWrapper;
import wangdaye.com.geometricweather.main.utils.DayNightColorWrapper;
import wangdaye.com.geometricweather.main.utils.MainModuleUtils;
import wangdaye.com.geometricweather.main.widgets.LocationItemTouchCallback;
import wangdaye.com.geometricweather.theme.ThemeManager;

public class ManagementFragment extends GeoFragment
        implements LocationItemTouchCallback.OnSelectProviderActivityStartedCallback {

    private FragmentManagementBinding mBinding;
    private MainActivityViewModel mViewModel;

    private LocationAdapter mAdapter;
    private LocationAdapterAnimWrapper mAdapterAnimWrapper;
    private ItemTouchHelper mItemTouchHelper;
    private ListDecoration mItemDecoration;

    private ValueAnimator mColorAnimator;

    private @Nullable Callback mCallback;

    public static final String KEY_CONTROL_SYSTEM_BAR = "control_system_bar";

    public interface Callback {
        void onSearchBarClicked(View searchBar);
        void onSelectProviderActivityStarted();
    }

    public static ManagementFragment getInstance(boolean controlSystemBar) {
        Bundle b = new Bundle();
        b.putBoolean(KEY_CONTROL_SYSTEM_BAR, controlSystemBar);

        ManagementFragment f = new ManagementFragment();
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentManagementBinding.inflate(getLayoutInflater(), container, false);
        initModel();
        initView();

        setCallback((Callback) requireActivity());

        Bundle b = getArguments();
        if (b != null) {
            boolean controlSystemBar = b.getBoolean(KEY_CONTROL_SYSTEM_BAR, false);
            if (controlSystemBar) {
                DisplayUtils.setSystemBarStyle(
                        requireContext(),
                        requireActivity().getWindow(),
                        false,
                        false,
                        true,
                        MainModuleUtils.isMainLightTheme(
                                requireContext(),
                                ThemeManager.getInstance(requireContext()).isDaylight()
                        )
                );
            }
        }

        return mBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mItemDecoration != null) {
            mBinding.recyclerView.removeItemDecoration(mItemDecoration);
        }
    }

    @Nullable
    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (enter && nextAnim != 0 && mAdapterAnimWrapper != null) {
            mAdapterAnimWrapper.setLastPosition(-1);
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    private void initModel() {
        mViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);
    }

    private void initView() {
        mBinding.searchBar.setOnClickListener(v -> {
            if (mCallback != null) {
                mCallback.onSearchBarClicked(mBinding.searchBar);
            }
        });
        mBinding.searchBar.setTransitionName(
                getString(R.string.transition_activity_search_bar)
        );

        mBinding.currentLocationButton.setOnClickListener(v -> {
            mViewModel.addLocation(Location.buildLocal(), null);
            SnackbarHelper.showSnackbar(getString(R.string.feedback_collect_succeed));
        });

        mAdapter = new LocationAdapter(
                requireActivity(),
                new ArrayList<>(),
                null,
                (v, formattedId) -> { // on click.
                    mViewModel.setLocation(formattedId);
                    getParentFragmentManager().popBackStack();
                },
                holder -> mItemTouchHelper.startDrag(holder) // on drag.
        );
        mAdapterAnimWrapper = new LocationAdapterAnimWrapper(requireContext(), mAdapter);
        mAdapterAnimWrapper.setLastPosition(Integer.MAX_VALUE);
        mBinding.recyclerView.setAdapter(mAdapterAnimWrapper);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(
                requireActivity(), RecyclerView.VERTICAL, false));
        mBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy != 0) {
                    mAdapterAnimWrapper.setScrolled();
                }
            }
        });

        registerDayNightColors();

        mItemTouchHelper = new ItemTouchHelper(
                new LocationItemTouchCallback(
                        (GeoActivity) requireActivity(),
                        mViewModel,
                        this
                )
        );
        mItemTouchHelper.attachToRecyclerView(mBinding.recyclerView);

        mViewModel.totalLocationList.observe(getViewLifecycleOwner(), selectableLocationList -> {
            mAdapter.update(
                    selectableLocationList.getLocationList(),
                    selectableLocationList.getSelectedId()
            );
            setCurrentLocationButtonEnabled(selectableLocationList.getLocationList());
        });
    }

    private void registerDayNightColors() {
        DayNightColorWrapper.bind(
                mBinding.recyclerView,
                new Integer[]{
                        android.R.attr.colorBackground,
                        R.attr.colorSurface,
                        R.attr.colorOutline,
                },
                (colors, animated) -> {
                    if (!animated) {
                        mBinding.recyclerView.setBackgroundColor(colors[0]);
                        mBinding.searchBar.setCardBackgroundColor(colors[1]);

                        mItemDecoration = new ListDecoration(requireContext(), colors[2]);

                        while (mBinding.recyclerView.getItemDecorationCount() > 0) {
                            mBinding.recyclerView.removeItemDecorationAt(0);
                        }
                        mBinding.recyclerView.addItemDecoration(mItemDecoration);

                        return null;
                    }

                    if (mColorAnimator != null) {
                        mColorAnimator.cancel();
                    }

                    final float[] progress = new float[1];
                    final int[] oldColors = new int[] {
                            mBinding.recyclerView.getBackground() instanceof ColorDrawable
                                    ? ((ColorDrawable) mBinding.recyclerView.getBackground()).getColor()
                                    : Color.TRANSPARENT,
                            mBinding.searchBar.getCardBackgroundColor().getDefaultColor(),
                            mItemDecoration.getColor(),
                    };
                    mColorAnimator = ValueAnimator.ofFloat(0, 1);
                    mColorAnimator.addUpdateListener(animation -> {
                        progress[0] = (float) animation.getAnimatedValue();
                        mBinding.recyclerView.setBackgroundColor(
                                DisplayUtils.blendColor(
                                        ColorUtils.setAlphaComponent(colors[0], (int) (255 * progress[0])),
                                        oldColors[0]
                                )
                        );
                        mBinding.searchBar.setCardBackgroundColor(
                                DisplayUtils.blendColor(
                                        ColorUtils.setAlphaComponent(colors[1], (int) (255 * progress[0])),
                                        oldColors[1]
                                )
                        );
                        mItemDecoration.setColor(
                                DisplayUtils.blendColor(
                                        ColorUtils.setAlphaComponent(colors[2], (int) (255 * progress[0])),
                                        oldColors[2]
                                )
                        );
                    });
                    mColorAnimator.setDuration(500); // same as 2 * changeDuration of default item animator.
                    mColorAnimator.start();
                    return null;
                }
        );
        DayNightColorWrapper.bind(
                mBinding.searchIcon,
                R.attr.colorBodyText,
                (color, animated) -> {
                    ImageViewCompat.setImageTintList(
                            mBinding.searchIcon,
                            ColorStateList.valueOf(color)
                    );
                    return null;
                }
        );
        DayNightColorWrapper.bind(
                mBinding.currentLocationButton,
                R.attr.colorBodyText,
                (color, animated) -> {
                    ImageViewCompat.setImageTintList(
                            mBinding.currentLocationButton,
                            ColorStateList.valueOf(color)
                    );
                    return null;
                }
        );
        DayNightColorWrapper.bind(
                mBinding.title,
                R.attr.colorCaptionText,
                (color, animated) -> {
                    mBinding.title.setTextColor(
                            ColorStateList.valueOf(color)
                    );
                    return null;
                }
        );
    }

    private void setCurrentLocationButtonEnabled(List<Location> list) {
        boolean enabled = list.size() != 0;
        for (int i = 0; i < list.size(); i ++) {
            if (list.get(i).isCurrentPosition()) {
                enabled = false;
                break;
            }
        }
        mBinding.currentLocationButton.setEnabled(enabled);
        mBinding.currentLocationButton.setAlpha(enabled ? 1 : .5f);
    }

    public void prepareReenterTransition() {
        postponeEnterTransition();
        mBinding.searchBar.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mBinding.searchBar.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return true;
            }
        });
    }

    // interface.

    public void setCallback(Callback l) {
        mCallback = l;
    }

    // on location list changed listener.

    @Override
    public void onSelectProviderActivityStarted() {
        if (mCallback != null) {
            mCallback.onSelectProviderActivityStarted();
        }
    }
}
