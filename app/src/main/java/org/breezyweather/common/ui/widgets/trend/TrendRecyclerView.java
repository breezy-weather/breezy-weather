package org.breezyweather.common.ui.widgets.trend;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import org.breezyweather.common.ui.widgets.horizontal.HorizontalRecyclerView;
import org.breezyweather.common.ui.widgets.trend.item.AbsTrendItemView;
import org.breezyweather.R;
import org.breezyweather.common.utils.DisplayUtils;

/**
 * Trend recycler view.
 * */

public class TrendRecyclerView extends HorizontalRecyclerView {

    private final Paint mPaint;
    @ColorInt private int mLineColor;

    private int mDrawingBoundaryTop;
    private int mDrawingBoundaryBottom;

    private @Nullable List<KeyLine> mKeyLineList;
    private boolean mKeyLineVisibility = true;

    private @Nullable Float mHighestData;
    private @Nullable Float mLowestData;

    private final int mTextSize;
    private final int mTextMargin;
    private final int mLineWidth;

    private static final int LINE_WIDTH_DIP = 1;
    private static final int TEXT_SIZE_DIP = 12;
    private static final int TEXT_MARGIN_DIP = 2;
    public static final int ITEM_MARGIN_BOTTOM_DIP = 16;

    public static class KeyLine {

        float value;
        String contentLeft;
        String contentRight;
        ContentPosition contentPosition;

        public enum ContentPosition {ABOVE_LINE, BELOW_LINE}

        public KeyLine(float value, String contentLeft, String contentRight, ContentPosition contentPosition) {
            this.value = value;
            this.contentLeft = contentLeft;
            this.contentRight = contentRight;
            this.contentPosition = contentPosition;
        }
    }

    public TrendRecyclerView(Context context) {
        this(context, null);
    }

    public TrendRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TrendRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setWillNotDraw(false);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setTypeface(
                DisplayUtils.getTypefaceFromTextAppearance(getContext(), R.style.subtitle_text)
        );

        mTextSize = (int) DisplayUtils.dpToPx(getContext(), TEXT_SIZE_DIP);
        mTextMargin = (int) DisplayUtils.dpToPx(getContext(), TEXT_MARGIN_DIP);
        mLineWidth = (int) DisplayUtils.dpToPx(getContext(), LINE_WIDTH_DIP);

        mDrawingBoundaryTop = -1;
        mDrawingBoundaryBottom = -1;

        setLineColor(Color.GRAY);

        mKeyLineList = new ArrayList<>();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawKeyLines(canvas);
    }

    private void drawKeyLines(Canvas canvas) {
        if (!mKeyLineVisibility
                || mKeyLineList == null
                || mKeyLineList.size() == 0
                || mHighestData == null
                || mLowestData == null) {
            return;
        }

        if (getChildCount() > 0) {
            mDrawingBoundaryTop = ((AbsTrendItemView) getChildAt(0)).getChartTop();
            mDrawingBoundaryBottom = ((AbsTrendItemView) getChildAt(0)).getChartBottom();
        }
        if (mDrawingBoundaryTop < 0 || mDrawingBoundaryBottom < 0) {
            return;
        }

        float dataRange = mHighestData - mLowestData;
        float boundaryRange = mDrawingBoundaryBottom - mDrawingBoundaryTop;
        for (KeyLine line : mKeyLineList) {
            if (line.value > mHighestData || line.value < mLowestData) {
                continue;
            }

            int y = (int) (mDrawingBoundaryBottom - (line.value - mLowestData) / dataRange * boundaryRange);

            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mLineWidth);
            mPaint.setColor(mLineColor);
            canvas.drawLine(0, y, getMeasuredWidth(), y, mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setTextSize(mTextSize);
            mPaint.setColor(
                    DisplayUtils.isDarkMode(getContext())
                            ? ContextCompat.getColor(getContext(), R.color.colorTextGrey)
                            : ContextCompat.getColor(getContext(), R.color.colorTextGrey2nd)
            );
            switch (line.contentPosition) {
                case ABOVE_LINE:
                    mPaint.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText(
                            line.contentLeft,
                            2 * mTextMargin,
                            y - mPaint.getFontMetrics().bottom - mTextMargin,
                            mPaint
                    );
                    mPaint.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText(
                            line.contentRight,
                            getMeasuredWidth() - 2 * mTextMargin,
                            y - mPaint.getFontMetrics().bottom - mTextMargin,
                            mPaint
                    );
                    break;

                case BELOW_LINE:
                    mPaint.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText(
                            line.contentLeft,
                            2 * mTextMargin,
                            y - mPaint.getFontMetrics().top + mTextMargin,
                            mPaint
                    );
                    mPaint.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText(
                            line.contentRight,
                            getMeasuredWidth() - 2 * mTextMargin,
                            y - mPaint.getFontMetrics().top + mTextMargin,
                            mPaint
                    );
                    break;
            }
        }
    }

    // control.

    public void setData(List<KeyLine> keyLineList, float highestData, float lowestData) {
        mKeyLineList = keyLineList;
        mHighestData = highestData;
        mLowestData = lowestData;
        invalidate();
    }

    public void setKeyLineVisibility(boolean visibility) {
        mKeyLineVisibility = visibility;
        invalidate();
    }

    public void setLineColor(@ColorInt int lineColor) {
        mLineColor = lineColor;
        invalidate();
    }
}
