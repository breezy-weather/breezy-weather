package wangdaye.com.geometricweather.ui.widget.weatherView.circularSkyView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Weather icon control view.
 * */

public class WeatherIconControlView extends FrameLayout {

    private OnWeatherIconChangingListener iconListener;
    private AnimListener animListener;

    private boolean rose = false;

    private float currentAngle;
    private int iconX;
    private int iconY;
    private int iconSize;
    private int radius;

    private class AnimRise extends Animation {

        //     90
        // 180      0
        //------------
        private float angleFrom;
        private float angleTo;

        AnimRise() {
            angleFrom = 180;
            angleTo = 90;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            ensureIconOffset(angleFrom, angleTo, interpolatedTime);

            getChildAt(2).setTranslationX(iconX + iconSize);
            getChildAt(2).setTranslationY(iconY + iconSize);
        }
    }

    private class AnimFall extends Animation {

        //     90
        // 180      0
        //------------
        private float angleFrom;
        private float angleTo;

        AnimFall() {
            angleFrom = currentAngle;
            angleTo = 0;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            ensureIconOffset(angleFrom, angleTo, interpolatedTime);

            getChildAt(2).setTranslationX(iconX + iconSize);
            getChildAt(2).setTranslationY(iconY + iconSize);
        }
    }

    private class AnimListener implements Animation.AnimationListener {

        private boolean canceled;
        private int type;

        static final int END_TYPE = 0;
        static final int CONTINUE_TYPE = 1;

        AnimListener(int type) {
            this.canceled = false;
            this.type = type;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            // do nothing.
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!canceled) {
                switch (type) {
                    case END_TYPE:
                        break;

                    case CONTINUE_TYPE:
                        animRise();
                        break;
                }
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // do nothing.
        }
    }

    public WeatherIconControlView(Context context) {
        super(context);
    }

    public WeatherIconControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WeatherIconControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // draw.

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = DisplayUtils.getTabletListAdaptiveWidth(getContext(), getMeasuredWidth());

        if (DisplayUtils.isTabletDevice(getContext())) {
            iconSize = (int) (width / Constants.UNIT_RADIUS_RATIO * 1.3);
        } else {
            iconSize = (int) (width / Constants.UNIT_RADIUS_RATIO * 1.8);
        }

        currentAngle = 0;
        iconX = -iconSize;
        iconY = -iconSize;
        radius = (int) (width / Constants.UNIT_RADIUS_RATIO * 4);

        // animatable icon.
        getChildAt(2).measure(
                MeasureSpec.makeMeasureSpec(iconSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(iconSize, MeasureSpec.EXACTLY)
        );
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        View child;

        child = getChildAt(0);
        child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());

        child = getChildAt(1);
        child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());

        child = getChildAt(2);
        child.layout(-iconSize, -iconSize, 0, 0);
    }

    // control.

    public void showWeatherIcon() {
        if (rose) {
            animFall();
        } else {
            rose = true;
            animRise();
        }
    }

    private void ensureIconOffset(float angleFrom, float angleTo, float time) {
        currentAngle = angleFrom + (angleTo - angleFrom) * time;
        double radians = Math.toRadians(currentAngle);
        iconX = (int) (getMeasuredWidth() / 2 + radius * Math.cos(radians) - iconSize / 2);
        iconY = (int) (getMeasuredHeight() - radius * Math.sin(radians) - iconSize / 2);
    }

    private void animRise() {
        if (iconListener != null) {
            iconListener.OnWeatherIconChanging();
        }

        if (animListener != null) {
            animListener.canceled = true;
        }
        animListener =  new AnimListener(AnimListener.END_TYPE);

        AnimRise animation = new AnimRise();
        animation.setDuration(800);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setAnimationListener(animListener);

        clearAnimation();
        startAnimation(animation);
    }

    private void animFall() {
        if (animListener != null) {
            animListener.canceled = true;
        }
        animListener =  new AnimListener(AnimListener.CONTINUE_TYPE);

        AnimFall animation = new AnimFall();
        animation.setDuration(400);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setAnimationListener(animListener);

        clearAnimation();
        startAnimation(animation);
    }

    // interface.

    interface OnWeatherIconChangingListener {
        void OnWeatherIconChanging();
    }

    public void setOnWeatherIconChangingListener(OnWeatherIconChangingListener l) {
        iconListener = l;
    }
}
