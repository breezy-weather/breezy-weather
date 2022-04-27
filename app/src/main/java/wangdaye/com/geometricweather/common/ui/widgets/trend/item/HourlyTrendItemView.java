package wangdaye.com.geometricweather.common.ui.widgets.trend.item;

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
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.common.ui.widgets.trend.chart.AbsChartItemView;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

/**
 * Hourly trend item view.
 * */
public class HourlyTrendItemView extends AbsTrendItemView {

    private @Nullable AbsChartItemView mChartItem;
    private Paint mHourTextPaint;
    private Paint mDateTextPaint;

    @Nullable private OnClickListener mClickListener;

    @Nullable private String mHourText;
    @Nullable private String mDayText;
    @Nullable private Drawable mIconDrawable;

    @ColorInt private int mContentColor;
    @ColorInt private int mSubTitleColor;

    private float mDayTextBaseLine;
    private float mHourTextBaseLine;

    private float mIconLeft;
    private float mIconTop;

    private float mTrendViewTop;

    private int mIconSize;

    private int mChartTop;
    private int mChartBottom;

    private static final int ICON_SIZE_DIP = 32;
    private static final int TEXT_MARGIN_DIP = 2;
    private static final int ICON_MARGIN_DIP = 8;

    public HourlyTrendItemView(Context context) {
        super(context);
        initialize();
    }

    public HourlyTrendItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public HourlyTrendItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public HourlyTrendItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        setWillNotDraw(false);

        mHourTextPaint = new Paint();
        mHourTextPaint.setAntiAlias(true);
        mHourTextPaint.setTextAlign(Paint.Align.CENTER);
        mHourTextPaint.setTypeface(
                DisplayUtils.getTypefaceFromTextAppearance(getContext(), R.style.title_text)
        );
        mHourTextPaint.setTextSize(
                getContext().getResources().getDimensionPixelSize(R.dimen.title_text_size)
        );

        mDateTextPaint = new Paint();
        mDateTextPaint.setAntiAlias(true);
        mDateTextPaint.setTextAlign(Paint.Align.CENTER);
        mDateTextPaint.setTypeface(
                DisplayUtils.getTypefaceFromTextAppearance(getContext(), R.style.content_text)
        );
        mDateTextPaint.setTextSize(
                getContext().getResources().getDimensionPixelSize(R.dimen.content_text_size)
        );

        setTextColor(Color.BLACK, Color.GRAY);

        mIconSize = (int) DisplayUtils.dpToPx(getContext(), ICON_SIZE_DIP);

        mChartTop = 0;
        mChartBottom = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        float y = 0;
        float textMargin = DisplayUtils.dpToPx(getContext(), TEXT_MARGIN_DIP);
        float iconMargin = DisplayUtils.dpToPx(getContext(), ICON_MARGIN_DIP);

        // hour text.
        Paint.FontMetrics fontMetrics = mHourTextPaint.getFontMetrics();
        y += textMargin;
        mHourTextBaseLine = y - fontMetrics.top;
        y += fontMetrics.bottom - fontMetrics.top;
        y += textMargin;

        // day text.
        fontMetrics = mDateTextPaint.getFontMetrics();
        y += textMargin;
        mDayTextBaseLine = y - fontMetrics.top;
        y += fontMetrics.bottom - fontMetrics.top;
        y += textMargin;

        // day icon.
        if (mIconDrawable != null) {
            y += iconMargin;
            mIconLeft = (width - mIconSize) / 2f;
            mIconTop = y;
            y += mIconSize;
            y += iconMargin;
        }

        // margin bottom.
        float marginBottom = DisplayUtils.dpToPx(
                getContext(), TrendRecyclerView.ITEM_MARGIN_BOTTOM_DIP);

        // chartItem item view.
        if (mChartItem != null) {
            mChartItem.measure(
                    MeasureSpec.makeMeasureSpec(
                            width,
                            MeasureSpec.EXACTLY
                    ), MeasureSpec.makeMeasureSpec(
                            (int) (height - marginBottom - y),
                            MeasureSpec.EXACTLY
                    )
            );
        }
        mTrendViewTop = y;

        mChartTop = (int) (mTrendViewTop + mChartItem.getMarginTop());
        mChartBottom = (int) (mTrendViewTop + mChartItem.getMeasuredHeight() - mChartItem.getMarginBottom());

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mChartItem != null) {
            mChartItem.layout(
                    0,
                    (int) mTrendViewTop,
                    mChartItem.getMeasuredWidth(),
                    (int) mTrendViewTop + mChartItem.getMeasuredHeight()
            );
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // hour text.
        if (mHourText != null) {
            mHourTextPaint.setColor(mContentColor);
            canvas.drawText(mHourText, getMeasuredWidth() / 2f, mHourTextBaseLine, mHourTextPaint);
        }

        // day text.
        if (mDayText != null) {
            mDateTextPaint.setColor(mSubTitleColor);
            canvas.drawText(mDayText, getMeasuredWidth() / 2f, mDayTextBaseLine, mDateTextPaint);
        }

        // day icon.
        if (mIconDrawable != null) {
            int restoreCount = canvas.save();
            canvas.translate(mIconLeft, mIconTop);
            mIconDrawable.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (mClickListener != null) {
                    mClickListener.onClick(this);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setDayText(String dayText) {
        mDayText = dayText;
        invalidate();
    }

    public void setHourText(String hourText) {
        mHourText = hourText;
        invalidate();
    }

    public void setTextColor(@ColorInt int contentColor, @ColorInt int subTitleColor) {
        mContentColor = contentColor;
        mSubTitleColor = subTitleColor;
        invalidate();
    }

    public void setIconDrawable(@Nullable Drawable d) {
        boolean nullDrawable = mIconDrawable == null;

        mIconDrawable = d;
        if (d != null) {
            d.setVisible(true, true);
            d.setCallback(this);
            d.setBounds(0, 0, mIconSize, mIconSize);
        }

        if (nullDrawable != (d == null)) {
            requestLayout();
        } else {
            invalidate();
        }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        mClickListener = l;
        super.setOnClickListener(v -> {});
    }

    @Override
    public void setChartItemView(AbsChartItemView t) {
        mChartItem = t;
        removeAllViews();
        addView(mChartItem);
        requestLayout();
    }

    @Override
    public AbsChartItemView getChartItemView() {
        return mChartItem;
    }

    @Override
    public int getChartTop() {
        return mChartTop;
    }

    @Override
    public int getChartBottom() {
        return mChartBottom;
    }
}
