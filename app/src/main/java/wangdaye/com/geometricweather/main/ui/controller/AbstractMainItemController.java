package wangdaye.com.geometricweather.main.ui.controller;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;

import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

public abstract class AbstractMainItemController {

    protected Context context;
    protected View view;
    protected ResourceProvider provider;
    protected MainColorPicker picker;

    AbstractMainItemController(Context context, @NonNull View view,
                               @NonNull ResourceProvider provider, @NonNull MainColorPicker picker) {
        this.context = context;
        this.view = view;
        this.provider = provider;
        this.picker = picker;
    }

    public abstract void onBindView(@NonNull Location location);

    boolean isDisplay(String targetValue) {
        for(String s: SettingsOptionManager.getInstance(context).getCardDisplayValues()){
            if(s.equals(targetValue))
                return true;
        }
        return false;
    }

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
