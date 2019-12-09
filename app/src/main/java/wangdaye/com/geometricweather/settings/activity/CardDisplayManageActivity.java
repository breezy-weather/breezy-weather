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
import wangdaye.com.geometricweather.basic.model.option.appearance.CardDisplay;
import wangdaye.com.geometricweather.ui.adapter.TagAdapter;
import wangdaye.com.geometricweather.settings.OptionMapper;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.settings.adapter.CardDisplayAdapter;
import wangdaye.com.geometricweather.ui.decotarion.GridMarginsDecoration;
import wangdaye.com.geometricweather.ui.decotarion.ListDecoration;

public class CardDisplayManageActivity extends GeoActivity {

    private CardDisplayAdapter cardDisplayAdapter;
    private TagAdapter tagAdapter;

    private FrameLayout bottomBar;
    private @Nullable AnimatorSet bottomAnimator;
    private @Nullable Boolean bottomBarVisibility;

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

            cardDisplayAdapter.moveItem(fromPosition, toPosition);
            ((CardDisplayAdapter.ViewHolder) viewHolder).drawDrag(CardDisplayManageActivity.this, false);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            setResult(RESULT_OK);
            cardDisplayAdapter.removeItem(viewHolder.getAdapterPosition());
        }

        @Override
        public void onChildDraw(@NonNull Canvas c,
                                @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            switch (actionState) {
                case ItemTouchHelper.ACTION_STATE_SWIPE:
                    ((CardDisplayAdapter.ViewHolder) viewHolder)
                            .drawSwipe(dX);
                    break;

                case ItemTouchHelper.ACTION_STATE_DRAG:
                    ((CardDisplayAdapter.ViewHolder) viewHolder)
                            .drawDrag(CardDisplayManageActivity.this, dY != 0);
                    break;

                case ItemTouchHelper.ACTION_STATE_IDLE:
                    ((CardDisplayAdapter.ViewHolder) viewHolder)
                            .drawSwipe(0)
                            .drawDrag(CardDisplayManageActivity.this, false);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_card_display_manage);

        Toolbar toolbar = findViewById(R.id.activity_card_display_manage_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(view -> finish());

        List<CardDisplay> displayCards = SettingsOptionManager.getInstance(this).getCardDisplayList();
        cardDisplayAdapter = new CardDisplayAdapter(displayCards, cardDisplay -> {
            setResult(RESULT_OK);
            tagAdapter.insertItem(new CardTag(cardDisplay));
            resetBottomBarVisibility();
        });

        RecyclerView recyclerView = findViewById(R.id.activity_card_display_manage_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new ListDecoration(this));
        recyclerView.setAdapter(cardDisplayAdapter);

        new ItemTouchHelper(
                new CardDisplaySwipeCallback(
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
                )
        ).attachToRecyclerView(recyclerView);

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
        tagAdapter = new TagAdapter(tagList, (checked, oldPosition, newPosition) -> {
            setResult(RESULT_OK);
            CardTag tag = (CardTag) tagAdapter.removeItem(newPosition);
            cardDisplayAdapter.insertItem(tag.card);
            resetBottomBarVisibility();
            return true;
        });

        bottomBar = findViewById(R.id.activity_card_display_manage_bottomBar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            bottomBar.setOnApplyWindowInsetsListener((v, insets) -> {
                bottomBar.setPadding(0, 0, 0, insets.getSystemWindowInsetBottom());
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
        bottomRecyclerView.setAdapter(tagAdapter);

        bottomAnimator = null;
        bottomBarVisibility = false;
        bottomRecyclerView.post(this::resetBottomBarVisibility);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(
                        getString(R.string.key_card_display),
                        OptionMapper.getCardDisplayValue(cardDisplayAdapter.getCardDisplayList())
                ).apply();
        SettingsOptionManager.getInstance(this).setCardDisplayList(cardDisplayAdapter.getCardDisplayList());
    }

    @Override
    public View getSnackbarContainer() {
        return findViewById(R.id.activity_card_display_manage_container);
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
                    ObjectAnimator.ofFloat(bottomBar, "alpha",
                            bottomBar.getAlpha(), visible ? 1 : 0),
                    ObjectAnimator.ofFloat(bottomBar, "translationY",
                            bottomBar.getTranslationY(), visible ? 0 : bottomBar.getMeasuredHeight())
            );
            bottomAnimator.setDuration(visible ? 350 : 150);
            bottomAnimator.setInterpolator(visible
                    ? new DecelerateInterpolator(2f)
                    : new AccelerateInterpolator(2f));
            bottomAnimator.start();
        }
    }
}
