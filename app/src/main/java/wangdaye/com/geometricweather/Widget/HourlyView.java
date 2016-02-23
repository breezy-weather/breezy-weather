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

import wangdaye.com.geometricweather.Data.Location;
import wangdaye.com.geometricweather.R;

/**
 * Created by WangDaYe on 2016/2/21.
 */
public class HourlyView extends View {
    // widget
    private Paint paint;
    private Context context;

    // data
    private int[] temp;
    private float[] pop;

    private final String[] hour = new String[] {"01", "04", "07", "10", "13", "16", "19", "22"};

    private final int MARGIN = 60;

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

        this.temp = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
        this.pop = new float[] {0, 0, 0, 0, 0, 0, 0, 0};
    }

    public void setData(int[] temp, float[] pop) {
        this.temp = temp;
        this.pop = pop;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (this.temp == null || this.pop == null) {
            drawNoData(canvas);
            return;
        } else if (this.temp.length == 0 || this.pop.length == 0) {
            drawNoData(canvas);
            return;
        }

        float width = getMeasuredWidth();
        float height = getMeasuredHeight() - 5 * MARGIN;

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
            tempCoordinate[i][1] = (highestTemp - temp[i]) * unitHeight + 2 * MARGIN; // y
        }

        float[][] popCoordinate = new float[temp.length][2]; // x, y
        for (int i = 0; i < popCoordinate.length; i ++) {
            popCoordinate[i][0] = (2 * i + 1) * unitWidth; // x
            popCoordinate[i][1] = (100 - pop[i]) / 100 * height  + 2 * MARGIN; // y
        }

        this.drawTimeLine(canvas, tempCoordinate);
        this.drawPopLine(canvas, popCoordinate);
        this.drawTemp(canvas, tempCoordinate);
    }

    private void drawTimeLine(Canvas canvas, float[][] coordinate) {
        for (int i = coordinate.length - 1; i > -1; i --) {
            paint.reset();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(4);
            paint.setColor(ContextCompat.getColor(context, R.color.chart_background_line_time));
            canvas.drawLine(coordinate[i][0], 2 * MARGIN, coordinate[i][0], getMeasuredHeight() - 3 * MARGIN, paint);

            paint.reset();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(2);
            paint.setTextSize(30);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.RIGHT);
            paint.setColor(ContextCompat.getColor(context, R.color.chart_background_line_time));
            canvas.drawText(hour[hour.length - coordinate.length + i], coordinate[i][0] - 10, getMeasuredHeight() - 3 * MARGIN - 10, paint);
        }
        paint.reset();
    }

    private void drawPopLine(Canvas canvas, float[][] coordinate) {
        if (coordinate.length == 1) {
            Shader linearGradientLeft = new LinearGradient(coordinate[0][0], coordinate[0][1], coordinate[0][0] - 5 * MARGIN, coordinate[0][1],
                    Color.argb(225, 75, 80, 115), Color.argb(0, 75, 80, 115), Shader.TileMode.CLAMP);
            paint.setShader(linearGradientLeft);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setAlpha(100);
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_1));
            paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
            Path path = new Path();
            path.reset();
            path.moveTo(coordinate[0][0], coordinate[0][1]);
            path.lineTo(coordinate[0][0] - 5 * MARGIN, coordinate[0][1]);
            canvas.drawPath(path, paint);
            paint.reset();
            path.reset();
