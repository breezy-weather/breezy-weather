package wangdaye.com.geometricweather.Widget;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Swipe refresh layout.
 * */

public class MySwipeRefreshLayout extends SwipeRefreshLayout {
    // data
    private int minTouchSlop;
    private float startX;
    private float startY;

    public MySwipeRefreshLayout(Context context) {
        super(context);
        minTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public MySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        minTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
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
                float xDiff = Math.abs(eventX - startX);
                if (Math.abs(eventX - startX) > Math.abs(eventY - startY) / Math.sqrt(3) || xDiff > minTouchSlop + 60) {
                    return false;
                }
        }
        return super.onInterceptTouchEvent(event);
    }
}
