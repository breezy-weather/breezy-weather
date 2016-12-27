package wangdaye.com.geometricweather.utils.widget;

import android.support.annotation.NonNull;

/**
 * Priority runnable.
 * */

public abstract class PriorityRunnable implements Runnable, Comparable<PriorityRunnable> {
    // data
    private boolean runNow;

    /** <br> life cycle. */

    public PriorityRunnable(boolean runNow) {
        this.runNow = runNow;
    }

    /** interface. */

    @Override
    public int compareTo(@NonNull PriorityRunnable priorityRunnable) {
        if (priorityRunnable.runNow && !runNow) {
            return 1;
        } else if (!priorityRunnable.runNow && runNow) {
            return -1;
        } else {
            return 0;
        }
    }
}
