package wangdaye.com.geometricweather.Widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Scroll view.
 * */

public class MyScrollView extends ScrollView {
    // widget
    private OnScrollViewListener onScrollViewListener = null;

    // data
    private float startX;
    private float startY;

    public MyScrollView(Context context) {
        super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float eventX = event.getX();
                final float eventY = event.getY();
                if (Math.abs(eventX - startX) > Math.abs(eventY - startY)) {
                    return false;
                }
        }
        return super.onInterceptTouchEvent(event);
    }

    public void setOnScrollViewListener(OnScrollViewListener scrollViewListener) {
        this.onScrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (onScrollViewListener != null) {
            onScrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

    public interface OnScrollViewListener {
        void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy);
    }
}
