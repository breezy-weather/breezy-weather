package wangdaye.com.geometricweather.utils.helpter;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

public class AsyncHelper {

    private static class Data<T> {
        final T t;

        Data(T t) {
            this.t = t;
        }
    }

    public static class Emitter<T> {

        final @NonNull ObservableEmitter<Data<T>> inner;

        Emitter(@NonNull ObservableEmitter<Data<T>> inner) {
            this.inner = inner;
        }

        public void send(@Nullable T t) {
            inner.onNext(new Data<>(t));
        }
    }

    public interface Task<T> {
        void execute(@NonNull Emitter<T> emitter);
    }

    public interface Callback<T> {
        void call(@Nullable T t);
    }

    public static <T> void runOnIO(Task<T> task, Callback<T> callback) {
        Observable.create((ObservableOnSubscribe<Data<T>>) emitter -> task.execute(new Emitter<>(emitter)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(data -> callback.call(data.t))
                .subscribe();
    }

    public static <T> void runOnIO(Runnable runnable) {
        Observable.create((ObservableOnSubscribe<T>) emitter -> runnable.run())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public static <T> void delayRunOnIO(Runnable runnable, long milliSeconds) {
        Observable.create((ObservableOnSubscribe<T>) emitter -> runnable.run())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .delay(milliSeconds, TimeUnit.MILLISECONDS)
                .subscribe();
    }
}
