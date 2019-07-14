package wangdaye.com.geometricweather.ui.widget.verticalScrollView;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Vertical swipe refresh layout.
 * */

public class VerticalSwipeRefreshLayout extends SwipeRefreshLayout {

    private float initialX, initialY;
    private int touchSlop;

    private boolean isBeingDragged = false;
    private boolean isHorizontalDragged = false;

    public VerticalSwipeRefreshLayout(Context context) {
        super(context);
        this.initialize();
    }

    public VerticalSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    private void initialize() {
        this.touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = super.onInterceptTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isBeingDragged = false;
                isHorizontalDragged = false;
                initialX = ev.getX();
                initialY = ev.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                if (!isBeingDragged && !isHorizontalDragged) {
                    if (Math.abs(ev.getX() - initialX) > touchSlop
                            || Math.abs(ev.getY() - initialY) > touchSlop) {
                        isBeingDragged = true;
                        if (Math.abs(ev.getX() - initialX) > Math.abs(ev.getY() - initialY)) {
                            isHorizontalDragged = true;
                        }
                    } else {
                        initialX = ev.getX();
                        initialY = ev.getY();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isBeingDragged = false;
                isHorizontalDragged = false;
                break;
        }

        if (isBeingDragged) {
            return result && !isHorizontalDragged;
        } else {
            return result;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = super.onTouchEvent(ev);
        if (isBeingDragged) {
            return result && !isHorizontalDragged;
        } else {
            return result;
        }
    }
}
