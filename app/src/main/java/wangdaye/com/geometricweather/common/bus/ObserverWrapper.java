package wangdaye.com.geometricweather.common.bus;

import androidx.lifecycle.Observer;

class ObserverWrapper<T> implements Observer<T> {

    private final LiveData<T> mHost;
    final Observer<? super T> observer;
    private int mVersion;

    ObserverWrapper(LiveData<T> host, Observer<? super T> inner, int version) {
        mHost = host;
        observer = inner;
        mVersion = version;
    }

    @Override
    public void onChanged(T t) {
        if (mVersion >= mHost.version) {
            return;
        }
        mVersion = mHost.version;
        observer.onChanged(t);
    }
}
