package wangdaye.com.geometricweather.ui.widget.trendView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;

/**
 * Trend item view.
 * */

public class TrendItemView extends View {

    private Paint paint;
    private Path path;
    private Shader shader;

    private float maxiTemps[] = new float[3];
    private float miniTemps[] = new float[3];
    private int highestTemp;
    private int lowestTemp;
    private int precipitation;

    private int[] maxiTempYs = new int[3];
    private int[] miniTempYs = new int[3];
    private int precipitationY;

    private int[] lineColors;
    private int[] shadowColors;
    private int textColor;
    private int precipitationTextColor;

    public static final int NONEXISTENT_VALUE = Integer.MAX_VALUE;

    // dp size.
    private float TREND_MARGIN_TOP = 24;
    private float TREND_MARGIN_BOTTOM = 36;
    private float WEATHER_TEXT_SIZE = 13;
    private float POP_TEXT_SIZE = 11;
    private float TREND_LINE_SIZE = 2;
    private float CHART_LINE_SIZE = 1;
    private float MARGIN_TEXT = 2;

    public TrendItemView(Context context) {
        super(context);
        this.initialize();
    }

    public TrendItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public TrendItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    private void initialize() {
        this.lineColors = new int[] {
                ContextCompat.getColor(getContext(), R.color.colorPrimary),
                ContextCompat.getColor(getContext(), R.color.colorTextDark),
                ContextCompat.getColor(getContext(), R.color.colorLine)};
        this.shadowColors = new int[] {
                Color.argb(50, 176, 176, 176),
                Color.argb(0, 176, 176, 176),
                Color.argb(200, 176, 176, 176)};
        this.textColor = ContextCompat.getColor(getContext(), R.color.colorTextContent);
        this.precipitationTextColor = ContextCompat.getColor(getContext(), R.color.colorTextSubtitle);

        this.TREND_MARGIN_TOP = DisplayUtils.dpToPx(getContext(), (int) TREND_MARGIN_TOP);
        this.TREND_MARGIN_BOTTOM = DisplayUtils.dpToPx(getContext(), (int) TREND_MARGIN_BOTTOM);
        this.WEATHER_TEXT_SIZE = DisplayUtils.dpToPx(getContext(), (int) WEATHER_TEXT_SIZE);
        this.POP_TEXT_SIZE = DisplayUtils.dpToPx(getContext(), (int) POP_TEXT_SIZE);
        this.TREND_LINE_SIZE = DisplayUtils.dpToPx(getContext(), (int) TREND_LINE_SIZE);
        this.CHART_LINE_SIZE = DisplayUtils.dpToPx(getContext(), (int) CHART_LINE_SIZE);
        this.MARGIN_TEXT = DisplayUtils.dpToPx(getContext(), (int) MARGIN_TEXT);

        this.paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);

        this.path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        computeCoordinates();

        if (shader == null) {
            this.shader = new LinearGradient(
                    0, TREND_MARGIN_TOP,
                    0, getMeasuredHeight() - TREND_MARGIN_BOTTOM,
                    shadowColors[0], shadowColors[1],
                    Shader.TileMode.CLAMP);
        }

