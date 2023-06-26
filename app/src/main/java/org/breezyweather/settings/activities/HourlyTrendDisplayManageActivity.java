package org.breezyweather.settings.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import org.breezyweather.common.basic.GeoActivity;
import org.breezyweather.common.basic.models.options.appearance.HourlyTrendDisplay;
import org.breezyweather.common.ui.adapters.TagAdapter;
import org.breezyweather.common.ui.decorations.GridMarginsDecoration;
import org.breezyweather.common.ui.decorations.ListDecoration;
import org.breezyweather.common.ui.widgets.slidingItem.SlidingItemTouchCallback;
import org.breezyweather.theme.ThemeManager;
import org.breezyweather.R;
import org.breezyweather.common.utils.DisplayUtils;
import org.breezyweather.databinding.ActivityHourlyTrendDisplayManageBinding;
import org.breezyweather.settings.SettingsManager;
import org.breezyweather.settings.adapters.HourlyTrendDisplayAdapter;

public class HourlyTrendDisplayManageActivity extends GeoActivity {

    private ActivityHourlyTrendDisplayManageBinding mBinding;

    private HourlyTrendDisplayAdapter mHourlyTrendDisplayAdapter;
    private ItemTouchHelper mHourlyTrendDisplayItemTouchHelper;

    private TagAdapter mTagAdapter;

    private @Nullable AnimatorSet mBottomAnimator;
    private @Nullable Boolean mBottomBarVisibility;
    private @Px int mElevation;

    private class HourlyTrendTag implements TagAdapter.Tag {

        HourlyTrendDisplay tag;

        HourlyTrendTag(HourlyTrendDisplay tag) {
            this.tag = tag;
        }

        @Override
        public String getName() {
            return tag.getName(HourlyTrendDisplayManageActivity.this);
        }
    }

    private class CardDisplaySwipeCallback extends SlidingItemTouchCallback {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getBindingAdapterPosition();
            int toPosition = target.getBindingAdapterPosition();

            mHourlyTrendDisplayAdapter.moveItem(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            setResult(RESULT_OK);
            mHourlyTrendDisplayAdapter.removeItem(viewHolder.getBindingAdapterPosition());
        }

