package wangdaye.com.geometricweather.settings.activities;

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
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.options.appearance.DailyTrendDisplay;
import wangdaye.com.geometricweather.common.ui.adapters.TagAdapter;
import wangdaye.com.geometricweather.common.ui.decotarions.GridMarginsDecoration;
import wangdaye.com.geometricweather.common.ui.decotarions.ListDecoration;
import wangdaye.com.geometricweather.common.ui.widgets.slidingItem.SlidingItemTouchCallback;
import wangdaye.com.geometricweather.theme.DefaultThemeManager;
import wangdaye.com.geometricweather.databinding.ActivityDailyTrendDisplayManageBinding;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.settings.adapters.DailyTrendDisplayAdapter;

public class DailyTrendDisplayManageActivity extends GeoActivity {

    private ActivityDailyTrendDisplayManageBinding mBinding;

    private DailyTrendDisplayAdapter mDailyTrendDisplayAdapter;
    private ItemTouchHelper mDailyTrendDisplayItemTouchHelper;

    private TagAdapter mTagAdapter;

    private @Nullable AnimatorSet mBottomAnimator;
    private @Nullable Boolean mBottomBarVisibility;
    private @Px int mElevation;

    private class DailyTrendTag implements TagAdapter.Tag {

        DailyTrendDisplay tag;

        DailyTrendTag(DailyTrendDisplay tag) {
            this.tag = tag;
        }

        @Override
        public String getName() {
            return tag.getTagName(DailyTrendDisplayManageActivity.this);
        }
    }

    private class CardDisplaySwipeCallback extends SlidingItemTouchCallback {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            setResult(RESULT_OK);

            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            mDailyTrendDisplayAdapter.moveItem(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            setResult(RESULT_OK);
            mDailyTrendDisplayAdapter.removeItem(viewHolder.getAdapterPosition());
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
        mBinding = ActivityDailyTrendDisplayManageBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mElevation = getResources().getDimensionPixelSize(R.dimen.touch_rise_z);

        mBinding.toolbar.setNavigationOnClickListener(view -> finish());

        List<DailyTrendDisplay> displayTags
                = SettingsManager.getInstance(this).getDailyTrendDisplayList();
        mDailyTrendDisplayAdapter = new DailyTrendDisplayAdapter(
                displayTags,
                dailyTrendDisplay -> {
                    setResult(RESULT_OK);
                    mTagAdapter.insertItem(new DailyTrendTag(dailyTrendDisplay));
                    resetBottomBarVisibility();
                },
                holder -> mDailyTrendDisplayItemTouchHelper.startDrag(holder)
        );

        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recyclerView.addItemDecoration(new ListDecoration(
                this, ContextCompat.getColor(this, R.color.colorLine)));
        mBinding.recyclerView.setAdapter(mDailyTrendDisplayAdapter);

        mDailyTrendDisplayItemTouchHelper = new ItemTouchHelper(new CardDisplaySwipeCallback());
        mDailyTrendDisplayItemTouchHelper.attachToRecyclerView(mBinding.recyclerView);

        List<DailyTrendDisplay> otherTags = new ArrayList<>();
        otherTags.add(DailyTrendDisplay.TAG_TEMPERATURE);
        otherTags.add(DailyTrendDisplay.TAG_AIR_QUALITY);
        otherTags.add(DailyTrendDisplay.TAG_WIND);
        otherTags.add(DailyTrendDisplay.TAG_UV_INDEX);
        otherTags.add(DailyTrendDisplay.TAG_PRECIPITATION);
        for (int i = otherTags.size() - 1; i >= 0; i --) {
            for (int j = 0; j < displayTags.size(); j ++) {
                if (otherTags.get(i) == displayTags.get(j)) {
                    otherTags.remove(i);
                    break;
                }
            }
        }
        List<TagAdapter.Tag> tagList = new ArrayList<>();
        for (DailyTrendDisplay tag : otherTags) {
            tagList.add(new DailyTrendTag(tag));
        }
        mTagAdapter = new TagAdapter(tagList, (checked, oldPosition, newPosition) -> {
            setResult(RESULT_OK);
            DailyTrendTag tag = (DailyTrendTag) mTagAdapter.removeItem(newPosition);
            mDailyTrendDisplayAdapter.insertItem(tag.tag);
            resetBottomBarVisibility();
            return true;
        }, new DefaultThemeManager());

        mBinding.bottomRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        mBinding.bottomRecyclerView.setAdaptiveWidthEnabled(false);
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
        SettingsManager.getInstance(this).setDailyTrendDisplayList(
                mDailyTrendDisplayAdapter.getDailyTrendDisplayList());
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
