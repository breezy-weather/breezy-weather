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
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.options.appearance.CardDisplay;
import wangdaye.com.geometricweather.common.ui.adapters.TagAdapter;
import wangdaye.com.geometricweather.common.ui.decotarions.GridMarginsDecoration;
import wangdaye.com.geometricweather.common.ui.decotarions.ListDecoration;
import wangdaye.com.geometricweather.common.ui.widgets.insets.FitSystemBarAppBarLayout;
import wangdaye.com.geometricweather.common.ui.widgets.insets.FitSystemBarRecyclerView;
import wangdaye.com.geometricweather.common.ui.widgets.slidingItem.SlidingItemTouchCallback;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.settings.adapters.CardDisplayAdapter;
import wangdaye.com.geometricweather.theme.ThemeManager;

public class CardDisplayManageActivity extends GeoActivity {

    private CardDisplayAdapter mCardDisplayAdapter;
    private ItemTouchHelper mCardDisplayItemTouchHelper;

    private TagAdapter mTagAdapter;

    private AppBarLayout mBottomBar;
    private @Nullable AnimatorSet mBottomAnimator;
    private @Nullable Boolean mBottomBarVisibility;
    private @Px int mElevation;

    private class CardTag implements TagAdapter.Tag {

        CardDisplay card;

        CardTag(CardDisplay card) {
            this.card = card;
        }

        @Override
        public String getName() {
            return card.getName(CardDisplayManageActivity.this);
        }
    }

    private class CardDisplaySwipeCallback extends SlidingItemTouchCallback {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getBindingAdapterPosition();
            int toPosition = target.getBindingAdapterPosition();

            mCardDisplayAdapter.moveItem(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            mCardDisplayAdapter.removeItem(viewHolder.getBindingAdapterPosition());
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
        this.setContentView(R.layout.activity_card_display_manage);

        mElevation = getResources().getDimensionPixelSize(R.dimen.touch_rise_z);

        FitSystemBarAppBarLayout appBarLayout = findViewById(R.id.activity_card_display_manage_appBar);
        appBarLayout.injectDefaultSurfaceTintColor();

        Toolbar toolbar = findViewById(R.id.activity_card_display_manage_toolbar);
        toolbar.setBackgroundColor(
                DisplayUtils.getWidgetSurfaceColor(
                        6f,
                        ThemeManager.getInstance(this).getThemeColor(this, R.attr.colorPrimary),
                        ThemeManager.getInstance(this).getThemeColor(this, R.attr.colorSurface)
                )
        );
        toolbar.setNavigationOnClickListener(view -> finish());

        List<CardDisplay> displayCards = SettingsManager.getInstance(this).getCardDisplayList();
        mCardDisplayAdapter = new CardDisplayAdapter(
                displayCards,
                cardDisplay -> {
                    setResult(RESULT_OK);
                    mTagAdapter.insertItem(new CardTag(cardDisplay));
                    resetBottomBarVisibility();
                },
                holder -> mCardDisplayItemTouchHelper.startDrag(holder)
        );

        RecyclerView recyclerView = findViewById(R.id.activity_card_display_manage_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(
                new ListDecoration(
                        this,
                        ThemeManager.getInstance(this).getThemeColor(this, R.attr.colorOutline)
                )
        );
        recyclerView.setAdapter(mCardDisplayAdapter);

        this.mCardDisplayItemTouchHelper = new ItemTouchHelper(new CardDisplaySwipeCallback());
        mCardDisplayItemTouchHelper.attachToRecyclerView(recyclerView);

        List<CardDisplay> otherCards = new ArrayList<>();
        otherCards.add(CardDisplay.CARD_DAILY_OVERVIEW);
        otherCards.add(CardDisplay.CARD_HOURLY_OVERVIEW);
        otherCards.add(CardDisplay.CARD_AIR_QUALITY);
        otherCards.add(CardDisplay.CARD_ALLERGEN);
        otherCards.add(CardDisplay.CARD_SUNRISE_SUNSET);
        otherCards.add(CardDisplay.CARD_LIFE_DETAILS);
        for (int i = otherCards.size() - 1; i >= 0; i --) {
            for (int j = 0; j < displayCards.size(); j ++) {
                if (otherCards.get(i) == displayCards.get(j)) {
                    otherCards.remove(i);
                    break;
                }
            }
        }
        List<TagAdapter.Tag> tagList = new ArrayList<>();
        for (CardDisplay card : otherCards) {
            tagList.add(new CardTag(card));
        }
        int[] colors = ThemeManager.getInstance(this).getThemeColors(
                this, new int[] {
                        R.attr.colorOnPrimaryContainer,
                        R.attr.colorOnSecondaryContainer,
                        R.attr.colorPrimaryContainer,
                        R.attr.colorSecondaryContainer
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
                    CardTag tag = (CardTag) mTagAdapter.removeItem(newPosition);
                    mCardDisplayAdapter.insertItem(tag.card);
                    resetBottomBarVisibility();
                    return true;
                }
        );

        mBottomBar = findViewById(R.id.activity_card_display_manage_bottomBar);

        FitSystemBarRecyclerView bottomRecyclerView = findViewById(R.id.activity_card_display_manage_bottomRecyclerView);
        bottomRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        bottomRecyclerView.addItemDecoration(
                new GridMarginsDecoration(
                        getResources().getDimension(R.dimen.normal_margin), bottomRecyclerView
                )
        );
        bottomRecyclerView.setAdapter(mTagAdapter);

        mBottomAnimator = null;
        mBottomBarVisibility = false;
        mBottomBar.post(this::resetBottomBarVisibility);
    }

    @Override
    protected void onStop() {
        super.onStop();

        List<CardDisplay> oldList = SettingsManager.getInstance(this).getCardDisplayList();
        List<CardDisplay> newList = mCardDisplayAdapter.getCardDisplayList();
        if (!oldList.equals(newList)) {
            SettingsManager.getInstance(this).setCardDisplayList(newList);
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
                    ObjectAnimator.ofFloat(mBottomBar, "alpha",
                            mBottomBar.getAlpha(), visible ? 1 : 0),
                    ObjectAnimator.ofFloat(mBottomBar, "translationY",
                            mBottomBar.getTranslationY(), visible ? 0 : mBottomBar.getMeasuredHeight())
            );
            mBottomAnimator.setDuration(visible ? 350 : 150);
            mBottomAnimator.setInterpolator(visible
                    ? new DecelerateInterpolator(2f)
                    : new AccelerateInterpolator(2f));
            mBottomAnimator.start();
        }
    }
}