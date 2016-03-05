package wangdaye.com.geometricweather.Widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import wangdaye.com.geometricweather.R;

/**
 * Show trend of daily data.
 * */

public class TrendView extends View {
    // widget
    private Context context;
    private Paint paint;

    // data
    private int[] maxiTemp;
    private int[] miniTemp;

    private int yesterdayMaxiTemp;
    private int yesterdayMiniTemp;
    private boolean haveYesterdayData;

    private float MARGIN = 50;
    private float YESTERDAY_TEXT_SIZE = 30;
    private float WEATHER_TEXT_SIZE = 40;
    private float NUM_TEXT_SIZE = 2;
    private float TREND_LINE_SIZE = 5;
    private float TIME_LINE_SIZE = 4;

    // TAG
//    private final String TAG = "TrendView";

    public TrendView(Context context) {
        super(context);
        this.initialize(context);
    }

    public TrendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize(context);
    }

    public TrendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TrendView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize(context);
    }

    private void initialize(Context context) {
        this.context = context;
        this.paint = new Paint();

        this.maxiTemp = new int[] {7, 7, 7, 7, 7, 7, 7};
        this.miniTemp = new int[] {0, 0, 0, 0, 0, 0, 0};

        this.yesterdayMaxiTemp = 7;
        this.yesterdayMiniTemp = 0;
        this.haveYesterdayData = false;
    }

    public void setData(int[] maxiTemp, int[] miniTemp, int[] yesterdayTemp) {
        this.maxiTemp = maxiTemp;
        this.miniTemp = miniTemp;
        if (yesterdayTemp == null) {
            this.yesterdayMaxiTemp = 7;
            this.yesterdayMiniTemp = 0;
            this.haveYesterdayData = false;
        } else {
            this.yesterdayMiniTemp = yesterdayTemp[0];
            this.yesterdayMaxiTemp = yesterdayTemp[1];
            this.haveYesterdayData = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float dpiLevel = getResources().getDisplayMetrics().density;
        MARGIN = (float) (50 * (dpiLevel / 2.625));
        YESTERDAY_TEXT_SIZE = (float) (30 * (dpiLevel / 2.625));
        WEATHER_TEXT_SIZE = (float) (40 * (dpiLevel / 2.625));
        NUM_TEXT_SIZE = (float) (2 * (dpiLevel / 2.625));
        TREND_LINE_SIZE = (float) (5 * (dpiLevel / 2.625));
        TIME_LINE_SIZE = (float) (4 * (dpiLevel / 2.625));

        float width = getMeasuredWidth();
        float height = getMeasuredHeight() - 7 * MARGIN;

        int highestTemp;
        int lowestTemp;
        if (haveYesterdayData) {
            highestTemp = this.yesterdayMaxiTemp;
            lowestTemp = this.yesterdayMiniTemp;
        } else {
            highestTemp = this.maxiTemp[0];
            lowestTemp = this.miniTemp[0];
        }
        for (int aMaxiTemp : maxiTemp) {
            if (aMaxiTemp > highestTemp) {
                highestTemp = aMaxiTemp;
            }
        }
        for (int aMiniTemp : miniTemp) {
            if (aMiniTemp < lowestTemp) {
                lowestTemp = aMiniTemp;
            }
        }

        float unitHeight;
        float unitWidth;
        if (lowestTemp != highestTemp) {
            unitWidth = width / 14;
            unitHeight = height / (highestTemp - lowestTemp);
        } else {
            unitWidth = width / 14;
            unitHeight = height / 14;
            highestTemp += 7;
        }

        float[][] highestCoordinate = new float[7][2]; // x, y
        for (int i = 0; i < highestCoordinate.length; i ++) {
            highestCoordinate[i][0] = (2 * i + 1) * unitWidth; // x
            highestCoordinate[i][1] = (highestTemp - maxiTemp[i]) * unitHeight + 3 * MARGIN; // y
        }

        float[][] lowestCoordinate = new float[7][2]; // x, y
        for (int i = 0; i < lowestCoordinate.length; i ++) {
            lowestCoordinate[i][0] = (2 * i + 1) * unitWidth; // x
            lowestCoordinate[i][1] = (highestTemp - miniTemp[i]) * unitHeight + 3 * MARGIN; // y
        }

        float[] yesterdayCoordinate = new float[2];
        yesterdayCoordinate[0] = (highestTemp - yesterdayMaxiTemp) * unitHeight + 3 * MARGIN;
        yesterdayCoordinate[1] = (highestTemp - yesterdayMiniTemp) * unitHeight + 3 * MARGIN;

        this.drawTimeLine(canvas, highestCoordinate);
        this.drawYesterdayLine(canvas, yesterdayCoordinate);
        this.drawMaxiTemp(canvas, highestCoordinate);
        this.drawMiniTemp(canvas, lowestCoordinate);
    }

    private void drawTimeLine(Canvas canvas, float[][] coordinate) {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(TIME_LINE_SIZE);
        paint.setColor(ContextCompat.getColor(context, R.color.chart_background_line_time));
        for (float[] aCoordinate : coordinate) {
            canvas.drawLine(aCoordinate[0], 3 * MARGIN, aCoordinate[0], getMeasuredHeight() - 4 * MARGIN, paint);
        }
        paint.reset();
    }

    private void drawYesterdayLine(Canvas canvas, float[] yesterdayCoordinate) {
        if (! haveYesterdayData) {
            return;
        }

        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(NUM_TEXT_SIZE);
        paint.setColor(ContextCompat.getColor(context, R.color.chart_background_line_temp));
        canvas.drawLine(0, yesterdayCoordinate[0], getMeasuredWidth(), yesterdayCoordinate[0], paint);
        canvas.drawLine(0, yesterdayCoordinate[1], getMeasuredWidth(), yesterdayCoordinate[1], paint);

        paint.reset();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(NUM_TEXT_SIZE);
        paint.setTextSize(YESTERDAY_TEXT_SIZE);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(context, R.color.chart_background_line_temp));
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(Integer.toString(yesterdayMaxiTemp) + "°", 10, yesterdayCoordinate[0] - 10, paint);
        canvas.drawText(Integer.toString(yesterdayMiniTemp) + "°", 10, yesterdayCoordinate[1] - 10, paint);

        paint.reset();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(NUM_TEXT_SIZE);
        paint.setTextSize(YESTERDAY_TEXT_SIZE);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(context, R.color.chart_background_line_temp));
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(context.getString(R.string.yesterday), getMeasuredWidth() - 10, yesterdayCoordinate[0] - 10, paint);
        canvas.drawText(context.getString(R.string.yesterday), getMeasuredWidth() - 10, yesterdayCoordinate[1] - 10, paint);

        paint.reset();
    }

    private void drawMaxiTemp(Canvas canvas, float[][] coordinate) {
        Shader linearGradient = new LinearGradient(0, 3 * MARGIN, 0, 8 * MARGIN,
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
        pathShadow.lineTo(coordinate[6][0], getMeasuredHeight());
        pathShadow.lineTo(coordinate[0][0], getMeasuredHeight());
        pathShadow.close();
        canvas.drawPath(pathShadow, paint);
        paint.reset();
        pathShadow.reset();

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(TREND_LINE_SIZE);
        paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_3));
        paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
        Path pathLine = new Path();
        pathLine.moveTo(coordinate[0][0], coordinate[0][1]);
        for (int i = 1; i < coordinate.length; i ++) {
            pathLine.lineTo(coordinate[i][0], coordinate[i][1]);
        }
        canvas.drawPath(pathLine, paint);
        canvas.drawCircle(coordinate[0][0], coordinate[0][1], 1, paint);
        canvas.drawCircle(coordinate[6][0], coordinate[6][1], 1, paint);
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
            canvas.drawText(Integer.toString(maxiTemp[i]) + "°", coordinate[i][0], coordinate[i][1] - 20, paint);
        }
        paint.reset();
    }

    private void drawMiniTemp(Canvas canvas, float[][] coordinate) {
        float startPosition = coordinate[0][1];
        for (int i = 1; i < coordinate.length; i ++) {
            if (coordinate[i][1] < startPosition) {
                startPosition = coordinate[i][1];
            }
        }
        Shader linearGradient = new LinearGradient(0, startPosition, 0, startPosition + 5 * MARGIN,
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
        pathShadow.lineTo(coordinate[6][0], getMeasuredHeight());
        pathShadow.lineTo(coordinate[0][0], getMeasuredHeight());
        pathShadow.close();
        canvas.drawPath(pathShadow, paint);
        paint.reset();
        pathShadow.reset();

        Path path = new Path();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(TREND_LINE_SIZE);
        paint.setAlpha(100);
        paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_1));
        paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));

        path.moveTo(coordinate[0][0], coordinate[0][1]);
        for (int i = 1; i < coordinate.length; i ++) {
            path.lineTo(coordinate[i][0], coordinate[i][1]);
        }
        canvas.drawPath(path, paint);
        canvas.drawCircle(coordinate[0][0], coordinate[0][1], 1, paint);
        canvas.drawCircle(coordinate[6][0], coordinate[6][1], 1, paint);
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
            canvas.drawText(Integer.toString(miniTemp[i]) + "°", coordinate[i][0], coordinate[i][1] + 60, paint);
        }
        paint.reset();
    }
}
