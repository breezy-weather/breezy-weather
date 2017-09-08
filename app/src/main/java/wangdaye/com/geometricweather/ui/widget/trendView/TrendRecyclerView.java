package wangdaye.com.geometricweather.ui.widget.trendView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.widget.verticalScrollView.SwipeSwitchLayout;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;

/**
 * Trend recycler view.
 * */

public class TrendRecyclerView extends RecyclerView {

    private ViewParent switchView;

    private Paint paint;

    private int[] historyTemps;
    private int[] historyTempYs;

    private int highestTemp;
    private int lowestTemp;

    private float TREND_ITEM_HEIGHT;
    private float BOTTOM_MARGIN;
    private float TREND_MARGIN_TOP = 24;
    private float TREND_MARGIN_BOTTOM = 36;
    private float CHART_LINE_SIZE = 1;
    private float TEXT_SIZE = 10;
    private float MARGIN_TEXT = 2;

    public TrendRecyclerView(Context context) {
        super(context);
        this.initialize();
    }

    public TrendRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public TrendRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.initialize();
    }

    private void initialize() {
        this.switchView = null;

        setWillNotDraw(false);

        this.paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setTextSize(TEXT_SIZE);

        this.TREND_MARGIN_TOP = DisplayUtils.dpToPx(getContext(), (int) TREND_MARGIN_TOP);
        this.TREND_MARGIN_BOTTOM = DisplayUtils.dpToPx(getContext(), (int) TREND_MARGIN_BOTTOM);
        this.TEXT_SIZE = DisplayUtils.dpToPx(getContext(), (int) TEXT_SIZE);
        this.CHART_LINE_SIZE = DisplayUtils.dpToPx(getContext(), (int) CHART_LINE_SIZE);
        this.MARGIN_TEXT = DisplayUtils.dpToPx(getContext(), (int) MARGIN_TEXT);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = super.onInterceptTouchEvent(ev);

        ensureSwitchView(this);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (switchView != null) {
                    switchView.requestDisallowInterceptTouchEvent(true);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (switchView != null) {
                    switchView.requestDisallowInterceptTouchEvent(false);
                }
                break;
        }

        return result;
    }

    private void ensureSwitchView(View v) {
        if (switchView == null) {
            ViewParent parent = v.getParent();
            if (parent != null) {
                if (parent instanceof SwipeSwitchLayout) {
                    switchView = parent;
                } else {
                    ensureSwitchView((View) parent);
                }
            }
        }
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
        paint.setColor(ContextCompat.getColor(getContext(), R.color.colorLine));
        canvas.drawLine(
                0, historyTempYs[0],
                getMeasuredWidth(), historyTempYs[0],
                paint);
        canvas.drawLine(
                0, historyTempYs[1],
                getMeasuredWidth(), historyTempYs[1],
                paint);


        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(TEXT_SIZE);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.colorTextGrey2nd));
        canvas.drawText(
                ValueUtils.buildAbbreviatedCurrentTemp(historyTemps[0], GeometricWeather.getInstance().isFahrenheit()),
                2 * MARGIN_TEXT,
                historyTempYs[0] - paint.getFontMetrics().bottom - MARGIN_TEXT,
                paint);
        canvas.drawText(
                ValueUtils.buildAbbreviatedCurrentTemp(historyTemps[1], GeometricWeather.getInstance().isFahrenheit()),
                2 * MARGIN_TEXT,
                historyTempYs[1] - paint.getFontMetrics().top + MARGIN_TEXT,
                paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(
                getContext().getString(R.string.yesterday),
                getMeasuredWidth() - 2 * MARGIN_TEXT,
                historyTempYs[0] - paint.getFontMetrics().bottom - MARGIN_TEXT,
                paint);
        canvas.drawText(
                getContext().getString(R.string.yesterday),
                getMeasuredWidth() - 2 * MARGIN_TEXT,
                historyTempYs[1] - paint.getFontMetrics().top + MARGIN_TEXT,
                paint);
    }

    // control.

    public void setData(@Nullable int[] historyTemps, int highestTemp, int lowestTemp, boolean daily) {
        this.historyTemps = historyTemps;
        this.highestTemp = highestTemp;
        this.lowestTemp = lowestTemp;
        if (daily) {
            this.TREND_ITEM_HEIGHT = DisplayUtils.dpToPx(getContext(), 144);
            this.BOTTOM_MARGIN = DisplayUtils.dpToPx(getContext(), 48 + 16);
        } else {
            this.TREND_ITEM_HEIGHT = DisplayUtils.dpToPx(getContext(), 128);
            this.BOTTOM_MARGIN = DisplayUtils.dpToPx(getContext(), 16);
        }
        invalidate();
    }

    private void computeCoordinates() {
        historyTempYs = new int[] {
                computeSingleCoordinate(historyTemps[0], highestTemp, lowestTemp),
                computeSingleCoordinate(historyTemps[1], highestTemp, lowestTemp)};
    }

    private int computeSingleCoordinate(float value, float max, float min) {
        float canvasHeight = TREND_ITEM_HEIGHT - TREND_MARGIN_TOP - TREND_MARGIN_BOTTOM;
        return (int) (getMeasuredHeight() - BOTTOM_MARGIN - TREND_MARGIN_BOTTOM
                - canvasHeight * (value - min) / (max - min));
    }
}
