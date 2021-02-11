package wangdaye.com.geometricweather.utils.managers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Thread manager.
 * */

public class ThreadManager {

    private static ThreadManager sInstance;

    public static ThreadManager getInstance() {
        if (sInstance == null) {
            synchronized (ThreadManager.class) {
                if (sInstance == null) {
                    sInstance = new ThreadManager();
                }
            }
        }
        return sInstance;
    }

    private final ExecutorService mThreadPool;

    private ThreadManager() {
        this.mThreadPool = Executors.newCachedThreadPool();
    }

    public void execute(Runnable runnable) {
        mThreadPool.execute(runnable);
    }
}
