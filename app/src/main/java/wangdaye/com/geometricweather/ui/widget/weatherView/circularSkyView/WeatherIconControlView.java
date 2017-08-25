package wangdaye.com.geometricweather.ui.widget.weatherView.circularSkyView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Weather icon control view.
 * */

public class WeatherIconControlView extends FrameLayout {

    private OnWeatherIconChangingListener iconListener;
    private AnimListener animListener;

    private boolean rose = false;

    private int cX;
    private int iconSize;
    private int radius;

    private int width;
    private int height;

    private class AnimRise extends Animation {

        private int startX;
        private int endX;

        AnimRise() {
            startX = (int) (width / 2.0 - radius);
            endX = (int) (width / 2.0);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            calcCX(startX, endX, interpolatedTime);
            requestLayout();
        }
    }

    private class AnimFall extends Animation {

        private int startX;
        private int endX;

        AnimFall() {
            startX = cX;
            endX = (int) (width / 2.0 + radius);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            calcCX(startX, endX, interpolatedTime);
            requestLayout();
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
                        getChildAt(2).setVisibility(GONE);
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
        this.initialize();
    }

    public WeatherIconControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public WeatherIconControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    // init.

    private void initialize() {
        this.width = getResources().getDisplayMetrics().widthPixels;
        this.height = (int) (width / 6.8 * 5.0);
        if (DisplayUtils.isTabletDevice(getContext())) {
            iconSize = (int) (width / 6.8 * 1.3);
        } else {
            iconSize = (int) (width / 6.8 * 1.8);
        }
        cX = (int) (-iconSize * 0.5);
        radius = (int) (width / 6.8 * 4.0);
    }

    // draw.

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(
                getChildAt(0).getMeasuredWidth(),
                getChildAt(0).getMeasuredHeight());
        for (int i = 0; i < getChildCount(); i ++) {
            if (getChildAt(i).getId() == R.id.container_circular_sky_view_iconContainer) {
                getChildAt(i).measure(
                        MeasureSpec.makeMeasureSpec(iconSize, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(iconSize, MeasureSpec.EXACTLY));
                break;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        View child;

        child = getChildAt(0);
        child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());

        child = getChildAt(1);
        child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());

        child = getChildAt(2);
        child.layout(
                getIconLeft(),
                getIconTop(),
                getIconLeft() + iconSize,
                getIconTop() + iconSize);
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

    private void calcCX(int startX, int endX, float time) {
        cX = (int) (startX + (endX - startX) * time);
    }

    private int getIconLeft() {
        return (int) (cX - iconSize * 0.5);
    }

    private int getIconTop() {
        return (int) (getIconCY() - iconSize * 0.5);
    }

    private int getIconCY() {
        return (int) (height - Math.sqrt(Math.pow(radius, 2) - Math.pow(cX - width / 2.0, 2)));
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

        getChildAt(2).setVisibility(VISIBLE);
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
