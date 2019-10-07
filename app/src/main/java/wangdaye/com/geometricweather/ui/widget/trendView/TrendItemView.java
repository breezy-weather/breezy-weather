package wangdaye.com.geometricweather.ui.widget.trendView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.core.graphics.ColorUtils;

import android.util.AttributeSet;
import android.view.View;

import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Temperature;
import wangdaye.com.geometricweather.ui.widget.DayNightShaderWrapper;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Trend item view.
 * */

public class TrendItemView extends View {

    private Paint paint;
    private Path path;
    private DayNightShaderWrapper shaderWrapper;

    private float[] maxiTemps = new float[3];
    private float[] miniTemps = new float[3];
    private int highestTemp;
    private int lowestTemp;
    private int precipitation;
    private TemperatureUnit unit;

    private int[] maxiTempYs = new int[3];
    private int[] miniTempYs = new int[3];
    private int precipitationY;

    private int[] lineColors;
    private int[] shadowColors;
    private int textColor;
    private int textShadowColor;
    private int precipitationTextColor;

    private float precipitationAlpha;

    public static final int NONEXISTENT_VALUE = Integer.MAX_VALUE;

    private float TREND_MARGIN_TOP = 24;
    private float TREND_MARGIN_BOTTOM = 36;
    private float WEATHER_TEXT_SIZE = 13;
    private float POP_TEXT_SIZE = 11;
    private float TREND_LINE_SIZE = 3;
    private float PRECIPITATION_BAR_SIZE = 3;
    private float CHART_LINE_SIZE = 1;
    private float MARGIN_TEXT = 2;

    private static final float SHADOW_ALPHA_FACTOR_LIGHT = 0.1f;
    private static final float SHADOW_ALPHA_FACTOR_DARK = 0.1f;

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
        lineColors = new int[] {Color.BLACK, Color.DKGRAY, Color.LTGRAY};
        shadowColors = new int[] {Color.BLACK, Color.WHITE, Color.GRAY};

        setTextColors(Color.BLACK, Color.GRAY);
        setPrecipitationAlpha(0.33f);

        this.TREND_MARGIN_TOP = DisplayUtils.dpToPx(getContext(), (int) TREND_MARGIN_TOP);
        this.TREND_MARGIN_BOTTOM = DisplayUtils.dpToPx(getContext(), (int) TREND_MARGIN_BOTTOM);
        this.WEATHER_TEXT_SIZE = DisplayUtils.dpToPx(getContext(), (int) WEATHER_TEXT_SIZE);
        this.POP_TEXT_SIZE = DisplayUtils.dpToPx(getContext(), (int) POP_TEXT_SIZE);
        this.TREND_LINE_SIZE = DisplayUtils.dpToPx(getContext(), (int) TREND_LINE_SIZE);
        this.PRECIPITATION_BAR_SIZE = DisplayUtils.dpToPx(getContext(), (int) PRECIPITATION_BAR_SIZE);
        this.CHART_LINE_SIZE = DisplayUtils.dpToPx(getContext(), (int) CHART_LINE_SIZE);
        this.MARGIN_TEXT = DisplayUtils.dpToPx(getContext(), (int) MARGIN_TEXT);

