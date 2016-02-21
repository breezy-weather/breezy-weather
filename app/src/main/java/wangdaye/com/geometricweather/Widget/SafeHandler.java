package wangdaye.com.geometricweather.Widget;

import android.os.Handler;

import java.lang.ref.WeakReference;

/**
 * Created by WangDaYe on 2016/2/6.
 */
public class SafeHandler<T extends HandlerContainer> extends Handler {
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
}
