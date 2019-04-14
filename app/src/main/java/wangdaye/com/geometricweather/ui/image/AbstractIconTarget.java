package wangdaye.com.geometricweather.ui.image;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.SquaringDrawable;
import com.bumptech.glide.request.target.Target;

public abstract class AbstractIconTarget
        implements Target<GlideDrawable>, GlideAnimation.ViewAdapter {

    private GlideDrawable resource;
    private int size;

    public AbstractIconTarget(int size) {
        this.size = size;
    }

    public abstract View getTarget();

    public abstract void setDrawableForTarget(Drawable d);

    public abstract Drawable getDrawableFromTarget();

    public abstract void setTagForTarget(Object tag);

    public abstract Object getTagFromTarget();

    private void setTag(Object tag) {
        setTagForTarget(tag);
    }

    private Object getTag() {
        return getTagFromTarget();
    }

    public int getSize() {
        return size;
    }

    // interface.

    // target.

    @Override
    public void setRequest(Request request) {
        setTag(request);
    }

    @Override
    public Request getRequest() {
        Object tag = getTag();
        Request request = null;
        if (tag != null) {
            if (tag instanceof Request) {
                request = (Request) tag;
            } else {
                throw new IllegalArgumentException("You must not call setTagForTarget() on a view Glide is targeting");
            }
        }
        return request;
    }

    @Override
    public void onLoadStarted(Drawable placeholder) {
        setDrawableForTarget(placeholder);
    }

    @Override
    public void onLoadFailed(Exception e, Drawable errorDrawable) {
        setDrawableForTarget(errorDrawable);
    }

    @Override
    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
        if (!resource.isAnimated()) {
            resource = new SquaringDrawable(resource, size);
        }
        if (glideAnimation == null || !glideAnimation.animate(resource, this)) {
            setDrawableForTarget(resource);
        }
        this.resource = resource;
        resource.setLoopCount(GlideDrawable.LOOP_FOREVER);
        resource.start();
    }

    @Override
    public void onLoadCleared(Drawable placeholder) {
        setDrawableForTarget(placeholder);
    }

    @Override
    public void getSize(SizeReadyCallback cb) {
        cb.onSizeReady(size, size);
    }

    // lifecycle.

    @Override
    public void onStart() {
        if (resource != null) {
            resource.start();
        }
    }

    @Override
    public void onStop() {
        if (resource != null) {
            resource.stop();
        }
    }

    @Override
    public void onDestroy() {
        // do nothing.
    }

    // view adapter.

    @Override
    public View getView() {
        return getTarget();
    }

    @Override
    public Drawable getCurrentDrawable() {
        return getDrawableFromTarget();
    }

    @Override
    public void setDrawable(Drawable drawable) {
        setDrawableForTarget(drawable);
    }
}