        this.paint = new Paint();
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);

        this.path = new Path();
        this.shaderWrapper = new DayNightShaderWrapper(getMeasuredWidth(), getMeasuredHeight());
        setShadowColors(Color.BLACK, Color.GRAY, true);

        this.unit = TemperatureUnit.C;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        ensureShader(shaderWrapper.isLightTheme());
        computeCoordinates();

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
        paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

        canvas.drawLine(
                getMeasuredWidth() / 2.f, TREND_MARGIN_TOP,
                getMeasuredWidth() / 2.f, getMeasuredHeight() - TREND_MARGIN_BOTTOM,
                paint
        );
    }

    private void drawMaxiTemp(Canvas canvas) {
        if (maxiTempYs[0] != NONEXISTENT_VALUE && maxiTempYs[2] != NONEXISTENT_VALUE) {
            // shadow.
            paint.setColor(Color.BLACK);
            paint.setShader(shaderWrapper.getShader());
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
            paint.setShadowLayer(1, 0, 1, shadowColors[2]);

            path.reset();
            path.moveTo(getRTLCompactX(0), maxiTempYs[0]);
            path.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), maxiTempYs[1]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), maxiTempYs[2]);
            canvas.drawPath(path, paint);
        } else if (maxiTempYs[0] == NONEXISTENT_VALUE) {
            // shadow.
            paint.setColor(Color.BLACK);
            paint.setShader(shaderWrapper.getShader());
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
            paint.setShadowLayer(1, 0, 1, shadowColors[2]);

            path.reset();
            path.moveTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), maxiTempYs[1]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), maxiTempYs[2]);
            canvas.drawPath(path, paint);
        } else {
            // shadow.
            paint.setColor(Color.BLACK);
            paint.setShader(shaderWrapper.getShader());
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
            paint.setShadowLayer(1, 0, 1, shadowColors[2]);

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
        paint.setShadowLayer(2, 0, 1, textShadowColor);
        canvas.drawText(
                Temperature.getShortTemperature((int) maxiTemps[1], unit),
                getRTLCompactX((float) (getMeasuredWidth() / 2.0)),
                maxiTempYs[1] - paint.getFontMetrics().bottom - MARGIN_TEXT,
                paint
        );
    }

    private void drawMiniTemp(Canvas canvas) {
        if (miniTempYs[0] != NONEXISTENT_VALUE && miniTempYs[2] != NONEXISTENT_VALUE) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(TREND_LINE_SIZE);
            paint.setColor(lineColors[1]);
            paint.setShadowLayer(1, 0, 1, shadowColors[2]);

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
            paint.setShadowLayer(1, 0, 1, shadowColors[2]);

            path.reset();
            path.moveTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), miniTempYs[1]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), miniTempYs[2]);
            canvas.drawPath(path, paint);
        } else {
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(TREND_LINE_SIZE);
            paint.setColor(lineColors[1]);
            paint.setShadowLayer(1, 0, 1, shadowColors[2]);

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
        paint.setShadowLayer(2, 0, 1, textShadowColor);
        canvas.drawText(
                Temperature.getShortTemperature((int) miniTemps[1], unit),
                getRTLCompactX((float) (getMeasuredWidth() / 2.0)),
                miniTempYs[1] - paint.getFontMetrics().top + MARGIN_TEXT,
                paint
        );
    }

    private void drawPrecipitationData(Canvas canvas) {
        paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

        paint.setColor(lineColors[1]);
        paint.setAlpha((int) (255 * precipitationAlpha));
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRoundRect(
                new RectF(
                        (float) (getMeasuredWidth() / 2.0 - PRECIPITATION_BAR_SIZE),
                        precipitationY,
                        (float) (getMeasuredWidth() / 2.0 + PRECIPITATION_BAR_SIZE),
                        getMeasuredHeight() - TREND_MARGIN_BOTTOM
                ),
                PRECIPITATION_BAR_SIZE, PRECIPITATION_BAR_SIZE,
                paint
        );

        paint.setColor(precipitationTextColor);
        paint.setAlpha(255);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(POP_TEXT_SIZE);
        canvas.drawText(
                precipitation + "%",
                (float) (getMeasuredWidth() / 2.0),
                (float) (
                        getMeasuredHeight()
                                - TREND_MARGIN_BOTTOM
                                - paint.getFontMetrics().top
                                + 2.0 * MARGIN_TEXT
                                + WEATHER_TEXT_SIZE
                ), paint
        );

        paint.setAlpha(255);
    }

    // control.

    public void setData(@NonNull @Size(3) float[] maxiTemps, @Nullable @Size(3) float[] miniTemps,
                        int precipitation, int highestTemp, int lowestTemp, TemperatureUnit unit) {
        this.maxiTemps = maxiTemps;
        if (miniTemps != null) {
            this.miniTemps = miniTemps;
        } else {
            this.miniTemps = new float[] {NONEXISTENT_VALUE, NONEXISTENT_VALUE, NONEXISTENT_VALUE};
        }
        this.precipitation = precipitation;
        this.highestTemp = highestTemp;
        this.lowestTemp = lowestTemp;
        this.unit = unit;
        invalidate();
    }

    public void setLineColors(@ColorInt int colorDay, @ColorInt int colorNight,
                              @ColorInt int colorSubLine) {
        lineColors[0] = colorDay;
        lineColors[1] = colorNight;
        lineColors[2] = colorSubLine;
        invalidate();
    }

    public void setShadowColors(@ColorInt int colorDay, @ColorInt int colorNight, boolean lightTheme) {
        shadowColors[0] = lightTheme
                ? ColorUtils.setAlphaComponent(colorDay, (int) (255 * SHADOW_ALPHA_FACTOR_LIGHT))
                : ColorUtils.setAlphaComponent(colorNight, (int) (255 * SHADOW_ALPHA_FACTOR_DARK));
        shadowColors[1] = Color.TRANSPARENT;
        shadowColors[2] = getDarkerColor(lightTheme ? colorDay : colorNight);

        ensureShader(lightTheme);
        invalidate();
    }

    private int getDarkerColor(@ColorInt int color){
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = hsv[1] + 0.15f;
        hsv[2] = hsv[2] - 0.15f;
        return Color.HSVToColor(hsv);
    }

    public void setTextColors(@ColorInt int textColor, @ColorInt int precipitationTextColor) {
        this.textColor = textColor;
        this.textShadowColor = Color.argb((int) (255 * 0.2), 0, 0, 0);
        this.precipitationTextColor = precipitationTextColor;
        invalidate();
    }

    public void setPrecipitationAlpha(@FloatRange(from = 0, to = 1) float precipitationAlpha) {
        this.precipitationAlpha = precipitationAlpha;
        invalidate();
    }

    private void ensureShader(boolean lightTheme) {
        if (shaderWrapper.isDifferent(
                getMeasuredWidth(), getMeasuredHeight(), lightTheme, shadowColors)) {
            shaderWrapper.setShader(
                    new LinearGradient(
                            0, TREND_MARGIN_TOP,
                            0, getMeasuredHeight() - TREND_MARGIN_BOTTOM,
                            shadowColors[0], shadowColors[1],
                            Shader.TileMode.CLAMP
                    ),
                    getMeasuredWidth(), getMeasuredHeight(),
                    lightTheme,
                    shadowColors
            );
        }
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

        return (int) (
                getMeasuredHeight()
                        - TREND_MARGIN_BOTTOM
                        - canvasHeight * (value - min) / (max - min)
        );
    }

    private float getRTLCompactX(float x) {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL ? (getMeasuredWidth() - x) : x;
    }
}
