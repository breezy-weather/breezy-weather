package wangdaye.com.geometricweather.common.rxjava;

import io.reactivex.observers.DisposableObserver;
import retrofit2.HttpException;

public abstract class BaseObserver<T> extends DisposableObserver<T> {

    protected Integer code;

    public abstract void onSucceed(T t);

    public abstract void onFailed();

    public Integer getStatusCode() {
        return code;
    }

    public Boolean isApiLimitReached() {
        return code == 429;
    }

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
        this.code = ((HttpException) e).code();
        onFailed();
    }

    @Override
    public void onComplete() {
        // do nothing.
    }
}