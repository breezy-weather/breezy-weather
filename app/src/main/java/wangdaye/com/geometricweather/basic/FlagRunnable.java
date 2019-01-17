package wangdaye.com.geometricweather.basic;

/**
 * Flag runnable.
 * */

public abstract class FlagRunnable implements Runnable {

    private boolean running;

    public FlagRunnable() {
        this.running = true;
    }

    public void setRunning(boolean b) {
        this.running = b;
    }

    public boolean isRunning() {
        return running;
    }
}
