package wangdaye.com.geometricweather.ui.widget.trend.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import wangdaye.com.geometricweather.ui.widget.trend.abs.ChartItemView;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Double histogram view.
 * */
public class DoubleHistogramView extends ChartItemView {

    private Paint paint;

    private @Nullable Float highHistogramValue;
    private @Nullable Float lowHistogramValue;
    private @Nullable String highHistogramValueStr;
    private @Nullable String lowHistogramValueStr;
    private @Nullable Float highestHistogramValue;

    private int highHistogramY;
    private int lowHistogramY;

    private int margins;
    private int marginCenter;
    private int histogramWidth;
    private int histogramTextSize;
    private int chartLineWith;
    private int textMargin;

    private int[] lineColors;
    private int textColor;
    private int textShadowColor;
    private @Size(2) float[] histogramAlphas;

    private static final float MARGIN_DIP = 24;
    private static final float MARGIN_CENTER_DIP = 4;
    private static final float HISTOGRAM_WIDTH_DIP = 8;
    private static final float HISTOGRAM_TEXT_SIZE_DIP = 13;
    private static final float CHART_LINE_SIZE_DIP = 1;
    private static final float TEXT_MARGIN_DIP = 2;

    public DoubleHistogramView(Context context) {
        super(context);
        this.initialize();
    }

    public DoubleHistogramView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public DoubleHistogramView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    private void initialize() {
        lineColors = new int[] {Color.BLACK, Color.DKGRAY, Color.LTGRAY};

        setTextColors(Color.BLACK);

        this.margins = (int) DisplayUtils.dpToPx(getContext(), MARGIN_DIP);
        this.marginCenter = (int) DisplayUtils.dpToPx(getContext(), MARGIN_CENTER_DIP);
        this.histogramWidth = (int) DisplayUtils.dpToPx(getContext(), HISTOGRAM_WIDTH_DIP);
        this.histogramTextSize = (int) DisplayUtils.dpToPx(getContext(), HISTOGRAM_TEXT_SIZE_DIP);
        this.chartLineWith = (int) DisplayUtils.dpToPx(getContext(), CHART_LINE_SIZE_DIP);
        this.textMargin = (int) DisplayUtils.dpToPx(getContext(), TEXT_MARGIN_DIP);

        this.paint = new Paint();
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);

        histogramAlphas = new float[] {1, 1};
    }

    @Override
    public int getMarginTop() {
        return margins;
    }

    @Override
    public int getMarginBottom() {
        return margins;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        computeCoordinates();

        drawTimeLine(canvas);

        if (highestHistogramValue != null) {
            if (highHistogramValue != null && highHistogramValue != 0 && highHistogramValueStr != null) {
                drawHighHistogram(canvas);
            }
            if (lowHistogramValue != null && lowHistogramValue != 0 && lowHistogramValueStr != null) {
                drawLowHistogram(canvas);
            }
        }
    }

    private void drawTimeLine(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(chartLineWith);
        paint.setColor(lineColors[2]);

        canvas.drawLine(
                getMeasuredWidth() / 2.f, margins,
                getMeasuredWidth() / 2.f, getMeasuredHeight() - margins,
                paint
        );
    }

    private void drawHighHistogram(Canvas canvas) {
        assert highHistogramValue != null;
        assert highHistogramValueStr != null;

        float cx = getMeasuredWidth() / 2f;
        float cy = getMeasuredHeight() / 2f - marginCenter / 2f;

        // histogram.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(lineColors[0]);
        paint.setAlpha((int) (255 * histogramAlphas[0]));
        canvas.drawRoundRect(
                new RectF(cx - histogramWidth / 2f, highHistogramY, cx + histogramWidth / 2f, cy),
                histogramWidth / 2f, histogramWidth / 2f,
                paint
        );

        // text.
        paint.setColor(textColor);
        paint.setAlpha(255);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(histogramTextSize);
        paint.setShadowLayer(2, 0, 1, textShadowColor);
        canvas.drawText(
                highHistogramValueStr, cx, highHistogramY - paint.getFontMetrics().bottom - textMargin, paint);
        paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
    }

    private void drawLowHistogram(Canvas canvas) {
        assert lowHistogramValue != null;
        assert lowHistogramValueStr != null;

        float cx = getMeasuredWidth() / 2f;
        float cy = getMeasuredHeight() / 2f + marginCenter / 2f;

        // histogram.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(lineColors[1]);
        paint.setAlpha((int) (255 * histogramAlphas[1]));
        canvas.drawRoundRect(
                new RectF(cx - histogramWidth / 2f, cy, cx + histogramWidth / 2f, lowHistogramY),
                histogramWidth / 2f, histogramWidth / 2f,
                paint
        );

        // text.
        paint.setColor(textColor);
        paint.setAlpha(255);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(histogramTextSize);
        paint.setShadowLayer(2, 0, 1, textShadowColor);
        canvas.drawText(
                lowHistogramValueStr, cx, lowHistogramY - paint.getFontMetrics().top + textMargin, paint);
        paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
    }

    // control.

    public void setData(@Nullable Float highHistogramValues,
                        @Nullable Float lowHistogramValues,
                        @Nullable String highHistogramValueStr,
                        @Nullable String lowHistogramValueStr,
                        @Nullable Float highestHistogramValue) {
        this.highHistogramValue = highHistogramValues;
        this.lowHistogramValue = lowHistogramValues;
        this.highHistogramValueStr = highHistogramValueStr;
        this.lowHistogramValueStr = lowHistogramValueStr;
        this.highestHistogramValue = highestHistogramValue;
        invalidate();
    }

    public void setLineColors(@ColorInt int colorHigh, @ColorInt int colorLow,
                              @ColorInt int colorSubLine) {
        lineColors[0] = colorHigh;
        lineColors[1] = colorLow;
        lineColors[2] = colorSubLine;
        invalidate();
    }

    public void setTextColors(@ColorInt int textColor) {
        this.textColor = textColor;
        this.textShadowColor = Color.argb((int) (255 * 0.2), 0, 0, 0);
        invalidate();
    }

    public void setHistogramAlphas(float highAlpha, float lowAlpha) {
        this.histogramAlphas = new float[] {highAlpha, lowAlpha};
    }

    private void computeCoordinates() {
        float canvasHeight = (getMeasuredHeight() - margins * 2 - marginCenter) / 2f;
        float cy = getMeasuredHeight() / 2f;

        if (highestHistogramValue != null) {
            if (highHistogramValue != null) {
                highHistogramY = (int) (
                        cy - marginCenter / 2f - canvasHeight * highHistogramValue / highestHistogramValue
                );
            }
            if (lowHistogramValue != null) {
                lowHistogramY = (int) (
                        cy + marginCenter / 2f + canvasHeight * lowHistogramValue / highestHistogramValue
                );
            }
        }
    }
}