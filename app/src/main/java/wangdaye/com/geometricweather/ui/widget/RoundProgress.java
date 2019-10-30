package wangdaye.com.geometricweather.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Round progress.
 * */

public class RoundProgress extends View {

    private Paint progressPaint;

    private RectF backgroundRectF = new RectF();
    private RectF progressRectF = new RectF();

    private float progress;
    private float max;
    @ColorInt private int progressColor;
    @ColorInt private int backgroundColor;

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
        progress = 0;
        max = 100;
        progressColor = ContextCompat.getColor(getContext(), R.color.colorAccent);
        backgroundColor = ContextCompat.getColor(getContext(), R.color.colorLine);
    }

    private void initPaint() {
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.FILL);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        if (this.progress > getMax()) {
            this.progress = getMax();
        }
        invalidate();
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        if (max > 0) {
            this.max = max;
            invalidate();
        }
    }

    public void setProgressColor(@ColorInt int progressColor) {
        this.progressColor = progressColor;
        this.invalidate();
    }

    public void setProgressBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int padding = (int) DisplayUtils.dpToPx(getContext(), 2);
        backgroundRectF.set(
                padding,
                padding,
                getMeasuredWidth() - padding,
                getMeasuredHeight() - padding
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float radius = backgroundRectF.height() / 2f;
        progressPaint.setColor(backgroundColor);
        canvas.drawRoundRect(backgroundRectF, radius, radius, progressPaint);

        progressRectF.set(
                backgroundRectF.left,
                backgroundRectF.top,
                backgroundRectF.left + backgroundRectF.width() * progress / max,
                backgroundRectF.bottom
        );
        progressPaint.setColor(progressColor);
        if (progressRectF.width() < 2 * radius) {
            canvas.drawCircle(
                    progressRectF.left + radius,
                    progressRectF.top + radius,
                    radius,
                    progressPaint
            );
        } else {
            canvas.drawRoundRect(progressRectF, radius, radius, progressPaint);
        }
    }
}
