package wangdaye.com.geometricweather.settings.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.models.options.appearance.CardDisplay;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.settings.adapters.CardDisplayAdapter;
import wangdaye.com.geometricweather.ui.adapters.TagAdapter;
import wangdaye.com.geometricweather.ui.decotarions.GridMarginsDecoration;
import wangdaye.com.geometricweather.ui.decotarions.ListDecoration;
import wangdaye.com.geometricweather.ui.widgets.slidingItem.SlidingItemTouchCallback;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public class CardDisplayManageActivity extends GeoActivity {

    private CardDisplayAdapter mCardDisplayAdapter;
    private ItemTouchHelper mCardDisplayItemTouchHelper;

    private TagAdapter mTagAdapter;

    private FrameLayout mBottomBar;
    private @Nullable AnimatorSet mBottomAnimator;
    private @Nullable Boolean mBottomBarVisibility;

    private class CardTag implements TagAdapter.Tag {

        CardDisplay card;

        CardTag(CardDisplay card) {
            this.card = card;
        }

        @Override
        public String getName() {
            return card.getCardName(CardDisplayManageActivity.this);
        }
    }

    private class CardDisplaySwipeCallback extends SlidingItemTouchCallback {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            setResult(RESULT_OK);

            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            mCardDisplayAdapter.moveItem(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            setResult(RESULT_OK);
            mCardDisplayAdapter.removeItem(viewHolder.getAdapterPosition());
        }

        @Override
        public void onChildDraw(@NonNull Canvas c,
                                @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    && actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                viewHolder.itemView.setElevation(
                        DisplayUtils.dpToPx(CardDisplayManageActivity.this, dY == 0 ? 0 : 10)
                );
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_card_display_manage);

        Toolbar toolbar = findViewById(R.id.activity_card_display_manage_toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());

        List<CardDisplay> displayCards = SettingsOptionManager.getInstance(this).getCardDisplayList();
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
        recyclerView.addItemDecoration(new ListDecoration(this));
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
        mTagAdapter = new TagAdapter(this, tagList, (checked, oldPosition, newPosition) -> {
            setResult(RESULT_OK);
            CardTag tag = (CardTag) mTagAdapter.removeItem(newPosition);
            mCardDisplayAdapter.insertItem(tag.card);
            resetBottomBarVisibility();
            return true;
        });

        mBottomBar = findViewById(R.id.activity_card_display_manage_bottomBar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            mBottomBar.setOnApplyWindowInsetsListener((v, insets) -> {
                mBottomBar.setPadding(0, 0, 0, insets.getSystemWindowInsetBottom());
                return insets;
            });
        }

        RecyclerView bottomRecyclerView = findViewById(R.id.activity_card_display_manage_bottomRecyclerView);
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
        bottomRecyclerView.post(this::resetBottomBarVisibility);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(
                        getString(R.string.key_card_display),
                        CardDisplay.toValue(mCardDisplayAdapter.getCardDisplayList())
                ).apply();
        SettingsOptionManager.getInstance(this).setCardDisplayList(mCardDisplayAdapter.getCardDisplayList());
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
