package wangdaye.com.geometricweather.common.utils.helpers;

import androidx.annotation.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AsyncHelper {

    public static class Controller {

        final Disposable inner;

        Controller(Disposable inner) {
            this.inner = inner;
        }

        public void cancel() {
            inner.dispose();
        }
    }

    private static class Data<T> {

        final T t;
        final boolean done;

        Data(T t, boolean done) {
            this.t = t;
            this.done = done;
        }
    }

    public static class Emitter<T> {

        final @NonNull ObservableEmitter<Data<T>> inner;

        Emitter(@NonNull ObservableEmitter<Data<T>> inner) {
            this.inner = inner;
        }

        public void send(@Nullable T t, boolean done) {
            inner.onNext(new Data<>(t, done));
        }
    }

    public interface Task<T> {
        void execute(@NonNull Emitter<T> emitter);
    }

    public interface Callback<T> {
        void call(@Nullable T t, boolean done);
    }

    public static <T> Controller runOnIO(Task<T> task, Callback<T> callback) {
        return new Controller(
                Observable.create((ObservableOnSubscribe<Data<T>>) emitter -> task.execute(new Emitter<>(emitter)))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(data -> callback.call(data.t, data.done))
                        .subscribe()
        );
    }

    public static Controller runOnIO(Runnable runnable) {
        return new Controller(
                Observable.create(emitter -> runnable.run())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe()
        );
    }

    public static <T> Controller runOnExecutor(Task<T> task, Callback<T> callback, Executor executor) {
        return new Controller(
                Observable.create((ObservableOnSubscribe<Data<T>>) emitter -> task.execute(new Emitter<>(emitter)))
                        .subscribeOn(Schedulers.from(executor))
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(data -> callback.call(data.t, data.done))
                        .subscribe()
        );
    }

    public static Controller runOnExecutor(Runnable runnable, Executor executor) {
        return new Controller(
                Observable.create(emitter -> runnable.run())
                        .subscribeOn(Schedulers.from(executor))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe()
        );
    }

    public static <T> Controller delayRunOnIO(Runnable runnable, long milliSeconds) {
        return new Controller(
                Observable.timer(milliSeconds, TimeUnit.MILLISECONDS)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(Schedulers.io())
                        .doOnComplete(runnable::run)
                        .subscribe()
        );
    }

    public static Controller delayRunOnUI(Runnable runnable, long milliSeconds) {
        return new Controller(
                Observable.timer(milliSeconds, TimeUnit.MILLISECONDS)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete(runnable::run)
                        .subscribe()
        );
    }

    public static Controller intervalRunOnUI(Runnable runnable,
                                             long intervalMilliSeconds, long initDelayMilliSeconds) {
        return new Controller(
                Observable.interval(initDelayMilliSeconds, intervalMilliSeconds, TimeUnit.MILLISECONDS)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> runnable.run())
        );
    }
}
