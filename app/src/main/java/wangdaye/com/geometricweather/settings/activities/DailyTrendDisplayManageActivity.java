package wangdaye.com.geometricweather.settings.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.models.options.appearance.DailyTrendDisplay;
import wangdaye.com.geometricweather.databinding.ActivityDailyTrendDisplayManageBinding;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.settings.adapters.DailyTrendDisplayAdapter;
import wangdaye.com.geometricweather.ui.adapters.TagAdapter;
import wangdaye.com.geometricweather.ui.decotarions.GridMarginsDecoration;
import wangdaye.com.geometricweather.ui.decotarions.ListDecoration;
import wangdaye.com.geometricweather.ui.widgets.slidingItem.SlidingItemTouchCallback;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public class DailyTrendDisplayManageActivity extends GeoActivity {

    private ActivityDailyTrendDisplayManageBinding mBinding;

    private DailyTrendDisplayAdapter mDailyTrendDisplayAdapter;
    private ItemTouchHelper mDailyTrendDisplayItemTouchHelper;

    private TagAdapter mTagAdapter;

    private @Nullable AnimatorSet mBottomAnimator;
    private @Nullable Boolean mBottomBarVisibility;

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    && actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                viewHolder.itemView.setElevation(
                        DisplayUtils.dpToPx(DailyTrendDisplayManageActivity.this, dY == 0 ? 0 : 10)
                );
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityDailyTrendDisplayManageBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.toolbar.setNavigationOnClickListener(view -> finish());

        List<DailyTrendDisplay> displayTags
                = SettingsOptionManager.getInstance(this).getDailyTrendDisplayList();
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
        mBinding.recyclerView.addItemDecoration(new ListDecoration(this));
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
        mTagAdapter = new TagAdapter(this, tagList, (checked, oldPosition, newPosition) -> {
            setResult(RESULT_OK);
            DailyTrendTag tag = (DailyTrendTag) mTagAdapter.removeItem(newPosition);
            mDailyTrendDisplayAdapter.insertItem(tag.tag);
            resetBottomBarVisibility();
            return true;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            mBinding.bottomBar.setOnApplyWindowInsetsListener((v, insets) -> {
                mBinding.bottomBar.setPadding(0, 0, 0, insets.getSystemWindowInsetBottom());
                return insets;
            });
        }

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
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(
                        getString(R.string.key_daily_trend_display),
                        DailyTrendDisplay.toValue(mDailyTrendDisplayAdapter.getDailyTrendDisplayList())
                ).apply();
        SettingsOptionManager.getInstance(this).setDailyTrendDisplayList(
                mDailyTrendDisplayAdapter.getDailyTrendDisplayList());
    }

    @Override
    public View getSnackbarContainer() {
        return mBinding.container;
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
