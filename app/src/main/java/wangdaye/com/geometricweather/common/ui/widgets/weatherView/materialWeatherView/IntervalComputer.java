package wangdaye.com.geometricweather.common.ui.widgets.weatherView.materialWeatherView;

public class IntervalComputer {

    private long mCurrentTime;
    private long mLastTime;

    private double mInterval;

    public IntervalComputer() {
        reset();
    }

    public void reset() {
        mCurrentTime = -1;
        mLastTime = -1;

        mInterval = 0;
    }

    public void invalidate() {
        mCurrentTime = System.currentTimeMillis();
        mInterval = mLastTime == -1 ? 0 : (mCurrentTime - mLastTime);
        mLastTime = mCurrentTime;
    }

    public double getInterval() {
        return mInterval;
    }
}
