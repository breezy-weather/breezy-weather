package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView;

import android.content.Context;
import android.view.WindowManager;

public class IntervalComputer {

    private long mCurrentTime;
    private long mLastTime;

    private double mDefaultInterval;
    private double mInterval;

    public IntervalComputer(Context context) {
        reset(context);
    }

    public void reset(Context context) {
        mCurrentTime = -1;
        mLastTime = -1;

        double screenRefreshRate = 60;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            screenRefreshRate = windowManager.getDefaultDisplay().getRefreshRate();
        }
        if (screenRefreshRate < 60) {
            screenRefreshRate = 60;
        }

        mDefaultInterval = 1000.0 / screenRefreshRate;
        mInterval = mDefaultInterval;
    }

    public void invalidate() {
        mCurrentTime = System.currentTimeMillis();
        mInterval = mLastTime == -1 ? mDefaultInterval : (mCurrentTime - mLastTime);
        mLastTime = mCurrentTime;
    }

    public double getInterval() {
        return mInterval;
    }
}
