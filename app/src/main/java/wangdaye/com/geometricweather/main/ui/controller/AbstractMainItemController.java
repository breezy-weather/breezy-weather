package wangdaye.com.geometricweather.main.ui.controller;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import android.view.View;
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

    AbstractMainItemController(Context context, @NonNull View view,
                               @NonNull ResourceProvider provider, @NonNull MainColorPicker picker) {
        this.context = context;
        this.view = view;
        if (view instanceof CardView) {
            View child = ((CardView) view).getChildAt(0);
            if (child instanceof LinearLayout) {
                container = (LinearLayout) child;
            }
        }
        this.provider = provider;
        this.picker = picker;
    }

    public abstract void onBindView(@NonNull Location location);

    public int getTop() {
        return view.getTop();
    }

    public @Nullable LinearLayout getContainer() {
        return container;
    }

    public void onEnterScreen() {
        // do nothing.
    }

    public void onDestroy() {
        // do nothing.
    }
}
