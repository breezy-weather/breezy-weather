package wangdaye.com.geometricweather.ui.widget.verticalScrollView;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Swipe switch layout.
 * */

public class SwipeSwitchLayout extends CoordinatorLayout {

    private View target;

    private OnSwitchListener switchListener;
    private OnPagerSwipeListener pageSwipeListener;

    private int totalCount = 1;
    private int position = 0;

    private int swipeDistance;
    private int swipeTrigger;

    private float initialX, initialY;
    private int touchSlop;

    private boolean isBeingDragged;
    private boolean isHorizontalDragged;
    private boolean isBeingNestedScrolling;

    private static final float SWIPE_RADIO = 0.4f;

    public static final int SWIPE_DIRECTION_LEFT = -1;
    public static final int SWIPE_DIRECTION_RIGHT = 1;

    private Animation resetAnimation = new Animation() {

        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            swipeDistance *= (1 - interpolatedTime);
            setTranslation();
            notifySwipeListenerScrolled();
        }
    };

    private Animation.AnimationListener animListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            setEnabled(false);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            setEnabled(true);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // do nothing.
        }
    };

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
        this.swipeDistance = 0;
        this.swipeTrigger = (int) (getContext().getResources().getDisplayMetrics().widthPixels / 3.0);
        this.touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    // touch.

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return switchListener != null && super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()
                || (ev.getAction() != MotionEvent.ACTION_DOWN && isBeingNestedScrolling)) {
            return false;
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isBeingDragged = false;
                isHorizontalDragged = false;
                initialX = ev.getX();
                initialY = ev.getY();
                swipeDistance = 0;
                break;

            case MotionEvent.ACTION_MOVE:
                float x = ev.getX();
                float y = ev.getY();

                if (!isBeingDragged && !isHorizontalDragged) {
                    if (Math.abs(x - initialX) > touchSlop || Math.abs(y - initialY) > touchSlop) {
                        isBeingDragged = true;
                        if (Math.abs(x - initialX) > Math.abs(y - initialY)) {
                            initialX += x > initialX ? touchSlop : -touchSlop;
                            isHorizontalDragged = true;
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
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
                isBeingDragged = false;
                isHorizontalDragged = false;
                initialX = ev.getX();
                initialY = ev.getY();
                swipeDistance = 0;
                break;

            case MotionEvent.ACTION_MOVE:
                if (isBeingDragged && isHorizontalDragged) {
                    swipeDistance = (int) (ev.getX() - initialX);
                    setTranslation();
                    notifySwipeListenerScrolled();
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                release();
                break;
        }

        return true;
    }

    // control.

    public void reset() {
        isBeingDragged = false;
        isHorizontalDragged = false;
        swipeDistance = 0;
        setTranslation();
    }

    private void getTarget() {
        if (target == null) {
            for (int i = 0; i <getChildCount(); i ++) {
                if (getChildAt(i) instanceof SwipeRefreshLayout) {
                    target = getChildAt(i);
                    return;
                }
            }
        }
    }

    private void setTranslation() {
        this.getTarget();

        float realDistance = swipeDistance;
        realDistance = Math.min(realDistance, swipeTrigger);
        realDistance = Math.max(realDistance, -swipeTrigger);

        int swipeDirection = swipeDistance < 0 ? SWIPE_DIRECTION_LEFT : SWIPE_DIRECTION_RIGHT;
        float progress = (float) (1.0 * Math.abs(realDistance) / swipeTrigger);

        target.setAlpha(1 - progress);
        target.setTranslationX(
                (float) (
                        swipeDirection * SWIPE_RADIO * swipeTrigger * Math.log10(
                                1 + 9.0 * Math.abs(swipeDistance) / swipeTrigger
                        )
                )
        );

        switchListener.onSwipeProgressChanged(swipeDirection, progress);
    }

    private void release() {
        int swipeDirection = swipeDistance < 0 ? SWIPE_DIRECTION_LEFT : SWIPE_DIRECTION_RIGHT;
        if (Math.abs(swipeDistance) > Math.abs(swipeTrigger)) {
            setPosition(swipeDirection);
            switchListener.onSwipeReleased(swipeDirection, true);
            if (pageSwipeListener != null) {
                pageSwipeListener.onPageSelected(position);
            }
        } else {
            switchListener.onSwipeReleased(swipeDirection, false);
            resetAnimation.reset();
            resetAnimation.setDuration(300);
            resetAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            resetAnimation.setAnimationListener(animListener);
            startAnimation(resetAnimation);
        }
    }

    private void notifySwipeListenerScrolled() {
        if (pageSwipeListener != null) {
            if (swipeDistance > 0) {
                pageSwipeListener.onPageScrolled(
                        position - 1,
                        (float) (1 - Math.min(1, 1.0 * swipeDistance / swipeTrigger)),
                        Math.max(0, swipeTrigger - swipeDistance)
                );
            } else {
                pageSwipeListener.onPageScrolled(
                        position,
                        (float) Math.min(1, -1.0 * swipeDistance / swipeTrigger),
                        Math.min(-swipeDistance, swipeTrigger)
                );
            }
        }
    }

    // interface.

    public void setData(int currentIndex, int pageCount) {
        if (currentIndex < 0) {
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
        swipeDistance = 0;
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        isBeingNestedScrolling = false;
        release();
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
        setTranslation();
    }
}
