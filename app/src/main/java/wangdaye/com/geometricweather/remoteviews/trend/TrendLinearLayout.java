package wangdaye.com.geometricweather.remoteviews.trend;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Temperature;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Trend linear layout.
 * */

public class TrendLinearLayout extends LinearLayout {

    private Paint paint;

    private int[] historyTemps;
    private int[] historyTempYs;

    private int highestTemp;
    private int lowestTemp;
    private TemperatureUnit unit;

    @ColorInt private int lineColor;
    @ColorInt private int textColor;

    private float TREND_ITEM_HEIGHT;
    private float BOTTOM_MARGIN;
    private float TREND_MARGIN_TOP = 24;
    private float TREND_MARGIN_BOTTOM = 36;
    private float CHART_LINE_SIZE = 1;
    private float TEXT_SIZE = 10;
    private float MARGIN_TEXT = 2;

    public TrendLinearLayout(Context context) {
        super(context);
        this.initialize();
    }

    public TrendLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public TrendLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.initialize();
    }

    private void initialize() {
        setWillNotDraw(false);

        this.paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setTextSize(TEXT_SIZE);

        this.unit = TemperatureUnit.C;

        setColor(true);

        this.TREND_MARGIN_TOP = DisplayUtils.dpToPx(getContext(), (int) TREND_MARGIN_TOP);
        this.TREND_MARGIN_BOTTOM = DisplayUtils.dpToPx(getContext(), (int) TREND_MARGIN_BOTTOM);
        this.TEXT_SIZE = DisplayUtils.dpToPx(getContext(), (int) TEXT_SIZE);
        this.CHART_LINE_SIZE = DisplayUtils.dpToPx(getContext(), (int) CHART_LINE_SIZE);
        this.MARGIN_TEXT = DisplayUtils.dpToPx(getContext(), (int) MARGIN_TEXT);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (historyTemps == null) {
            return;
        }

        computeCoordinates();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(CHART_LINE_SIZE);
        paint.setColor(lineColor);
        canvas.drawLine(
                0, historyTempYs[0],
                getMeasuredWidth(), historyTempYs[0],
                paint
        );
        canvas.drawLine(
                0, historyTempYs[1],
                getMeasuredWidth(), historyTempYs[1],
                paint
        );


        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(TEXT_SIZE);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(textColor);
        canvas.drawText(
                Temperature.getShortTemperature(historyTemps[0], unit),
                2 * MARGIN_TEXT,
                historyTempYs[0] - paint.getFontMetrics().bottom - MARGIN_TEXT,
                paint
        );
        canvas.drawText(
                Temperature.getShortTemperature(historyTemps[1], unit),
                2 * MARGIN_TEXT,
                historyTempYs[1] - paint.getFontMetrics().top + MARGIN_TEXT,
                paint
        );

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(
                getContext().getString(R.string.yesterday),
                getMeasuredWidth() - 2 * MARGIN_TEXT,
                historyTempYs[0] - paint.getFontMetrics().bottom - MARGIN_TEXT,
                paint
        );
        canvas.drawText(
                getContext().getString(R.string.yesterday),
                getMeasuredWidth() - 2 * MARGIN_TEXT,
                historyTempYs[1] - paint.getFontMetrics().top + MARGIN_TEXT,
                paint
        );
    }

    // control.

    public void setColor(boolean lightTheme) {
        if (lightTheme) {
            lineColor = ColorUtils.setAlphaComponent(Color.BLACK, (int) (255 * 0.05));
            textColor = ContextCompat.getColor(getContext(), R.color.colorTextSubtitle_light);
        } else {
            lineColor = ColorUtils.setAlphaComponent(Color.WHITE, (int) (255 * 0.1));
            textColor = ContextCompat.getColor(getContext(), R.color.colorTextSubtitle_dark);
        }
    }

    public void setData(@Nullable int[] historyTemps,
                        int highestTemp, int lowestTemp, TemperatureUnit unit,
                        boolean daily) {
        this.historyTemps = historyTemps;
        this.highestTemp = highestTemp;
        this.lowestTemp = lowestTemp;
        this.unit = unit;
        if (daily) {
            this.TREND_ITEM_HEIGHT = DisplayUtils.dpToPx(
                    getContext(), WidgetItemView.TREND_VIEW_HEIGHT_DIP_2X);
            this.BOTTOM_MARGIN = DisplayUtils.dpToPx(
                    getContext(),
                    WidgetItemView.ICON_SIZE_DIP
                            + WidgetItemView.ICON_MARGIN_DIP
                            + WidgetItemView.MARGIN_VERTICAL_DIP
            );
        } else {
            this.TREND_ITEM_HEIGHT = DisplayUtils.dpToPx(
                    getContext(), WidgetItemView.TREND_VIEW_HEIGHT_DIP_1X);
            this.BOTTOM_MARGIN = DisplayUtils.dpToPx(
                    getContext(), WidgetItemView.MARGIN_VERTICAL_DIP);
        }
        invalidate();
    }

    private void computeCoordinates() {
        historyTempYs = new int[] {
                computeSingleCoordinate(historyTemps[0], highestTemp, lowestTemp),
                computeSingleCoordinate(historyTemps[1], highestTemp, lowestTemp)
        };
    }

    private int computeSingleCoordinate(float value, float max, float min) {
        float canvasHeight = TREND_ITEM_HEIGHT - TREND_MARGIN_TOP - TREND_MARGIN_BOTTOM;
        return (int) (
                getMeasuredHeight() - BOTTOM_MARGIN - TREND_MARGIN_BOTTOM
                        - canvasHeight * (value - min) / (max - min)
        );
    }
}
