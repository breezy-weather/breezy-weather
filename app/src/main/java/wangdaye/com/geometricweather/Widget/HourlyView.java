package wangdaye.com.geometricweather.Widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import wangdaye.com.geometricweather.R;

/**
 * Show the trend of the hourly data.
 * */

public class HourlyView extends View {
    // widget
    private Paint paint;
    private Context context;

    // data
    private int[] temp;
    private float[] pop;

    private final String[] hour = new String[] {"01", "04", "07", "10", "13", "16", "19", "22"};

    private float MARGIN = 50;
    private float WEATHER_TEXT_SIZE = 40;
    private float TIME_TEXT_SIZE = 35;
    private float NUM_TEXT_SIZE = 2;
    private float TREND_LINE_SIZE = 5;
    private float TIME_LINE_SIZE = 4;

    private final int DRAW_LOADING_VIEW_CYCLE_TIME = 90;
    private final int MAX_SWEEP_ANGLE = 300;
    private final int MIN_SWEEP_ANGLE = 30;

    private int startAngle;
    private int sweepAngle;
    private int drawLoadingViewTime;

    public HourlyView(Context context) {
        super(context);
        this.initialize(context);
    }

    public HourlyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize(context);
    }

    public HourlyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HourlyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize(context);
    }

    private void initialize(Context context) {
        this.context = context;
        this.paint = new Paint();

        this.temp = new int[] {100, 100, 100, 100, 100, 100, 100, 100};
        this.pop = new float[] {0, 0, 0, 0, 0, 0, 0, 0};

        this.startAngle = 0;
        this.sweepAngle = MIN_SWEEP_ANGLE;
        this.drawLoadingViewTime = 1;
    }

    public void setData(int[] temp, float[] pop) {
        this.temp = temp;
        this.pop = pop;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float dpiLevel = getResources().getDisplayMetrics().density;
        if (dpiLevel < 2.625) {
            MARGIN = (float) (50 * (dpiLevel / 2.625));
            TIME_TEXT_SIZE = (float) (35 * (dpiLevel / 2.625));
            WEATHER_TEXT_SIZE = (float) (40 * (dpiLevel / 2.625));
            NUM_TEXT_SIZE = (float) (2 * (dpiLevel / 2.625));
            TREND_LINE_SIZE = (float) (5 * (dpiLevel / 2.625));
            TIME_LINE_SIZE = (float) (4 * (dpiLevel / 2.625));
        }

        if (this.temp == null || this.pop == null) {
            drawNoData(canvas);
        } else if (this.temp.length == 0 || this.pop.length == 0) {
            drawNoData(canvas);
        } else if (this.temp.length == 8) {
            boolean refresh = true;
            for (int aTemp : temp) {
                if (aTemp != 100) {
                    refresh = false;
                    break;
                }
            }
            if (refresh) {
                drawLoadingView(canvas);
            }
        } else {
            float width = getMeasuredWidth();
            float height = getMeasuredHeight() - 7 * MARGIN;

            int highestTemp = this.temp[0];
            for (int i = 1; i < temp.length; i ++) {
                if (temp[i] > highestTemp) {
                    highestTemp = temp[i];
                }
            }
            int lowestTemp = this.temp[0];
            for (int i = 1; i < temp.length; i ++) {
                if (temp[i] < lowestTemp) {
                    lowestTemp = temp[i];
                }
            }

            float unitHeight;
            float unitWidth;
            if (lowestTemp != highestTemp) {
                unitWidth = width / (2 * temp.length);
                unitHeight = height / (highestTemp - lowestTemp);
            } else {
                unitWidth = width / (2 * temp.length);
                unitHeight = height / (2 * temp.length);
                highestTemp += temp.length;
            }

            float[][] tempCoordinate = new float[temp.length][2]; // x, y
            for (int i = 0; i < tempCoordinate.length; i ++) {
                tempCoordinate[i][0] = (2 * i + 1) * unitWidth; // x
                tempCoordinate[i][1] = (highestTemp - temp[i]) * unitHeight + 3 * MARGIN; // y
            }

            float[][] popCoordinate = new float[temp.length][2]; // x, y
            for (int i = 0; i < popCoordinate.length; i ++) {
                popCoordinate[i][0] = (2 * i + 1) * unitWidth; // x
                popCoordinate[i][1] = (100 - pop[i]) / 100 * height  + 3 * MARGIN; // y
            }

            this.drawTimeLine(canvas, tempCoordinate);
            this.drawPopLine(canvas, popCoordinate);
            this.drawTemp(canvas, tempCoordinate);
        }
    }

    private void drawLoadingView(Canvas canvas) {
        if (drawLoadingViewTime < DRAW_LOADING_VIEW_CYCLE_TIME / 2 + 1) {
            this.growUp(canvas);
        } else {
            this.aging(canvas);
        }
    }

    private void growUp(Canvas canvas) {
        RectF rectf = new RectF();
        rectf.left = (int) (getMeasuredWidth() / 2.0 - getMeasuredHeight() / 8.0);
        rectf.right = (int) (getMeasuredWidth() / 2.0 + getMeasuredHeight() / 8.0);
        rectf.top = (int) (getMeasuredHeight() / 2.0 - getMeasuredHeight() / 8.0);
        rectf.bottom = (int) (getMeasuredHeight() / 2.0 + getMeasuredHeight() / 8.0);

        paint.reset();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setColor(ContextCompat.getColor(context, R.color.notification_background));

        startAngle = startAngle + 360 / DRAW_LOADING_VIEW_CYCLE_TIME;
        sweepAngle = sweepAngle + (MAX_SWEEP_ANGLE - MIN_SWEEP_ANGLE) / (DRAW_LOADING_VIEW_CYCLE_TIME / 2);
        canvas.drawArc(rectf, startAngle, sweepAngle, false, paint);

        paint.reset();
        drawLoadingViewTime ++;
        invalidate();
    }

    private void aging(Canvas canvas) {
        RectF rectf = new RectF();
        rectf.left = (int) (getMeasuredWidth() / 2.0 - getMeasuredHeight() / 8.0);
        rectf.right = (int) (getMeasuredWidth() / 2.0 + getMeasuredHeight() / 8.0);
        rectf.top = (int) (getMeasuredHeight() / 2.0 - getMeasuredHeight() / 8.0);
        rectf.bottom = (int) (getMeasuredHeight() / 2.0 + getMeasuredHeight() / 8.0);

        paint.reset();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setColor(ContextCompat.getColor(context, R.color.notification_background));

        startAngle = startAngle + 360 / DRAW_LOADING_VIEW_CYCLE_TIME  + (MAX_SWEEP_ANGLE - MIN_SWEEP_ANGLE) / (DRAW_LOADING_VIEW_CYCLE_TIME / 2);
        sweepAngle = sweepAngle - (MAX_SWEEP_ANGLE - MIN_SWEEP_ANGLE) / (DRAW_LOADING_VIEW_CYCLE_TIME / 2);
        canvas.drawArc(rectf, startAngle, sweepAngle, false, paint);

        paint.reset();
        drawLoadingViewTime ++;
        if (drawLoadingViewTime == DRAW_LOADING_VIEW_CYCLE_TIME + 1) {
            drawLoadingViewTime = 1;
        }
        invalidate();
    }

    private void drawTimeLine(Canvas canvas, float[][] coordinate) {
        for (int i = coordinate.length - 1; i > -1; i --) {
            paint.reset();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(TIME_LINE_SIZE);
            paint.setColor(ContextCompat.getColor(context, R.color.chart_background_line_time));
            canvas.drawLine(coordinate[i][0], 3 * MARGIN, coordinate[i][0], getMeasuredHeight() - 4 * MARGIN, paint);

            paint.reset();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(NUM_TEXT_SIZE);
            paint.setTextSize(TIME_TEXT_SIZE);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.RIGHT);
            paint.setColor(ContextCompat.getColor(context, R.color.chart_background_line_temp));
            canvas.drawText(hour[hour.length - coordinate.length + i], coordinate[i][0] - 10, getMeasuredHeight() - 4 * MARGIN - 10, paint);
        }
        paint.reset();
    }

    private void drawPopLine(Canvas canvas, float[][] coordinate) {
        if (coordinate.length == 1) {
            Shader linearGradientLeft = new LinearGradient(coordinate[0][0], coordinate[0][1], coordinate[0][0] - 7 * MARGIN, coordinate[0][1],
                    Color.argb(225, 75, 80, 115), Color.argb(0, 75, 80, 115), Shader.TileMode.CLAMP);
            paint.setShader(linearGradientLeft);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(TREND_LINE_SIZE);
            paint.setAlpha(100);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_1));
            paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
            Path path = new Path();
            path.reset();
            path.moveTo(coordinate[0][0], coordinate[0][1]);
            path.lineTo(coordinate[0][0] - 7 * MARGIN, coordinate[0][1]);
            canvas.drawPath(path, paint);
            paint.reset();
            path.reset();
        } else {
            float startPosition = coordinate[0][1];
            for (int i = 1; i < coordinate.length; i ++) {
                if (coordinate[i][1] < startPosition) {
                    startPosition = coordinate[i][1];
                }
            }
            Shader linearGradient = new LinearGradient(0, 3 * MARGIN, 0, getMeasuredHeight() - 4 * MARGIN,
                    Color.argb(50, 176, 176, 176), Color.argb(0, 176, 176, 176), Shader.TileMode.CLAMP);
            paint.setShader(linearGradient);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(TREND_LINE_SIZE);
            paint.setPathEffect(new CornerPathEffect(getMeasuredWidth() / (coordinate.length)));
            Path pathShadow = new Path();
            pathShadow.moveTo(coordinate[0][0], coordinate[0][1]);
            for (int i = 1; i < coordinate.length; i ++) {
                pathShadow.lineTo(coordinate[i][0], coordinate[i][1]);
            }
            paint.setPathEffect(null);
            pathShadow.lineTo(coordinate[coordinate.length - 1][0], getMeasuredHeight());
            pathShadow.lineTo(coordinate[0][0], getMeasuredHeight());
            pathShadow.close();
            canvas.drawPath(pathShadow, paint);
            paint.reset();
            pathShadow.reset();
        }

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(TREND_LINE_SIZE);
        paint.setAlpha(100);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_1));
        paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
        paint.setPathEffect(new CornerPathEffect(getMeasuredWidth() / (coordinate.length)));
        Path path = new Path();
        path.moveTo(coordinate[0][0], coordinate[0][1]);
        for (int i = 1; i < coordinate.length; i ++) {
            path.lineTo(coordinate[i][0], coordinate[i][1]);
        }
        canvas.drawPath(path, paint);
        path.reset();
        paint.reset();

        paint.setAntiAlias(true);
        paint.setStrokeWidth(NUM_TEXT_SIZE);
        paint.setColor(ContextCompat.getColor(context, R.color.chart_number));
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
        paint.setTextSize(WEATHER_TEXT_SIZE);
        for (int i = 0; i < coordinate.length; i ++) {
            canvas.drawText(Float.toString(pop[i]) + "%", coordinate[i][0], getMeasuredHeight() - 4 * MARGIN + WEATHER_TEXT_SIZE, paint);
        }
        paint.reset();
    }

    private void drawTemp(Canvas canvas, float[][] coordinate) {
        if (coordinate.length == 1) {
            Shader linearGradientLeft = new LinearGradient(coordinate[0][0], coordinate[0][1], coordinate[0][0] - 7 * MARGIN, coordinate[0][1],
                    ContextCompat.getColor(context, R.color.lightPrimary_3), Color.argb(0, 150, 214, 219), Shader.TileMode.CLAMP);
            paint.setShader(linearGradientLeft);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(TREND_LINE_SIZE);
            paint.setAlpha(100);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_3));
            paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
            Path path = new Path();
            path.reset();
            path.moveTo(coordinate[0][0], coordinate[0][1]);
            path.lineTo(coordinate[0][0] - 7 * MARGIN, coordinate[0][1]);
            canvas.drawPath(path, paint);
            paint.reset();
            path.reset();
        }

        Shader linearGradient = new LinearGradient(0, 3 * MARGIN, 0, getMeasuredHeight() - 4 * MARGIN,
                Color.argb(50, 176, 176, 176), Color.argb(0, 176, 176, 176), Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(TREND_LINE_SIZE);
        Path pathShadow = new Path();
        pathShadow.moveTo(coordinate[0][0], coordinate[0][1]);
        for (int i = 1; i < coordinate.length; i ++) {
            pathShadow.lineTo(coordinate[i][0], coordinate[i][1]);
        }
        pathShadow.lineTo(coordinate[coordinate.length - 1][0], getMeasuredHeight());
        pathShadow.lineTo(coordinate[0][0], getMeasuredHeight());
        pathShadow.close();
        canvas.drawPath(pathShadow, paint);
        paint.reset();
        pathShadow.reset();

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(TREND_LINE_SIZE);
        paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_3));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
        Path pathLine = new Path();
        pathLine.moveTo(coordinate[0][0], coordinate[0][1]);
        for (int i = 1; i < coordinate.length; i ++) {
            pathLine.lineTo(coordinate[i][0], coordinate[i][1]);
        }
        canvas.drawPath(pathLine, paint);
        paint.reset();
        pathLine.reset();

        paint.setAntiAlias(true);
        paint.setStrokeWidth(NUM_TEXT_SIZE);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(context, R.color.chart_number));
        paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(WEATHER_TEXT_SIZE);
        for (int i = 0; i < coordinate.length; i ++) {
            canvas.drawText(Integer.toString(temp[i]) + "°", coordinate[i][0], coordinate[i][1] - 20, paint);
        }
        paint.reset();
    }

    private void drawNoData(Canvas canvas) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(NUM_TEXT_SIZE);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(context, R.color.chart_number));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(WEATHER_TEXT_SIZE);
        canvas.drawText(context.getString(R.string.no_data),
                getMeasuredWidth() / 2,
                (getMeasuredHeight() - 7 * MARGIN) / 2 + 3 * MARGIN,
                paint);
        paint.reset();
    }
}
