package wangdaye.com.geometricweather.ui.widget.trendView.overview;

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

    private Float[] daytimeTemperatureC = new Float[3];
    private Float[] nighttimeTemperatureC = new Float[3];
    private int highestTemperatureC;
    private int lowestTemperatureC;
    private @Nullable Float precipitation;
    private TemperatureUnit unit;

    private int[] daytimeY = new int[3];
    private int[] nighttimeY = new int[3];
    private int precipitationY;

    private int marginTop;
    private int marginBottom;
    private int temperatureTextSize;
    private int precipitationTextSize;
    private int trendLineWidth;
    private int precipitationPillarWidth;
    private int chartLineWith;
    private int textMargin;

    private int[] lineColors;
    private int[] shadowColors;
    private int textColor;
    private int textShadowColor;
    private int precipitationTextColor;

    private float precipitationAlpha;

    private static final float MARGIN_TOP_DIP = 24;
    private static final float MARGIN_BOTTOM_DIP = 36;
    private static final float TEMPERATURE_TEXT_SIZE_DIP = 13;
    private static final float PRECIPITATION_TEXT_SIZE_DIP = 11;
    private static final float TREND_LINE_SIZE_DIP = 3;
    private static final float PRECIPITATION_PILLAR_WIDTH_DIP = 3;
    private static final float CHART_LINE_SIZE_DIP = 1;
    private static final float TEXT_MARGIN_DIP = 2;

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

        this.marginTop = (int) DisplayUtils.dpToPx(getContext(), (int) MARGIN_TOP_DIP);
        this.marginBottom = (int) DisplayUtils.dpToPx(getContext(), (int) MARGIN_BOTTOM_DIP);
        this.temperatureTextSize = (int) DisplayUtils.dpToPx(getContext(), (int) TEMPERATURE_TEXT_SIZE_DIP);
        this.precipitationTextSize = (int) DisplayUtils.dpToPx(getContext(), (int) PRECIPITATION_TEXT_SIZE_DIP);
        this.trendLineWidth = (int) DisplayUtils.dpToPx(getContext(), (int) TREND_LINE_SIZE_DIP);
        this.precipitationPillarWidth = (int) DisplayUtils.dpToPx(getContext(), (int) PRECIPITATION_PILLAR_WIDTH_DIP);
        this.chartLineWith = (int) DisplayUtils.dpToPx(getContext(), (int) CHART_LINE_SIZE_DIP);
        this.textMargin = (int) DisplayUtils.dpToPx(getContext(), (int) TEXT_MARGIN_DIP);

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

        if (precipitation != null && precipitation > 5) {
            drawPrecipitationData(canvas);
        }
        if (daytimeTemperatureC != null) {
            drawMaxiTemperature(canvas);
        }
        if (nighttimeTemperatureC != null) {
            drawMiniTemperature(canvas);
        }
    }

    private void drawTimeLine(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(chartLineWith);
        paint.setColor(lineColors[2]);
        paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

        canvas.drawLine(
                getMeasuredWidth() / 2.f, marginTop,
                getMeasuredWidth() / 2.f, getMeasuredHeight() - marginBottom,
                paint
        );
    }

    private void drawMaxiTemperature(Canvas canvas) {
        if (daytimeTemperatureC[0] != null && daytimeTemperatureC[2] != null) {
            // shadow.
            paint.setColor(Color.BLACK);
            paint.setShader(shaderWrapper.getShader());
            paint.setStyle(Paint.Style.FILL);
            paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

            path.reset();
            path.moveTo(getRTLCompactX(0), daytimeY[0]);
            path.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), daytimeY[1]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), daytimeY[2]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), getMeasuredHeight() - marginBottom);
            path.lineTo(getRTLCompactX(0), getMeasuredHeight() - marginBottom);
            path.close();
            canvas.drawPath(path, paint);

            // line.
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(trendLineWidth);
            paint.setColor(lineColors[0]);
            paint.setShadowLayer(1, 0, 1, shadowColors[2]);

            path.reset();
            path.moveTo(getRTLCompactX(0), daytimeY[0]);
            path.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), daytimeY[1]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), daytimeY[2]);
            canvas.drawPath(path, paint);
        } else if (daytimeTemperatureC[0] == null) {
            // shadow.
            paint.setColor(Color.BLACK);
            paint.setShader(shaderWrapper.getShader());
            paint.setStyle(Paint.Style.FILL);
            paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

            path.reset();
            path.moveTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), daytimeY[1]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), daytimeY[2]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), getMeasuredHeight() - marginBottom);
            path.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), getMeasuredHeight() - marginBottom);
            path.close();
            canvas.drawPath(path, paint);

            // line.
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(trendLineWidth);
            paint.setColor(lineColors[0]);
            paint.setShadowLayer(1, 0, 1, shadowColors[2]);

            path.reset();
            path.moveTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), daytimeY[1]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), daytimeY[2]);
            canvas.drawPath(path, paint);
        } else {
            // shadow.
            paint.setColor(Color.BLACK);
            paint.setShader(shaderWrapper.getShader());
            paint.setStyle(Paint.Style.FILL);
            paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

            path.reset();
            path.moveTo(getRTLCompactX(0), daytimeY[0]);
            path.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), daytimeY[1]);
            path.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), getMeasuredHeight() - marginBottom);
            path.lineTo(getRTLCompactX(0), getMeasuredHeight() - marginBottom);
            path.close();
            canvas.drawPath(path, paint);

            // line.
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(trendLineWidth);
            paint.setColor(lineColors[0]);
            paint.setShadowLayer(1, 0, 1, shadowColors[2]);

            path.reset();
            path.moveTo(getRTLCompactX(0), daytimeY[0]);
            path.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), daytimeY[1]);
            canvas.drawPath(path, paint);
        }

        // text.
        paint.setColor(textColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(temperatureTextSize);
        paint.setShadowLayer(2, 0, 1, textShadowColor);
        canvas.drawText(
                Temperature.getShortTemperature(toInt(daytimeTemperatureC[1]), unit),
                getRTLCompactX((float) (getMeasuredWidth() / 2.0)),
                daytimeY[1] - paint.getFontMetrics().bottom - textMargin,
                paint
        );
    }

    private void drawMiniTemperature(Canvas canvas) {
        if (nighttimeTemperatureC[0] != null && nighttimeTemperatureC[2] != null) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(trendLineWidth);
            paint.setColor(lineColors[1]);
            paint.setShadowLayer(1, 0, 1, shadowColors[2]);

            path.reset();
            path.moveTo(getRTLCompactX(0), nighttimeY[0]);
            path.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), nighttimeY[1]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), nighttimeY[2]);
            canvas.drawPath(path, paint);
        } else if (nighttimeTemperatureC[0] == null) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(trendLineWidth);
            paint.setColor(lineColors[1]);
            paint.setShadowLayer(1, 0, 1, shadowColors[2]);

            path.reset();
            path.moveTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), nighttimeY[1]);
            path.lineTo(getRTLCompactX(getMeasuredWidth()), nighttimeY[2]);
            canvas.drawPath(path, paint);
        } else {
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(trendLineWidth);
            paint.setColor(lineColors[1]);
            paint.setShadowLayer(1, 0, 1, shadowColors[2]);

            path.reset();
            path.moveTo(getRTLCompactX(0), nighttimeY[0]);
            path.lineTo(getRTLCompactX((float) (getMeasuredWidth() / 2.0)), nighttimeY[1]);
            canvas.drawPath(path, paint);
        }

        // text.
        paint.setColor(textColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(temperatureTextSize);
        paint.setShadowLayer(2, 0, 1, textShadowColor);
        canvas.drawText(
                Temperature.getShortTemperature(toInt(nighttimeTemperatureC[1]), unit),
                getRTLCompactX((float) (getMeasuredWidth() / 2.0)),
                nighttimeY[1] - paint.getFontMetrics().top + textMargin,
                paint
        );
    }

    private int toInt(Float f) {
        float v = f;
        return (int) v;
    }

    private void drawPrecipitationData(Canvas canvas) {
        paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

        paint.setColor(lineColors[1]);
        paint.setAlpha((int) (255 * precipitationAlpha));
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRoundRect(
                new RectF(
                        (float) (getMeasuredWidth() / 2.0 - precipitationPillarWidth),
                        precipitationY,
                        (float) (getMeasuredWidth() / 2.0 + precipitationPillarWidth),
                        getMeasuredHeight() - marginBottom
                ),
                precipitationPillarWidth, precipitationPillarWidth,
                paint
        );

        paint.setColor(precipitationTextColor);
        paint.setAlpha(255);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(precipitationTextSize);
        canvas.drawText(
                precipitation + "%",
                (float) (getMeasuredWidth() / 2.0),
                (float) (
                        getMeasuredHeight()
                                - marginBottom
                                - paint.getFontMetrics().top
                                + 2.0 * textMargin
                                + temperatureTextSize
                ), paint
        );

        paint.setAlpha(255);
    }

    // control.

    public void setData(@NonNull @Size(3) Float[] daytimeTemperatureC,
                        @Nullable @Size(3) Float[] nighttimeTemperatureC,
                        @Nullable Float precipitation,
                        int highestTemperatureC, int lowestTemperatureC, TemperatureUnit unit) {
        this.daytimeTemperatureC = daytimeTemperatureC;
        this.nighttimeTemperatureC = nighttimeTemperatureC;
        this.precipitation = precipitation;
        this.highestTemperatureC = highestTemperatureC;
        this.lowestTemperatureC = lowestTemperatureC;
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
                            0, marginTop,
                            0, getMeasuredHeight() - marginBottom,
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
        float canvasHeight = getMeasuredHeight() - marginTop - marginBottom;
        for (int i = 0; i < daytimeTemperatureC.length; i ++) {
            if (daytimeTemperatureC[i] == null) {
                daytimeY[i] = 0;
            } else {
                daytimeY[i] = computeSingleCoordinate(
                        canvasHeight, daytimeTemperatureC[i], highestTemperatureC, lowestTemperatureC);
            }
        }
        if (nighttimeTemperatureC != null) {
            for (int i = 0; i < nighttimeTemperatureC.length; i ++) {
                if (nighttimeTemperatureC[i] == null) {
                    nighttimeY[i] = 0;
                } else {
                    nighttimeY[i] = computeSingleCoordinate(
                            canvasHeight, nighttimeTemperatureC[i], highestTemperatureC, lowestTemperatureC);
                }
            }
        }
        if (precipitation != null && precipitation > 5) {
            precipitationY = computeSingleCoordinate(
                    canvasHeight, precipitation, 100, 0);
        }
    }

    private int computeSingleCoordinate(float canvasHeight, float value, float max, float min) {
        return (int) (
                getMeasuredHeight()
                        - marginBottom
                        - canvasHeight * (value - min) / (max - min)
        );
    }

    private float getRTLCompactX(float x) {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL ? (getMeasuredWidth() - x) : x;
    }

    public int getMarginTop() {
        return marginTop;
    }

    public int getMarginBottom() {
        return marginBottom;
    }
}
