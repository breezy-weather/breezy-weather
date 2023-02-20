package wangdaye.com.geometricweather.common.ui.widgets.trend.item;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.common.ui.widgets.trend.chart.AbsChartItemView;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

/**
 * Daily trend item view.
 * */
public class DailyTrendItemView extends AbsTrendItemView {

    private @Nullable AbsChartItemView mChartItem;
    private Paint mWeekTextPaint;
    private Paint mDateTextPaint;

    @Nullable private OnClickListener mClickListener;

    @Nullable private String mWeekText;
    @Nullable private String mDateText;

    @Nullable private Drawable mDayIconDrawable;
    @Nullable private Drawable mNightIconDrawable;

    @ColorInt private int mContentColor;
    @ColorInt private int mSubTitleColor;

    private float mWeekTextBaseLine;

    private float mDateTextBaseLine;

    private float mDayIconLeft;
    private float mDayIconTop;

    private float mTrendViewTop;

    private float mNightIconLeft;
    private float mNightIconTop;

    private int mIconSize;

    private int mChartTop;
    private int mChartBottom;

    private static final int ICON_SIZE_DIP = 32;
    private static final int TEXT_MARGIN_DIP = 2;
    private static final int ICON_MARGIN_DIP = 8;

    public DailyTrendItemView(Context context) {
        super(context);
        initialize();
    }

    public DailyTrendItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public DailyTrendItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public DailyTrendItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        setWillNotDraw(false);

        mWeekTextPaint = new Paint();
        mWeekTextPaint.setAntiAlias(true);
        mWeekTextPaint.setTextAlign(Paint.Align.CENTER);
        mWeekTextPaint.setTypeface(
                DisplayUtils.getTypefaceFromTextAppearance(getContext(), R.style.title_text)
        );
        mWeekTextPaint.setTextSize(
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
        float consumedHeight;
        float textMargin = DisplayUtils.dpToPx(getContext(), TEXT_MARGIN_DIP);
        float iconMargin = DisplayUtils.dpToPx(getContext(), ICON_MARGIN_DIP);

        // week text.
        Paint.FontMetrics fontMetrics = mWeekTextPaint.getFontMetrics();
        y += textMargin;
        mWeekTextBaseLine = y - fontMetrics.top;
        y += fontMetrics.bottom - fontMetrics.top;
        y += textMargin;

        // date text.
        fontMetrics = mDateTextPaint.getFontMetrics();
        y += textMargin;
        mDateTextBaseLine = y - fontMetrics.top;
        y += fontMetrics.bottom - fontMetrics.top;
        y += textMargin;

        // day icon.
        if (mDayIconDrawable != null) {
            y += iconMargin;
            mDayIconLeft = (width - mIconSize) / 2f;
            mDayIconTop = y;
            y += mIconSize;
            y += iconMargin;
        }

        consumedHeight = y;

        // margin bottom.
        float marginBottom = DisplayUtils.dpToPx(
                getContext(), TrendRecyclerView.ITEM_MARGIN_BOTTOM_DIP);
        consumedHeight += marginBottom;

        // night icon.
        if (mNightIconDrawable != null) {
            mNightIconLeft = (width - mIconSize) / 2f;
            mNightIconTop = height - marginBottom - iconMargin - mIconSize;
            consumedHeight += mIconSize + 2 * iconMargin;
        }

        // chartItem item view.
        if (mChartItem != null) {
            mChartItem.measure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec((int) (height - consumedHeight), MeasureSpec.EXACTLY)
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
        // week text.
        if (mWeekText != null) {
            mWeekTextPaint.setColor(mContentColor);
            canvas.drawText(mWeekText, getMeasuredWidth() / 2f, mWeekTextBaseLine, mWeekTextPaint);
        }

        // date text.
        if (mDateText != null) {
            mDateTextPaint.setColor(mSubTitleColor);
            canvas.drawText(mDateText, getMeasuredWidth() / 2f, mDateTextBaseLine, mDateTextPaint);
        }

        int restoreCount;

        // day icon.
        if (mDayIconDrawable != null) {
            restoreCount = canvas.save();
            canvas.translate(mDayIconLeft, mDayIconTop);
            mDayIconDrawable.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }

        // night icon.
        if (mNightIconDrawable != null) {
            restoreCount = canvas.save();
            canvas.translate(mNightIconLeft, mNightIconTop);
            mNightIconDrawable.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mClickListener != null) {
                mClickListener.onClick(this);
            }
        }
        return super.onTouchEvent(event);
    }

    public void setWeekText(String weekText) {
        mWeekText = weekText;
        invalidate();
    }

    public void setDateText(String dateText) {
        mDateText = dateText;
        invalidate();
    }

    public void setTextColor(@ColorInt int contentColor, @ColorInt int subTitleColor) {
        mContentColor = contentColor;
        mSubTitleColor = subTitleColor;
        invalidate();
    }

    public void setDayIconDrawable(@Nullable Drawable d) {
        boolean nullDrawable = mDayIconDrawable == null;

        mDayIconDrawable = d;
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

    public void setNightIconDrawable(@Nullable Drawable d) {
        boolean nullDrawable = mNightIconDrawable == null;

        mNightIconDrawable = d;
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

