package wangdaye.com.geometricweather.main.controller;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.basic.model.Location;

public abstract class AbstractMainItemController {

    protected Context context;
    protected View view;

    AbstractMainItemController(Context context, @NonNull View view) {
        this.context = context;
        this.view = view;
    }

    public abstract void onBindView(@NonNull Location location);

    boolean isDisplay(String targetValue) {
        for(String s: GeometricWeather.getInstance().getCardDisplayValues()){
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
