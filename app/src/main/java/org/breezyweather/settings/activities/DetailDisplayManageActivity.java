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
import org.breezyweather.R;
import org.breezyweather.common.basic.GeoActivity;
import org.breezyweather.common.basic.models.options.appearance.DetailDisplay;
import org.breezyweather.common.ui.adapters.TagAdapter;
import org.breezyweather.common.ui.decorations.GridMarginsDecoration;
import org.breezyweather.common.ui.decorations.ListDecoration;
import org.breezyweather.common.ui.widgets.slidingItem.SlidingItemTouchCallback;
import org.breezyweather.common.utils.DisplayUtils;
import org.breezyweather.databinding.ActivityDetailDisplayManageBinding;
import org.breezyweather.settings.SettingsManager;
import org.breezyweather.settings.adapters.DetailDisplayAdapter;
import org.breezyweather.theme.ThemeManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DetailDisplayManageActivity extends GeoActivity {

    private ActivityDetailDisplayManageBinding mBinding;

    private DetailDisplayAdapter mDetailDisplayAdapter;
    private ItemTouchHelper mDetailDisplayItemTouchHelper;

    private TagAdapter mTagAdapter;

    private @Nullable AnimatorSet mBottomAnimator;
    private @Nullable Boolean mBottomBarVisibility;
    private @Px int mElevation;

    private class DetailTag implements TagAdapter.Tag {

        DetailDisplay detail;

        DetailTag(DetailDisplay detail) {
            this.detail = detail;
        }

        @Override
        public String getName() {
            return detail.getName(DetailDisplayManageActivity.this);
        }
    }

    private class DetailDisplaySwipeCallback extends SlidingItemTouchCallback {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getBindingAdapterPosition();
            int toPosition = target.getBindingAdapterPosition();

            mDetailDisplayAdapter.moveItem(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            mDetailDisplayAdapter.removeItem(viewHolder.getBindingAdapterPosition());
        }

        @Override
        public void onChildDraw(@NonNull Canvas c,
                                @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            ViewCompat.setElevation(viewHolder.itemView,
                    (dY != 0 || isCurrentlyActive) ? mElevation : 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityDetailDisplayManageBinding.inflate(getLayoutInflater());
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

        List<DetailDisplay> displayDetails = SettingsManager.getInstance(this).getDetailDisplayList();
        mDetailDisplayAdapter = new DetailDisplayAdapter(
                displayDetails,
                detailDisplay -> {
                    setResult(RESULT_OK);
                    mTagAdapter.insertItem(new DetailTag(detailDisplay));
                    resetBottomBarVisibility();
                },
                holder -> mDetailDisplayItemTouchHelper.startDrag(holder)
        );

        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recyclerView.addItemDecoration(
                new ListDecoration(
                        this,
                        ThemeManager.getInstance(this).getThemeColor(this, com.google.android.material.R.attr.colorOutline)
                )
        );
        mBinding.recyclerView.setAdapter(mDetailDisplayAdapter);

        mDetailDisplayItemTouchHelper = new ItemTouchHelper(new DetailDisplaySwipeCallback());
        mDetailDisplayItemTouchHelper.attachToRecyclerView(mBinding.recyclerView);

        List<DetailDisplay> otherDetails = new ArrayList<>();
        otherDetails.add(DetailDisplay.DETAIL_FEELS_LIKE);
        otherDetails.add(DetailDisplay.DETAIL_WIND);
        otherDetails.add(DetailDisplay.DETAIL_UV_INDEX);
        otherDetails.add(DetailDisplay.DETAIL_HUMIDITY);
        otherDetails.add(DetailDisplay.DETAIL_DEW_POINT);
        otherDetails.add(DetailDisplay.DETAIL_PRESSURE);
        otherDetails.add(DetailDisplay.DETAIL_VISIBILITY);
        otherDetails.add(DetailDisplay.DETAIL_CLOUD_COVER);
        otherDetails.add(DetailDisplay.DETAIL_CEILING);
        for (int i = otherDetails.size() - 1; i >= 0; i --) {
            for (int j = 0; j < displayDetails.size(); j ++) {
                if (otherDetails.get(i) == displayDetails.get(j)) {
                    otherDetails.remove(i);
                    break;
                }
            }
        }
        List<TagAdapter.Tag> tagList = new ArrayList<>();
        for (DetailDisplay detail : otherDetails) {
            tagList.add(new DetailTag(detail));
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
                    DetailTag tag = (DetailTag) mTagAdapter.removeItem(newPosition);
                    mDetailDisplayAdapter.insertItem(tag.detail);
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

        List<DetailDisplay> oldList = SettingsManager.getInstance(this).getDetailDisplayList();
        List<DetailDisplay> newList = mDetailDisplayAdapter.getDetailDisplayList();
        if (!oldList.equals(newList)) {
            SettingsManager.getInstance(this).setDetailDisplayList(newList);
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