/*
            Shader linearGradientRight = new LinearGradient(coordinate[0][0], coordinate[0][1], coordinate[0][0] + 4 * MARGIN, coordinate[0][1],
                    Color.argb(225, 75, 80, 115), Color.argb(0, 75, 80, 115), Shader.TileMode.CLAMP);
            paint.setShader(linearGradientRight);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setAlpha(100);
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_1));
            paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
            path.reset();
            path.moveTo(coordinate[0][0], coordinate[0][1]);
            path.lineTo(coordinate[0][0] + 4 * MARGIN, coordinate[0][1]);
            canvas.drawPath(path, paint);
            paint.reset();
            path.reset();*/
        }

        for (float aPop : pop) {
            if (aPop > 50) {
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
                paint.setStrokeWidth(5);
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
                break;
            }
        }

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setAlpha(100);
        paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_1));
        paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
        Path path = new Path();
        path.moveTo(coordinate[0][0], coordinate[0][1]);
        for (int i = 1; i < coordinate.length; i ++) {
            path.lineTo(coordinate[i][0], coordinate[i][1]);
        }
        canvas.drawPath(path, paint);
        canvas.drawCircle(coordinate[0][0], coordinate[0][1], 1, paint);
        canvas.drawCircle(coordinate[coordinate.length - 1][0], coordinate[coordinate.length - 1][1], 1, paint);
        path.reset();
        paint.reset();

        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
        paint.setColor(ContextCompat.getColor(context, R.color.chart_number));
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
        paint.setTextSize(40);
        for (int i = 0; i < coordinate.length; i ++) {
            canvas.drawText(Float.toString(pop[i]) + "%", coordinate[i][0], coordinate[i][1] + 60, paint);
        }
        paint.reset();
    }

    private void drawTemp(Canvas canvas, float[][] coordinate) {
        if (coordinate.length == 1) {
            Shader linearGradientLeft = new LinearGradient(coordinate[0][0], coordinate[0][1], coordinate[0][0] - 5 * MARGIN, coordinate[0][1],
                    ContextCompat.getColor(context, R.color.lightPrimary_3), Color.argb(0, 150, 214, 219), Shader.TileMode.CLAMP);
            paint.setShader(linearGradientLeft);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setAlpha(100);
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_3));
            paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
            Path path = new Path();
            path.reset();
            path.moveTo(coordinate[0][0], coordinate[0][1]);
            path.lineTo(coordinate[0][0] - 5 * MARGIN, coordinate[0][1]);
            canvas.drawPath(path, paint);
            paint.reset();
            path.reset();
/*
            Shader linearGradientRight = new LinearGradient(coordinate[0][0], coordinate[0][1], coordinate[0][0] + 4 * MARGIN, coordinate[0][1],
                    ContextCompat.getColor(context, R.color.lightPrimary_3), Color.argb(0, 150, 214, 219), Shader.TileMode.CLAMP);
            paint.setShader(linearGradientRight);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setAlpha(100);
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_3));
            paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
            path.reset();
            path.moveTo(coordinate[0][0], coordinate[0][1]);
            path.lineTo(coordinate[0][0] + 4 * MARGIN, coordinate[0][1]);
            canvas.drawPath(path, paint);
            paint.reset();
            path.reset();*/
        }

        Shader linearGradient = new LinearGradient(0, 2 * MARGIN, 0, 7 * MARGIN,
                Color.argb(50, 176, 176, 176), Color.argb(0, 176, 176, 176), Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(5);
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
        paint.setStrokeWidth(5);
        paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_3));
        paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
        Path pathLine = new Path();
        pathLine.moveTo(coordinate[0][0], coordinate[0][1]);
        for (int i = 1; i < coordinate.length; i ++) {
            pathLine.lineTo(coordinate[i][0], coordinate[i][1]);
        }
        canvas.drawPath(pathLine, paint);
        canvas.drawCircle(coordinate[0][0], coordinate[0][1], 1, paint);
        canvas.drawCircle(coordinate[coordinate.length - 1][0], coordinate[coordinate.length - 1][1], 1, paint);
        paint.reset();
        pathLine.reset();

        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(context, R.color.chart_number));
        paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(40);
        for (int i = 0; i < coordinate.length; i ++) {
            canvas.drawText(Integer.toString(temp[i]) + "°", coordinate[i][0], coordinate[i][1] - 20, paint);
        }
        paint.reset();
    }

    private void drawNoData(Canvas canvas) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(context, R.color.chart_number));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(40);
        canvas.drawText(context.getString(R.string.no_data),
                getMeasuredWidth() / 2,
                (getMeasuredHeight() - 5 * MARGIN) / 2 + 2 * MARGIN,
                paint);
        paint.reset();
    }
/*
    private void drawTempLine(Canvas canvas, float[] levelCoordinate, int highestTemp, int lowestTemp) {
        int temp = 30;
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(ContextCompat.getColor(context, R.color.chart_background_line_temp));
        for (float aLevelCoordinate : levelCoordinate) {
            if (lowestTemp < temp && temp < highestTemp) {
                canvas.drawLine(0, aLevelCoordinate, getMeasuredWidth(), aLevelCoordinate, paint);
                paint.reset();
                paint.setAntiAlias(true);
                paint.setStrokeWidth(2);
                paint.setTextSize(30);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(ContextCompat.getColor(context, R.color.chart_background_line_temp));
                paint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(Integer.toString(temp) + "°", 10, aLevelCoordinate - 10, paint);

                paint.reset();
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2);
                paint.setColor(ContextCompat.getColor(context, R.color.chart_background_line_temp));
            }
            temp -= 10;
        }
        paint.reset();
    }
*/
}
