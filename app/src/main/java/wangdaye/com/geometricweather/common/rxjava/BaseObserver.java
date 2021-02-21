package wangdaye.com.geometricweather.common.rxjava;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.reactivex.observers.DisposableObserver;

public abstract class BaseObserver<T> extends DisposableObserver<T> {

    public abstract void onSucceed(T t);

    public abstract void onFailed();

    @Override
    public void onNext(@Nullable T t) {
        if (t == null) {
            onFailed();
        } else {
            onSucceed(t);
        }
    }

    @Override
    public void onError(@NonNull Throwable e) {
        onFailed();
    }

    @Override
    public void onComplete() {
        // do nothing.
    }
}