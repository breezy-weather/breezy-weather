package wangdaye.com.geometricweather.utils;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Safe handler.
 * */

public class SafeHandler<T extends SafeHandler.HandlerContainer> extends Handler {

    protected WeakReference<T> mRef;

    public SafeHandler(WeakReference<T> ref) {
        mRef = ref;
    }

    public SafeHandler(T obj) {
        mRef = new WeakReference<>(obj);
    }

    public T getContainer() {
        return mRef.get();
    }

    @Override
    public void handleMessage(android.os.Message msg) {
        super.handleMessage(msg);
        HandlerContainer container = getContainer();
        if (container != null) {
            container.handleMessage(msg);
        }
    }

    public interface HandlerContainer {
        void handleMessage(Message message);
    }
}
