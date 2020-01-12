package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView;

import android.content.Context;
import android.view.WindowManager;

public class IntervalComputer {

    private long currentTime;
    private long lastTime;

    private double defaultInterval;
    private double interval;

    public IntervalComputer(Context context) {
        reset(context);
    }

    public void reset(Context context) {
        currentTime = -1;
        lastTime = -1;

        double screenRefreshRate = 60;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            screenRefreshRate = windowManager.getDefaultDisplay().getRefreshRate();
        }
        if (screenRefreshRate < 60) {
            screenRefreshRate = 60;
        }

        defaultInterval = 1000.0 / screenRefreshRate;
        interval = defaultInterval;
    }

    public void invalidate() {
        currentTime = System.currentTimeMillis();
        interval = lastTime == -1 ? defaultInterval : (currentTime - lastTime);
        lastTime = currentTime;
    }

    public double getInterval() {
        return interval;
    }
}
