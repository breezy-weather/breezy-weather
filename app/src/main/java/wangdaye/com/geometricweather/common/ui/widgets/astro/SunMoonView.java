package wangdaye.com.geometricweather.common.ui.widgets.astro;

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

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;

import wangdaye.com.geometricweather.common.ui.widgets.DayNightShaderWrapper;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public class SunMoonView extends View {

    // 0 - day / 1 - night.
    @Size(2) private Drawable[] mIconDrawables;

    private Paint mPaint;
    private Xfermode mClearXfermode;
    private DayNightShaderWrapper mX1ShaderWrapper;
    private DayNightShaderWrapper mX2ShaderWrapper;
    private PathEffect mEffect;
    private RectF mRectF;

    @Size(2) private float[] mIconRotations;
    @Size(2) private float[] mIconAlphas;
    @Size(2) private float[][] mIconPositions;

    @Size(2) private long[] mStartTimes;
    @Size(2) private long[] mCurrentTimes;
    @Size(2) private long[] mEndTimes;
    @Size(2) private long[] mProgresses;
    @Size(2) private long[] mMaxes;

    @Size(3) private int[] mLineColors;
    @Size(2) private int[] mX1ShaderColors;
    @Size(2) private int[] mX2ShaderColors;
    @ColorInt private int mRootColor;

    private float mLineSize;
    private float mDottedLineSize;
    private float mMargin;

    int iconSize;

    private final static float ICON_SIZE_DIP = 24;
    private final static float LINE_SIZE_DIP = 5f;
    private final static float DOTTED_LINE_SIZE_DIP = 1;
    private final static float MARGIN_DIP = 16;

    private static final int ARC_ANGLE = 135;

    private static final float SHADOW_ALPHA_FACTOR_LIGHT = 0.1f;
    private static final float SHADOW_ALPHA_FACTOR_DARK = 0.2f;

    public SunMoonView(@NonNull Context context) {
        super(context);
        initialize();
    }

    public SunMoonView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public SunMoonView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        mIconDrawables = new Drawable[2];

        mIconRotations = new float[] {0, 0};
        mIconAlphas = new float[] {0, 0};
        mIconPositions = new float[][] {{0, 0}, {0, 0}};

        mStartTimes = new long[] {1, 1};
        mEndTimes = new long[] {1, 1};
        mCurrentTimes = new long[] {0, 0};
        mProgresses = new long[] {-1, -1};
        mMaxes = new long[] {100, 100};

        mLineColors = new int[] {Color.BLACK, Color.GRAY, Color.LTGRAY};

        mX1ShaderColors = new int[] {Color.GRAY, Color.WHITE};
        mX2ShaderColors = new int[] {Color.BLACK, Color.WHITE};
        mRootColor = Color.WHITE;

        mLineSize = DisplayUtils.dpToPx(getContext(), LINE_SIZE_DIP);
        mDottedLineSize = DisplayUtils.dpToPx(getContext(), DOTTED_LINE_SIZE_DIP);
        mMargin = DisplayUtils.dpToPx(getContext(), MARGIN_DIP);

        iconSize = (int) DisplayUtils.dpToPx(getContext(), ICON_SIZE_DIP);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mClearXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        mX1ShaderWrapper = new DayNightShaderWrapper(getMeasuredWidth(), getMeasuredHeight());
        mX2ShaderWrapper = new DayNightShaderWrapper(getMeasuredWidth(), getMeasuredHeight());

        mEffect = new DashPathEffect(
                new float[] {
                        DisplayUtils.dpToPx(getContext(), 3),
                        2 * DisplayUtils.dpToPx(getContext(), 3)
                }, 0
        );
        mRectF = new RectF();
    }

    public void setTime(
            @Size(2) long[] startTimes,
            @Size(2) long[] endTimes,
            @Size(2) long[] currentTimes
    ) {
        mStartTimes = startTimes;
        mEndTimes = endTimes;
        mCurrentTimes = currentTimes;

        setIndicatorPosition(0);
        setIndicatorPosition(1);

        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setColors(@ColorInt int sunLineColor, @ColorInt int moonLineColor,
                          @ColorInt int backgroundLineColor, @ColorInt int rootColor,
                          boolean lightTheme) {
        mLineColors = new int[] {sunLineColor, moonLineColor, backgroundLineColor};
        ensureShader(rootColor, sunLineColor, moonLineColor, lightTheme);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setDayIndicatorRotation(float rotation) {
        mIconRotations[0] = rotation;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setNightIndicatorRotation(float rotation) {
        mIconRotations[1] = rotation;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void ensureShader(@ColorInt int rootColor,
                              @ColorInt int sunLineColor, @ColorInt int moonLineColor,
                              boolean lightTheme) {
        int lineShadowShader = lightTheme
                ? ColorUtils.setAlphaComponent(sunLineColor, (int) (255 * SHADOW_ALPHA_FACTOR_LIGHT))
                : ColorUtils.setAlphaComponent(moonLineColor, (int) (255 * SHADOW_ALPHA_FACTOR_DARK));

        mX1ShaderColors[0] = DisplayUtils.blendColor(lineShadowShader, rootColor);
        mX1ShaderColors[1] = rootColor;

        mX2ShaderColors[0] = DisplayUtils.blendColor(lineShadowShader, mX1ShaderColors[0]);
        mX2ShaderColors[1] = rootColor;

        mRootColor = rootColor;

        if (mX1ShaderWrapper.isDifferent(
                getMeasuredWidth(), getMeasuredHeight(), lightTheme, mX1ShaderColors)) {
            mX1ShaderWrapper.setShader(
                    new LinearGradient(
                            0, mRectF.top,
                            0, getMeasuredHeight() - mMargin,
                            mX1ShaderColors[0], mX1ShaderColors[1],
                            Shader.TileMode.CLAMP
                    ),
                    getMeasuredWidth(), getMeasuredHeight(),
                    lightTheme,
                    mX1ShaderColors
            );
        }
        if (mX2ShaderWrapper.isDifferent(
                getMeasuredWidth(), getMeasuredHeight(), lightTheme, mX2ShaderColors)) {
            mX2ShaderWrapper.setShader(
                    new LinearGradient(
                            0, mRectF.top,
                            0, getMeasuredHeight() - mMargin,
                            mX2ShaderColors[0], mX2ShaderColors[1],
                            Shader.TileMode.CLAMP
                    ),
                    getMeasuredWidth(), getMeasuredHeight(),
                    lightTheme,
                    mX2ShaderColors
            );
        }
    }

    private void ensureProgress(int index) {
        mMaxes[index] = mEndTimes[index] - mStartTimes[index];
        mProgresses[index] = mCurrentTimes[index] - mStartTimes[index];
        mProgresses[index] = Math.max(mProgresses[index], 0);
        mProgresses[index] = Math.min(mProgresses[index], mMaxes[index]);
    }

    private void setIndicatorPosition(int index) {
        ensureProgress(index);
        float startAngle = 270 - ARC_ANGLE / 2f;
        float progressSweepAngle = (float) (1.0 * mProgresses[index] / mMaxes[index] * ARC_ANGLE);
        float progressEndAngle = startAngle + progressSweepAngle;
        float deltaAngle = progressEndAngle - 180;
        float deltaWidth = (float) Math.abs(mRectF.width() / 2 * Math.cos(Math.toRadians(deltaAngle)));
        float deltaHeight = (float) Math.abs(mRectF.width() / 2 * Math.sin(Math.toRadians(deltaAngle)));

        if (progressSweepAngle == 0 && mIconAlphas[index] != 0) {
            mIconAlphas[index] = 0;
        } else if (progressSweepAngle != 0 && mIconAlphas[index] == 0) {
            mIconAlphas[index] = 1;
        }

        if (mIconDrawables[index] != null) {
            if (progressEndAngle < 270) {
                mIconPositions[index][0] = mRectF.centerX() - deltaWidth - iconSize / 2f;
            } else {
                mIconPositions[index][0] = mRectF.centerX() + deltaWidth - iconSize / 2f;
            }
            mIconPositions[index][1] = mRectF.centerY() - deltaHeight - iconSize / 2f;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) (MeasureSpec.getSize(widthMeasureSpec) - 2 * mMargin);
        double deltaRadians = Math.toRadians((180 - ARC_ANGLE) / 2d);
        int radius = (int) (width / 2 / Math.cos(deltaRadians));
        int height = (int) (radius - width / 2 * Math.tan(deltaRadians));
        setMeasuredDimension(
                MeasureSpec.makeMeasureSpec((int) (width + 2 * mMargin), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec((int) (height + 2 * mMargin), MeasureSpec.EXACTLY)
        );

        int centerX = getMeasuredWidth() / 2;
        int centerY = (int) (mMargin + radius);
        mRectF.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );

        ensureShader(mRootColor, mLineColors[0], mLineColors[1], mX1ShaderWrapper.isLightTheme());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // shadow.
        float startAngle = 270 - ARC_ANGLE / 2f;
        float progressSweepAngleDay = (float) (1.0 * mProgresses[0] / mMaxes[0] * ARC_ANGLE);
        float progressEndAngleDay = startAngle + progressSweepAngleDay;
        float progressSweepAngleNight = (float) (1.0 * mProgresses[1] / mMaxes[1] * ARC_ANGLE);
        float progressEndAngleNight = startAngle + progressSweepAngleNight;
        if (progressEndAngleDay == progressEndAngleNight) {
            drawShadow(canvas, 0, progressEndAngleDay, mX2ShaderWrapper.getShader());
        } else if (progressEndAngleDay > progressEndAngleNight) {
            drawShadow(canvas, 0, progressEndAngleDay, mX1ShaderWrapper.getShader());
            drawShadow(canvas, 1, progressEndAngleNight, mX2ShaderWrapper.getShader());
        } else { // progressEndAngleDay < progressEndAngleNight
            drawShadow(canvas, 1, progressEndAngleNight, mX1ShaderWrapper.getShader());
            drawShadow(canvas, 0, progressEndAngleDay, mX2ShaderWrapper.getShader());
        }

        // sub line.
        mPaint.setColor(mLineColors[2]);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mDottedLineSize);
        mPaint.setPathEffect(mEffect);
        canvas.drawArc(mRectF, startAngle, ARC_ANGLE, false, mPaint);
        canvas.drawLine(
                mMargin,
                getMeasuredHeight() - mMargin,
                getMeasuredWidth() - mMargin,
                getMeasuredHeight() - mMargin,
                mPaint
        );

        // path.
        drawPathLine(canvas, 1, startAngle, (float) (1.0 * mProgresses[1] / mMaxes[1] * ARC_ANGLE));
        drawPathLine(canvas, 0, startAngle, (float) (1.0 * mProgresses[0] / mMaxes[0] * ARC_ANGLE));

        int restoreCount;

        // icon.
        for (int i = 1; i >= 0; i --) {
            if (mIconDrawables[i] == null || mProgresses[i] <= 0) {
                continue;
            }
            restoreCount = canvas.save();
            canvas.translate(mIconPositions[i][0], mIconPositions[i][1]);
            canvas.rotate(mIconRotations[i], iconSize / 2f, iconSize / 2f);
            mIconDrawables[i].draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
    }

    private void drawShadow(Canvas canvas, int index, float progressEndAngle, Shader shader) {
        if (mProgresses[index] > 0) {
            int layerId = canvas.saveLayer(
                    mRectF.left, mRectF.top, mRectF.right, mRectF.top + mRectF.height() / 2,
                    null, Canvas.ALL_SAVE_FLAG
            );

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setShader(shader);
            canvas.drawArc(
                    mRectF,
                    270 - ARC_ANGLE / 2f,
                    ARC_ANGLE,
                    false,
                    mPaint
            );
            mPaint.setShader(null);

            mPaint.setXfermode(mClearXfermode);
            canvas.drawRect(
                    (float) (
                            mRectF.centerX() + mRectF.width() / 2
                                    * Math.cos((360 - progressEndAngle) * Math.PI / 180)
                    ),
                    mRectF.top,
                    mRectF.right,
                    mRectF.top + mRectF.height() / 2,
                    mPaint
            );
            mPaint.setXfermode(null);

            canvas.restoreToCount(layerId);
        }
    }

    private void drawPathLine(Canvas canvas, int index, float startAngle, float progressSweepAngle) {
        if (mProgresses[index] > 0) {
            mPaint.setColor(mLineColors[index]);
            mPaint.setStrokeWidth(mLineSize);
            mPaint.setPathEffect(null);
            canvas.drawArc(mRectF, startAngle, progressSweepAngle, false, mPaint);
        }
    }

    public void setSunDrawable(Drawable d) {
        if (d != null) {
            mIconDrawables[0] = d;
            mIconDrawables[0].setBounds(0, 0, iconSize, iconSize);
        }
    }

    public void setMoonDrawable(Drawable d) {
        if (d != null) {
            mIconDrawables[1] = d;
            mIconDrawables[1].setBounds(0, 0, iconSize, iconSize);
        }
    }
}