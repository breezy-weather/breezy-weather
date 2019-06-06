package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView;

import wangdaye.com.geometricweather.basic.FlagRunnable;

public abstract class RenderRunnable extends FlagRunnable {

    private long interval;

    public RenderRunnable(long interval) {
        this.interval = interval;
    }

    @Override
    public void run() {
        long timestamp;
        long remaining;
        while (isRunning()) {
            timestamp = System.currentTimeMillis();

            onRender(interval);

            remaining = interval - (System.currentTimeMillis() - timestamp);
            if (remaining > 0) {
                try {
                    Thread.sleep(remaining);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected abstract void onRender(long interval);
}