        drawTimeLine(canvas);
        if (precipitation > 5) {
            drawPrecipitationData(canvas);
        }
        if (maxiTempYs != null) {
            drawMaxiTemp(canvas);
        }
        if (maxiTempYs != null) {
            drawMiniTemp(canvas);
        }
    }

    private void drawTimeLine(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(CHART_LINE_SIZE);
        paint.setColor(lineColors[2]);
        canvas.drawLine(
                (float) (getMeasuredWidth() / 2.0), TREND_MARGIN_TOP,
                (float) (getMeasuredWidth() / 2.0), getMeasuredHeight() - TREND_MARGIN_BOTTOM,
                paint);
    }

    private void drawMaxiTemp(Canvas canvas) {
        if (maxiTempYs[0] != NONEXISTENT_VALUE && maxiTempYs[2] != NONEXISTENT_VALUE) {
            // shadow.
            paint.setShader(shader);
            paint.setStyle(Paint.Style.FILL);
            paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

            path.reset();
            path.moveTo(getRTLCompactX(0), maxiTempYs[0]);
            path.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), maxiTempYs[1]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), maxiTempYs[2]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), getMeasuredHeight() - TREND_MARGIN_BOTTOM);
            path.lineTo(getRTLCompactX(0), getMeasuredHeight() - TREND_MARGIN_BOTTOM);
            path.close();
            canvas.drawPath(path, paint);

            // line.
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(TREND_LINE_SIZE);
            paint.setColor(lineColors[0]);
            paint.setShadowLayer(2, 0, 2, shadowColors[2]);

            path.reset();
            path.moveTo(getRTLCompactX(0), maxiTempYs[0]);
            path.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), maxiTempYs[1]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), maxiTempYs[2]);
            canvas.drawPath(path, paint);
        } else if (maxiTempYs[0] == NONEXISTENT_VALUE) {
            // shadow.
            paint.setShader(shader);
            paint.setStyle(Paint.Style.FILL);
            paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

            path.reset();
            path.moveTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), maxiTempYs[1]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), maxiTempYs[2]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), getMeasuredHeight() - TREND_MARGIN_BOTTOM);
            path.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), getMeasuredHeight() - TREND_MARGIN_BOTTOM);
            path.close();
            canvas.drawPath(path, paint);

            // line.
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(TREND_LINE_SIZE);
            paint.setColor(lineColors[0]);
            paint.setShadowLayer(2, 0, 2, shadowColors[2]);

            path.reset();
            path.moveTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), maxiTempYs[1]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), maxiTempYs[2]);
            canvas.drawPath(path, paint);
        } else {
            // shadow.
            paint.setShader(shader);
            paint.setStyle(Paint.Style.FILL);
            paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

            path.reset();
            path.moveTo(getRTLCompactX(0), maxiTempYs[0]);
            path.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), maxiTempYs[1]);
            path.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), getMeasuredHeight() - TREND_MARGIN_BOTTOM);
            path.lineTo(getRTLCompactX(0), getMeasuredHeight() - TREND_MARGIN_BOTTOM);
            path.close();
            canvas.drawPath(path, paint);

            // line.
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(TREND_LINE_SIZE);
            paint.setColor(lineColors[0]);
            paint.setShadowLayer(2, 0, 2, shadowColors[2]);

            path.reset();
            path.moveTo(getRTLCompactX(0), maxiTempYs[0]);
            path.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), maxiTempYs[1]);
            canvas.drawPath(path, paint);
        }

        // text.
        paint.setColor(textColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(WEATHER_TEXT_SIZE);
        paint.setShadowLayer(2, 0, 2, shadowColors[2]);
        canvas.drawText(
                ValueUtils.buildAbbreviatedCurrentTemp((int) maxiTemps[1], GeometricWeather.getInstance().isFahrenheit()),
                getRTLCompactX((float) (getMeasuredWidth() / 2.0)),
                maxiTempYs[1] - paint.getFontMetrics().bottom - MARGIN_TEXT,
                paint);
    }

    private void drawMiniTemp(Canvas canvas) {
        if (miniTempYs[0] != NONEXISTENT_VALUE && miniTempYs[2] != NONEXISTENT_VALUE) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(TREND_LINE_SIZE);
            paint.setColor(lineColors[1]);
            paint.setShadowLayer(2, 0, 2, shadowColors[2]);

            path.reset();
            path.moveTo(getRTLCompactX(0), miniTempYs[0]);
            path.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), miniTempYs[1]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), miniTempYs[2]);
            canvas.drawPath(path, paint);
        } else if (miniTempYs[0] == NONEXISTENT_VALUE) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(TREND_LINE_SIZE);
            paint.setColor(lineColors[1]);
            paint.setShadowLayer(2, 0, 2, shadowColors[2]);

            path.reset();
            path.moveTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), miniTempYs[1]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), miniTempYs[2]);
            canvas.drawPath(path, paint);
        } else {
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(TREND_LINE_SIZE);
            paint.setColor(lineColors[1]);
            paint.setShadowLayer(2, 0, 2, shadowColors[2]);

            path.reset();
            path.moveTo(getRTLCompactX(0), miniTempYs[0]);
            path.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), miniTempYs[1]);
            canvas.drawPath(path, paint);
        }

        // text.
        paint.setColor(textColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(WEATHER_TEXT_SIZE);
        paint.setShadowLayer(2, 0, 2, shadowColors[2]);
        canvas.drawText(
                ValueUtils.buildAbbreviatedCurrentTemp((int) miniTemps[1], GeometricWeather.getInstance().isFahrenheit()),
                getRTLCompactX((float) (getMeasuredWidth() / 2.0)),
                miniTempYs[1] - paint.getFontMetrics().top + MARGIN_TEXT,
                paint);
    }

    private void drawPrecipitationData(Canvas canvas) {
        paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

        paint.setColor(lineColors[1]);
        paint.setAlpha((int) (255 * 0.1));
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRoundRect(
                new RectF(
                        (float) (getMeasuredWidth() / 2.0 - TREND_LINE_SIZE * 1.5),
                        precipitationY,
                        (float) (getMeasuredWidth() / 2.0 + TREND_LINE_SIZE * 1.5),
                        getMeasuredHeight() - TREND_MARGIN_BOTTOM),
                TREND_LINE_SIZE * 3, TREND_LINE_SIZE * 3,
                paint);

        paint.setColor(precipitationTextColor);
        paint.setAlpha(255);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(POP_TEXT_SIZE);
        canvas.drawText(
                precipitation + "%",
                (float) (getMeasuredWidth() / 2.0),
                (float) (getMeasuredHeight() - TREND_MARGIN_BOTTOM - paint.getFontMetrics().top + 2.0 * MARGIN_TEXT + WEATHER_TEXT_SIZE),
                paint);

        paint.setAlpha(255);
    }

    // control.

    public void setData(@NonNull float[] maxiTemps, @Nullable float[] miniTemps, int precipitation,
                        int highestTemp, int lowestTemp) {
        this.maxiTemps = maxiTemps;
        if (miniTemps != null) {
            this.miniTemps = miniTemps;
        } else {
            this.miniTemps = new float[] {NONEXISTENT_VALUE, NONEXISTENT_VALUE, NONEXISTENT_VALUE};
        }
        this.precipitation = precipitation;
        this.highestTemp = highestTemp;
        this.lowestTemp = lowestTemp;
        invalidate();
    }

    public void setLineColors(@ColorInt int c1, @ColorInt int c2) {
        this.lineColors = new int[] {
                c1, c2, ContextCompat.getColor(getContext(), R.color.colorLine)};
    }

    private void computeCoordinates() {
        for (int i = 0; i < maxiTemps.length; i ++) {
            if (maxiTemps[i] == NONEXISTENT_VALUE) {
                maxiTempYs[i] = NONEXISTENT_VALUE;
            } else {
                maxiTempYs[i] = computeSingleCoordinate(maxiTemps[i], highestTemp, lowestTemp);
            }
        }
        for (int i = 0; i < miniTemps.length; i ++) {
            if (miniTemps[i] == NONEXISTENT_VALUE) {
                miniTempYs[i] = NONEXISTENT_VALUE;
            } else {
                miniTempYs[i] = computeSingleCoordinate(miniTemps[i], highestTemp, lowestTemp);
            }
        }
        if (precipitation > 5) {
            precipitationY = computeSingleCoordinate(precipitation, 100, 0);
        }
    }

    private int computeSingleCoordinate(float value, float max, float min) {
        float canvasHeight = getMeasuredHeight() - TREND_MARGIN_TOP - TREND_MARGIN_BOTTOM;
        return (int) (getMeasuredHeight() - TREND_MARGIN_BOTTOM
                - canvasHeight * (value - min) / (max - min));
    }

    private float getRTLCompactX(float x) {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL ? (getMeasuredWidth() - x) : x;
    }
}
