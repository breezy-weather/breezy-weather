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

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.ui.widget.trend.chart.AbsChartItemView;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Daily trend item view.
 * */
public class DailyTrendItemView extends AbsTrendItemView {

    private @Nullable AbsChartItemView chartItem;
    private Paint paint;

    @Nullable private OnClickListener clickListener;

    @Nullable private String weekText;
    @Nullable private String dateText;

    @Nullable private Drawable dayIconDrawable;
    @Nullable private Drawable nightIconDrawable;

    @ColorInt private int contentColor;
    @ColorInt private int subTitleColor;

    private float weekTextBaseLine;

    private float dateTextBaseLine;

    private float dayIconLeft;
    private float dayIconTop;

    private float trendViewTop;

    private float nightIconLeft;
    private float nightIconTop;

    private int iconSize;

    private int chartTop;
    private int chartBottom;

    private static final int ICON_SIZE_DIP = 32;
    private static final int TEXT_MARGIN_DIP = 4;
    private static final int ICON_MARGIN_DIP = 8;

    public DailyTrendItemView(Context context) {
        super(context);
        this.initialize();
    }

    public DailyTrendItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public DailyTrendItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DailyTrendItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize();
    }

    private void initialize() {
        setWillNotDraw(false);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.content_text_size));
        paint.setTextAlign(Paint.Align.CENTER);

        setTextColor(Color.BLACK, Color.GRAY);

        iconSize = (int) DisplayUtils.dpToPx(getContext(), ICON_SIZE_DIP);

        chartTop = 0;
        chartBottom = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        float y = 0;
        float consumedHeight;
        float textMargin = DisplayUtils.dpToPx(getContext(), TEXT_MARGIN_DIP);
        float iconMargin = DisplayUtils.dpToPx(getContext(), ICON_MARGIN_DIP);

        Paint.FontMetrics fontMetrics = paint.getFontMetrics();

        // week text.
        y += textMargin;
        weekTextBaseLine = y - fontMetrics.top;
        y += fontMetrics.bottom - fontMetrics.top;
        y += textMargin;

        // date text.
        y += textMargin;
        dateTextBaseLine = y - fontMetrics.top;
        y += fontMetrics.bottom - fontMetrics.top;
        y += textMargin;

        // day icon.
        if (dayIconDrawable != null) {
            y += iconMargin;
            dayIconLeft = (width - iconSize) / 2f;
            dayIconTop = y;
            y += iconSize;
            y += iconMargin;
        }

        consumedHeight = y;

        // margin bottom.
        float marginBottom = DisplayUtils.dpToPx(
                getContext(), TrendRecyclerView.ITEM_MARGIN_BOTTOM_DIP);
        consumedHeight += marginBottom;

        // night icon.
        if (nightIconDrawable != null) {
            nightIconLeft = (width - iconSize) / 2f;
            nightIconTop = height - marginBottom - iconMargin - iconSize;
            consumedHeight += iconSize + 2 * iconMargin;
        }

        // chartItem item view.
        if (chartItem != null) {
            chartItem.measure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec((int) (height - consumedHeight), MeasureSpec.EXACTLY)
            );
        }
        trendViewTop = y;

        chartTop = (int) (trendViewTop + chartItem.getMarginTop());
        chartBottom = (int) (trendViewTop + chartItem.getMeasuredHeight() - chartItem.getMarginBottom());

        setMeasuredDimension(width, height);
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
        if (weekText != null) {
            paint.setColor(contentColor);
            canvas.drawText(weekText, getMeasuredWidth() / 2f, weekTextBaseLine, paint);
        }

        // date text.
        if (dateText != null) {
            paint.setColor(subTitleColor);
            canvas.drawText(dateText, getMeasuredWidth() / 2f, dateTextBaseLine, paint);
        }

        int restoreCount;

        // day icon.
        if (dayIconDrawable != null) {
            restoreCount = canvas.save();
            canvas.translate(dayIconLeft, dayIconTop);
            dayIconDrawable.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }

        // night icon.
        if (nightIconDrawable != null) {
            restoreCount = canvas.save();
            canvas.translate(nightIconLeft, nightIconTop);
            nightIconDrawable.draw(canvas);
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

    public void setWeekText(String weekText) {
        this.weekText = weekText;
        invalidate();
    }

    public void setDateText(String dateText) {
        this.dateText = dateText;
        invalidate();
    }

    public void setTextColor(@ColorInt int contentColor, @ColorInt int subTitleColor) {
        this.contentColor = contentColor;
        this.subTitleColor = subTitleColor;
        invalidate();
    }

    public void setDayIconDrawable(@Nullable Drawable d) {
        boolean nullDrawable = dayIconDrawable == null;

        dayIconDrawable = d;
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

    public void setNightIconDrawable(@Nullable Drawable d) {
        boolean nullDrawable = nightIconDrawable == null;

        nightIconDrawable = d;
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

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        clickListener = l;
        super.setOnClickListener(v -> {});
    }

    @Override
    public void setChartItemView(AbsChartItemView t) {
        chartItem = t;
        removeAllViews();
        addView(chartItem);
        requestLayout();
    }

    @Override
    public AbsChartItemView getChartItemView() {
        return chartItem;
    }

    @Override
    public int getChartTop() {
        return chartTop;
    }

    @Override
    public int getChartBottom() {
        return chartBottom;
    }
}

