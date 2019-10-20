package wangdaye.com.geometricweather.main.ui.controller;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.cardview.widget.CardView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;

public abstract class AbstractMainItemController {

    protected Context context;
    protected View view;
    protected @Nullable LinearLayout container;
    protected ResourceProvider provider;
    protected MainColorPicker picker;
    private boolean inScreen;

    AbstractMainItemController(Context context, @NonNull View view,
                               @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                               @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                               @Px float cardRadius) {
        this.context = context;
        this.view = view;
        if (view instanceof CardView) {
            CardView card = (CardView) view;
            card.setRadius(cardRadius);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) card.getLayoutParams();
            params.setMargins((int) cardMarginsHorizontal, 0, (int) cardMarginsHorizontal, (int) cardMarginsVertical);
            card.setLayoutParams(params);

            View child = card.getChildAt(0);
            if (child instanceof LinearLayout) {
                container = (LinearLayout) child;
            }
        }
        this.provider = provider;
        this.picker = picker;
        this.inScreen = false;
    }

    public abstract void onBindView(@NonNull Location location);

    public int getTop() {
        return view.getTop();
    }

    public @Nullable LinearLayout getContainer() {
        return container;
    }

    public final void enterScreen() {
        if (!inScreen) {
            inScreen = true;
            onEnterScreen();
        }
    }

    public void onEnterScreen() {
        // do nothing.
    }

    public void onDestroy() {
        // do nothing.
    }
}
