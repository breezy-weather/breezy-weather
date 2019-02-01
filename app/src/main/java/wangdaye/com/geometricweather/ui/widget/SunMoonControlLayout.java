package wangdaye.com.geometricweather.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

public class SunMoonControlLayout extends ViewGroup {

    // 0 - day / 1 - night.
    @Size(2) private AppCompatImageView[] indicators;

    private Paint paint;
    private Shader x1Shader;
    private Shader x2Shader;
    private PathEffect effect;
    private RectF rectF;

    @Size(2) private int[] startTimes;
    @Size(2) private int[] currentTimes;
    @Size(2) private int[] endTimes;
    @Size(2) private int[] progresses;
    @Size(2) private int[] maxes;

    @Size(3) private int[] lineColors;
    @Size(3)private int[] shadowColors;

    private float LINE_SIZE = 2;
    private float DOTTED_LINE_SIZE = 1;
    private float MARGIN = 16;

    private static final int ARC_ANGLE = 135;

    public SunMoonControlLayout(@NonNull Context context) {
        super(context);
        this.initialize();
    }

    public SunMoonControlLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public SunMoonControlLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    private void initialize() {
        setWillNotDraw(false);

        this.indicators = new AppCompatImageView[] {
                new AppCompatImageView(getContext()), new AppCompatImageView(getContext())};
        Glide.with(getContext())
                .load(WeatherHelper.getWeatherIcon(WeatherHelper.KIND_CLEAR, true)[3])
                .into(indicators[0]);
        Glide.with(getContext())
                .load(WeatherHelper.getWeatherIcon(WeatherHelper.KIND_CLEAR, false)[3])
                .into(indicators[1]);
        addView(indicators[1]);
        addView(indicators[0]);

        this.lineColors = new int[] {
                ContextCompat.getColor(getContext(), R.color.colorPrimaryDark),
                ContextCompat.getColor(getContext(), R.color.colorPrimary),
                ContextCompat.getColor(getContext(), R.color.colorLine)};
        this.shadowColors = new int[] {
                Color.argb(33, 176, 176, 176),
                Color.argb(0, 176, 176, 176),
                Color.argb((int) (255 * 0.2), 0, 0, 0)};
        this.startTimes = new int[] {decodeTime("6:00"), decodeTime("18:00")};
        this.endTimes = new int[] {decodeTime("18:00"), decodeTime("6:00") + 24 * 60};
        this.currentTimes = new int[] {decodeTime("12:00"), decodeTime("12:00")};
        this.progresses = new int[] {-1, -1};
        this.maxes = new int[] {100, 100};

        LINE_SIZE = DisplayUtils.dpToPx(getContext(), (int) LINE_SIZE);
        DOTTED_LINE_SIZE = DisplayUtils.dpToPx(getContext(), (int) DOTTED_LINE_SIZE);
        MARGIN = DisplayUtils.dpToPx(getContext(), (int) MARGIN);

        this.paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);

