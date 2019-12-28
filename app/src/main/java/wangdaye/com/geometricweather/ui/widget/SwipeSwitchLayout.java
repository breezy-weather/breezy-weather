package wangdaye.com.geometricweather.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

/**
 * Swipe switch layout.
 * */

public class SwipeSwitchLayout extends FrameLayout
        implements NestedScrollingParent3 {

    @Nullable private View target;
    @Nullable private OnSwitchListener switchListener;
    @Nullable private OnPagerSwipeListener pageSwipeListener;

    private int totalCount = 1;
    private int position = 0;

    private int swipeDistance;
    private int swipeTrigger;
    private int nestedScrollingTrigger;

    private float lastX, lastY;
    private int touchSlop;

    private boolean isBeingTouched;
    private boolean isBeingDragged;
    private boolean isHorizontalDragged;
    private boolean isBeingNestedScrolling;

    private static final float SWIPE_RATIO = 0.4f;
    private static final float NESTED_SCROLLING_RATIO = 0.075f;

    public static final int SWIPE_DIRECTION_LEFT = -1;
    public static final int SWIPE_DIRECTION_RIGHT = 1;

    private class ResetAnimation extends Animation {

        private int triggerDistance;
        private float translateRatio;

        ResetAnimation(int triggerDistance, float translateRatio) {
            super();
            this.triggerDistance = triggerDistance;
            this.translateRatio = translateRatio;
        }

        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            swipeDistance *= (1 - interpolatedTime);
            setTranslation(triggerDistance, translateRatio);
            notifySwipeListenerScrolled(triggerDistance);
        }
    }

    public SwipeSwitchLayout(Context context) {
        super(context);
        this.initialize();
    }

    public SwipeSwitchLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public SwipeSwitchLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    // init.

    private void initialize() {
        this.target = null;
        this.swipeDistance = 0;
        this.swipeTrigger = getContext().getResources().getDisplayMetrics().widthPixels / 3;
        this.nestedScrollingTrigger = (int) (swipeTrigger * 2.25);
        this.touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    // touch.

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()
                || (ev.getAction() != MotionEvent.ACTION_DOWN && isBeingNestedScrolling)) {
            return false;
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                clearAnimation();

                isBeingTouched = true;
                isBeingDragged = false;
                isHorizontalDragged = false;
                lastX = ev.getX();
                lastY = ev.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                if (!isBeingTouched) {
                    isBeingTouched = true;
                    lastY= ev.getY();
                }

                float x = ev.getX();
                float y = ev.getY();

                if (!isBeingDragged && !isHorizontalDragged) {
                    if (Math.abs(x - lastX) > touchSlop || Math.abs(y - lastY) > touchSlop) {
                        isBeingDragged = true;
                        if (Math.abs(x - lastX) > Math.abs(y - lastY)) {
                            lastX += x > lastX ? touchSlop : -touchSlop;
                            isHorizontalDragged = true;
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isBeingTouched = false;
                isBeingDragged = false;
                isHorizontalDragged = false;
                break;
        }

        return isBeingDragged && isHorizontalDragged;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled() || isBeingNestedScrolling) {
            return false;
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                clearAnimation();

                isBeingTouched = true;
                isBeingDragged = false;
                isHorizontalDragged = false;
                lastX = ev.getX();
                lastY = ev.getY();
                swipeDistance = 0;
                break;

            case MotionEvent.ACTION_MOVE:
                if (isBeingDragged && isHorizontalDragged) {
                    swipeDistance += (int) (ev.getX() - lastX);
                    setTranslation(swipeTrigger, SWIPE_RATIO);
                    notifySwipeListenerScrolled(swipeTrigger);
                }

                lastX = ev.getX();
                lastY = ev.getY();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isBeingTouched = false;
                release(swipeTrigger, SWIPE_RATIO);
                break;
        }

        return true;
    }

    // control.

    public void reset() {
        isBeingDragged = false;
        isHorizontalDragged = false;
        swipeDistance = 0;
        setTranslation(swipeTrigger, SWIPE_RATIO);
    }

    private void setTranslation(int triggerDistance, float translateRatio) {
        float realDistance = swipeDistance;
        realDistance = Math.min(realDistance, triggerDistance);
        realDistance = Math.max(realDistance, -triggerDistance);

        int swipeDirection = swipeDistance < 0 ? SWIPE_DIRECTION_LEFT : SWIPE_DIRECTION_RIGHT;
        float progress = (float) (1.0 * Math.abs(realDistance) / triggerDistance);

        if (getChildCount() > 0) {
            target = getChildAt(0);
            target.setAlpha(1 - progress);
            target.setTranslationX(
                    (float) (
                            swipeDirection * translateRatio * triggerDistance * Math.log10(
                                    1 + 9.0 * Math.abs(swipeDistance) / triggerDistance
                            )
                    )
            );
        }

        if (switchListener != null) {
            switchListener.onSwipeProgressChanged(swipeDirection, progress);
        }
    }

    private void release(int triggerDistance, float translateRatio) {
        int swipeDirection = swipeDistance < 0 ? SWIPE_DIRECTION_LEFT : SWIPE_DIRECTION_RIGHT;
        if (Math.abs(swipeDistance) > Math.abs(triggerDistance)) {
            setPosition(swipeDirection);
            if (switchListener != null) {
                switchListener.onSwipeReleased(swipeDirection, true);
            }
            if (pageSwipeListener != null) {
                pageSwipeListener.onPageSelected(position);
            }
        } else {
            if (switchListener != null) {
                switchListener.onSwipeReleased(swipeDirection, false);
            }

            ResetAnimation resetAnimation = new ResetAnimation(triggerDistance, translateRatio);
            resetAnimation.setDuration(300);
            resetAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            startAnimation(resetAnimation);
        }
    }

    private void notifySwipeListenerScrolled(int triggerDistance) {
        if (pageSwipeListener != null) {
            if (swipeDistance > 0) {
                pageSwipeListener.onPageScrolled(
                        position - 1,
                        (float) (1 - Math.min(1, 1.0 * swipeDistance / triggerDistance)),
                        Math.max(0, triggerDistance - swipeDistance)
                );
            } else {
                pageSwipeListener.onPageScrolled(
                        position,
                        (float) Math.min(1, -1.0 * swipeDistance / triggerDistance),
                        Math.min(-swipeDistance, triggerDistance)
                );
            }
        }
    }

    // interface.

    public void setData(int currentIndex, int pageCount) {
        if (currentIndex < 0 || currentIndex >= pageCount) {
            throw new RuntimeException("Invalid current index.");
        }
        position = currentIndex;
        totalCount = pageCount;
    }

    private void setPosition(int swipeDirection) {
        switch (swipeDirection) {
            case SWIPE_DIRECTION_LEFT:
                position ++;
                break;

            case SWIPE_DIRECTION_RIGHT:
                position --;
                break;
        }
        if (position < 0) {
            position = totalCount - 1;
        } else if (position > totalCount - 1) {
            position = 0;
        }
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getPosition() {
        return position;
    }

    // interface.

    // on switch listener.

    public interface OnSwitchListener {
        void onSwipeProgressChanged(int swipeDirection, float progress);
        void onSwipeReleased(int swipeDirection, boolean doSwitch);
    }

    public void setOnSwitchListener(OnSwitchListener l) {
        switchListener = l;
    }

    // on swipe listener.

    public interface OnPagerSwipeListener {
        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);
        void onPageSelected(int position);
    }

    public void setOnPageSwipeListener(OnPagerSwipeListener l) {
        pageSwipeListener = l;
    }

    // nested scrolling parent.

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        return (axes & ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0
                && switchListener != null
                && type == ViewCompat.TYPE_TOUCH
                && isEnabled();
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        isBeingNestedScrolling = true;
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        isBeingNestedScrolling = false;
        release(nestedScrollingTrigger, NESTED_SCROLLING_RATIO);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (swipeDistance != 0) {
            if ((swipeDistance > 0 && swipeDistance - dx < 0)
                    || (swipeDistance < 0 && swipeDistance - dx > 0)) {
                consumed[0] = swipeDistance;
            } else {
                consumed[0] = dx;
            }
            onNestedScroll(target, 0, 0, consumed[0], dy, type);
        }
    }

    @Override
    public void onNestedScroll(@NonNull View target,
                               int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed,
                               int type, @NonNull int[] consumed) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        consumed[0] += dxUnconsumed;
    }

    @Override
    public void onNestedScroll(@NonNull View target,
                               int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed,
                               int type) {
        swipeDistance -= dxUnconsumed;
        setTranslation(nestedScrollingTrigger, NESTED_SCROLLING_RATIO);
    }
}
