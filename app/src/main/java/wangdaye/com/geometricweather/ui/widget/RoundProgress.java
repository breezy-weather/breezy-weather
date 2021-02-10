package wangdaye.com.geometricweather.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.View;

import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Round progress.
 * */

public class RoundProgress extends View {

    private Paint mProgressPaint;

    private final RectF mBackgroundRectF = new RectF();
    private final RectF mProgressRectF = new RectF();

    private float mProgress;
    private float mMax;
    @ColorInt private int mProgressColor;
    @ColorInt private int mBackgroundColor;

    public RoundProgress(Context context) {
        this(context, null);
    }

    public RoundProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
        initPaint();
    }

    private void initialize() {
        mProgress = 0;
        mMax = 100;
        mProgressColor = Color.BLACK;
        mBackgroundColor = Color.GRAY;
    }

    private void initPaint() {
        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStyle(Paint.Style.FILL);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public float getProgress() {
        return mProgress;
    }

    public void setProgress(float progress) {
        mProgress = progress;
        if (mProgress > getMax()) {
            mProgress = getMax();
        }
        invalidate();
    }

    public float getMax() {
        return mMax;
    }

    public void setMax(float max) {
        if (max > 0) {
            mMax = max;
            invalidate();
        }
    }

    public void setProgressColor(@ColorInt int progressColor) {
        mProgressColor = progressColor;
        invalidate();
    }

    public void setProgressBackgroundColor(@ColorInt int backgroundColor) {
        mBackgroundColor = backgroundColor;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int padding = (int) DisplayUtils.dpToPx(getContext(), 2);
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
        float radius = mBackgroundRectF.height() / 2f;
        mProgressPaint.setColor(mBackgroundColor);
        canvas.drawRoundRect(mBackgroundRectF, radius, radius, mProgressPaint);

        mProgressRectF.set(
                mBackgroundRectF.left,
                mBackgroundRectF.top,
                mBackgroundRectF.left + mBackgroundRectF.width() * mProgress / mMax,
                mBackgroundRectF.bottom
        );
        mProgressPaint.setColor(mProgressColor);
        if (mProgressRectF.width() < 2 * radius) {
            canvas.drawCircle(
                    mProgressRectF.left + radius,
                    mProgressRectF.top + radius,
                    radius,
                    mProgressPaint
            );
        } else {
            canvas.drawRoundRect(mProgressRectF, radius, radius, mProgressPaint);
        }
    }
}
