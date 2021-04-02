package wangdaye.com.geometricweather.common.bus;

import androidx.lifecycle.Observer;

import java.lang.ref.WeakReference;

class ObserverWrapper<T> implements Observer<T> {

    private final WeakReference<LiveData<T>> mHost;
    final Observer<? super T> observer;
    private int mVersion;

    ObserverWrapper(LiveData<T> host, Observer<? super T> inner, int version) {
        mHost = new WeakReference<>(host);
        observer = inner;
        mVersion = version;
    }

    @Override
    public void onChanged(T t) {
        if (mHost.get() == null) {
            return;
        }
        if (mVersion >= mHost.get().version) {
            return;
        }
        mVersion = mHost.get().version;
        observer.onChanged(t);
    }
}
