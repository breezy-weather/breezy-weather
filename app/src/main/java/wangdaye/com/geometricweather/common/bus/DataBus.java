package wangdaye.com.geometricweather.common.bus;

import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;

public class DataBus {

    private static volatile DataBus sInstance;

    public static DataBus getInstance() {
        if (sInstance == null) {
            synchronized (DataBus.class) {
                if (sInstance == null) {
                    sInstance = new DataBus();
                }
            }
        }
        return sInstance;
    }

    private final Map<String, LiveData<Object>> mLiveDataMap;
    private final Handler mMainHandler;

    private DataBus() {
        mLiveDataMap = new HashMap<>();
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    public <T> LiveData<T> with(String key, Class<T> type) {
        if (!mLiveDataMap.containsKey(key)) {
            mLiveDataMap.put(key, new LiveData<>(mMainHandler));
        }
        return (LiveData<T>) mLiveDataMap.get(key);
    }

    public LiveData<Object> with(String key) {
        return with(key, Object.class);
    }
}
