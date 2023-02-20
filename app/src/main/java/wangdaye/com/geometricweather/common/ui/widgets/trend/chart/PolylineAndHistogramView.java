package wangdaye.com.geometricweather.common.ui.widgets.trend.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.core.graphics.ColorUtils;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.ui.widgets.DayNightShaderWrapper;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

/**
 * Polyline and histogram view.
 * */
public class PolylineAndHistogramView extends AbsChartItemView {

    private Paint mPaint;
    private Path mPath;
    private DayNightShaderWrapper mShaderWrapper;

    private @Nullable @Size(3) Float[] mHighPolylineValues = new Float[3];
    private @Nullable @Size(3) Float[] mLowPolylineValues = new Float[3];
    private @Nullable String mHighPolylineValueStr;
    private @Nullable String mLowPolylineValueStr;
    private @Nullable Float mHighestPolylineValue;
    private @Nullable Float mLowestPolylineValue;

    private @Nullable Float mHistogramValue;
    private @Nullable String mHistogramValueStr;
    private @Nullable Float mHighestHistogramValue;
    private @Nullable Float mLowestHistogramValue;

    private final int[] mHighPolylineY = new int[3];
    private final int[] mLowPolylineY = new int[3];
    private int mHistogramY;

    private int mMarginTop;
    private int mMarginBottom;
    private int mPolylineWidth;
    private int mPolylineTextSize;
    private int mHistogramWidth;
    private int mHistogramTextSize;
    private int mChartLineWith;
    private int mTextMargin;

    private int[] mLineColors;
    private int[] mShadowColors;
    private int mHighTextColor;
    private int mLowTextColor;
    private int mTextShadowColor;
    private int mHistogramTextColor;

    private float mHistogramAlpha;

    private static final float MARGIN_TOP_DIP = 24;
    private static final float MARGIN_BOTTOM_DIP = 36;
    private static final float POLYLINE_SIZE_DIP = 5f;
    private static final float POLYLINE_TEXT_SIZE_DIP = 14;
    private static final float HISTOGRAM_WIDTH_DIP = 4.5f;
    private static final float HISTOGRAM_TEXT_SIZE_DIP = 12;
    private static final float CHART_LINE_SIZE_DIP = 1;
    private static final float TEXT_MARGIN_DIP = 2;

    private static final float SHADOW_ALPHA_FACTOR_LIGHT = 0.15f;
    private static final float SHADOW_ALPHA_FACTOR_DARK = 0.3f;

    public PolylineAndHistogramView(Context context) {
        super(context);
        initialize();
    }

    public PolylineAndHistogramView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public PolylineAndHistogramView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        mLineColors = new int[] {Color.BLACK, Color.DKGRAY, Color.LTGRAY};
        mShadowColors = new int[] {Color.BLACK, Color.WHITE};

        setTextColors(Color.BLACK, Color.DKGRAY, Color.GRAY);
        setHistogramAlpha(0.33f);

        mMarginTop = (int) DisplayUtils.dpToPx(getContext(), MARGIN_TOP_DIP);
        mMarginBottom = (int) DisplayUtils.dpToPx(getContext(), MARGIN_BOTTOM_DIP);
        mPolylineTextSize = (int) DisplayUtils.dpToPx(getContext(), POLYLINE_TEXT_SIZE_DIP);
        mHistogramTextSize = (int) DisplayUtils.dpToPx(getContext(), HISTOGRAM_TEXT_SIZE_DIP);
        mPolylineWidth = (int) DisplayUtils.dpToPx(getContext(), POLYLINE_SIZE_DIP);
        mHistogramWidth = (int) DisplayUtils.dpToPx(getContext(), HISTOGRAM_WIDTH_DIP);
        mChartLineWith = (int) DisplayUtils.dpToPx(getContext(), CHART_LINE_SIZE_DIP);
        mTextMargin = (int) DisplayUtils.dpToPx(getContext(), TEXT_MARGIN_DIP);

        mPaint = new Paint();
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mPaint.setTypeface(
                DisplayUtils.getTypefaceFromTextAppearance(getContext(), R.style.title_text)
        );

