package wangdaye.com.geometricweather.view.widget.weatherView.trend;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Trend item view.
 * */

public class TrendItemView extends FrameLayout {
    // widget.
    private Paint paint;
    private Path path;
    private Shader shader;

    // data.
    private int temps[] = new int[2];
    private int[] maxiTempYs = new int[3];
    private int[] miniTempYs = new int[3];

    private int dataType = DATA_TYPE_NULL;
    public static final int DATA_TYPE_NULL = 0;
    public static final int DATA_TYPE_DAILY = 1;
    public static final int DATA_TYPE_HOURLY = -1;

    private int positionType = POSITION_TYPE_NULL;
    public static final int POSITION_TYPE_NULL = 7;
    public static final int POSITION_TYPE_LEFT = -1;
    public static final int POSITION_TYPE_CENTER = 0;
    public static final int POSITION_TYPE_RIGHT = 1;

    private int[] lineColors;
    private int[] shadowColors;
    private int textColor;

    private float MARGIN_TOP;
    private float MARGIN_BOTTOM;
    private float WEATHER_TEXT_SIZE = 13;
    private float POP_TEXT_SIZE = 11;
    private float TREND_LINE_SIZE = 2;
    private float CHART_LINE_SIZE = 1;
    private float MARGIN_TEXT = 2;

    private static final int DRAW_SPEC_MARGIN_TOP = 48;
    private static final int DRAW_SPEC_USABLE_HEIGHT = 96;
    private static final int DRAW_SPEC_MARGIN_BOTTOM = 60;

    /** <br> life cycle. */

    public TrendItemView(Context context) {
        super(context);
        this.initialize();
    }

