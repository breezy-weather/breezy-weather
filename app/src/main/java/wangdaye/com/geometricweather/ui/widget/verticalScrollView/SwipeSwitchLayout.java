package wangdaye.com.geometricweather.ui.widget.verticalScrollView;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.util.List;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.ui.widget.InkPageIndicator;

/**
 * Swipe switch layout.
 * */

public class SwipeSwitchLayout extends CoordinatorLayout
        implements GestureDetector.OnGestureListener {

    private View target;
    private InkPageIndicator indicator;
    private GestureDetector gestureDetector;
    private OnSwitchListener switchListener;
    private OnSwipeListener swipeListener;

    private int totalCount = 1;
    private int position = 0;

    private float swipeDistance;
    private float swipeTrigger;

    private float initialX, initialY;
    private int touchSlop;

    private boolean isBeingDragged = false;
    private boolean isHorizontalDragged = false;

    public static final int DIRECTION_LEFT = -1;
    public static final int DIRECTION_RIGHT = 1;

    private Animation resetAnimation = new Animation() {

        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            swipeDistance *= (1 - interpolatedTime);
            setTranslation();
            notifySwipeListenerScrolled();
        }
    };

    private class LeftExitAnimation extends Animation {
        // data
        private float startX;
        private boolean notified;

        LeftExitAnimation(float distanceNow) {
            startX = distanceNow;
            notified = false;
        }

        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            if (!notified) {
                swipeDistance = startX + (-swipeTrigger - startX) * interpolatedTime;
                setTranslation();
                notifySwipeListenerScrolled();
                if (interpolatedTime == 1) {
                    notified = true;
                    setPosition(DIRECTION_LEFT);
                    switchListener.swipeTakeEffect(DIRECTION_LEFT);
                    if (swipeListener != null) {
                        swipeListener.onPageSelected(position);
                    }
                }
            }
        }
    }

    private class RightExitAnimation extends Animation {
        // data
        private float startX;
        private boolean notified;

        RightExitAnimation(float distanceNow) {
            startX = distanceNow;
            notified = false;
        }

        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            if (!notified) {
                swipeDistance = startX + (swipeTrigger - startX) * interpolatedTime;
                setTranslation();
                notifySwipeListenerScrolled();
                if (interpolatedTime == 1) {
                    notified = true;
                    setPosition(DIRECTION_RIGHT);
                    switchListener.swipeTakeEffect(DIRECTION_RIGHT);
                    if (swipeListener != null) {
                        swipeListener.onPageSelected(position);
                    }
                }
            }
        }
    }

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
        this.gestureDetector = new GestureDetector(getContext(), this);

        this.swipeDistance = 0;
        this.swipeTrigger = (int) (getContext().getResources().getDisplayMetrics().widthPixels / 2.0);
        this.touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    // touch.

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return switchListener != null && super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isBeingDragged = false;
                isHorizontalDragged = false;
                initialX = ev.getX();
                initialY = ev.getY();
                swipeDistance = 0;
                break;

            case MotionEvent.ACTION_MOVE:
                if (!isBeingDragged && !isHorizontalDragged) {
                    if (Math.abs(ev.getX() - initialX) > touchSlop
                            || Math.abs(ev.getY() - initialY) > touchSlop) {
                        isBeingDragged = true;
                        if (Math.abs(ev.getX() - initialX) > Math.abs(ev.getY() - initialY)) {
                            isHorizontalDragged = true;
                            if (indicator != null) {
                                indicator.setDisplayState(true);
                            }
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

        return isBeingDragged && isHorizontalDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (gestureDetector.onTouchEvent(ev)) {
            return true;
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
                    swipeDistance = ev.getX() - initialX;
                    setTranslation();
                    notifySwipeListenerScrolled();
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (indicator != null) {
                    indicator.setDisplayState(false);
                }
                if (swipeDistance != 0) {
                    if (Math.abs(swipeDistance) > Math.abs(swipeTrigger)) {
                        setPosition(ev.getX() < initialX ? DIRECTION_LEFT : DIRECTION_RIGHT);
                        switchListener.swipeTakeEffect(
                                ev.getX() < initialX ? DIRECTION_LEFT : DIRECTION_RIGHT);
                        if (swipeListener != null) {
                            swipeListener.onPageSelected(position);
                        }
                    } else {
                        resetAnimation.reset();
                        resetAnimation.setDuration(300);
                        resetAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                        resetAnimation.setAnimationListener(animListener);
                        startAnimation(resetAnimation);
                    }
                }
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
        if (realDistance > swipeTrigger) {
            realDistance = swipeTrigger;
        } else if (realDistance < -swipeTrigger) {
            realDistance = -swipeTrigger;
        }
        target.setTranslationX(realDistance);
        target.setAlpha(1 - Math.abs(realDistance) / swipeTrigger);
    }

    private void notifySwipeListenerScrolled() {
        if (swipeListener != null) {
            if (swipeDistance > 0) {
                swipeListener.onPageScrolled(
                        position - 1,
                        1 - Math.min(1, swipeDistance / swipeTrigger),
                        (int) Math.max(0, swipeTrigger - swipeDistance));
            } else {
                swipeListener.onPageScrolled(
                        position,
                        Math.min(1, -swipeDistance / swipeTrigger),
                        (int) Math.min(-swipeDistance, swipeTrigger));
            }
        }
    }

    // interface.

    public void setData(List<Location> locationList, Location locationNow) {
        this.totalCount = locationList.size();
        position = 0;
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationNow.equals(locationList.get(i))) {
                position = i;
                return;
            }
        }
    }

    private void setPosition(int dir) {
        switch (dir) {
            case DIRECTION_LEFT:
                position ++;
                break;

            case DIRECTION_RIGHT:
                position --;
                break;
        }
        if (position < 0) {
            position = totalCount - 1;
        } else if (position > totalCount - 1) {
            position = 0;
        }
    }

    public void setIndicator(InkPageIndicator indicator) {
        this.indicator = indicator;
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
        void swipeTakeEffect(int direction);
    }

    public void setOnSwitchListener(OnSwitchListener l) {
        this.switchListener = l;
    }

    // on swipe listener.

    public interface OnSwipeListener {

        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);
        void onPageSelected(int position);
    }

    public void setOnSwipeListener(OnSwipeListener l) {
        this.swipeListener = l;
    }

    // on gesture switchListener.

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        if (indicator != null) {
            indicator.setDisplayState(false);
        }
        if (Math.abs(swipeDistance) >= swipeTrigger) {
            setPosition(swipeDistance < 0 ? DIRECTION_LEFT : DIRECTION_RIGHT);
            switchListener.swipeTakeEffect(swipeDistance < 0 ? DIRECTION_LEFT : DIRECTION_RIGHT);
            if (swipeListener != null) {
                swipeListener.onPageSelected(position);
            }
        } else if (Math.abs(v) >= 2000 && Math.abs(swipeDistance) >= swipeTrigger / 3.0) {
            if (v > 0) {
                // to right.
                RightExitAnimation animation = new RightExitAnimation(swipeDistance);
                animation.setDuration((long) (1000.0 * (swipeTrigger - swipeDistance) / v));
                animation.setAnimationListener(animListener);
                clearAnimation();
                startAnimation(animation);
            } else {
                // to left.
                LeftExitAnimation animation = new LeftExitAnimation(swipeDistance);
                animation.setDuration((long) (1000.0 * (-swipeTrigger - swipeDistance) / v));
                animation.setAnimationListener(animListener);
                clearAnimation();
                startAnimation(animation);
            }
        } else {
            resetAnimation.reset();
            resetAnimation.setDuration(300);
            resetAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            resetAnimation.setAnimationListener(animListener);
            startAnimation(resetAnimation);
        }
        return true;
    }
}
