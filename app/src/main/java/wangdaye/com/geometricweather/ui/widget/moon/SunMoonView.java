package wangdaye.com.geometricweather.ui.widget.moon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.core.content.ContextCompat;

import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.view.ViewCompat;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public class SunMoonView extends View {

    // 0 - day / 1 - night.
    @Size(2) private Drawable[] iconDrawables;

    private Paint paint;
    private Xfermode clearXfermode;
    private Shader x1Shader;
    private Shader x2Shader;
    private PathEffect effect;
    private RectF rectF;

    @Size(2) private float[] iconRotations;
    @Size(2) private float[] iconAlphas;
    @Size(2) private float[][] iconPositions;

    @Size(2) private float[] startTimes;
    @Size(2) private float[] currentTimes;
    @Size(2) private float[] endTimes;
    @Size(2) private float[] progresses;
    @Size(2) private float[] maxes;

    @Size(3) private int[] lineColors;
    @Size(3)private int[] shadowColors;

    private float lineSize;
    private float dottedLineSize;
    private float margin;

    private final static int ICON_SIZE_DIP = 24;
    private final static int LINE_SIZE_DIP = 2;
    private final static int DOTTED_LINE_SIZE_DIP = 1;
    private final static int MARGIN_DIP = 16;

    private static final int ARC_ANGLE = 135;

    public SunMoonView(@NonNull Context context) {
        super(context);
        this.initialize();
    }

    public SunMoonView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public SunMoonView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    private void initialize() {
        this.iconDrawables = new Drawable[2];

        this.iconRotations = new float[] {0, 0};
        this.iconAlphas = new float[] {0, 0};
        this.iconPositions = new float[][] {{0, 0}, {0, 0}};

        this.startTimes = new float[] {decodeTime("6:00"), decodeTime("18:00")};
        this.endTimes = new float[] {decodeTime("18:00"), decodeTime("6:00") + 24 * 60};
        this.currentTimes = new float[] {decodeTime("12:00"), decodeTime("12:00")};
        this.progresses = new float[] {-1, -1};
        this.maxes = new float[] {100, 100};

        this.lineColors = new int[] {
                ContextCompat.getColor(getContext(), R.color.colorPrimaryDark),
                ContextCompat.getColor(getContext(), R.color.colorPrimary),
                ContextCompat.getColor(getContext(), R.color.colorLine)
        };
        this.shadowColors = new int[] {
                Color.argb(33, 176, 176, 176),
                Color.argb(0, 176, 176, 176),
                Color.argb((int) (255 * 0.2), 0, 0, 0)
        };

        lineSize = DisplayUtils.dpToPx(getContext(), LINE_SIZE_DIP);
        dottedLineSize = DisplayUtils.dpToPx(getContext(), DOTTED_LINE_SIZE_DIP);
        margin = DisplayUtils.dpToPx(getContext(), MARGIN_DIP);

        this.paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);

        this.clearXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

        this.effect = new DashPathEffect(
                new float[] {
                        DisplayUtils.dpToPx(getContext(), 3),
                        2 * DisplayUtils.dpToPx(getContext(), 3)
                }, 0
        );
        this.rectF = new RectF();
    }

    public static int decodeTime(String time) {
        String[] t = time.split(":");
        return Integer.parseInt(t[0]) * 60 + Integer.parseInt(t[1]);
    }

    public void setTime(@Size(2) float[] startTimes, @Size(2) float[] endTimes, @Size(2) float[] currentTimes) {
        this.startTimes = startTimes;
        this.endTimes = endTimes;
        this.currentTimes = currentTimes;

        setIndicatorPosition(0);
        setIndicatorPosition(1);

        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setColors(@Size(3) int[] colors) {
        this.lineColors = colors;
        ensureShader();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setDayIndicatorRotation(float rotation) {
        iconRotations[0] = rotation;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setNightIndicatorRotation(float rotation) {
        iconRotations[1] = rotation;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void ensureShader() {
        int rootColor = ContextCompat.getColor(getContext(), R.color.colorRoot);
        if (DisplayUtils.isDarkMode(getContext())) {
            x1Shader = new LinearGradient(
                    0, rectF.top,
                    0, getMeasuredHeight() - margin,
                    Color.rgb(51, 51, 51),
                    rootColor,
                    Shader.TileMode.CLAMP
            );
            x2Shader = new LinearGradient(
                    0, rectF.top,
                    0, getMeasuredHeight() - margin,
                    Color.rgb(61, 61, 61),
                    rootColor,
                    Shader.TileMode.CLAMP
            );
        } else {
            x1Shader = new LinearGradient(
                    0, rectF.top,
                    0, getMeasuredHeight() - margin,
                    Color.rgb(235, 235, 235),
                    rootColor,
                    Shader.TileMode.CLAMP
            );
            x2Shader = new LinearGradient(
                    0, rectF.top,
                    0, getMeasuredHeight() - margin,
                    Color.rgb(223, 223, 223),
                    rootColor,
                    Shader.TileMode.CLAMP
            );
        }
    }

    private void ensureProgress(int index) {
        maxes[index] = endTimes[index] - startTimes[index];
        progresses[index] = currentTimes[index] - startTimes[index];
        progresses[index] = Math.max(progresses[index], 0);
        progresses[index] = Math.min(progresses[index], maxes[index]);
    }

    private void setIndicatorPosition(int index) {
        ensureProgress(index);
        float startAngle = 270 - ARC_ANGLE / 2f;
        float progressSweepAngle = (float) (1.0 * progresses[index] / maxes[index] * ARC_ANGLE);
        float progressEndAngle = startAngle + progressSweepAngle;
        float deltaAngle = progressEndAngle - 180;
        float deltaWidth = (float) Math.abs(rectF.width() / 2 * Math.cos(Math.toRadians(deltaAngle)));
        float deltaHeight = (float) Math.abs(rectF.width() / 2 * Math.sin(Math.toRadians(deltaAngle)));

        if (progressSweepAngle == 0 && iconAlphas[index] != 0) {
            iconAlphas[index] = 0;
        } else if (progressSweepAngle != 0 && iconAlphas[index] == 0) {
            iconAlphas[index] = 1;
        }

        if (iconDrawables[index] != null) {
            if (progressEndAngle < 270) {
                iconPositions[index][0] = rectF.centerX()
                        - deltaWidth
                        - iconDrawables[index].getIntrinsicWidth() / 2f;
                iconPositions[index][1] = rectF.centerY()
                        - deltaHeight
                        - iconDrawables[index].getIntrinsicWidth() / 2f;
            } else {
                iconPositions[index][0] = rectF.centerX()
                        + deltaWidth
                        - iconDrawables[index].getIntrinsicWidth() / 2f;
                iconPositions[index][1] = rectF.centerY()
                        - deltaHeight
                        - iconDrawables[index].getIntrinsicWidth() / 2f;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) (MeasureSpec.getSize(widthMeasureSpec) - 2 * margin);
        double deltaRadians = Math.toRadians((180 - ARC_ANGLE) / 2d);
        int radius = (int) (width / 2 / Math.cos(deltaRadians));
        int height = (int) (radius - width / 2 * Math.tan(deltaRadians));
        setMeasuredDimension(
                MeasureSpec.makeMeasureSpec((int) (width + 2 * margin), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec((int) (height + 2 * margin), MeasureSpec.EXACTLY)
        );

        int centerX = getMeasuredWidth() / 2;
        int centerY = (int) (margin + radius);
        rectF.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );

        ensureShader();
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
        paint.setStrokeWidth(dottedLineSize);
        paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
        paint.setPathEffect(effect);
        canvas.drawArc(rectF, startAngle, ARC_ANGLE, false, paint);
        canvas.drawLine(
                margin,
                getMeasuredHeight() - margin,
                getMeasuredWidth() - margin,
                getMeasuredHeight() - margin,
                paint
        );

        // path.
        drawPathLine(canvas, 1, startAngle, (float) (1.0 * progresses[1] / maxes[1] * ARC_ANGLE));
        drawPathLine(canvas, 0, startAngle, (float) (1.0 * progresses[0] / maxes[0] * ARC_ANGLE));

        int restoreCount;

        // icon.
        for (int i = 1; i >= 0; i --) {
            if (iconDrawables[i] == null || progresses[i] <= 0) {
                continue;
            }
            restoreCount = canvas.save();
            canvas.translate(iconPositions[i][0], iconPositions[i][1]);
            canvas.rotate(
                    iconRotations[i],
                    iconDrawables[i].getIntrinsicWidth() / 2f,
                    iconDrawables[i].getIntrinsicHeight() / 2f
            );
            iconDrawables[i].draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
    }

    private void drawShadow(Canvas canvas, int index, float progressEndAngle, Shader shader) {
        if (progresses[index] > 0) {
            int layerId = canvas.saveLayer(
                    rectF.left, rectF.top, rectF.right, rectF.top + rectF.height() / 2,
                    null, Canvas.ALL_SAVE_FLAG
            );

            paint.setStyle(Paint.Style.FILL);
            paint.setShader(shader);
            canvas.drawArc(
                    rectF,
                    270 - ARC_ANGLE / 2f,
                    ARC_ANGLE,
                    false,
                    paint
            );
            paint.setShader(null);

            paint.setXfermode(clearXfermode);
            canvas.drawRect(
                    (float) (
                            rectF.centerX() + rectF.width() / 2
                                    * Math.cos((360 - progressEndAngle) * Math.PI / 180)
                    ),
                    rectF.top,
                    rectF.right,
                    rectF.top + rectF.height() / 2,
                    paint
            );
            paint.setXfermode(null);

            canvas.restoreToCount(layerId);
        }
    }

    private void drawPathLine(Canvas canvas, int index, float startAngle, float progressSweepAngle) {
        if (progresses[index] > 0) {
            paint.setColor(lineColors[index]);
            paint.setStrokeWidth(lineSize);
            paint.setPathEffect(null);
            paint.setShadowLayer(
                    2,
                    0,
                    2,
                    shadowColors[2]
            );
            canvas.drawArc(rectF, startAngle, progressSweepAngle, false, paint);
        }
    }

    public void setSunDrawable(Drawable d) {
        if (iconDrawables != null) {
            int iconSize = (int) DisplayUtils.dpToPx(getContext(), ICON_SIZE_DIP);
            iconDrawables[0] = d;
            iconDrawables[0].setBounds(0, 0, iconSize, iconSize);
        }
    }

    public void setMoonDrawable(Drawable d) {
        if (iconDrawables != null) {
            int iconSize = (int) DisplayUtils.dpToPx(getContext(), ICON_SIZE_DIP);
            iconDrawables[1] = d;
            iconDrawables[1].setBounds(0, 0, iconSize, iconSize);
        }
    }
}
