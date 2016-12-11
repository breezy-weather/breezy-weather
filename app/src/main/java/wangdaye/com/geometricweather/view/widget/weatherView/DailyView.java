package wangdaye.com.geometricweather.view.widget.weatherView;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Shader;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.History;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Show trend of daily data.
 * */

public class DailyView extends View {
    // widget
    private Paint paint;

    // data
    private int[] maxiTemps;
    private int[] miniTemps;
    private int[] yesterdayTemps;

    private float MARGIN_TOP = 60;
    private float MARGIN_BOTTOM = 80;
    private float WEATHER_TEXT_SIZE = 14;
    private float WEATHER_TEXT_WIDTH = 0.7F;
    private float TREND_LINE_SIZE = 2;
    private float YESTERDAY_TEXT_SIZE = 10;
    private float YESTERDAY_TEXT_WIDTH = 0.25F;
    private float CHART_LINE_SIZE = 1;
    private float MARGIN_TEXT = 2;

    /** <br> life cycle. */

    public DailyView(Context context) {
        super(context);
        this.initialize();
    }

    public DailyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public DailyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DailyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize();
    }

    private void initialize() {
        this.paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);

        this.maxiTemps = new int[] {7, 7, 7, 7, 7};
        this.miniTemps = new int[] {0, 0, 0, 0, 0};
        this.yesterdayTemps = null;

        MARGIN_TOP = DisplayUtils.dpToPx(getContext(), (int) MARGIN_TOP);
        MARGIN_BOTTOM = DisplayUtils.dpToPx(getContext(), (int) MARGIN_BOTTOM);
        WEATHER_TEXT_SIZE = DisplayUtils.dpToPx(getContext(), (int) WEATHER_TEXT_SIZE);
        WEATHER_TEXT_WIDTH = DisplayUtils.dpToPx(getContext(), (int) WEATHER_TEXT_WIDTH);
        TREND_LINE_SIZE = DisplayUtils.dpToPx(getContext(), (int) TREND_LINE_SIZE);
        YESTERDAY_TEXT_SIZE = DisplayUtils.dpToPx(getContext(), (int) YESTERDAY_TEXT_SIZE);
        YESTERDAY_TEXT_WIDTH = DisplayUtils.dpToPx(getContext(), (int) YESTERDAY_TEXT_WIDTH);
        CHART_LINE_SIZE = DisplayUtils.dpToPx(getContext(), (int) CHART_LINE_SIZE);
        MARGIN_TEXT = DisplayUtils.dpToPx(getContext(), (int) MARGIN_TEXT);
    }

    /** <br> data. */

    public void setData(Weather weather, History history) {
        if (weather == null || weather.dailyList == null || weather.dailyList.size() < 1) {
            return;
        }

        this.maxiTemps = new int[weather.dailyList.size()];
        this.miniTemps = new int[weather.dailyList.size()];
        for (int i = 0; i < weather.dailyList.size(); i ++) {
            maxiTemps[i] = weather.dailyList.get(i).temps[0];
            miniTemps[i] = weather.dailyList.get(i).temps[1];
        }

        if (history != null) {
            yesterdayTemps = new int[] {history.maxiTemp, history.miniTemp};
        } else {
            yesterdayTemps = null;
        }

        invalidate();
    }

    /** <br> draw. */

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        float drawSpaceWidth = getMeasuredWidth();
        float drawSpaceHeight = getMeasuredHeight() - MARGIN_BOTTOM - MARGIN_TOP;

        int highestTemp;
        int lowestTemp;
        if (yesterdayTemps != null) {
            highestTemp = yesterdayTemps[0];
            lowestTemp = yesterdayTemps[1];
        } else {
            highestTemp = maxiTemps[0];
            lowestTemp = miniTemps[0];
        }
        for (int t : maxiTemps) {
            if (t > highestTemp) {
                highestTemp = t;
            }
        }
        for (int t : miniTemps) {
            if (t < lowestTemp) {
                lowestTemp = t;
            }
        }
        if (highestTemp == lowestTemp) {
            highestTemp += 7;
            lowestTemp -= 7;
        }

        int[] timeLineCoordinates = new int[maxiTemps.length];
        for (int i = 0; i < timeLineCoordinates.length; i ++) {
            timeLineCoordinates[i] = (int) (drawSpaceWidth / (timeLineCoordinates.length * 2.0) * (2 * i + 1));
        }

        Point[] maxiTempPoints = new Point[timeLineCoordinates.length];
        for (int i = 0; i < maxiTempPoints.length; i ++) {
            maxiTempPoints[i] = new Point(
                    timeLineCoordinates[i],
                    (int) (drawSpaceHeight / (highestTemp - lowestTemp) * (highestTemp - maxiTemps[i]) + MARGIN_TOP));
        }

        Point[] miniTempPoints = new Point[timeLineCoordinates.length];
        for (int i = 0; i < miniTempPoints.length; i ++) {
            miniTempPoints[i] = new Point(
                    timeLineCoordinates[i],
                    (int) (drawSpaceHeight / (highestTemp - lowestTemp) * (highestTemp - miniTemps[i]) + MARGIN_TOP));
        }

        int[] yesterdayTempCoordinates = yesterdayTemps == null ?
                null
                :
                new int[] {
                        (int) (drawSpaceHeight / (highestTemp - lowestTemp) * (highestTemp - yesterdayTemps[0]) + MARGIN_TOP),
                        (int) (drawSpaceHeight / (highestTemp - lowestTemp) * (highestTemp - yesterdayTemps[1]) + MARGIN_TOP)};

        this.drawTimeLine(canvas, timeLineCoordinates);
        this.drawYesterdayTempLine(canvas, yesterdayTempCoordinates);
        this.drawMaxiTemp(canvas, maxiTempPoints);
        this.drawMiniTemp(canvas, miniTempPoints);
    }

    private void drawTimeLine(Canvas canvas, int[] coordinates) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(CHART_LINE_SIZE);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.colorLine));
        for (int c : coordinates) {
            canvas.drawLine(
                    c, MARGIN_TOP,
                    c, getMeasuredHeight() - MARGIN_BOTTOM,
                    paint);
        }
    }

    private void drawYesterdayTempLine(Canvas canvas, int[] coordinates) {
        if (coordinates == null) {
            return;
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(CHART_LINE_SIZE);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.colorLine));
        canvas.drawLine(
                0, coordinates[0],
                getMeasuredWidth(), coordinates[0],
                paint);
        canvas.drawLine(
                0, coordinates[1],
                getMeasuredWidth(), coordinates[1],
                paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(YESTERDAY_TEXT_SIZE);
        paint.setStrokeWidth(YESTERDAY_TEXT_WIDTH);
        canvas.drawText(
                Integer.toString(yesterdayTemps[0]) + "°",
                MARGIN_TEXT,
                coordinates[0] - paint.getFontMetrics().bottom - MARGIN_TEXT,
                paint);
        canvas.drawText(
                Integer.toString(yesterdayTemps[1]) + "°",
                10,
                coordinates[1] - paint.getFontMetrics().bottom - MARGIN_TEXT,
                paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(
                getContext().getString(R.string.yesterday),
                getMeasuredWidth() - MARGIN_TEXT,
                coordinates[0] - paint.getFontMetrics().bottom - MARGIN_TEXT,
                paint);
        canvas.drawText(
                getContext().getString(R.string.yesterday),
                getMeasuredWidth() - MARGIN_TEXT,
                coordinates[1] - paint.getFontMetrics().bottom - MARGIN_TEXT,
                paint);
    }

    private void drawMaxiTemp(Canvas canvas, Point[] points) {
        Path path = new Path();

        Shader linearGradient = new LinearGradient(
                0, MARGIN_TOP,
                0, getMeasuredHeight() - MARGIN_BOTTOM,
                Color.argb(50, 176, 176, 176), Color.argb(0, 176, 176, 176),
                Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        paint.setStyle(Paint.Style.FILL);
        path.moveTo(points[0].x, getMeasuredHeight() - MARGIN_BOTTOM);
        for (Point p : points) {
            path.lineTo(p.x, p.y);
        }
        path.lineTo(points[points.length - 1].x, getMeasuredHeight() - MARGIN_BOTTOM);
        path.close();
        canvas.drawPath(path, paint);
        paint.setShader(null);
        path.reset();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(TREND_LINE_SIZE);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.lightPrimary_3));
        paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
        path.moveTo(points[0].x, points[0].y);
        for (int i = 1; i < points.length; i ++) {
            path.lineTo(points[i].x, points[i].y);
        }
        canvas.drawPath(path, paint);
        path.reset();

        paint.setColor(ContextCompat.getColor(getContext(), R.color.colorTextContent));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(WEATHER_TEXT_SIZE);
        paint.setStrokeWidth(WEATHER_TEXT_WIDTH);
        for (int i = 0; i < points.length; i ++) {
            canvas.drawText(
                    Integer.toString(maxiTemps[i]) + "°",
                    points[i].x,
                    points[i].y - paint.getFontMetrics().bottom - MARGIN_TEXT,
                    paint);
        }
        paint.setShadowLayer(0, 0, 0, ContextCompat.getColor(getContext(), android.R.color.transparent));
    }

    private void drawMiniTemp(Canvas canvas, Point[] points) {
        Path path = new Path();

        Shader linearGradient = new LinearGradient(
                0, MARGIN_TOP,
                0, getMeasuredHeight() - MARGIN_BOTTOM,
                Color.argb(50, 176, 176, 176), Color.argb(0, 176, 176, 176),
                Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        paint.setStyle(Paint.Style.FILL);
        path.moveTo(points[0].x, getMeasuredHeight() - MARGIN_BOTTOM);
        for (Point p : points) {
            path.lineTo(p.x, p.y);
        }
        path.lineTo(points[points.length - 1].x, getMeasuredHeight() - MARGIN_BOTTOM);
        path.close();
        canvas.drawPath(path, paint);
        paint.setShader(null);
        path.reset();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(TREND_LINE_SIZE);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.darkPrimary_1));
        paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
        path.moveTo(points[0].x, points[0].y);
        for (int i = 1; i < points.length; i ++) {
            path.lineTo(points[i].x, points[i].y);
        }
        canvas.drawPath(path, paint);
        path.reset();

        paint.setColor(ContextCompat.getColor(getContext(), R.color.colorTextContent));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(WEATHER_TEXT_SIZE);
        paint.setStrokeWidth(WEATHER_TEXT_WIDTH);
        for (int i = 0; i < points.length; i ++) {
            canvas.drawText(
                    Integer.toString(miniTemps[i]) + "°",
                    points[i].x,
                    points[i].y - paint.getFontMetrics().top + MARGIN_TEXT,
                    paint);
        }
        paint.setShadowLayer(0, 0, 0, ContextCompat.getColor(getContext(), android.R.color.transparent));
    }
}
