package wangdaye.com.geometricweather.ui.widget.trendView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Temperature;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Trend recycler view.
 * */

public class TrendRecyclerView extends RecyclerView {

    private Paint paint;
    @ColorInt private int lineColor;

    private int[] historyTemps;
    private int[] historyTempYs;

    private int highestTemp;
    private int lowestTemp;
    private TemperatureUnit unit;

    private int pointerId;
    private float initialX;
    private float initialY;
    private int touchSlop;
    private boolean isBeingDragged;
    private boolean isHorizontalDragged;

    private float TREND_ITEM_HEIGHT;
    private float BOTTOM_MARGIN;
    private float TREND_MARGIN_TOP = 24;
    private float TREND_MARGIN_BOTTOM = 36;
    private float CHART_LINE_SIZE = 1;
    private float TEXT_SIZE = 10;
    private float MARGIN_TEXT = 2;

    private static final String TAG = "TrendRecyclerView";

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
        setWillNotDraw(false);

        this.unit = TemperatureUnit.C;

        this.paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setTextSize(TEXT_SIZE);

        setLineColor(Color.GRAY);

        this.TREND_MARGIN_TOP = DisplayUtils.dpToPx(getContext(), (int) TREND_MARGIN_TOP);
        this.TREND_MARGIN_BOTTOM = DisplayUtils.dpToPx(getContext(), (int) TREND_MARGIN_BOTTOM);
        this.TEXT_SIZE = DisplayUtils.dpToPx(getContext(), (int) TEXT_SIZE);
        this.CHART_LINE_SIZE = DisplayUtils.dpToPx(getContext(), (int) CHART_LINE_SIZE);
        this.MARGIN_TEXT = DisplayUtils.dpToPx(getContext(), (int) MARGIN_TEXT);

        this.touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isBeingDragged = false;
                isHorizontalDragged = false;

                pointerId = ev.getPointerId(0);
                initialX = ev.getX();
                initialY = ev.getY();
                break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                int index = ev.getActionIndex();
                pointerId = ev.getPointerId(index);
                initialX = ev.getX(index);
                initialY = ev.getY(index);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int index = ev.findPointerIndex(pointerId);
                if (index == -1) {
                    Log.e(TAG, "Invalid pointerId=" + pointerId + " in onTouchEvent");
                    break;
                }

                float x = ev.getX(index);
                float y = ev.getY(index);

                if (!isBeingDragged && !isHorizontalDragged) {
                    if (Math.abs(x - initialX) > touchSlop || Math.abs(y - initialY) > touchSlop) {
                        isBeingDragged = true;
                        if (Math.abs(x - initialX) > Math.abs(y - initialY)) {
                            isHorizontalDragged = true;
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int index = ev.getActionIndex();
                int id = ev.getPointerId(index);
                if (pointerId == id) {
                    int newIndex = index == 0 ? 1 : 0;

                    this.pointerId = ev.getPointerId(newIndex);
                    initialX = (int) ev.getX(newIndex);
                    initialY = (int) ev.getY(newIndex);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isBeingDragged = false;
                isHorizontalDragged = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        return super.onInterceptTouchEvent(ev) && isBeingDragged && isHorizontalDragged;
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
        paint.setColor(ContextCompat.getColor(getContext(), R.color.colorTextGrey2nd));
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

    public void setData(@Nullable int[] historyTemps,
                        int highestTemp, int lowestTemp, TemperatureUnit unit,
                        boolean daily) {
        this.historyTemps = historyTemps;
        this.highestTemp = highestTemp;
        this.lowestTemp = lowestTemp;
        this.unit = unit;
        if (daily) {
            this.TREND_ITEM_HEIGHT = DisplayUtils.dpToPx(getContext(), 144);
            this.BOTTOM_MARGIN = DisplayUtils.dpToPx(getContext(), 48 + 16);
        } else {
            this.TREND_ITEM_HEIGHT = DisplayUtils.dpToPx(getContext(), 128);
            this.BOTTOM_MARGIN = DisplayUtils.dpToPx(getContext(), 16);
        }
        invalidate();
    }

    public void setLineColor(@ColorInt int lineColor) {
        this.lineColor = lineColor;
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
