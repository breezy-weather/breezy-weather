package wangdaye.com.geometricweather.common.ui.widgets.weatherView.circularSkyView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

/**
 * Circle view.
 * */

public class CircleView extends View {

    private Paint mPaint;

    private final float[] mInitRadius = new float[4];
    private final float[] mRealRadius = new float[4];
    private int[] mColors;
    private int mPaintAlpha = 255;
    private float mCX, mCY;
    private boolean mDayTime;
    private boolean mAnimating = false;

    private static final int SHOW_ANIM_DURATION = 400;
    private static final int HIDE_ANIM_DURATION = 400;
    private static final int TOUCH_ANIM_DURATION = 1500;

    private final Animation mAnimShow = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            mPaintAlpha = (int) (255 * interpolatedTime);
            calcRadiusWhenShowing(interpolatedTime);
            invalidate();
        }
    };

    private final Animation mAnimHide = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            mPaintAlpha = (int) (255 * (1 - interpolatedTime));
            calcRadiusWhenHiding(interpolatedTime);
            invalidate();
        }
    };

    private final Animation mAnimTouch = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            mPaintAlpha = 255;
            calcRadiusWhenTouching(interpolatedTime);
            invalidate();
        }
    };

    public CircleView(Context context) {
        super(context);
        initialize();
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    // init.

    private void initialize() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        try {
            mDayTime = ((CircularSkyWeatherView) getParent().getParent()).isDaytime();
        } catch (Exception e) {
            mDayTime = true;
        }
        setColor();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            mDayTime = ((CircularSkyWeatherView) getParent().getParent()).isDaytime();
        } catch (Exception e) {
            mDayTime = true;
        }
        setColor();
    }

    // draw.

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float unitRadius = DisplayUtils.getTabletListAdaptiveWidth(getContext(), getMeasuredWidth())
                / Constants.UNIT_RADIUS_RATIO;
        for (int i = 0; i < 4; i ++) {
            mInitRadius[i] = unitRadius * (i + 1);
            mRealRadius[i] = mInitRadius[i];
        }
        mCX = getMeasuredWidth() / 2f;
        mCY = getMeasuredHeight();
    }

    // control.

    /**
     * @return Return true whether execute switch animation.
     * */
    public boolean showCircle(boolean dayTime) {
        if (mDayTime != dayTime) {
            doHide(dayTime);
            return true;
        }
        return false;
    }

    public void touchCircle() {
        if (!mAnimating) {
            doTouch();
        }
    }

    private void doShow(boolean dayTime) {
        mDayTime = dayTime;
        setColor();

        mAnimShow.setDuration(SHOW_ANIM_DURATION);
        mAnimShow.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimShow.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing.
            }
        });

        clearAnimation();
        startAnimation(mAnimShow);
    }

    private void doHide(final boolean dayTime) {
        mAnimHide.setDuration(HIDE_ANIM_DURATION);
        mAnimHide.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimHide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAnimating = false;
                doShow(dayTime);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing.
            }
        });

        clearAnimation();
        startAnimation(mAnimHide);
    }

    private void doTouch() {
        mAnimTouch.setDuration(TOUCH_ANIM_DURATION);
        mAnimTouch.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimTouch.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing.
            }
        });

        clearAnimation();
        startAnimation(mAnimTouch);
    }

    private void setColor() {
        if (mDayTime) {
            mColors = new int[] {
                    ContextCompat.getColor(getContext(), R.color.lightPrimary_1),
                    ContextCompat.getColor(getContext(), R.color.lightPrimary_2),
                    ContextCompat.getColor(getContext(), R.color.lightPrimary_3),
                    ContextCompat.getColor(getContext(), R.color.lightPrimary_4),
                    ContextCompat.getColor(getContext(), R.color.lightPrimary_5)};
        } else {
            mColors = new int[] {
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
        mPaint.setColor(mColors[4]);
        mPaint.setAlpha(mPaintAlpha);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
    }

    private void drawFourthFloor(Canvas canvas) {
        mPaint.setColor(mColors[3]);
        mPaint.setAlpha(mPaintAlpha);
        canvas.drawCircle(mCX, mCY, mRealRadius[3], mPaint);
    }

    private void drawThirdFloor(Canvas canvas) {
        mPaint.setColor(mColors[2]);
        mPaint.setAlpha(mPaintAlpha);
        canvas.drawCircle(mCX, mCY, mRealRadius[2], mPaint);
    }

    private void drawSecondFloor(Canvas canvas) {
        mPaint.setColor(mColors[1]);
        mPaint.setAlpha(mPaintAlpha);
        canvas.drawCircle(mCX, mCY, mRealRadius[1], mPaint);
    }

    private void drawFirstFloor(Canvas canvas) {
        mPaint.setColor(mColors[0]);
        mPaint.setAlpha(mPaintAlpha);
        canvas.drawCircle(mCX, mCY, mRealRadius[0], mPaint);
    }

    private void calcRadiusWhenShowing(float animTime) {
        mRealRadius[0] = (float) (mInitRadius[0] * (0.5 * animTime + 0.5));
        mRealRadius[1] = (float) (mInitRadius[1] * (0.5 * animTime + 0.5));
        mRealRadius[2] = (float) (mInitRadius[2] * (0.5 * animTime + 0.5));
        mRealRadius[3] = (float) (mInitRadius[3] * (0.5 * animTime + 0.5));
    }

    private void calcRadiusWhenHiding(float animTime) {
        mRealRadius[0] = (float) (mInitRadius[0] * (1 - 0.5 * animTime));
        mRealRadius[1] = (float) (mInitRadius[1] * (1 - 0.5 * animTime));
        mRealRadius[2] = (float) (mInitRadius[2] * (1 - 0.5 * animTime));
        mRealRadius[3] = (float) (mInitRadius[3] * (1 - 0.5 * animTime));
    }

    private void calcRadiusWhenTouching(float animTime) {
        float partTime = (float) (1.0 / 6.0);
        if (mDayTime) {
            if (animTime < partTime) {
                mRealRadius[0] = (float) (mInitRadius[0] * (1 + 0.15 * (animTime / partTime)));
                mRealRadius[1] = mInitRadius[1];
                mRealRadius[2] = mInitRadius[2];
                mRealRadius[3] = mInitRadius[3];
            } else if (animTime < partTime * 2) {
                mRealRadius[0] = (float) (mInitRadius[0] * (1.15 - 0.25 * ((animTime - partTime) / partTime)));
                mRealRadius[1] = (float) (mInitRadius[0] * (2 + 0.2 * ((animTime - partTime) / partTime)));
                mRealRadius[2] = mInitRadius[2];
                mRealRadius[3] = mInitRadius[3];
            } else if (animTime < partTime * 3) {
                mRealRadius[0] = (float) (mInitRadius[0] * (0.9 + 0.1 * ((animTime - 2 * partTime) / partTime)));
                mRealRadius[1] = (float) (mInitRadius[0] * (2.2 - 0.3 * ((animTime - 2 * partTime) / partTime)));
                mRealRadius[2] = (float) (mInitRadius[0] * (3 + 0.25 * ((animTime - 2 * partTime) / partTime)));
                mRealRadius[3] = mInitRadius[3];
            } else if (animTime < partTime * 4) {
                mRealRadius[0] = mInitRadius[0];
                mRealRadius[1] = (float) (mInitRadius[0] * (1.9 + 0.1 * ((animTime - 3 * partTime) / partTime)));
                mRealRadius[2] = (float) (mInitRadius[0] * (3.25 - 0.35 * ((animTime - 3 * partTime) / partTime)));
                mRealRadius[3] = (float) (mInitRadius[0] * (4 + 0.3 * ((animTime - 3 * partTime) / partTime)));
            } else if (animTime < partTime * 5) {
                mRealRadius[0] = mInitRadius[0];
                mRealRadius[1] = mInitRadius[1];
                mRealRadius[2] = (float) (mInitRadius[0] * (2.9 + 0.1 * ((animTime - 4 * partTime) / partTime)));
                mRealRadius[3] = (float) (mInitRadius[0] * (4.3 - 0.4 * ((animTime - 4 * partTime) / partTime)));
            } else {
                mRealRadius[0] = mInitRadius[0];
                mRealRadius[1] = mInitRadius[1];
                mRealRadius[2] = mInitRadius[2];
                mRealRadius[3] = (float) (mInitRadius[0] * (3.9 + 0.1 * ((animTime - 5 * partTime) / partTime)));
            }
        } else {
            if (animTime < partTime) {
                mRealRadius[0] = (float) (mInitRadius[0] * (1 + 0.15 * (animTime / partTime)));
                mRealRadius[1] = mInitRadius[1];
                mRealRadius[2] = mInitRadius[2];
                mRealRadius[3] = mInitRadius[3];
            } else if (animTime < partTime * 2) {
                mRealRadius[0] = (float) (mInitRadius[0] * (1.15 - 0.2 * ((animTime - partTime) / partTime)));
                mRealRadius[1] = (float) (mInitRadius[0] * (2 + 0.12 * ((animTime - partTime) / partTime)));
                mRealRadius[2] = mInitRadius[2];
                mRealRadius[3] = mInitRadius[3];
            } else if (animTime < partTime * 3) {
                mRealRadius[0] = (float) (mInitRadius[0] * (0.95 + 0.05 * ((animTime - 2 * partTime) / partTime)));
                mRealRadius[1] = (float) (mInitRadius[0] * (2.12 - 0.17 * ((animTime - 2 * partTime) / partTime)));
                mRealRadius[2] = (float) (mInitRadius[0] * (3 + 0.09 * ((animTime - 2 * partTime) / partTime)));
                mRealRadius[3] = mInitRadius[3];
            } else if (animTime < partTime * 4) {
                mRealRadius[0] = mInitRadius[0];
                mRealRadius[1] = (float) (mInitRadius[0] * (1.95 + 0.05 * ((animTime - 3 * partTime) / partTime)));
                mRealRadius[2] = (float) (mInitRadius[0] * (3.09 - 0.14 * ((animTime - 3 * partTime) / partTime)));
                mRealRadius[3] = (float) (mInitRadius[0] * (4 + 0.06 * ((animTime - 3 * partTime) / partTime)));
            } else if (animTime < partTime * 5) {
                mRealRadius[0] = mInitRadius[0];
                mRealRadius[1] = mInitRadius[1];
                mRealRadius[2] = (float) (mInitRadius[0] * (2.95 + 0.05 * ((animTime - 4 * partTime) / partTime)));
                mRealRadius[3] = (float) (mInitRadius[0] * (4.06 - 0.11 * ((animTime - 4 * partTime) / partTime)));
            } else {
                mRealRadius[0] = mInitRadius[0];
                mRealRadius[1] = mInitRadius[1];
                mRealRadius[2] = mInitRadius[2];
                mRealRadius[3] = (float) (mInitRadius[0] * (3.95 + 0.05 * ((animTime - 5 * partTime) / partTime)));
            }
        }
    }
}