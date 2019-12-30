package wangdaye.com.geometricweather.ui.widget.weatherView.circularSkyView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

/**
 * Circle view.
 * */

public class CircleView extends View {

    private Paint paint;

    private float[] initRadius = new float[4];
    private float[] realRadius = new float[4];
    private int[] colors;
    private int paintAlpha = 255;
    private float cX, cY;
    private boolean dayTime;
    private boolean animating = false;

    private static final int SHOW_ANIM_DURATION = 400;
    private static final int HIDE_ANIM_DURATION = 400;
    private static final int TOUCH_ANIM_DURATION = 1500;

    private Animation animShow = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            paintAlpha = (int) (255 * interpolatedTime);
            calcRadiusWhenShowing(interpolatedTime);
            invalidate();
        }
    };

    private Animation animHide = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            paintAlpha = (int) (255 * (1 - interpolatedTime));
            calcRadiusWhenHiding(interpolatedTime);
            invalidate();
        }
    };

    private Animation animTouch = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            paintAlpha = 255;
            calcRadiusWhenTouching(interpolatedTime);
            invalidate();
        }
    };

    public CircleView(Context context) {
        super(context);
        this.initialize();
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    // init.

    private void initialize() {
        this.paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        this.dayTime = TimeManager.getInstance(getContext()).isDayTime();
        setColor();
    }

    // draw.

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float unitRadius = DisplayUtils.getTabletListAdaptiveWidth(getContext(), getMeasuredWidth())
                / Constants.UNIT_RADIUS_RATIO;
        for (int i = 0; i < 4; i ++) {
            initRadius[i] = unitRadius * (i + 1);
            realRadius[i] = initRadius[i];
        }
        cX = getMeasuredWidth() / 2;
        cY = getMeasuredHeight();
    }

    // control.

    /**
     * @return Return true whether execute switch animation.
     * */
    public boolean showCircle(boolean dayTime) {
        if (this.dayTime != dayTime) {
            doHide(dayTime);
            return true;
        }
        return false;
    }

    public void touchCircle() {
        if (!animating) {
            doTouch();
        }
    }

    private void doShow(boolean dayTime) {
        this.dayTime = dayTime;
        setColor();

        animShow.setDuration(SHOW_ANIM_DURATION);
        animShow.setInterpolator(new AccelerateDecelerateInterpolator());
        animShow.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing.
            }
        });

        clearAnimation();
        startAnimation(animShow);
    }

    private void doHide(final boolean dayTime) {
        animHide.setDuration(HIDE_ANIM_DURATION);
        animHide.setInterpolator(new AccelerateDecelerateInterpolator());
        animHide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animating = false;
                doShow(dayTime);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing.
            }
        });

        clearAnimation();
        startAnimation(animHide);
    }

    private void doTouch() {
        animTouch.setDuration(TOUCH_ANIM_DURATION);
        animTouch.setInterpolator(new AccelerateDecelerateInterpolator());
        animTouch.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing.
            }
        });

        clearAnimation();
        startAnimation(animTouch);
    }

    private void setColor() {
        if (dayTime) {
            colors = new int[] {
                    ContextCompat.getColor(getContext(), R.color.lightPrimary_1),
                    ContextCompat.getColor(getContext(), R.color.lightPrimary_2),
                    ContextCompat.getColor(getContext(), R.color.lightPrimary_3),
                    ContextCompat.getColor(getContext(), R.color.lightPrimary_4),
                    ContextCompat.getColor(getContext(), R.color.lightPrimary_5)};
        } else {
            colors = new int[] {
                    ContextCompat.getColor(getContext(), R.color.darkPrimary_1),
                    ContextCompat.getColor(getContext(), R.color.darkPrimary_2),
                    ContextCompat.getColor(getContext(), R.color.darkPrimary_3),
                    ContextCompat.getColor(getContext(), R.color.darkPrimary_4),
                    ContextCompat.getColor(getContext(), R.color.darkPrimary_5)};
        }
    }

    // anim.

    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        drawFourthFloor(canvas);
        drawThirdFloor(canvas);
        drawSecondFloor(canvas);
        drawFirstFloor(canvas);
    }

    private void drawBackground(Canvas canvas) {
        paint.setColor(colors[4]);
        paint.setAlpha(paintAlpha);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint);
    }

    private void drawFourthFloor(Canvas canvas) {
        paint.setColor(colors[3]);
        paint.setAlpha(paintAlpha);
        canvas.drawCircle(cX, cY, realRadius[3], paint);
    }

    private void drawThirdFloor(Canvas canvas) {
        paint.setColor(colors[2]);
        paint.setAlpha(paintAlpha);
        canvas.drawCircle(cX, cY, realRadius[2], paint);
    }

    private void drawSecondFloor(Canvas canvas) {
        paint.setColor(colors[1]);
        paint.setAlpha(paintAlpha);
        canvas.drawCircle(cX, cY, realRadius[1], paint);
    }

    private void drawFirstFloor(Canvas canvas) {
        paint.setColor(colors[0]);
        paint.setAlpha(paintAlpha);
        canvas.drawCircle(cX, cY, realRadius[0], paint);
    }

    private void calcRadiusWhenShowing(float animTime) {
        realRadius[0] = (float) (initRadius[0] * (0.5 * animTime + 0.5));
        realRadius[1] = (float) (initRadius[1] * (0.5 * animTime + 0.5));
        realRadius[2] = (float) (initRadius[2] * (0.5 * animTime + 0.5));
        realRadius[3] = (float) (initRadius[3] * (0.5 * animTime + 0.5));
    }

    private void calcRadiusWhenHiding(float animTime) {
        realRadius[0] = (float) (initRadius[0] * (1 - 0.5 * animTime));
        realRadius[1] = (float) (initRadius[1] * (1 - 0.5 * animTime));
        realRadius[2] = (float) (initRadius[2] * (1 - 0.5 * animTime));
        realRadius[3] = (float) (initRadius[3] * (1 - 0.5 * animTime));
    }

    private void calcRadiusWhenTouching(float animTime) {
        float partTime = (float) (1.0 / 6.0);
        if (dayTime) {
            if (animTime < partTime) {
                realRadius[0] = (float) (initRadius[0] * (1 + 0.15 * (animTime / partTime)));
                realRadius[1] = initRadius[1];
                realRadius[2] = initRadius[2];
                realRadius[3] = initRadius[3];
            } else if (animTime < partTime * 2) {
                realRadius[0] = (float) (initRadius[0] * (1.15 - 0.25 * ((animTime - partTime) / partTime)));
                realRadius[1] = (float) (initRadius[0] * (2 + 0.2 * ((animTime - partTime) / partTime)));
                realRadius[2] = initRadius[2];
                realRadius[3] = initRadius[3];
            } else if (animTime < partTime * 3) {
                realRadius[0] = (float) (initRadius[0] * (0.9 + 0.1 * ((animTime - 2 * partTime) / partTime)));
                realRadius[1] = (float) (initRadius[0] * (2.2 - 0.3 * ((animTime - 2 * partTime) / partTime)));
                realRadius[2] = (float) (initRadius[0] * (3 + 0.25 * ((animTime - 2 * partTime) / partTime)));
                realRadius[3] = initRadius[3];
            } else if (animTime < partTime * 4) {
                realRadius[0] = initRadius[0];
                realRadius[1] = (float) (initRadius[0] * (1.9 + 0.1 * ((animTime - 3 * partTime) / partTime)));
                realRadius[2] = (float) (initRadius[0] * (3.25 - 0.35 * ((animTime - 3 * partTime) / partTime)));
                realRadius[3] = (float) (initRadius[0] * (4 + 0.3 * ((animTime - 3 * partTime) / partTime)));
            } else if (animTime < partTime * 5) {
                realRadius[0] = initRadius[0];
                realRadius[1] = initRadius[1];
                realRadius[2] = (float) (initRadius[0] * (2.9 + 0.1 * ((animTime - 4 * partTime) / partTime)));
                realRadius[3] = (float) (initRadius[0] * (4.3 - 0.4 * ((animTime - 4 * partTime) / partTime)));
            } else {
                realRadius[0] = initRadius[0];
                realRadius[1] = initRadius[1];
                realRadius[2] = initRadius[2];
                realRadius[3] = (float) (initRadius[0] * (3.9 + 0.1 * ((animTime - 5 * partTime) / partTime)));
            }
        } else {
            if (animTime < partTime) {
                realRadius[0] = (float) (initRadius[0] * (1 + 0.15 * (animTime / partTime)));
                realRadius[1] = initRadius[1];
                realRadius[2] = initRadius[2];
                realRadius[3] = initRadius[3];
            } else if (animTime < partTime * 2) {
                realRadius[0] = (float) (initRadius[0] * (1.15 - 0.2 * ((animTime - partTime) / partTime)));
                realRadius[1] = (float) (initRadius[0] * (2 + 0.12 * ((animTime - partTime) / partTime)));
                realRadius[2] = initRadius[2];
                realRadius[3] = initRadius[3];
            } else if (animTime < partTime * 3) {
                realRadius[0] = (float) (initRadius[0] * (0.95 + 0.05 * ((animTime - 2 * partTime) / partTime)));
                realRadius[1] = (float) (initRadius[0] * (2.12 - 0.17 * ((animTime - 2 * partTime) / partTime)));
                realRadius[2] = (float) (initRadius[0] * (3 + 0.09 * ((animTime - 2 * partTime) / partTime)));
                realRadius[3] = initRadius[3];
            } else if (animTime < partTime * 4) {
                realRadius[0] = initRadius[0];
                realRadius[1] = (float) (initRadius[0] * (1.95 + 0.05 * ((animTime - 3 * partTime) / partTime)));
                realRadius[2] = (float) (initRadius[0] * (3.09 - 0.14 * ((animTime - 3 * partTime) / partTime)));
                realRadius[3] = (float) (initRadius[0] * (4 + 0.06 * ((animTime - 3 * partTime) / partTime)));
            } else if (animTime < partTime * 5) {
                realRadius[0] = initRadius[0];
                realRadius[1] = initRadius[1];
                realRadius[2] = (float) (initRadius[0] * (2.95 + 0.05 * ((animTime - 4 * partTime) / partTime)));
                realRadius[3] = (float) (initRadius[0] * (4.06 - 0.11 * ((animTime - 4 * partTime) / partTime)));
            } else {
                realRadius[0] = initRadius[0];
                realRadius[1] = initRadius[1];
                realRadius[2] = initRadius[2];
                realRadius[3] = (float) (initRadius[0] * (3.95 + 0.05 * ((animTime - 5 * partTime) / partTime)));
            }
        }
    }
}