package wangdaye.com.geometricweather.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import java.util.List;

import wangdaye.com.geometricweather.basic.models.weather.Minutely;

public class PrecipitationBar extends View {

    @Nullable private List<Minutely> mMinutelyList;
    private final Paint mPaint;
    @ColorInt private int mPrecipitationColor;
    @ColorInt private int mBackgroundColor;

    public PrecipitationBar(Context context) {
        this(context, null);
    }

    public PrecipitationBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrecipitationBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setClipToOutline(true);
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(
                            0,
                            0,
                            view.getMeasuredWidth(),
                            view.getMeasuredHeight(),
                            view.getMeasuredHeight() / 2f
                    );
                }
            });
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mMinutelyList == null || mMinutelyList.size() == 0) {
            return;
        }

        float itemWidth = getMeasuredWidth() / mMinutelyList.size();
        float itemHeight = getMeasuredHeight();

        canvas.drawColor(mBackgroundColor);
        mPaint.setColor(mPrecipitationColor);
        if (getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            float x = getMeasuredWidth();
            for (Minutely m : mMinutelyList) {
                if (m.isPrecipitation()) {
                    x -= itemWidth;
                    canvas.drawRect(x, 0, x + itemWidth, itemHeight, mPaint);
                }
            }
        } else {
            float x = 0;
            for (Minutely m : mMinutelyList) {
                if (m.isPrecipitation()) {
                    canvas.drawRect(x, 0, x + itemWidth, itemHeight, mPaint);
                    x += itemWidth;
                }
            }
        }
    }

    public void setMinutelyList(@Nullable List<Minutely> minutelyList) {
        mMinutelyList = minutelyList;
        invalidate();
    }

    public void setPrecipitationColor(@ColorInt int precipitationColor) {
        mPrecipitationColor = precipitationColor;
        invalidate();
    }

    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        mBackgroundColor = backgroundColor;
        invalidate();
    }
}
