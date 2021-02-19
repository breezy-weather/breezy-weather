package wangdaye.com.geometricweather.common.ui.widgets.insets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;

import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public class FitHorizontalSystemBarRootLayout extends FrameLayout {

    private final Paint mPaint;
    private @ColorInt int mRootColor;
    private @ColorInt int mLineColor;

    public FitHorizontalSystemBarRootLayout(Context context) {
        this(context, null);
    }

    public FitHorizontalSystemBarRootLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FitHorizontalSystemBarRootLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(DisplayUtils.dpToPx(context, 2));

        mRootColor = Color.TRANSPARENT;
        mLineColor = Color.GRAY;

        setWillNotDraw(false);
        setFitsSystemWindows(false);
        ViewCompat.setOnApplyWindowInsetsListener(this, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public void setOnApplyWindowInsetsListener(OnApplyWindowInsetsListener listener) {
        super.setOnApplyWindowInsetsListener((v, insets) -> {
            if (listener != null) {
                return listener.onApplyWindowInsets(v, insets);
            }

            Rect waterfull = Utils.getWaterfullInsets(insets);
            fitSystemBar(
                    new Rect(
                            insets.getSystemWindowInsetLeft() + waterfull.left,
                            insets.getSystemWindowInsetTop() + waterfull.top,
                            insets.getSystemWindowInsetRight() + waterfull.right,
                            insets.getSystemWindowInsetBottom() + waterfull.bottom
                    )
            );
            return insets;
        });
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
        fitSystemBar(insets);
        return false;
    }

    private void fitSystemBar(Rect insets) {
        setPadding(insets.left, 0, insets.right, 0);
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(mRootColor);

        mPaint.setColor(mLineColor);
        if (getPaddingLeft() > 0) {
            canvas.drawLine(getPaddingLeft(), 0, getPaddingLeft(), getMeasuredHeight(), mPaint);
        }
        if (getPaddingRight() > 0) {
            canvas.drawLine(getMeasuredWidth() - getPaddingRight(), 0,
                    getMeasuredWidth() - getPaddingRight(), getMeasuredHeight(), mPaint);
        }
    }

    public void setLineColor(@ColorInt int lineColor) {
        mLineColor = lineColor;
        invalidate();
    }

    public void setRootColor(@ColorInt int rootColor) {
        mRootColor = rootColor;
        invalidate();
    }
}
