package wangdaye.com.geometricweather.settings.activity;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.option.appearance.CardDisplay;
import wangdaye.com.geometricweather.main.ui.adapter.TagAdapter;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.settings.adapter.CardDisplayAdapter;

public class CardDisplayManageActivity extends GeoActivity {

    private CardDisplayAdapter cardDisplayAdapter;
    private RecyclerView recyclerView;

    private LinearLayout bottomBar;

    private TagAdapter tagAdapter;
    private RecyclerView bottomRecyclerView;

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
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            cardDisplayAdapter.moveItem(fromPosition, toPosition);
            ((CardDisplayAdapter.ViewHolder) viewHolder).drawDrag(CardDisplayManageActivity.this, false);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
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
            // TODO: 2019/10/27
        });

        recyclerView = findViewById(R.id.activity_card_display_manage_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cardDisplayAdapter);

        List<CardDisplay> otherCards = new ArrayList<>();
        otherCards.add(CardDisplay.CARD_DAILY_OVERVIEW);
        otherCards.add(CardDisplay.CARD_HOURLY_OVERVIEW);
        otherCards.add(CardDisplay.CARD_AIR_QUALITY);
        otherCards.add(CardDisplay.CARD_SUNRISE_SUNSET);
        otherCards.add(CardDisplay.CARD_LIFE_DETAILS);
        for (int i = otherCards.size() - 1; i >= 0; i --) {
            for (int j = 0; j < displayCards.size(); i ++) {
                if (otherCards.get(i) == displayCards.get(j)) {
                    otherCards.remove(i);
                    break;
                }
            }
        }
        List<CardTag> tagList = new ArrayList<>();
        for (CardDisplay card : otherCards) {
            tagList.add(new CardTag(card));
        }
        tagAdapter = new TagAdapter(tagList, (checked, position) -> {

        });

        bottomRecyclerView = findViewById(R.id.activity_card_display_manage_bottomRecyclerView);
        bottomRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        bottomRecyclerView.setAdapter(tagAdapter);

        bottomBar = findViewById(R.id.activity_card_display_manage_bottomBar);
        bottomBar.setVisibility(tagAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
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
}
