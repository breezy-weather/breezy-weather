package wangdaye.com.geometricweather.utils.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Thread manager.
 * */

public class ThreadManager {

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

    private ExecutorService threadPool;

    private ThreadManager() {
        this.threadPool = Executors.newCachedThreadPool();
    }

    public void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }
}