        @Override
        public void onChildDraw(@NonNull Canvas c,
                                @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            ViewCompat.setElevation(viewHolder.itemView,
                    (dY != 0 || isCurrentlyActive) ? mElevation : 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityHourlyTrendDisplayManageBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mElevation = getResources().getDimensionPixelSize(R.dimen.touch_rise_z);

        mBinding.appBar.injectDefaultSurfaceTintColor();

        mBinding.toolbar.setBackgroundColor(
                DisplayUtils.getWidgetSurfaceColor(
                        6f,
                        ThemeManager.getInstance(this).getThemeColor(this, androidx.appcompat.R.attr.colorPrimary),
                        ThemeManager.getInstance(this).getThemeColor(this, com.google.android.material.R.attr.colorSurface)
                )
        );
        mBinding.toolbar.setNavigationOnClickListener(view -> finish());

        List<HourlyTrendDisplay> displayTags = SettingsManager.getInstance(this).getHourlyTrendDisplayList();
        mHourlyTrendDisplayAdapter = new HourlyTrendDisplayAdapter(
                displayTags,
                hourlyTrendDisplay -> {
                    setResult(RESULT_OK);
                    mTagAdapter.insertItem(new HourlyTrendTag(hourlyTrendDisplay));
                    resetBottomBarVisibility();
                },
                holder -> mHourlyTrendDisplayItemTouchHelper.startDrag(holder)
        );

        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recyclerView.addItemDecoration(
                new ListDecoration(
                        this,
                        ThemeManager.getInstance(this).getThemeColor(this, com.google.android.material.R.attr.colorOutline)
                )
        );
        mBinding.recyclerView.setAdapter(mHourlyTrendDisplayAdapter);

        mHourlyTrendDisplayItemTouchHelper = new ItemTouchHelper(new CardDisplaySwipeCallback());
        mHourlyTrendDisplayItemTouchHelper.attachToRecyclerView(mBinding.recyclerView);

        List<HourlyTrendDisplay> otherTags = new ArrayList<>();
        otherTags.add(HourlyTrendDisplay.TAG_TEMPERATURE);
        otherTags.add(HourlyTrendDisplay.TAG_WIND);
        otherTags.add(HourlyTrendDisplay.TAG_UV_INDEX);
        otherTags.add(HourlyTrendDisplay.TAG_PRECIPITATION);
        for (int i = otherTags.size() - 1; i >= 0; i--) {
            for (HourlyTrendDisplay displayTag : displayTags) {
                if (otherTags.get(i) == displayTag) {
                    otherTags.remove(i);
                    break;
                }
            }
        }
        List<TagAdapter.Tag> tagList = new ArrayList<>();
        for (HourlyTrendDisplay tag : otherTags) {
            tagList.add(new HourlyTrendTag(tag));
        }
        int[] colors = ThemeManager.getInstance(this).getThemeColors(
                this, new int[] {
                        com.google.android.material.R.attr.colorOnPrimaryContainer,
                        com.google.android.material.R.attr.colorOnSecondaryContainer,
                        com.google.android.material.R.attr.colorPrimaryContainer,
                        com.google.android.material.R.attr.colorSecondaryContainer
                }
        );
        mTagAdapter = new TagAdapter(
                tagList,
                colors[0],
                colors[1],
                colors[2],
                colors[3],
                (checked, oldPosition, newPosition) -> {
                    setResult(RESULT_OK);
                    HourlyTrendTag tag = (HourlyTrendTag) mTagAdapter.removeItem(newPosition);
                    mHourlyTrendDisplayAdapter.insertItem(tag.tag);
                    resetBottomBarVisibility();
                    return true;
                }
        );

        mBinding.bottomRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        mBinding.bottomRecyclerView.addItemDecoration(
                new GridMarginsDecoration(
                        getResources().getDimension(R.dimen.normal_margin), mBinding.bottomRecyclerView
                )
        );
        mBinding.bottomRecyclerView.setAdapter(mTagAdapter);

        mBottomAnimator = null;
        mBottomBarVisibility = false;
        mBinding.bottomRecyclerView.post(this::resetBottomBarVisibility);
    }

    @Override
    protected void onStop() {
        super.onStop();

        List<HourlyTrendDisplay> oldList = SettingsManager.getInstance(this).getHourlyTrendDisplayList();
        List<HourlyTrendDisplay> newList = mHourlyTrendDisplayAdapter.getHourlyTrendDisplayList();
        if (!oldList.equals(newList)) {
            SettingsManager.getInstance(this).setHourlyTrendDisplayList(newList);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        // do nothing.
    }

    private void resetBottomBarVisibility() {
        boolean visible = mTagAdapter.getItemCount() != 0;
        if (mBottomBarVisibility == null || mBottomBarVisibility != visible) {
            mBottomBarVisibility = visible;

            if (mBottomAnimator != null) {
                mBottomAnimator.cancel();
            }
            mBottomAnimator = new AnimatorSet();
            mBottomAnimator.playTogether(
                    ObjectAnimator.ofFloat(mBinding.bottomBar, "alpha",
                            mBinding.bottomBar.getAlpha(), visible ? 1 : 0),
                    ObjectAnimator.ofFloat(mBinding.bottomBar, "translationY",
                            mBinding.bottomBar.getTranslationY(), visible ? 0 : mBinding.bottomBar.getMeasuredHeight())
            );
            mBottomAnimator.setDuration(visible ? 350 : 150);
            mBottomAnimator.setInterpolator(visible
                    ? new DecelerateInterpolator(2f)
                    : new AccelerateInterpolator(2f));
            mBottomAnimator.start();
        }
    }
}