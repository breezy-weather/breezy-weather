package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView;

public class IntervalComputer {

    private long currentTime;
    private long lastTime;
    private double interval;

    private static final double DEFAULT_INTERVAL = 16.6;

    public IntervalComputer() {
        reset();
    }

    public void reset() {
        currentTime = -1;
        lastTime = -1;
        interval = DEFAULT_INTERVAL;
    }

    public void invalidate() {
        currentTime = System.currentTimeMillis();
        interval = lastTime == -1
                ? DEFAULT_INTERVAL
                : (currentTime - lastTime);
        lastTime = currentTime;
    }

    public double getInterval() {
        return interval;
    }
}
