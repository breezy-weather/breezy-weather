package wangdaye.com.geometricweather.settings.activity;

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
import wangdaye.com.geometricweather.basic.model.option.appearance.DailyTrendDisplay;
import wangdaye.com.geometricweather.basic.model.option.utils.OptionMapper;
import wangdaye.com.geometricweather.databinding.ActivityDailyTrendDisplayManageBinding;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.settings.adapter.DailyTrendDisplayAdapter;
import wangdaye.com.geometricweather.ui.adapter.TagAdapter;
import wangdaye.com.geometricweather.ui.decotarion.GridMarginsDecoration;
import wangdaye.com.geometricweather.ui.decotarion.ListDecoration;

public class DailyTrendDisplayManageActivity extends GeoActivity {

    ActivityDailyTrendDisplayManageBinding binding;
    private DailyTrendDisplayAdapter dailyTrendDisplayAdapter;
    private TagAdapter tagAdapter;

    private @Nullable AnimatorSet bottomAnimator;
    private @Nullable Boolean bottomBarVisibility;

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

    private class CardDisplaySwipeCallback extends ItemTouchHelper.SimpleCallback {

        CardDisplaySwipeCallback(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            setResult(RESULT_OK);

            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            dailyTrendDisplayAdapter.moveItem(fromPosition, toPosition);
            ((DailyTrendDisplayAdapter.ViewHolder) viewHolder).drawDrag(DailyTrendDisplayManageActivity.this, false);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            setResult(RESULT_OK);
            dailyTrendDisplayAdapter.removeItem(viewHolder.getAdapterPosition());
        }

        @Override
        public void onChildDraw(@NonNull Canvas c,
                                @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            switch (actionState) {
                case ItemTouchHelper.ACTION_STATE_SWIPE:
                    ((DailyTrendDisplayAdapter.ViewHolder) viewHolder).drawSwipe(dX);
                    break;

                case ItemTouchHelper.ACTION_STATE_DRAG:
                    ((DailyTrendDisplayAdapter.ViewHolder) viewHolder)
                            .drawDrag(DailyTrendDisplayManageActivity.this, dY != 0);
                    break;

                case ItemTouchHelper.ACTION_STATE_IDLE:
                    ((DailyTrendDisplayAdapter.ViewHolder) viewHolder)
                            .drawSwipe(0)
                            .drawDrag(DailyTrendDisplayManageActivity.this, false);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityDailyTrendDisplayManageBinding.inflate(getLayoutInflater());
        this.setContentView(binding.getRoot());

        binding.toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        List<DailyTrendDisplay> displayTags
                = SettingsOptionManager.getInstance(this).getDailyTrendDisplayList();
        dailyTrendDisplayAdapter = new DailyTrendDisplayAdapter(displayTags, dailyTrendDisplay -> {
            setResult(RESULT_OK);
            tagAdapter.insertItem(new DailyTrendTag(dailyTrendDisplay));
            resetBottomBarVisibility();
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.addItemDecoration(new ListDecoration(this));
        binding.recyclerView.setAdapter(dailyTrendDisplayAdapter);

        new ItemTouchHelper(
                new CardDisplaySwipeCallback(
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
                )
        ).attachToRecyclerView(binding.recyclerView);

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
        tagAdapter = new TagAdapter(this, tagList, (checked, oldPosition, newPosition) -> {
            setResult(RESULT_OK);
            DailyTrendTag tag = (DailyTrendTag) tagAdapter.removeItem(newPosition);
            dailyTrendDisplayAdapter.insertItem(tag.tag);
            resetBottomBarVisibility();
            return true;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            binding.bottomBar.setOnApplyWindowInsetsListener((v, insets) -> {
                binding.bottomBar.setPadding(0, 0, 0, insets.getSystemWindowInsetBottom());
                return insets;
            });
        }

        binding.bottomRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.bottomRecyclerView.addItemDecoration(
                new GridMarginsDecoration(
                        getResources().getDimension(R.dimen.normal_margin), binding.bottomRecyclerView
                )
        );
        binding.bottomRecyclerView.setAdapter(tagAdapter);

        bottomAnimator = null;
        bottomBarVisibility = false;
        binding.bottomRecyclerView.post(this::resetBottomBarVisibility);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(
                        getString(R.string.key_daily_trend_display),
                        OptionMapper.getDailyTrendDisplayValue(dailyTrendDisplayAdapter.getDailyTrendDisplayList())
                ).apply();
        SettingsOptionManager.getInstance(this).setDailyTrendDisplayList(
                dailyTrendDisplayAdapter.getDailyTrendDisplayList());
    }

    @Override
    public View getSnackbarContainer() {
        return binding.container;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        // do nothing.
    }

    private void resetBottomBarVisibility() {
        boolean visible = tagAdapter.getItemCount() != 0;
        if (bottomBarVisibility == null || bottomBarVisibility != visible) {
            bottomBarVisibility = visible;

            if (bottomAnimator != null) {
                bottomAnimator.cancel();
            }
            bottomAnimator = new AnimatorSet();
            bottomAnimator.playTogether(
                    ObjectAnimator.ofFloat(binding.bottomBar, "alpha",
                            binding.bottomBar.getAlpha(), visible ? 1 : 0),
                    ObjectAnimator.ofFloat(binding.bottomBar, "translationY",
                            binding.bottomBar.getTranslationY(), visible ? 0 : binding.bottomBar.getMeasuredHeight())
            );
            bottomAnimator.setDuration(visible ? 350 : 150);
            bottomAnimator.setInterpolator(visible
                    ? new DecelerateInterpolator(2f)
                    : new AccelerateInterpolator(2f));
            bottomAnimator.start();
        }
    }
}
