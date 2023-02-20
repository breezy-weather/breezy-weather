package wangdaye.com.geometricweather.common.ui.widgets.astro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;

import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public class MoonPhaseView extends View {

    private Paint mPaint;
    private RectF mForegroundRectF = new RectF();
    private RectF mBackgroundRectF = new RectF();

    private float mSurfaceAngle; // head of light surface, clockwise.
    @ColorInt private int mLightColor;
    @ColorInt private int mDarkColor;
    @ColorInt private int mStrokeColor;

    private float LINE_WIDTH = 1;

    public MoonPhaseView(Context context) {
        this(context, null);
    }

    public MoonPhaseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoonPhaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
        initPaint();
    }

    private void initialize() {
        setColor(Color.WHITE, Color.BLACK, Color.GRAY);
        setSurfaceAngle(0); // from 0 -> phase : 🌑 (new)

        mForegroundRectF = new RectF();
        mBackgroundRectF = new RectF();

        LINE_WIDTH = DisplayUtils.dpToPx(getContext(), (int) LINE_WIDTH);
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setColor(@ColorInt int lightColor, @ColorInt int darkColor,
                         @ColorInt int strokeColor) {
        mLightColor = lightColor;
        mDarkColor = darkColor;
        mStrokeColor = strokeColor;
    }

    public void setSurfaceAngle(float surfaceAngle) {
        mSurfaceAngle = surfaceAngle;
        if (mSurfaceAngle >= 360) {
            mSurfaceAngle %= 360;
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        int padding = (int) DisplayUtils.dpToPx(getContext(), 4);
        mBackgroundRectF.set(
                padding,
                padding,
                getMeasuredWidth() - padding,
                getMeasuredHeight() - padding
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setStyle(Paint.Style.FILL);
        if (mSurfaceAngle == 0) { // 🌑
            drawDarkCircle(canvas);
        } else if (mSurfaceAngle < 90) { // 🌒
            drawLightCircle(canvas);

            mPaint.setColor(mDarkColor);
            canvas.drawArc(mBackgroundRectF, 90, 180, true, mPaint);
            float halfWidth = (float) (
                    mBackgroundRectF.width() / 2 * Math.cos(Math.toRadians(mSurfaceAngle))
            );
            mForegroundRectF.set(
                    mBackgroundRectF.centerX() - halfWidth,
                    mBackgroundRectF.top,
                    mBackgroundRectF.centerX() + halfWidth,
                    mBackgroundRectF.bottom
            );
            canvas.drawArc(mForegroundRectF, 270, 180, true, mPaint);
        } else if (mSurfaceAngle == 90) { // 🌓
            drawDarkCircle(canvas);
            mPaint.setColor(mLightColor);
            canvas.drawArc(mBackgroundRectF, 270, 180, true, mPaint);
        } else if (mSurfaceAngle < 180) { // 🌔
            drawDarkCircle(canvas);

            mPaint.setColor(mLightColor);
            canvas.drawArc(mBackgroundRectF, 270, 180, true, mPaint);
            float halfWidth = (float) (
                    mBackgroundRectF.width() / 2 * Math.sin(Math.toRadians(mSurfaceAngle - 90))
            );
            mForegroundRectF.set(
                    mBackgroundRectF.centerX() - halfWidth,
                    mBackgroundRectF.top,
                    mBackgroundRectF.centerX() + halfWidth,
                    mBackgroundRectF.bottom
            );
            canvas.drawArc(mForegroundRectF, 90, 180, true, mPaint);
        } else if (mSurfaceAngle == 180) { // 🌕
            drawLightCircle(canvas);
        } else if (mSurfaceAngle < 270) { // 🌖
            drawDarkCircle(canvas);

            mPaint.setColor(mLightColor);
            canvas.drawArc(mBackgroundRectF, 90, 180, true, mPaint);
            float halfWidth = (float) (
                    mBackgroundRectF.width() / 2 * Math.cos(Math.toRadians(mSurfaceAngle - 180))
            );
            mForegroundRectF.set(
                    mBackgroundRectF.centerX() - halfWidth,
                    mBackgroundRectF.top,
                    mBackgroundRectF.centerX() + halfWidth,
                    mBackgroundRectF.bottom
            );
            canvas.drawArc(mForegroundRectF, 270, 180, true, mPaint);
        } else if (mSurfaceAngle == 270) { // 🌗
            drawDarkCircle(canvas);
            mPaint.setColor(mLightColor);
            canvas.drawArc(mBackgroundRectF, 90, 180, true, mPaint);
        } else { // surface angle < 360. 🌘
            drawLightCircle(canvas);

            mPaint.setColor(mDarkColor);
            canvas.drawArc(mBackgroundRectF, 270, 180, true, mPaint);
            float halfWidth = (float) (
                    mBackgroundRectF.width() / 2 * Math.cos(Math.toRadians(360 - mSurfaceAngle))
            );
            mForegroundRectF.set(
                    mBackgroundRectF.centerX() - halfWidth,
                    mBackgroundRectF.top,
                    mBackgroundRectF.centerX() + halfWidth,
                    mBackgroundRectF.bottom
            );
            canvas.drawArc(mForegroundRectF, 90, 180, true, mPaint);
        }

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(LINE_WIDTH);
        if (mSurfaceAngle < 90 || 270 < mSurfaceAngle) {
            mPaint.setColor(mDarkColor);
            canvas.drawLine(
                    mBackgroundRectF.centerX(), mBackgroundRectF.top,
                    mBackgroundRectF.centerX(), mBackgroundRectF.bottom,
                    mPaint
            );
        } else if (90 < mSurfaceAngle && mSurfaceAngle < 270) {
            mPaint.setColor(mLightColor);
            canvas.drawLine(
                    mBackgroundRectF.centerX(), mBackgroundRectF.top,
                    mBackgroundRectF.centerX(), mBackgroundRectF.bottom,
                    mPaint
            );
        }
        mPaint.setColor(mStrokeColor);
        canvas.drawCircle(
                mBackgroundRectF.centerX(),
                mBackgroundRectF.centerY(),
                mBackgroundRectF.width() / 2,
                mPaint
        );
    }

    private void drawLightCircle(Canvas canvas) {
        mPaint.setColor(mLightColor);
        canvas.drawCircle(
                mBackgroundRectF.centerX(),
                mBackgroundRectF.centerY(),
                mBackgroundRectF.width() / 2,
                mPaint
        );
    }

    private void drawDarkCircle(Canvas canvas) {
        mPaint.setColor(mDarkColor);
        canvas.drawCircle(
                mBackgroundRectF.centerX(),
                mBackgroundRectF.centerY(),
                mBackgroundRectF.width() / 2,
                mPaint
        );
    }
}
