package wangdaye.com.geometricweather.utils.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Thread manager.
 * */

public class ThreadManager {
    private ExecutorService threadPool;

    /** <br> singleton. */

    private static ThreadManager instance;

    public static ThreadManager getInstance() {
        if (instance == null) {
            synchronized (ThreadManager.class) {
                if (instance == null) {
                    instance = new ThreadManager();
                }
            }
        }
        return instance;
    }

    /** <br> life cycle. */

    private ThreadManager() {
        this.threadPool = Executors.newCachedThreadPool();
    }

    /** <br> data. */

    public void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }
}
