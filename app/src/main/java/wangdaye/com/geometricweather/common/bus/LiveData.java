package wangdaye.com.geometricweather.common.bus;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.HashMap;
import java.util.Map;

public class LiveData<T> extends MutableLiveData<T> {

    private final Map<Observer<? super T>, ObserverWrapper<T>> mWrapperMap;
    private final Handler mMainHandler;
    int version;

    static final int START_VERSION = -1;

    LiveData(Handler mainHandler) {
        super();
        mWrapperMap = new HashMap<>();
        mMainHandler = mainHandler;
        version = START_VERSION;
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        runOnMainThread(() -> innerObserver(owner,
                new ObserverWrapper<>(this, observer, version)));
    }

    public void observeStickily(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        runOnMainThread(() -> innerObserver(owner,
                new ObserverWrapper<>(this, observer, START_VERSION)));
    }

    private void innerObserver(@NonNull LifecycleOwner owner, ObserverWrapper<T> wrapper) {
        mWrapperMap.put(wrapper.observer, wrapper);
        super.observe(owner, wrapper);
    }

    @Override
    public void observeForever(@NonNull Observer<? super T> observer) {
        runOnMainThread(() -> innerObserverForever(
                new ObserverWrapper<>(this, observer, version)));
    }

    public void observeStickilyForever(@NonNull Observer<? super T> observer) {
        runOnMainThread(() -> innerObserverForever(
                new ObserverWrapper<>(this, observer, START_VERSION)));
    }

    private void innerObserverForever(ObserverWrapper<T> wrapper) {
        mWrapperMap.put(wrapper.observer, wrapper);
        super.observeForever(wrapper);
    }

    @Override
    public void removeObserver(@NonNull Observer<? super T> observer) {
        runOnMainThread(() -> {
            ObserverWrapper<T> wrapper = mWrapperMap.remove(observer);
            if (wrapper != null) {
                super.removeObserver(wrapper);
            }
        });
    }

    @Override
    public void setValue(T value) {
        version ++;
        super.setValue(value);
    }

    @Override
    public void postValue(T value) {
        runOnMainThread(() -> setValue(value));
    }

    private void runOnMainThread(Runnable r) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            r.run();
        } else {
            mMainHandler.post(r);
        }
    }
}
