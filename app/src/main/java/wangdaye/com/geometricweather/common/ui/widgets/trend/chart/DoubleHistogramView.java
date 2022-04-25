package wangdaye.com.geometricweather.common.ui.widgets.trend.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

/**
 * Double histogram view.
 * */
public class DoubleHistogramView extends AbsChartItemView {

    private Paint mPaint;

    private @Nullable Float mHighHistogramValue;
    private @Nullable Float mLowHistogramValue;
    private @Nullable String mHighHistogramValueStr;
    private @Nullable String mLowHistogramValueStr;
    private @Nullable Float mHighestHistogramValue;

    private int mHighHistogramY;
    private int mLowHistogramY;

    private int mMargins;
    private int mMarginCenter;
    private int mHistogramWidth;
    private int mHistogramTextSize;
    private int mChartLineWith;
    private int mTextMargin;

    private int[] mLineColors;
    private int mTextColor;
    private int mTextShadowColor;
    private @Size(2) float[] mHistogramAlphas;

    private static final float MARGIN_DIP = 24;
    private static final float MARGIN_CENTER_DIP = 4;
    private static final float HISTOGRAM_WIDTH_DIP = 8;
    private static final float HISTOGRAM_TEXT_SIZE_DIP = 14;
    private static final float CHART_LINE_SIZE_DIP = 1;
    private static final float TEXT_MARGIN_DIP = 2;

    public DoubleHistogramView(Context context) {
        super(context);
        initialize();
    }

    public DoubleHistogramView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public DoubleHistogramView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        mLineColors = new int[] {Color.BLACK, Color.DKGRAY, Color.LTGRAY};

        setTextColors(Color.BLACK);

        mMargins = (int) DisplayUtils.dpToPx(getContext(), MARGIN_DIP);
        mMarginCenter = (int) DisplayUtils.dpToPx(getContext(), MARGIN_CENTER_DIP);
        mHistogramWidth = (int) DisplayUtils.dpToPx(getContext(), HISTOGRAM_WIDTH_DIP);
        mHistogramTextSize = (int) DisplayUtils.dpToPx(getContext(), HISTOGRAM_TEXT_SIZE_DIP);
        mChartLineWith = (int) DisplayUtils.dpToPx(getContext(), CHART_LINE_SIZE_DIP);
        mTextMargin = (int) DisplayUtils.dpToPx(getContext(), TEXT_MARGIN_DIP);

        mPaint = new Paint();
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mPaint.setTypeface(
                DisplayUtils.getTypefaceFromTextAppearance(getContext(), R.style.title_text)
        );

        mHistogramAlphas = new float[] {1, 1};
    }

    @Override
    public int getMarginTop() {
        return mMargins;
    }

    @Override
    public int getMarginBottom() {
        return mMargins;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        computeCoordinates();

        drawTimeLine(canvas);

        if (mHighestHistogramValue != null) {
            if (mHighHistogramValue != null && mHighHistogramValue != 0 && mHighHistogramValueStr != null) {
                drawHighHistogram(canvas);
            }
            if (mLowHistogramValue != null && mLowHistogramValue != 0 && mLowHistogramValueStr != null) {
                drawLowHistogram(canvas);
            }
        }
    }

    private void drawTimeLine(Canvas canvas) {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mChartLineWith);
        mPaint.setColor(mLineColors[2]);

        canvas.drawLine(
                getMeasuredWidth() / 2.f, mMargins,
                getMeasuredWidth() / 2.f, getMeasuredHeight() - mMargins,
                mPaint
        );
    }

    private void drawHighHistogram(Canvas canvas) {
        assert mHighHistogramValue != null;
        assert mHighHistogramValueStr != null;

        float cx = getMeasuredWidth() / 2f;
        float cy = getMeasuredHeight() / 2f - mMarginCenter / 2f;

        // histogram.
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mLineColors[0]);
        mPaint.setAlpha((int) (255 * mHistogramAlphas[0]));
        canvas.drawRoundRect(
                new RectF(cx - mHistogramWidth / 2f, mHighHistogramY, cx + mHistogramWidth / 2f, cy),
                mHistogramWidth / 2f, mHistogramWidth / 2f,
                mPaint
        );

        // text.
        mPaint.setColor(mTextColor);
        mPaint.setAlpha(255);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(mHistogramTextSize);
        mPaint.setShadowLayer(2, 0, 1, mTextShadowColor);
        canvas.drawText(
                mHighHistogramValueStr, cx, mHighHistogramY - mPaint.getFontMetrics().bottom - mTextMargin, mPaint);
        mPaint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
    }

    private void drawLowHistogram(Canvas canvas) {
        assert mLowHistogramValue != null;
        assert mLowHistogramValueStr != null;

        float cx = getMeasuredWidth() / 2f;
        float cy = getMeasuredHeight() / 2f + mMarginCenter / 2f;

        // histogram.
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mLineColors[1]);
        mPaint.setAlpha((int) (255 * mHistogramAlphas[1]));
        canvas.drawRoundRect(
                new RectF(cx - mHistogramWidth / 2f, cy, cx + mHistogramWidth / 2f, mLowHistogramY),
                mHistogramWidth / 2f, mHistogramWidth / 2f,
                mPaint
        );

        // text.
        mPaint.setColor(mTextColor);
        mPaint.setAlpha(255);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(mHistogramTextSize);
        mPaint.setShadowLayer(2, 0, 1, mTextShadowColor);
        canvas.drawText(
                mLowHistogramValueStr, cx, mLowHistogramY - mPaint.getFontMetrics().top + mTextMargin, mPaint);
        mPaint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
    }

    // control.

    public void setData(@Nullable Float highHistogramValues,
                        @Nullable Float lowHistogramValues,
                        @Nullable String highHistogramValueStr,
                        @Nullable String lowHistogramValueStr,
                        @Nullable Float highestHistogramValue) {
        mHighHistogramValue = highHistogramValues;
        mLowHistogramValue = lowHistogramValues;
        mHighHistogramValueStr = highHistogramValueStr;
        mLowHistogramValueStr = lowHistogramValueStr;
        mHighestHistogramValue = highestHistogramValue;
        invalidate();
    }

    public void setLineColors(@ColorInt int colorHigh, @ColorInt int colorLow,
                              @ColorInt int colorSubLine) {
        mLineColors[0] = colorHigh;
        mLineColors[1] = colorLow;
        mLineColors[2] = colorSubLine;
        invalidate();
    }

    public void setTextColors(@ColorInt int textColor) {
        mTextColor = textColor;
        mTextShadowColor = Color.argb((int) (255 * 0.2), 0, 0, 0);
        invalidate();
    }

    public void setHistogramAlphas(float highAlpha, float lowAlpha) {
        mHistogramAlphas = new float[] {highAlpha, lowAlpha};
    }

    private void computeCoordinates() {
        float canvasHeight = (getMeasuredHeight() - mMargins * 2 - mMarginCenter) / 2f;
        float cy = getMeasuredHeight() / 2f;

        if (mHighestHistogramValue != null) {
            if (mHighHistogramValue != null) {
                mHighHistogramY = (int) (
                        cy - mMarginCenter / 2f - canvasHeight * mHighHistogramValue / mHighestHistogramValue
                );
            }
            if (mLowHistogramValue != null) {
                mLowHistogramY = (int) (
                        cy + mMarginCenter / 2f + canvasHeight * mLowHistogramValue / mHighestHistogramValue
                );
            }
        }
    }
}