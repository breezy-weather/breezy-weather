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
import android.view.View;

import wangdaye.com.geometricweather.R;

/**
 * Created by WangDaYe on 2016/2/6.
 */

public class TrendView extends View {
    // widget
    private Context context;
    private Paint paint;

    // data
    private int[] maxiTemp;
    private int[] miniTemp;

    private final int MARGIN = 60;

    // TAG
    private final String TAG = "TrendView";

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
    }

    public void setData(int[] maxiTemp, int[] miniTemp) {
        this.maxiTemp = maxiTemp;
        this.miniTemp = miniTemp;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float width = getMeasuredWidth();
        float height = getMeasuredHeight() - 5 * MARGIN;

        int highestTemp = this.maxiTemp[0];
        for (int i = 1; i < 7; i ++) {
            if (maxiTemp[i] > highestTemp) {
                highestTemp = maxiTemp[i];
            }
        }
        int lowestTemp = this.miniTemp[0];
        for (int i = 1; i < 7; i ++) {
            if (miniTemp[i] < lowestTemp) {
                lowestTemp = miniTemp[i];
            }
        }

        float unitHeight;
        float unitWidth;
        if (lowestTemp != highestTemp) {
            unitWidth = width / 14;
            unitHeight = height / (highestTemp - lowestTemp);
        } else {
            unitWidth = width / 14;
            unitHeight = height / (highestTemp + lowestTemp);
            if (highestTemp > 0) {
                highestTemp = 0;
            } else if(highestTemp == 0) {
                highestTemp = -5;
            } else {
                highestTemp = highestTemp * 2;
            }
        }

        float[][] highestCoordinate = new float[7][2]; // x, y
        for (int i = 0; i < 7; i ++) {
            highestCoordinate[i][0] = (2 * i + 1) * unitWidth; // x
            highestCoordinate[i][1] = (highestTemp - maxiTemp[i]) * unitHeight + 2 * MARGIN; // y
        }

        float[][] lowestCoordinate = new float[7][2]; // x, y
        for (int i = 0; i < 7; i ++) {
            lowestCoordinate[i][0] = (2 * i + 1) * unitWidth; // x
            lowestCoordinate[i][1] = (highestTemp - miniTemp[i]) * unitHeight + 2 * MARGIN; // y
        }

        float[] levelCoordinate = new float[7];
        int temp = 30;
        for (int i = 0; i < 7; i ++) {
            levelCoordinate[i] = (highestTemp - temp) * unitHeight + 2 * MARGIN;
            temp -= 10;
        }

        this.drawTimeLine(canvas, highestCoordinate);
        this.drawTempLine(canvas, levelCoordinate, highestTemp, lowestTemp);
        this.drawMaxiTemp(canvas, highestCoordinate);
        this.drawMiniTemp(canvas, lowestCoordinate);
    }

    private void drawTimeLine(Canvas canvas, float[][] coordinate) {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(4);
        paint.setColor(ContextCompat.getColor(context, R.color.chart_background_line_time));
        for (int i = 0; i < 7; i ++) {
            canvas.drawLine(coordinate[i][0], 2 * MARGIN, coordinate[i][0], getMeasuredHeight() - 3 * MARGIN, paint);
        }
        paint.reset();
    }

    private void drawTempLine(Canvas canvas, float[] levelCoordinate, int highestTemp, int lowestTemp) {
        int temp = 30;
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(ContextCompat.getColor(context, R.color.chart_background_line_temp));
        for (int i = 0; i < 7; i ++) {
            if (lowestTemp < temp && temp < highestTemp) {
                canvas.drawLine(0, levelCoordinate[i], getMeasuredWidth(), levelCoordinate[i], paint);
                paint.reset();
                paint.setAntiAlias(true);
                paint.setStrokeWidth(2);
                paint.setTextSize(30);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(ContextCompat.getColor(context, R.color.chart_background_line_temp));
                paint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(Integer.toString(temp) + "°", 10, levelCoordinate[i] - 10, paint);

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

    private void drawMaxiTemp(Canvas canvas, float[][] coordinate) {
        Shader linearGradient = new LinearGradient(0, 2 * MARGIN, 0, 7 * MARGIN,
                Color.argb(50, 176, 176, 176), Color.argb(0, 176, 176, 176), Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(5);
        Path pathShadow = new Path();
        pathShadow.moveTo(coordinate[0][0], coordinate[0][1]);
        for (int i = 1; i < 7; i ++) {
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
        paint.setStrokeWidth(5);
        paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_3));
        paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
        Path pathLine = new Path();
        pathLine.moveTo(coordinate[0][0], coordinate[0][1]);
        for (int i = 1; i < 7; i ++) {
            pathLine.lineTo(coordinate[i][0], coordinate[i][1]);
        }
        canvas.drawPath(pathLine, paint);
        canvas.drawCircle(coordinate[0][0], coordinate[0][1], 1, paint);
        canvas.drawCircle(coordinate[6][0], coordinate[6][1], 1, paint);
        paint.reset();
        pathLine.reset();

        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(context, R.color.chart_number));
        paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(40);
        for (int i = 0; i < 7; i ++) {
            canvas.drawText(Integer.toString(maxiTemp[i]) + "°", coordinate[i][0], coordinate[i][1] - 20, paint);
        }
        paint.reset();
    }

    private void drawMiniTemp(Canvas canvas, float[][] coordinate) {
        float startPosition = coordinate[0][1];
        for (int i = 1; i < 7; i ++) {
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
        for (int i = 1; i < 7; i ++) {
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
        paint.setStrokeWidth(5);
        paint.setAlpha(100);
        paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_1));
        paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));

        path.moveTo(coordinate[0][0], coordinate[0][1]);
        for (int i = 1; i < 7; i ++) {
            path.lineTo(coordinate[i][0], coordinate[i][1]);
        }
        canvas.drawPath(path, paint);
        canvas.drawCircle(coordinate[0][0], coordinate[0][1], 1, paint);
        canvas.drawCircle(coordinate[6][0], coordinate[6][1], 1, paint);
        path.reset();
        paint.reset();

        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
        paint.setColor(ContextCompat.getColor(context, R.color.chart_number));
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setShadowLayer(2, 0, 2, Color.argb(200, 176, 176, 176));
        paint.setTextSize(40);
        for (int i = 0; i < 7; i ++) {
            canvas.drawText(Integer.toString(miniTemp[i]) + "°", coordinate[i][0], coordinate[i][1] + 60, paint);
        }
        paint.reset();
    }
}