        mPath = new Path();
        mShaderWrapper = new DayNightShaderWrapper(getMeasuredWidth(), getMeasuredHeight());
        setShadowColors(Color.BLACK, Color.GRAY, true);
    }

    @Override
    public int getMarginTop() {
        return mMarginTop;
    }

    @Override
    public int getMarginBottom() {
        return mMarginBottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        ensureShader(mShaderWrapper.isLightTheme());
        computeCoordinates();

        drawTimeLine(canvas);

        if (mHistogramValue != null
                && (mHistogramValue != 0 || (mHighestPolylineValue == null && mLowestPolylineValue == null))
                && mHistogramValueStr != null
                && mHighestHistogramValue != null
                && mLowestHistogramValue != null) {
            drawHistogram(canvas);
        }
        if (mHighestPolylineValue != null && mLowestPolylineValue != null) {
            if (mHighPolylineValues != null && mHighPolylineValueStr != null) {
                drawHighPolyLine(canvas);
            }
            if (mLowPolylineValues != null && mLowPolylineValueStr != null) {
                drawLowPolyline(canvas);
            }
        }
    }

    private void drawTimeLine(Canvas canvas) {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mChartLineWith);
        mPaint.setColor(mLineColors[2]);

        canvas.drawLine(
                getMeasuredWidth() / 2.f, mMarginTop,
                getMeasuredWidth() / 2.f, getMeasuredHeight() - mMarginBottom,
                mPaint
        );
    }

    private void drawHighPolyLine(Canvas canvas) {
        assert mHighPolylineValues != null;
        assert mHighPolylineValueStr != null;
        if (mHighPolylineValues[0] != null && mHighPolylineValues[2] != null) {
            // shadow.
            mPaint.setColor(Color.BLACK);
            mPaint.setShader(mShaderWrapper.getShader());
            mPaint.setStyle(Paint.Style.FILL);

            mPath.reset();
            mPath.moveTo(getRTLCompactX(0), mHighPolylineY[0]);
            mPath.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), mHighPolylineY[1]);
            mPath.lineTo(getRTLCompactX(getMeasuredWidth()), mHighPolylineY[2]);
            mPath.lineTo(getRTLCompactX(getMeasuredWidth()), getMeasuredHeight() - mMarginBottom);
            mPath.lineTo(getRTLCompactX(0), getMeasuredHeight() - mMarginBottom);
            mPath.close();
            canvas.drawPath(mPath, mPaint);

            // line.
            mPaint.setShader(null);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mPolylineWidth);
            mPaint.setColor(mLineColors[0]);

            mPath.reset();
            mPath.moveTo(getRTLCompactX(0), mHighPolylineY[0]);
            mPath.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), mHighPolylineY[1]);
            mPath.lineTo(getRTLCompactX(getMeasuredWidth()), mHighPolylineY[2]);
            canvas.drawPath(mPath, mPaint);
        } else if (mHighPolylineValues[0] == null) {
            // shadow.
            mPaint.setColor(Color.BLACK);
            mPaint.setShader(mShaderWrapper.getShader());
            mPaint.setStyle(Paint.Style.FILL);

            mPath.reset();
            mPath.moveTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), mHighPolylineY[1]);
            mPath.lineTo(getRTLCompactX(getMeasuredWidth()), mHighPolylineY[2]);
            mPath.lineTo(getRTLCompactX(getMeasuredWidth()), getMeasuredHeight() - mMarginBottom);
            mPath.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), getMeasuredHeight() - mMarginBottom);
            mPath.close();
            canvas.drawPath(mPath, mPaint);

            // line.
            mPaint.setShader(null);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mPolylineWidth);
            mPaint.setColor(mLineColors[0]);

            mPath.reset();
            mPath.moveTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), mHighPolylineY[1]);
            mPath.lineTo(getRTLCompactX(getMeasuredWidth()), mHighPolylineY[2]);
            canvas.drawPath(mPath, mPaint);
        } else {
            // shadow.
            mPaint.setColor(Color.BLACK);
            mPaint.setShader(mShaderWrapper.getShader());
            mPaint.setStyle(Paint.Style.FILL);

            mPath.reset();
            mPath.moveTo(getRTLCompactX(0), mHighPolylineY[0]);
            mPath.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), mHighPolylineY[1]);
            mPath.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), getMeasuredHeight() - mMarginBottom);
            mPath.lineTo(getRTLCompactX(0), getMeasuredHeight() - mMarginBottom);
            mPath.close();
            canvas.drawPath(mPath, mPaint);

            // line.
            mPaint.setShader(null);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mPolylineWidth);
            mPaint.setColor(mLineColors[0]);

            mPath.reset();
            mPath.moveTo(getRTLCompactX(0), mHighPolylineY[0]);
            mPath.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), mHighPolylineY[1]);
            canvas.drawPath(mPath, mPaint);
        }

        // text.
        mPaint.setColor(mHighTextColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(mPolylineTextSize);
        mPaint.setShadowLayer(2, 0, 1, mTextShadowColor);
        canvas.drawText(
                mHighPolylineValueStr,
                getRTLCompactX((float) (getMeasuredWidth() / 2.0)),
                mHighPolylineY[1] - mPaint.getFontMetrics().bottom - mTextMargin,
                mPaint
        );
        mPaint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
    }

    private void drawLowPolyline(Canvas canvas) {
        assert mLowPolylineValues != null;
        assert mLowPolylineValueStr != null;
        if (mLowPolylineValues[0] != null && mLowPolylineValues[2] != null) {
            mPaint.setShader(null);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mPolylineWidth);
            mPaint.setColor(mLineColors[1]);

            mPath.reset();
            mPath.moveTo(getRTLCompactX(0), mLowPolylineY[0]);
            mPath.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), mLowPolylineY[1]);
            mPath.lineTo(getRTLCompactX(getMeasuredWidth()), mLowPolylineY[2]);
            canvas.drawPath(mPath, mPaint);
        } else if (mLowPolylineValues[0] == null) {
            mPaint.setShader(null);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mPolylineWidth);
            mPaint.setColor(mLineColors[1]);

            mPath.reset();
            mPath.moveTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), mLowPolylineY[1]);
            mPath.lineTo(getRTLCompactX(getMeasuredWidth()), mLowPolylineY[2]);
            canvas.drawPath(mPath, mPaint);
        } else {
            mPaint.setShader(null);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mPolylineWidth);
            mPaint.setColor(mLineColors[1]);

            mPath.reset();
            mPath.moveTo(getRTLCompactX(0), mLowPolylineY[0]);
            mPath.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), mLowPolylineY[1]);
            canvas.drawPath(mPath, mPaint);
        }

        // text.
        mPaint.setColor(mLowTextColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(mPolylineTextSize);
        mPaint.setShadowLayer(2, 0, 1, mTextShadowColor);
        canvas.drawText(
                mLowPolylineValueStr,
                getRTLCompactX((float) (getMeasuredWidth() / 2.0)),
                mLowPolylineY[1] - mPaint.getFontMetrics().top + mTextMargin,
                mPaint
        );
        mPaint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
    }

    private void drawHistogram(Canvas canvas) {
        assert mHistogramValueStr != null;

        mPaint.setColor(mLineColors[1]);
        mPaint.setAlpha((int) (255 * mHistogramAlpha));
        mPaint.setStyle(Paint.Style.FILL);

        canvas.drawRoundRect(
                new RectF(
                        (float) (getMeasuredWidth() / 2.0 - mHistogramWidth),
                        mHistogramY,
                        (float) (getMeasuredWidth() / 2.0 + mHistogramWidth),
                        getMeasuredHeight() - mMarginBottom
                ),
                mHistogramWidth, mHistogramWidth,
                mPaint
        );

        mPaint.setColor(mHistogramTextColor);
        mPaint.setAlpha(255);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(mHistogramTextSize);
        canvas.drawText(
                mHistogramValueStr,
                (float) (getMeasuredWidth() / 2.0),
                (float) (
                        getMeasuredHeight()
                                - mMarginBottom
                                - mPaint.getFontMetrics().top
                                + 2.0 * mTextMargin
                                + mPolylineTextSize
                ), mPaint
        );

        mPaint.setAlpha(255);
    }

    // control.

    public void setData(@Nullable @Size(3) Float[] highPolylineValues,
                        @Nullable @Size(3) Float[] lowPolylineValues,
                        @Nullable String highPolylineValueStr,
                        @Nullable String lowPolylineValueStr,
                        @Nullable Float highestPolylineValue,
                        @Nullable Float lowestPolylineValue,
                        @Nullable Float histogramValue,
                        @Nullable String histogramValueStr,
                        @Nullable Float highestHistogramValue,
                        @Nullable Float lowestHistogramValue) {
        mHighPolylineValues = highPolylineValues;
        mLowPolylineValues = lowPolylineValues;
        mHighPolylineValueStr = highPolylineValueStr;
        mLowPolylineValueStr = lowPolylineValueStr;
        mHighestPolylineValue = highestPolylineValue;
        mLowestPolylineValue = lowestPolylineValue;
        mHistogramValue = histogramValue;
        mHistogramValueStr = histogramValueStr;
        mHighestHistogramValue = highestHistogramValue;
        mLowestHistogramValue = lowestHistogramValue;
        invalidate();
    }

    public void setLineColors(@ColorInt int colorHigh, @ColorInt int colorLow,
                              @ColorInt int colorSubLine) {
        mLineColors[0] = colorHigh;
        mLineColors[1] = colorLow;
        mLineColors[2] = colorSubLine;
        invalidate();
    }

    public void setShadowColors(@ColorInt int colorHigh, @ColorInt int colorLow, boolean lightTheme) {
        mShadowColors[0] = lightTheme
                ? ColorUtils.setAlphaComponent(colorHigh, (int) (255 * SHADOW_ALPHA_FACTOR_LIGHT))
                : ColorUtils.setAlphaComponent(colorLow, (int) (255 * SHADOW_ALPHA_FACTOR_DARK));
        mShadowColors[1] = Color.TRANSPARENT;

        ensureShader(lightTheme);
        invalidate();
    }

    public void setTextColors(
            @ColorInt int highTextColor,
            @ColorInt int lowTextColor,
            @ColorInt int histogramTextColor
    ) {
        mHighTextColor = highTextColor;
        mLowTextColor = lowTextColor;
        mTextShadowColor = Color.argb((int) (255 * 0.2), 0, 0, 0);
        mHistogramTextColor = histogramTextColor;
        invalidate();
    }

    public void setHistogramAlpha(@FloatRange(from = 0, to = 1) float histogramAlpha) {
        mHistogramAlpha = histogramAlpha;
        invalidate();
    }

    private void ensureShader(boolean lightTheme) {
        if (mShaderWrapper.isDifferent(
                getMeasuredWidth(), getMeasuredHeight(), lightTheme, mShadowColors)) {
            mShaderWrapper.setShader(
                    new LinearGradient(
                            0, mMarginTop,
                            0, getMeasuredHeight() - mMarginBottom,
                            mShadowColors[0], mShadowColors[1],
                            Shader.TileMode.CLAMP
                    ),
                    getMeasuredWidth(), getMeasuredHeight(),
                    lightTheme,
                    mShadowColors
            );
        }
    }

    private void computeCoordinates() {
        float canvasHeight = getMeasuredHeight() - mMarginTop - mMarginBottom;
        if (mHighestPolylineValue != null && mLowestPolylineValue != null) {
            if (mHighPolylineValues != null) {
                for (int i = 0; i < mHighPolylineValues.length; i ++) {
                    if (mHighPolylineValues[i] == null) {
                        mHighPolylineY[i] = 0;
                    } else {
                        mHighPolylineY[i] = computeSingleCoordinate(
                                canvasHeight, mHighPolylineValues[i], mHighestPolylineValue, mLowestPolylineValue);
                    }
                }
            }
            if (mLowPolylineValues != null) {
                for (int i = 0; i < mLowPolylineValues.length; i ++) {
                    if (mLowPolylineValues[i] == null) {
                        mLowPolylineY[i] = 0;
                    } else {
                        mLowPolylineY[i] = computeSingleCoordinate(
                                canvasHeight, mLowPolylineValues[i], mHighestPolylineValue, mLowestPolylineValue);
                    }
                }
            }
        }

        if (mHistogramValue != null && mHighestHistogramValue != null && mLowestHistogramValue != null) {
            mHistogramY = computeSingleCoordinate(
                    canvasHeight, mHistogramValue, mHighestHistogramValue, mLowestHistogramValue);
        }
    }

    private int computeSingleCoordinate(float canvasHeight, float value, float max, float min) {
        return (int) (
                getMeasuredHeight()
                        - mMarginBottom
                        - canvasHeight * (value - min) / (max - min)
        );
    }

    private float getRTLCompactX(float x) {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL ? (getMeasuredWidth() - x) : x;
    }
}