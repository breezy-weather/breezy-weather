package wangdaye.com.geometricweather.ui.widget.weatherView;

import wangdaye.com.geometricweather.basic.FlagRunnable;

public abstract class RenderRunnable extends FlagRunnable {

    @Override
    public void run() {
        long timestamp = -1;
        long interval;
        long remaining;
        while (isRunning()) {
            if (timestamp < 0) {
                interval = 0;
            } else {
                interval = System.currentTimeMillis() - timestamp;
            }
            timestamp = System.currentTimeMillis();
            onRender(interval);

            remaining = getInterval() - (System.currentTimeMillis() - timestamp);
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

    protected abstract long getInterval();
}
