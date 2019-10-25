package wangdaye.com.geometricweather.ui.widget.astro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.View;

import wangdaye.com.geometricweather.utils.DisplayUtils;

public class MoonPhaseView extends View {

    private Paint paint;
    private RectF foregroundRectF = new RectF();
    private RectF backgroundRectF = new RectF();

    private float surfaceAngle; // head of light surface, clockwise.
    @ColorInt private int lightColor;
    @ColorInt private int darkColor;
    @ColorInt private int strokeColor;

    private float LINE_WIDTH = 1;

    public MoonPhaseView(Context context) {
        this(context, null);
    }

    public MoonPhaseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoonPhaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
        initPaint();
    }

    private void initialize() {
        setColor(Color.WHITE, Color.BLACK, Color.GRAY);
        setSurfaceAngle(0); // from 0 -> phase : 🌑 (new)

        foregroundRectF = new RectF();
        backgroundRectF = new RectF();

        LINE_WIDTH = DisplayUtils.dpToPx(getContext(), (int) LINE_WIDTH);
    }

    private void initPaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setColor(@ColorInt int lightColor, @ColorInt int darkColor,
                         @ColorInt int strokeColor) {
        this.lightColor = lightColor;
        this.darkColor = darkColor;
        this.strokeColor = strokeColor;
    }

    public void setSurfaceAngle(float surfaceAngle) {
        this.surfaceAngle = surfaceAngle;
        if (this.surfaceAngle >= 360) {
            this.surfaceAngle %= 360;
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        int padding = (int) DisplayUtils.dpToPx(getContext(), 4);
        backgroundRectF.set(
                padding,
                padding,
                getMeasuredWidth() - padding,
                getMeasuredHeight() - padding
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setStyle(Paint.Style.FILL);
        if (surfaceAngle == 0) { // 🌑
            drawDarkCircle(canvas);
        } else if (surfaceAngle < 90) { // 🌒
            drawLightCircle(canvas);

            paint.setColor(darkColor);
            canvas.drawArc(backgroundRectF, 90, 180, true, paint);
            float halfWidth = (float) (
                    backgroundRectF.width() / 2 * Math.cos(Math.toRadians(surfaceAngle))
            );
            foregroundRectF.set(
                    backgroundRectF.centerX() - halfWidth,
                    backgroundRectF.top,
                    backgroundRectF.centerX() + halfWidth,
                    backgroundRectF.bottom
            );
            canvas.drawArc(foregroundRectF, 270, 180, true, paint);
        } else if (surfaceAngle == 90) { // 🌓
            drawDarkCircle(canvas);
            paint.setColor(lightColor);
            canvas.drawArc(backgroundRectF, 270, 180, true, paint);
        } else if (surfaceAngle < 180) { // 🌔
            drawDarkCircle(canvas);

            paint.setColor(lightColor);
            canvas.drawArc(backgroundRectF, 270, 180, true, paint);
            float halfWidth = (float) (
                    backgroundRectF.width() / 2 * Math.sin(Math.toRadians(surfaceAngle - 90))
            );
            foregroundRectF.set(
                    backgroundRectF.centerX() - halfWidth,
                    backgroundRectF.top,
                    backgroundRectF.centerX() + halfWidth,
                    backgroundRectF.bottom
            );
            canvas.drawArc(foregroundRectF, 90, 180, true, paint);
        } else if (surfaceAngle == 180) { // 🌕
            drawLightCircle(canvas);
        } else if (surfaceAngle < 270) { // 🌖
            drawDarkCircle(canvas);

            paint.setColor(lightColor);
            canvas.drawArc(backgroundRectF, 90, 180, true, paint);
            float halfWidth = (float) (
                    backgroundRectF.width() / 2 * Math.cos(Math.toRadians(surfaceAngle - 180))
            );
            foregroundRectF.set(
                    backgroundRectF.centerX() - halfWidth,
                    backgroundRectF.top,
                    backgroundRectF.centerX() + halfWidth,
                    backgroundRectF.bottom
            );
            canvas.drawArc(foregroundRectF, 270, 180, true, paint);
        } else if (surfaceAngle == 270) { // 🌗
            drawDarkCircle(canvas);
            paint.setColor(lightColor);
            canvas.drawArc(backgroundRectF, 90, 180, true, paint);
        } else { // surface angle < 360. 🌘
            drawLightCircle(canvas);

            paint.setColor(darkColor);
            canvas.drawArc(backgroundRectF, 270, 180, true, paint);
            float halfWidth = (float) (
                    backgroundRectF.width() / 2 * Math.cos(Math.toRadians(360 - surfaceAngle))
            );
            foregroundRectF.set(
                    backgroundRectF.centerX() - halfWidth,
                    backgroundRectF.top,
                    backgroundRectF.centerX() + halfWidth,
                    backgroundRectF.bottom
            );
            canvas.drawArc(foregroundRectF, 90, 180, true, paint);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(LINE_WIDTH);
        if (surfaceAngle < 90 || 270 < surfaceAngle) {
            paint.setColor(darkColor);
            canvas.drawLine(
                    backgroundRectF.centerX(), backgroundRectF.top,
                    backgroundRectF.centerX(), backgroundRectF.bottom,
                    paint
            );
        } else if (90 < surfaceAngle && surfaceAngle < 270) {
            paint.setColor(lightColor);
            canvas.drawLine(
                    backgroundRectF.centerX(), backgroundRectF.top,
                    backgroundRectF.centerX(), backgroundRectF.bottom,
                    paint
            );
        }
        paint.setColor(strokeColor);
        canvas.drawCircle(
                backgroundRectF.centerX(),
                backgroundRectF.centerY(),
                backgroundRectF.width() / 2,
                paint
        );
    }

    private void drawLightCircle(Canvas canvas) {
        paint.setColor(lightColor);
        canvas.drawCircle(
                backgroundRectF.centerX(),
                backgroundRectF.centerY(),
                backgroundRectF.width() / 2,
                paint
        );
    }

    private void drawDarkCircle(Canvas canvas) {
        paint.setColor(darkColor);
        canvas.drawCircle(
                backgroundRectF.centerX(),
                backgroundRectF.centerY(),
                backgroundRectF.width() / 2,
                paint
        );
    }
}
