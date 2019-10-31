package wangdaye.com.geometricweather.ui.widget.trend.item;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.image.AbstractIconTarget;
import wangdaye.com.geometricweather.ui.widget.trend.abs.ChartItemView;
import wangdaye.com.geometricweather.ui.widget.trend.abs.TrendChild;
import wangdaye.com.geometricweather.ui.widget.trend.abs.TrendParent;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Hourly trend item view.
 * */
public class HourlyTrendItemView extends ViewGroup
        implements TrendChild {

    private @Nullable ChartItemView chartItem;
    private TrendParent trendParent;
    private Paint paint;

    @Nullable private OnClickListener clickListener;

    @Nullable private String hourText;
    @Nullable private Drawable iconDrawable;

    @ColorInt private int contentColor;

    private float width;
    private float height;

    private float hourTextBaseLine;

    private float iconLeft;
    private float iconTop;

    private float trendViewTop;

    private int iconSize;

    private static final int ICON_SIZE_DIP = 32;
    private static final int TEXT_MARGIN_DIP = 4;
    private static final int ICON_MARGIN_DIP = 8;
    private static final int MARGIN_BOTTOM_DIP = 16;
    private static final int MIN_ITEM_WIDTH = 56;
    private static final int MIN_ITEM_HEIGHT = 128;

    public HourlyTrendItemView(Context context) {
        super(context);
        this.initialize();
    }

    public HourlyTrendItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public HourlyTrendItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public HourlyTrendItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize();
    }

    private void initialize() {
        setWillNotDraw(false);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.content_text_size));
        paint.setTextAlign(Paint.Align.CENTER);

        setTextColor(Color.BLACK);

        width = 0;
        height = 0;
        iconSize = (int) DisplayUtils.dpToPx(getContext(), ICON_SIZE_DIP);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = Math.max(DisplayUtils.dpToPx(getContext(), MIN_ITEM_WIDTH), width);
        height = Math.max(DisplayUtils.dpToPx(getContext(), MIN_ITEM_HEIGHT), height);

        float y = 0;
        float textMargin = DisplayUtils.dpToPx(getContext(), TEXT_MARGIN_DIP);
        float iconMargin = DisplayUtils.dpToPx(getContext(), ICON_MARGIN_DIP);

        Paint.FontMetrics fontMetrics = paint.getFontMetrics();

        // week text.
        y += textMargin;
        hourTextBaseLine = y - fontMetrics.top;
        y += fontMetrics.bottom - fontMetrics.top;
        y += textMargin;

        // day icon.
        if (iconDrawable != null) {
            y += iconMargin;
            iconLeft = (width - iconSize) / 2f;
            iconTop = y;
            y += iconSize;
            y += iconMargin;
        }

        // margin bottom.
        float marginBottom = DisplayUtils.dpToPx(getContext(), MARGIN_BOTTOM_DIP);

        // chartItem item view.
        if (chartItem != null) {
            chartItem.measure(
                    MeasureSpec.makeMeasureSpec(
                            (int) width,
                            MeasureSpec.EXACTLY
                    ), MeasureSpec.makeMeasureSpec(
                            (int) (height - marginBottom - y),
                            MeasureSpec.EXACTLY
                    )
            );
        }
        trendViewTop = y;

        setMeasuredDimension((int) width, (int) height);
        if (trendParent != null) {
            trendParent.setDrawingBoundary(
                    (int) (trendViewTop + chartItem.getMarginTop()),
                    (int) (trendViewTop + chartItem.getMeasuredHeight() - chartItem.getMarginBottom())
            );
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (chartItem != null) {
            chartItem.layout(
                    0,
                    (int) trendViewTop,
                    chartItem.getMeasuredWidth(),
                    (int) trendViewTop + chartItem.getMeasuredHeight()
            );
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // week text.
        if (hourText != null) {
            paint.setColor(contentColor);
            canvas.drawText(hourText, getMeasuredWidth() / 2f, hourTextBaseLine, paint);
        }

        // day icon.
        if (iconDrawable != null) {
            int restoreCount = canvas.save();
            canvas.translate(iconLeft, iconTop);
            iconDrawable.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (clickListener != null) {
                    clickListener.onClick(this);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setHourText(String hourText) {
        this.hourText = hourText;
        invalidate();
    }

    public void setTextColor(@ColorInt int contentColor) {
        this.contentColor = contentColor;
        invalidate();
    }

    public void setIconDrawable(@Nullable Drawable d) {
        boolean nullDrawable = iconDrawable == null;

        iconDrawable = d;
        if (d != null) {
            d.setVisible(true, true);
            d.setCallback(this);
            d.setBounds(0, 0, iconSize, iconSize);
        }

        if (nullDrawable != (d == null)) {
            requestLayout();
        } else {
            invalidate();
        }
    }

    public AbstractIconTarget getIconTarget() {
        return new AbstractIconTarget(iconSize) {
            @Override
            public View getTarget() {
                return HourlyTrendItemView.this;
            }

            @Override
            public void setDrawableForTarget(Drawable d) {
                setIconDrawable(d);
            }

            @Override
            public Drawable getDrawableFromTarget() {
                return iconDrawable;
            }

            @Override
            public void setTagForTarget(Object tag) {
                setTag(tag);
            }

            @Override
            public Object getTagFromTarget() {
                return getTag();
            }
        };
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        clickListener = l;
        super.setOnClickListener(v -> {});
    }

    @Override
    public void setParent(@NonNull TrendParent parent) {
        trendParent = parent;
    }

    @Override
    public void setChartItemView(ChartItemView t) {
        chartItem = t;
        removeAllViews();
        addView(chartItem);
        requestLayout();
    }

    @Override
    public ChartItemView getChartItemView() {
        return chartItem;
    }

    @Override
    public void setWidth(float width) {
        this.width = width;
    }

    @Override
    public void setHeight(float height) {
        this.height = height;
    }
}

