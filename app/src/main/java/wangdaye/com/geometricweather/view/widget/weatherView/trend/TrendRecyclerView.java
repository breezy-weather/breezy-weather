package wangdaye.com.geometricweather.view.widget.weatherView.trend;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.History;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.view.widget.verticalScrollView.SwipeSwitchLayout;

/**
 * Trend recycler view.
 * */

public class TrendRecyclerView extends RecyclerView {
    // widget
    private SwipeSwitchLayout switchLayout;
    private Paint paint;

    // data
    private History history;
    private int[] tempYs;
    private boolean canScroll = false;

    private float MARGIN_BOTTOM;
    private float CHART_LINE_SIZE = 1;
    private float TEXT_SIZE = 10;
    private float MARGIN_TEXT = 2;

    /** <br> life cycle. */

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

        this.paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setTextSize(TEXT_SIZE);

        this.MARGIN_BOTTOM = TrendItemView.calcDrawSpecMarginBottom(getContext());
        this.TEXT_SIZE = DisplayUtils.dpToPx(getContext(), (int) TEXT_SIZE);
        this.CHART_LINE_SIZE = DisplayUtils.dpToPx(getContext(), (int) CHART_LINE_SIZE);
        this.MARGIN_TEXT = DisplayUtils.dpToPx(getContext(), (int) MARGIN_TEXT);
    }

    /** <br> touch. */

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (canScroll) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (switchLayout != null) {
                        switchLayout.requestDisallowInterceptTouchEvent(true);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (switchLayout != null) {
                        switchLayout.requestDisallowInterceptTouchEvent(false);
                    }
                    break;
            }
        }
        return super.onInterceptTouchEvent(e);
    }

    /** <br> UI. */

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (tempYs == null) {
            return;
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(CHART_LINE_SIZE);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.colorLine));
        canvas.drawLine(
                0, tempYs[0],
                getMeasuredWidth(), tempYs[0],
                paint);
        canvas.drawLine(
                0, tempYs[1],
                getMeasuredWidth(), tempYs[1],
                paint);


        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(TEXT_SIZE);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.colorTextGrey2nd));
        canvas.drawText(
                ValueUtils.buildAbbreviatedCurrentTemp(history.maxiTemp, GeometricWeather.getInstance().isFahrenheit()),
                2 * MARGIN_TEXT,
                tempYs[0] - paint.getFontMetrics().bottom - MARGIN_TEXT,
                paint);
        canvas.drawText(
                ValueUtils.buildAbbreviatedCurrentTemp(history.miniTemp, GeometricWeather.getInstance().isFahrenheit()),
                2 * MARGIN_TEXT,
                tempYs[1] - paint.getFontMetrics().top + MARGIN_TEXT,
                paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(
                getContext().getString(R.string.yesterday),
                getMeasuredWidth() - 2 * MARGIN_TEXT,
                tempYs[0] - paint.getFontMetrics().bottom - MARGIN_TEXT,
                paint);
        canvas.drawText(
                getContext().getString(R.string.yesterday),
                getMeasuredWidth() - 2 * MARGIN_TEXT,
                tempYs[1] - paint.getFontMetrics().top + MARGIN_TEXT,
                paint);
    }

    public void setSwitchLayout(SwipeSwitchLayout v) {
        this.switchLayout = v;
    }

    /** <br> data. */

    public void setData(Weather weather, History history, int state) {
        if (weather == null) {
            return;
        }
        this.history = history;
        if (state == TrendItemView.DATA_TYPE_DAILY) {
            canScroll = weather.dailyList.size() > 7;
        } else
            canScroll = state == TrendItemView.DATA_TYPE_HOURLY && weather.hourlyList.size() > 7;
        calcTempYs(weather, history, state);
        invalidate();
    }

    private void calcTempYs(Weather weather, History history, int state) {
        if (history == null) {
            tempYs = null;
            return;
        }
        int highest = GeometricWeather.getInstance().isFahrenheit()
                ? ValueUtils.calcFahrenheit(history.maxiTemp) : history.maxiTemp;
        int lowest = GeometricWeather.getInstance().isFahrenheit()
                ? ValueUtils.calcFahrenheit(history.miniTemp) : history.miniTemp;
        switch (state) {
            case TrendItemView.DATA_TYPE_DAILY:
                if (GeometricWeather.getInstance().isFahrenheit()) {
                    for (int i = 0; i < weather.dailyList.size(); i ++) {
                        if (ValueUtils.calcFahrenheit(weather.dailyList.get(i).temps[0]) > highest) {
                            highest = ValueUtils.calcFahrenheit(weather.dailyList.get(i).temps[0]);
                        }
                        if (ValueUtils.calcFahrenheit(weather.dailyList.get(i).temps[1]) < lowest) {
                            lowest = ValueUtils.calcFahrenheit(weather.dailyList.get(i).temps[1]);
                        }
                    }
                } else {
                    for (int i = 0; i < weather.dailyList.size(); i ++) {
                        if (weather.dailyList.get(i).temps[0] > highest) {
                            highest = weather.dailyList.get(i).temps[0];
                        }
                        if (weather.dailyList.get(i).temps[1] < lowest) {
                            lowest = weather.dailyList.get(i).temps[1];
                        }
                    }
                }
                break;

            case TrendItemView.DATA_TYPE_HOURLY:
                if (GeometricWeather.getInstance().isFahrenheit()) {
                    for (int i = 0; i < weather.hourlyList.size(); i ++) {
                        if (ValueUtils.calcFahrenheit(weather.hourlyList.get(i).temp) > highest) {
                            highest = ValueUtils.calcFahrenheit(weather.hourlyList.get(i).temp);
                        }
                        if (ValueUtils.calcFahrenheit(weather.hourlyList.get(i).temp) < lowest) {
                            lowest = ValueUtils.calcFahrenheit(weather.hourlyList.get(i).temp);
                        }
                    }
                } else {
                    for (int i = 0; i < weather.hourlyList.size(); i ++) {
                        if (weather.hourlyList.get(i).temp > highest) {
                            highest = weather.hourlyList.get(i).temp;
                        }
                        if (weather.hourlyList.get(i).temp < lowest) {
                            lowest = weather.hourlyList.get(i).temp;
                        }
                    }
                }
                break;
        }

        tempYs = new int[] {
                (int) (TrendItemView.calcHeaderHeight(getContext()) + TrendItemView.calcDrawSpecHeight(getContext()) - MARGIN_BOTTOM
                        - TrendItemView.calcDrawSpecUsableHeight(getContext()) * (history.maxiTemp - lowest) / (highest - lowest)),
                (int) (TrendItemView.calcHeaderHeight(getContext()) + TrendItemView.calcDrawSpecHeight(getContext()) - MARGIN_BOTTOM
                        - TrendItemView.calcDrawSpecUsableHeight(getContext()) * (history.miniTemp - lowest) / (highest - lowest))
        };
    }
}
