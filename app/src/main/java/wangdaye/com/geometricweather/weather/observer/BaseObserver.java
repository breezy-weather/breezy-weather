package wangdaye.com.geometricweather.weather.observer;

import io.reactivex.observers.DisposableObserver;

public abstract class BaseObserver<T> extends DisposableObserver<T> {

    public abstract void onSucceed(T t);

    public abstract void onFailed();

    @Override
    public void onNext(T t) {
        if (t == null) {
            onFailed();
        } else {
            onSucceed(t);
        }
    }

    @Override
    public void onError(Throwable e) {
        onFailed();
    }

    @Override
    public void onComplete() {
        // do nothing.
    }
}