    public TrendItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public TrendItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TrendItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize();
    }

    private void initialize() {
        setWillNotDraw(false);

        this.lineColors = new int[] {
                ContextCompat.getColor(getContext(), R.color.lightPrimary_5),
                ContextCompat.getColor(getContext(), R.color.darkPrimary_1),
                ContextCompat.getColor(getContext(), R.color.colorLine)};
        this.shadowColors = new int[] {
                Color.argb(50, 176, 176, 176),
                Color.argb(0, 176, 176, 176),
                Color.argb(200, 176, 176, 176)};
        this.textColor = ContextCompat.getColor(getContext(), R.color.colorTextContent);

        this.MARGIN_TOP = calcHeaderHeight(getContext()) + calcDrawSpecMarginTop(getContext());
        this.MARGIN_BOTTOM = calcDrawSpecMarginBottom(getContext());
        this.WEATHER_TEXT_SIZE = DisplayUtils.dpToPx(getContext(), (int) WEATHER_TEXT_SIZE);
        this.POP_TEXT_SIZE = DisplayUtils.dpToPx(getContext(), (int) POP_TEXT_SIZE);
        this.TREND_LINE_SIZE = DisplayUtils.dpToPx(getContext(), (int) TREND_LINE_SIZE);
        this.CHART_LINE_SIZE = DisplayUtils.dpToPx(getContext(), (int) CHART_LINE_SIZE);
        this.MARGIN_TEXT = DisplayUtils.dpToPx(getContext(), (int) MARGIN_TEXT);

        this.paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);

        this.path = new Path();

        this.shader = new LinearGradient(
                0, MARGIN_TOP,
                0, calcHeaderHeight(getContext()) + calcDrawSpecHeight(getContext()) - MARGIN_BOTTOM,
                shadowColors[0], shadowColors[1],
                Shader.TileMode.CLAMP);
    }

    /** <br> UI. */

    // measure.

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        getChildAt(0).measure(
                MeasureSpec.makeMeasureSpec(calcWidth(getContext()), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(calcHeaderHeight(getContext()), MeasureSpec.EXACTLY));
        setMeasuredDimension(
                calcWidth(getContext()),
                calcHeaderHeight(getContext()) + calcDrawSpecHeight(getContext()));
    }

    // draw.

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (dataType) {
            case DATA_TYPE_DAILY:
                drawTimeLine(canvas);
                drawDailyMaxiTemp(canvas);
                drawDailyMiniTemp(canvas);
                break;

            case DATA_TYPE_HOURLY:
                drawTimeLine(canvas);
                drawHourlyPrecipitationData(canvas);
                drawHourlyTemp(canvas);
                break;
        }
    }

    private void drawTimeLine(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(CHART_LINE_SIZE);
        paint.setColor(lineColors[2]);
        canvas.drawLine(
                (float) (getMeasuredWidth() / 2.0), MARGIN_TOP,
                (float) (getMeasuredWidth() / 2.0), getMeasuredHeight() - MARGIN_BOTTOM,
                paint);
    }

    // daily.

    private void drawDailyMaxiTemp(Canvas canvas) {
        switch (positionType) {
            case POSITION_TYPE_NULL:
                return;

            case POSITION_TYPE_LEFT:
                // shadow.
                paint.setShader(shader);
                paint.setStyle(Paint.Style.FILL);
                paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

                path.reset();
                path.moveTo((float) (getMeasuredWidth() / 2.0), maxiTempYs[1]);
                path.lineTo(getMeasuredWidth(), maxiTempYs[2]);
                path.lineTo(getMeasuredWidth(), getMeasuredHeight() - MARGIN_BOTTOM);
                path.lineTo((float) (getMeasuredWidth() / 2.0), getMeasuredHeight() - MARGIN_BOTTOM);
                path.close();
                canvas.drawPath(path, paint);

                // line.
                paint.setShader(null);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(TREND_LINE_SIZE);
                paint.setColor(lineColors[0]);
                paint.setShadowLayer(2, 0, 2, shadowColors[2]);

                path.reset();
                path.moveTo((float) (getMeasuredWidth() / 2.0), maxiTempYs[1]);
                path.lineTo(getMeasuredWidth(), maxiTempYs[2]);
                canvas.drawPath(path, paint);
                break;

            case POSITION_TYPE_RIGHT:
                // shadow.
                paint.setShader(shader);
                paint.setStyle(Paint.Style.FILL);
                paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

                path.reset();
                path.moveTo(0, maxiTempYs[0]);
                path.lineTo((float) (getMeasuredWidth() / 2.0), maxiTempYs[1]);
                path.lineTo((float) (getMeasuredWidth() / 2.0), getMeasuredHeight() - MARGIN_BOTTOM);
                path.lineTo(0, getMeasuredHeight() - MARGIN_BOTTOM);
                path.close();
                canvas.drawPath(path, paint);

                // line.
                paint.setShader(null);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(TREND_LINE_SIZE);
                paint.setColor(lineColors[0]);
                paint.setShadowLayer(2, 0, 2, shadowColors[2]);

                path.reset();
                path.moveTo(0, maxiTempYs[0]);
                path.lineTo((float) (getMeasuredWidth() / 2.0), maxiTempYs[1]);
                canvas.drawPath(path, paint);
                break;

            case POSITION_TYPE_CENTER:
                // shadow.
                paint.setShader(shader);
                paint.setStyle(Paint.Style.FILL);
                paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

                path.reset();
                path.moveTo(0, maxiTempYs[0]);
                path.lineTo((float) (getMeasuredWidth() / 2.0), maxiTempYs[1]);
                path.lineTo(getMeasuredWidth(), maxiTempYs[2]);
                path.lineTo(getMeasuredWidth(), getMeasuredHeight() - MARGIN_BOTTOM);
                path.lineTo(0, getMeasuredHeight() - MARGIN_BOTTOM);
                path.close();
                canvas.drawPath(path, paint);

                // line.
                paint.setShader(null);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(TREND_LINE_SIZE);
                paint.setColor(lineColors[0]);
                paint.setShadowLayer(2, 0, 2, shadowColors[2]);

                path.reset();
                path.moveTo(0, maxiTempYs[0]);
                path.lineTo((float) (getMeasuredWidth() / 2.0), maxiTempYs[1]);
                path.lineTo(getMeasuredWidth(), maxiTempYs[2]);
                canvas.drawPath(path, paint);
                break;
        }

        // text.
        paint.setColor(textColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(WEATHER_TEXT_SIZE);
        paint.setShadowLayer(2, 0, 2, shadowColors[2]);
        canvas.drawText(
                temps[0] + "°",
                (float) (getMeasuredWidth() / 2.0),
                maxiTempYs[1] - paint.getFontMetrics().bottom - MARGIN_TEXT,
                paint);
    }

    private void drawDailyMiniTemp(Canvas canvas) {
        switch (positionType) {
            case POSITION_TYPE_NULL:
                return;

            case POSITION_TYPE_LEFT:
                // line.
                paint.setShader(null);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(TREND_LINE_SIZE);
                paint.setColor(lineColors[1]);
                paint.setShadowLayer(2, 0, 2, shadowColors[2]);

                path.reset();
                path.moveTo((float) (getMeasuredWidth() / 2.0), miniTempYs[1]);
                path.lineTo(getMeasuredWidth(), miniTempYs[2]);
                canvas.drawPath(path, paint);
                break;

            case POSITION_TYPE_RIGHT:
                // line.
                paint.setShader(null);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(TREND_LINE_SIZE);
                paint.setColor(lineColors[1]);
                paint.setShadowLayer(2, 0, 2, shadowColors[2]);

                path.reset();
                path.moveTo(0, miniTempYs[0]);
                path.lineTo((float) (getMeasuredWidth() / 2.0), miniTempYs[1]);
                canvas.drawPath(path, paint);
                break;

            case POSITION_TYPE_CENTER:
                // line.
                paint.setShader(null);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(TREND_LINE_SIZE);
                paint.setColor(lineColors[1]);
                paint.setShadowLayer(2, 0, 2, shadowColors[2]);

                path.reset();
                path.moveTo(0, miniTempYs[0]);
                path.lineTo((float) (getMeasuredWidth() / 2.0), miniTempYs[1]);
                path.lineTo(getMeasuredWidth(), miniTempYs[2]);
                canvas.drawPath(path, paint);
                break;
        }

        // text.
        paint.setColor(textColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(WEATHER_TEXT_SIZE);
        paint.setShadowLayer(2, 0, 2, shadowColors[2]);
        canvas.drawText(
                temps[1] + "°",
                (float) (getMeasuredWidth() / 2.0),
                miniTempYs[1] - paint.getFontMetrics().top + MARGIN_TEXT,
                paint);
    }

    // hourly.

    private void drawHourlyTemp(Canvas canvas) {
        switch (positionType) {
            case POSITION_TYPE_NULL:
                return;

            case POSITION_TYPE_LEFT:
                // shadow.
                paint.setShader(shader);
                paint.setStyle(Paint.Style.FILL);
                paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

                path.reset();
                path.moveTo((float) (getMeasuredWidth() / 2.0), maxiTempYs[1]);
                path.lineTo(getMeasuredWidth(), maxiTempYs[2]);
                path.lineTo(getMeasuredWidth(), getMeasuredHeight() - MARGIN_BOTTOM);
                path.lineTo((float) (getMeasuredWidth() / 2.0), getMeasuredHeight() - MARGIN_BOTTOM);
                path.close();
                canvas.drawPath(path, paint);

                // line.
                paint.setShader(null);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(TREND_LINE_SIZE);
                paint.setColor(lineColors[0]);
                paint.setShadowLayer(2, 0, 2, shadowColors[2]);

                path.reset();
                path.moveTo((float) (getMeasuredWidth() / 2.0), maxiTempYs[1]);
                path.lineTo(getMeasuredWidth(), maxiTempYs[2]);
                canvas.drawPath(path, paint);
                break;

            case POSITION_TYPE_RIGHT:
                // shadow.
                paint.setShader(shader);
                paint.setStyle(Paint.Style.FILL);
                paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

                path.reset();
                path.moveTo(0, maxiTempYs[0]);
                path.lineTo((float) (getMeasuredWidth() / 2.0), maxiTempYs[1]);
                path.lineTo((float) (getMeasuredWidth() / 2.0), getMeasuredHeight() - MARGIN_BOTTOM);
                path.lineTo(0, getMeasuredHeight() - MARGIN_BOTTOM);
                path.close();
                canvas.drawPath(path, paint);

                // line.
                paint.setShader(null);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(TREND_LINE_SIZE);
                paint.setColor(lineColors[0]);
                paint.setShadowLayer(2, 0, 2, shadowColors[2]);

                path.reset();
                path.moveTo(0, maxiTempYs[0]);
                path.lineTo((float) (getMeasuredWidth() / 2.0), maxiTempYs[1]);
                canvas.drawPath(path, paint);
                break;

            case POSITION_TYPE_CENTER:
                // shadow.
                paint.setShader(shader);
                paint.setStyle(Paint.Style.FILL);
                paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

                path.reset();
                path.moveTo(0, maxiTempYs[0]);
                path.lineTo((float) (getMeasuredWidth() / 2.0), maxiTempYs[1]);
                path.lineTo(getMeasuredWidth(), maxiTempYs[2]);
                path.lineTo(getMeasuredWidth(), getMeasuredHeight() - MARGIN_BOTTOM);
                path.lineTo(0, getMeasuredHeight() - MARGIN_BOTTOM);
                path.close();
                canvas.drawPath(path, paint);

                // line.
                paint.setShader(null);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(TREND_LINE_SIZE);
                paint.setColor(lineColors[0]);
                paint.setShadowLayer(2, 0, 2, shadowColors[2]);

                path.reset();
                path.moveTo(0, maxiTempYs[0]);
                path.lineTo((float) (getMeasuredWidth() / 2.0), maxiTempYs[1]);
                path.lineTo(getMeasuredWidth(), maxiTempYs[2]);
                canvas.drawPath(path, paint);
                break;
        }

        // text.
        paint.setColor(textColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(WEATHER_TEXT_SIZE);
        paint.setShadowLayer(2, 0, 2, shadowColors[2]);
        canvas.drawText(
                temps[0] + "°",
                (float) (getMeasuredWidth() / 2.0),
                maxiTempYs[1] - paint.getFontMetrics().bottom - MARGIN_TEXT,
                paint);
    }

    private void drawHourlyPrecipitationData(Canvas canvas) {
        paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

        paint.setColor(lineColors[1]);
        paint.setAlpha((int) (255 * 0.1));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(
                new RectF(
                        (float) (getMeasuredWidth() / 2.0 - TREND_LINE_SIZE * 1.5),
                        miniTempYs[0],
                        (float) (getMeasuredWidth() / 2.0 + TREND_LINE_SIZE * 1.5),
                        getMeasuredHeight() - MARGIN_BOTTOM),
                TREND_LINE_SIZE * 3, TREND_LINE_SIZE * 3,
                paint);

        if (temps[1] != 0) {
            paint.setAlpha((int) (255 * 0.2));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(POP_TEXT_SIZE);
            canvas.drawText(
                    temps[1] + "%",
                    (float) (getMeasuredWidth() / 2.0),
                    getMeasuredHeight() - MARGIN_BOTTOM - paint.getFontMetrics().top + MARGIN_TEXT,
                    paint);
        }

        paint.setAlpha(255);
    }

    /** <br> data. */

    // init.

    public void setData(Weather weather, int dataType, int position, int highest, int lowest) {
        this.dataType = dataType;
        switch (dataType) {
            case DATA_TYPE_DAILY:
                setDailyData(weather, position, highest, lowest);
                break;

            case DATA_TYPE_HOURLY:
                setHourlyData(weather, position, highest, lowest);
                break;
        }
    }

    public void setNullData() {
        this.dataType = DATA_TYPE_NULL;
    }

    // daily.

    private void setDailyData(Weather weather, int position, int highest, int lowest) {
        this.temps = new int[] {
                weather.dailyList.get(position).temps[0],
                weather.dailyList.get(position).temps[1]};
        if (position == 0) {
            positionType = POSITION_TYPE_LEFT;
            setDailyLeftData(weather, position, highest, lowest);
        } else if (position == weather.dailyList.size() - 1) {
            positionType = POSITION_TYPE_RIGHT;
            setDailyRightData(weather, position, highest, lowest);
        } else {
            positionType = POSITION_TYPE_CENTER;
            setDailyCenterData(weather, position, highest, lowest);
        }
    }

    private void setDailyLeftData(Weather weather, int position, int highest, int lowest) {
        float[] maxiTemps = new float[] {
                0,
                weather.dailyList.get(position).temps[0],
                (float) ((weather.dailyList.get(position).temps[0] + weather.dailyList.get(position + 1).temps[0]) / 2.0)};
        for (int i = 0; i < maxiTemps.length; i ++) {
            maxiTempYs[i] = (int) (calcHeaderHeight(getContext()) + calcDrawSpecHeight(getContext()) - MARGIN_BOTTOM
                    - calcDrawSpecUsableHeight(getContext()) * (maxiTemps[i] - lowest) / (highest - lowest));
        }

        float[] miniTemps = new float[] {
                0,
                weather.dailyList.get(position).temps[1],
                (float) ((weather.dailyList.get(position).temps[1] + weather.dailyList.get(position + 1).temps[1]) / 2.0)};
        for (int i = 0; i < miniTemps.length; i ++) {
            miniTempYs[i] = (int) (calcHeaderHeight(getContext()) + calcDrawSpecHeight(getContext()) - MARGIN_BOTTOM
                    - calcDrawSpecUsableHeight(getContext()) * (miniTemps[i] - lowest) / (highest - lowest));
        }
    }

    private void setDailyRightData(Weather weather, int position, int highest, int lowest) {
        float[] maxiTemps = new float[] {
                (float) ((weather.dailyList.get(position - 1).temps[0] + weather.dailyList.get(position).temps[0]) / 2.0),
                weather.dailyList.get(position).temps[0],
                0};
        for (int i = 0; i < maxiTemps.length; i ++) {
            maxiTempYs[i] = (int) (calcHeaderHeight(getContext()) + calcDrawSpecHeight(getContext()) - MARGIN_BOTTOM
                    - calcDrawSpecUsableHeight(getContext()) * (maxiTemps[i] - lowest) / (highest - lowest));
        }

        float[] miniTemps = new float[] {
                (float) ((weather.dailyList.get(position - 1).temps[1] + weather.dailyList.get(position).temps[1]) / 2.0),
                weather.dailyList.get(position).temps[1],
                0};
        for (int i = 0; i < miniTemps.length; i ++) {
            miniTempYs[i] = (int) (calcHeaderHeight(getContext()) + calcDrawSpecHeight(getContext()) - MARGIN_BOTTOM
                    - calcDrawSpecUsableHeight(getContext()) * (miniTemps[i] - lowest) / (highest - lowest));
        }
    }

    private void setDailyCenterData(Weather weather, int position, int highest, int lowest) {
        float[] maxiTemps = new float[] {
                (float) ((weather.dailyList.get(position - 1).temps[0] + weather.dailyList.get(position).temps[0]) / 2.0),
                weather.dailyList.get(position).temps[0],
                (float) ((weather.dailyList.get(position).temps[0] + weather.dailyList.get(position + 1).temps[0]) / 2.0)};
        for (int i = 0; i < maxiTemps.length; i ++) {
            maxiTempYs[i] = (int) (calcHeaderHeight(getContext()) + calcDrawSpecHeight(getContext()) - MARGIN_BOTTOM
                    - calcDrawSpecUsableHeight(getContext()) * (maxiTemps[i] - lowest) / (highest - lowest));
        }

        float[] miniTemps = new float[] {
                (float) ((weather.dailyList.get(position - 1).temps[1] + weather.dailyList.get(position).temps[1]) / 2.0),
                weather.dailyList.get(position).temps[1],
                (float) ((weather.dailyList.get(position).temps[1] + weather.dailyList.get(position + 1).temps[1]) / 2.0)};
        for (int i = 0; i < miniTemps.length; i ++) {
            miniTempYs[i] = (int) (calcHeaderHeight(getContext()) + calcDrawSpecHeight(getContext()) - MARGIN_BOTTOM
                    - calcDrawSpecUsableHeight(getContext()) * (miniTemps[i] - lowest) / (highest - lowest));
        }
    }

    // hourly.

    public void setHourlyData(Weather weather, int position, int highest, int lowest) {
        this.temps = new int[] {
                weather.hourlyList.get(position).temp,
                weather.hourlyList.get(position).precipitation};
        if (position == 0) {
            positionType = POSITION_TYPE_LEFT;
            setHourlyLeftData(weather, position, highest, lowest);
        } else if (position == weather.hourlyList.size() - 1) {
            positionType = POSITION_TYPE_RIGHT;
            setHourlyRightData(weather, position, highest, lowest);
        } else {
            positionType = POSITION_TYPE_CENTER;
            setHourlyCenterData(weather, position, highest, lowest);
        }
        serPrecipitationData(weather, position);
    }

    private void setHourlyLeftData(Weather weather, int position, int highest, int lowest) {
        float[] temps = new float[] {
                0,
                weather.hourlyList.get(position).temp,
                (float) ((weather.hourlyList.get(position).temp + weather.hourlyList.get(position + 1).temp) / 2.0)};
        for (int i = 0; i < temps.length; i ++) {
            maxiTempYs[i] = (int) (calcHeaderHeight(getContext()) + calcDrawSpecHeight(getContext()) - MARGIN_BOTTOM
                    - calcDrawSpecUsableHeight(getContext()) * (temps[i] - lowest) / (highest - lowest));
        }
    }

    private void setHourlyRightData(Weather weather, int position, int highest, int lowest) {
        float[] temps = new float[] {
                (float) ((weather.hourlyList.get(position - 1).temp + weather.hourlyList.get(position).temp) / 2.0),
                weather.hourlyList.get(position).temp,
                0};
        for (int i = 0; i < temps.length; i ++) {
            maxiTempYs[i] = (int) (calcHeaderHeight(getContext()) + calcDrawSpecHeight(getContext()) - MARGIN_BOTTOM
                    - calcDrawSpecUsableHeight(getContext()) * (temps[i] - lowest) / (highest - lowest));
        }
    }

    private void setHourlyCenterData(Weather weather, int position, int highest, int lowest) {
        float[] temps = new float[] {
                (float) ((weather.hourlyList.get(position - 1).temp + weather.hourlyList.get(position).temp) / 2.0),
                weather.hourlyList.get(position).temp,
                (float) ((weather.hourlyList.get(position).temp + weather.hourlyList.get(position + 1).temp) / 2.0)};
        for (int i = 0; i < temps.length; i ++) {
            maxiTempYs[i] = (int) (calcHeaderHeight(getContext()) + calcDrawSpecHeight(getContext()) - MARGIN_BOTTOM
                    - calcDrawSpecUsableHeight(getContext()) * (temps[i] - lowest) / (highest - lowest));
        }
    }

    private void serPrecipitationData(Weather weather, int position) {
        miniTempYs[0] = (int) (calcHeaderHeight(getContext()) + calcDrawSpecHeight(getContext()) - MARGIN_BOTTOM
                - calcDrawSpecUsableHeight(getContext()) * weather.hourlyList.get(position).precipitation / 100.0);
    }

    public static int calcWidth(Context context) {
        return (int) ((context.getResources().getDisplayMetrics().widthPixels - 2.0 * DisplayUtils.dpToPx(context, 8)) / 7.0);
    }

    public static int calcHeaderHeight(Context context) {
        return (int) (DisplayUtils.dpToPx(context, 2 * 8 + 14)
                + Math.min(calcWidth(context), DisplayUtils.dpToPx(context, 42 + 2 * 8)));
    }

    public static int calcDrawSpecHeight(Context context) {
        return (int) DisplayUtils.dpToPx(
                context,
                DRAW_SPEC_MARGIN_TOP + DRAW_SPEC_USABLE_HEIGHT + DRAW_SPEC_MARGIN_BOTTOM);
    }

    public static int calcDrawSpecMarginTop(Context context) {
        return (int) DisplayUtils.dpToPx(
                context,
                DRAW_SPEC_MARGIN_TOP);
    }

    public static int calcDrawSpecUsableHeight(Context context) {
        return (int) DisplayUtils.dpToPx(
                context,
                DRAW_SPEC_USABLE_HEIGHT);
    }

    public static int calcDrawSpecMarginBottom(Context context) {
        return (int) DisplayUtils.dpToPx(
                context,
                DRAW_SPEC_MARGIN_BOTTOM);
    }
}
