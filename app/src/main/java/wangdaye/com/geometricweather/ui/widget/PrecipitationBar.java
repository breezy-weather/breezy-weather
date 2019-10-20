package wangdaye.com.geometricweather.ui.widget;

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

import wangdaye.com.geometricweather.basic.model.weather.Minutely;

public class PrecipitationBar extends View {

    @Nullable private List<Minutely> minutelyList;
    private Paint paint;
    @ColorInt private int precipitationColor;
    @ColorInt private int backgroundColor;

    public PrecipitationBar(Context context) {
        this(context, null);
    }

    public PrecipitationBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrecipitationBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
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
        if (minutelyList == null || minutelyList.size() == 0) {
            return;
        }

        float itemWidth = getMeasuredWidth() / minutelyList.size();
        float itemHeight = getMeasuredHeight();

        canvas.drawColor(backgroundColor);
        paint.setColor(precipitationColor);
        if (getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            float x = getMeasuredWidth();
            for (Minutely m : minutelyList) {
                if (m.isPrecipitation()) {
                    x -= itemWidth;
                    canvas.drawRect(x, 0, x + itemWidth, itemHeight, paint);
                }
            }
        } else {
            float x = 0;
            for (Minutely m : minutelyList) {
                if (m.isPrecipitation()) {
                    canvas.drawRect(x, 0, x + itemWidth, itemHeight, paint);
                    x += itemWidth;
                }
            }
        }
    }

    public void setMinutelyList(@Nullable List<Minutely> minutelyList) {
        this.minutelyList = minutelyList;
        invalidate();
    }

    public void setPrecipitationColor(@ColorInt int precipitationColor) {
        this.precipitationColor = precipitationColor;
        invalidate();
    }

    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        invalidate();
    }
}