        this.effect = new DashPathEffect(
                new float[] {
                        DisplayUtils.dpToPx(getContext(), 3),
                        2 * DisplayUtils.dpToPx(getContext(), 3)},
                0);
        this.rectF = new RectF();
    }

    public static int decodeTime(String time) {
        String[] t = time.split(":");
        return Integer.parseInt(t[0]) * 60 + Integer.parseInt(t[1]);
    }

    public void setTime(@Size(2) int[] startTimes, @Size(2) int[] endTimes, @Size(2) int[] currentTimes) {
        this.startTimes = startTimes;
        this.endTimes = endTimes;
        this.currentTimes = currentTimes;

        invalidate();
        setIndicatorPosition(0);
        setIndicatorPosition(1);
    }

    public void setLineColors(@Size(3) int[] colors) {
        this.lineColors = colors;
        invalidate();
    }

    public void setDayIndicatorRotation(int rotation) {
        indicators[0].setRotation(rotation);
    }

    public void setNightIndicatorRotation(int rotation) {
        indicators[1].setRotation(rotation);
    }

    private void ensureProgress(int index) {
        maxes[index] = endTimes[index] - startTimes[index];
        progresses[index] = currentTimes[index] - startTimes[index];
        progresses[index] = Math.max(progresses[index], 0);
        progresses[index] = Math.min(progresses[index], maxes[index]);
    }

    public void ensureShader(Context context) {
        if (DisplayUtils.isDarkMode(context)) {
            x1Shader = new LinearGradient(
                    0, rectF.top,
                    0, getMeasuredHeight() - MARGIN,
                    Color.rgb(51, 51, 51),
                    ContextCompat.getColor(context, R.color.colorRoot),
                    Shader.TileMode.CLAMP);
            x2Shader = new LinearGradient(
                    0, rectF.top,
                    0, getMeasuredHeight() - MARGIN,
                    Color.rgb(61, 61, 61),
                    ContextCompat.getColor(context, R.color.colorRoot),
                    Shader.TileMode.CLAMP);
        } else {
            x1Shader = new LinearGradient(
                    0, rectF.top,
                    0, getMeasuredHeight() - MARGIN,
                    Color.rgb(235, 235, 235),
                    ContextCompat.getColor(context, R.color.colorRoot),
                    Shader.TileMode.CLAMP);
            x2Shader = new LinearGradient(
                    0, rectF.top,
                    0, getMeasuredHeight() - MARGIN,
                    Color.rgb(223, 223, 223),
                    ContextCompat.getColor(context, R.color.colorRoot),
                    Shader.TileMode.CLAMP);
        }
    }

    private void setIndicatorPosition(int index) {
        ensureProgress(index);
        float startAngle = 270 - ARC_ANGLE / 2f;
        float progressSweepAngle = (float) (1.0 * progresses[index] / maxes[index] * ARC_ANGLE);
        float progressEndAngle = startAngle + progressSweepAngle;
        float deltaAngle = progressEndAngle - 180;
        float deltaWidth = (float) Math.abs(rectF.width() / 2 * Math.cos(Math.toRadians(deltaAngle)));
        float deltaHeight = (float) Math.abs(rectF.width() / 2 * Math.sin(Math.toRadians(deltaAngle)));

        if (progressSweepAngle == 0 && indicators[index].getAlpha() != 0) {
            indicators[index].setAlpha(0f);
        } else if (progressSweepAngle != 0 && indicators[index].getAlpha() == 0) {
            indicators[index].setAlpha(1f);
        }

        if (progressEndAngle < 270) {
            indicators[index].setTranslationX(
                    rectF.centerX() - deltaWidth - indicators[index].getMeasuredWidth() / 2f);
            indicators[index].setTranslationY(
                    rectF.centerY() - deltaHeight - indicators[index].getMeasuredHeight() / 2f);
        } else {
            indicators[index].setTranslationX(
                    rectF.centerX() + deltaWidth - indicators[index].getMeasuredWidth() / 2f);
            indicators[index].setTranslationY(
                    rectF.centerY() - deltaHeight - indicators[index].getMeasuredHeight() / 2f);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) (MeasureSpec.getSize(widthMeasureSpec) - 2 * MARGIN);
        double deltaRadians = Math.toRadians((180 - ARC_ANGLE) / 2d);
        int radius = (int) (width / 2 / Math.cos(deltaRadians));
        int height = (int) (radius - width / 2 * Math.tan(deltaRadians));
        setMeasuredDimension(
                MeasureSpec.makeMeasureSpec((int) (width + 2 * MARGIN), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec((int) (height + 2 * MARGIN), MeasureSpec.EXACTLY));

        int indicatorSize = (int) DisplayUtils.dpToPx(getContext(), 24);
        indicators[0].measure(
                MeasureSpec.makeMeasureSpec(indicatorSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(indicatorSize, MeasureSpec.EXACTLY));
        indicators[1].measure(
                MeasureSpec.makeMeasureSpec(indicatorSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(indicatorSize, MeasureSpec.EXACTLY));

        int centerX = getMeasuredWidth() / 2;
        int centerY = (int) (MARGIN + radius);
        rectF.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed || x1Shader == null || x2Shader == null) {
            ensureShader(getContext());
        }
        indicators[0].layout(0, 0, indicators[0].getMeasuredWidth(), indicators[0].getMeasuredHeight());
        indicators[0].setAlpha(0f);
        indicators[1].layout(0, 0, indicators[1].getMeasuredWidth(), indicators[1].getMeasuredHeight());
        indicators[1].setAlpha(0f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // shadow.
        float startAngle = 270 - ARC_ANGLE / 2f;
        float progressSweepAngleDay = (float) (1.0 * progresses[0] / maxes[0] * ARC_ANGLE);
        float progressEndAngleDay = startAngle + progressSweepAngleDay;
        float progressSweepAngleNight = (float) (1.0 * progresses[1] / maxes[1] * ARC_ANGLE);
        float progressEndAngleNight = startAngle + progressSweepAngleNight;
        if (progressEndAngleDay == progressEndAngleNight) {
            drawShadow(canvas, 0, progressEndAngleDay, x2Shader);
        } else if (progressEndAngleDay > progressEndAngleNight) {
            drawShadow(canvas, 0, progressEndAngleDay, x1Shader);
            drawShadow(canvas, 1, progressEndAngleNight, x2Shader);
        } else { // progressEndAngleDay < progressEndAngleNight
            drawShadow(canvas, 1, progressEndAngleNight, x1Shader);
            drawShadow(canvas, 0, progressEndAngleDay, x2Shader);
        }

        // sub line.
        paint.setColor(lineColors[2]);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(DOTTED_LINE_SIZE);
        paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
        paint.setShader(null);
        paint.setPathEffect(effect);
        canvas.drawArc(rectF, startAngle, ARC_ANGLE, false, paint);
        canvas.drawLine(
                MARGIN,
                getMeasuredHeight() - MARGIN,
                getMeasuredWidth() - MARGIN,
                getMeasuredHeight() - MARGIN,
                paint);

        // path.
        drawPathLine(canvas, 1, startAngle, (float) (1.0 * progresses[1] / maxes[1] * ARC_ANGLE));
        drawPathLine(canvas, 0, startAngle, (float) (1.0 * progresses[0] / maxes[0] * ARC_ANGLE));
    }

    private void drawShadow(Canvas canvas, int index, float progressEndAngle, Shader shader) {
        if (progresses[index] > 0) {
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(shader);
            canvas.drawArc(
                    rectF,
                    360 - progressEndAngle,
                    360 - 2 * (360 - progressEndAngle),
                    false,
                    paint);
        }
    }

    private void drawPathLine(Canvas canvas, int index, float startAngle, float progressSweepAngle) {
        if (progresses[index] > 0) {
            paint.setColor(lineColors[index]);
            paint.setStrokeWidth(LINE_SIZE);
            paint.setPathEffect(null);
            paint.setShadowLayer(
                    2,
                    0,
                    2,
                    shadowColors[2]);
            canvas.drawArc(rectF, startAngle, progressSweepAngle, false, paint);
        }
    }
}
