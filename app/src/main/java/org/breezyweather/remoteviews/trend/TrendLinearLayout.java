package org.breezyweather.remoteviews.trend;

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

import org.breezyweather.common.basic.models.options.unit.TemperatureUnit;
import org.breezyweather.common.basic.models.weather.Temperature;
import org.breezyweather.R;
import org.breezyweather.common.extensions.DisplayExtensionsKt;
import org.breezyweather.common.utils.DisplayUtils;

/**
 * Trend linear layout.
 * */

public class TrendLinearLayout extends LinearLayout {

    private Paint mPaint;

    private Integer[] mHistoryTemps;
    private Integer[] mHistoryTempYs;

    private Integer mHighestTemp;
    private Integer mLowestTemp;
    private TemperatureUnit mTemperatureUnit;

    @ColorInt private int mLineColor;
    @ColorInt private int mTextColor;

    private float TREND_ITEM_HEIGHT;
    private float BOTTOM_MARGIN;
    private float TREND_MARGIN_TOP = 24;
    private float TREND_MARGIN_BOTTOM = 36;
    private float CHART_LINE_SIZE = 1;
    private float TEXT_SIZE = 12;
    private float MARGIN_TEXT = 2;

    public TrendLinearLayout(Context context) {
        this(context, null);
    }

    public TrendLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TrendLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setTypeface(
                DisplayUtils.getTypefaceFromTextAppearance(getContext(), R.style.subtitle_text)
        );
        mPaint.setTextSize(TEXT_SIZE);

        mTemperatureUnit = TemperatureUnit.C;

        setColor(true);

        TREND_MARGIN_TOP = DisplayExtensionsKt.dpToPx(getContext(), (int) TREND_MARGIN_TOP);
        TREND_MARGIN_BOTTOM = DisplayExtensionsKt.dpToPx(getContext(), (int) TREND_MARGIN_BOTTOM);
        TEXT_SIZE = DisplayExtensionsKt.dpToPx(getContext(), (int) TEXT_SIZE);
        CHART_LINE_SIZE = DisplayExtensionsKt.dpToPx(getContext(), (int) CHART_LINE_SIZE);
        MARGIN_TEXT = DisplayExtensionsKt.dpToPx(getContext(), (int) MARGIN_TEXT);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mHistoryTemps == null) {
            return;
        }

        computeCoordinates();

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(CHART_LINE_SIZE);
        mPaint.setColor(mLineColor);
        canvas.drawLine(
                0, mHistoryTempYs[0],
                getMeasuredWidth(), mHistoryTempYs[0],
                mPaint
        );
        canvas.drawLine(
                0, mHistoryTempYs[1],
                getMeasuredWidth(), mHistoryTempYs[1],
                mPaint
        );


        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(TEXT_SIZE);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setColor(mTextColor);
        canvas.drawText(
                Temperature.getShortTemperature(getContext(), mHistoryTemps[0], mTemperatureUnit),
                2 * MARGIN_TEXT,
                mHistoryTempYs[0] - mPaint.getFontMetrics().bottom - MARGIN_TEXT,
                mPaint
        );
        canvas.drawText(
                Temperature.getShortTemperature(getContext(), mHistoryTemps[1], mTemperatureUnit),
                2 * MARGIN_TEXT,
                mHistoryTempYs[1] - mPaint.getFontMetrics().top + MARGIN_TEXT,
                mPaint
        );

        mPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(
                getContext().getString(R.string.short_yesterday),
                getMeasuredWidth() - 2 * MARGIN_TEXT,
                mHistoryTempYs[0] - mPaint.getFontMetrics().bottom - MARGIN_TEXT,
                mPaint
        );
        canvas.drawText(
                getContext().getString(R.string.short_yesterday),
                getMeasuredWidth() - 2 * MARGIN_TEXT,
                mHistoryTempYs[1] - mPaint.getFontMetrics().top + MARGIN_TEXT,
                mPaint
        );
    }

    // control.

    public void setColor(boolean lightTheme) {
        if (lightTheme) {
            mLineColor = ColorUtils.setAlphaComponent(Color.BLACK, (int) (255 * 0.05));
            mTextColor = ContextCompat.getColor(getContext(), R.color.colorTextGrey2nd);
        } else {
            mLineColor = ColorUtils.setAlphaComponent(Color.WHITE, (int) (255 * 0.1));
            mTextColor = ContextCompat.getColor(getContext(), R.color.colorTextGrey);
        }
    }

    public void setData(Integer[] historyTemps,
                        Integer highestTemp, Integer lowestTemp, TemperatureUnit unit,
                        boolean daily) {
        mHistoryTemps = historyTemps;
        mHighestTemp = highestTemp;
        mLowestTemp = lowestTemp;
        mTemperatureUnit = unit;
        if (daily) {
            TREND_ITEM_HEIGHT = DisplayExtensionsKt.dpToPx(
                    getContext(), WidgetItemView.TREND_VIEW_HEIGHT_DIP_2X);
            BOTTOM_MARGIN = DisplayExtensionsKt.dpToPx(
                    getContext(),
                    WidgetItemView.ICON_SIZE_DIP
                            + WidgetItemView.ICON_MARGIN_DIP
                            + WidgetItemView.MARGIN_VERTICAL_DIP
            );
        } else {
            TREND_ITEM_HEIGHT = DisplayExtensionsKt.dpToPx(
                    getContext(), WidgetItemView.TREND_VIEW_HEIGHT_DIP_1X);
            BOTTOM_MARGIN = DisplayExtensionsKt.dpToPx(
                    getContext(), WidgetItemView.MARGIN_VERTICAL_DIP);
        }
        invalidate();
    }

    private void computeCoordinates() {
        mHistoryTempYs = new Integer[] {
                computeSingleCoordinate(mHistoryTemps[0], mHighestTemp, mLowestTemp),
                computeSingleCoordinate(mHistoryTemps[1], mHighestTemp, mLowestTemp)
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
