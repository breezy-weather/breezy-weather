package wangdaye.com.geometricweather.ui.activity.main.controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import wangdaye.com.geometricweather.data.entity.model.Location;

public abstract class AbstractMainItemController {

    protected Context context;
    protected View view;

    AbstractMainItemController(Context context, @NonNull View view) {
        this.context = context;
        this.view = view;
    }

    public abstract void onBindView(@NonNull Location location);

    public int getTop() {
        return view.getTop();
    }

    public void onEnterScreen() {
        // do nothing.
    }

    public void onDestroy() {
        // do nothing.
    }
}
