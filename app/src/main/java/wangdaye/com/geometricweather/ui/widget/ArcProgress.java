package wangdaye.com.geometricweather.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import androidx.annotation.ColorInt;
import androidx.annotation.Size;
import androidx.core.graphics.ColorUtils;

import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Acr progress.
 * */

public class ArcProgress extends View {

    private Paint mProgressPaint;
    private Paint mShadowPaint;
    private Paint mTextPaint;

    private final DayNightShaderWrapper mShaderWrapper;

    private final RectF mRectF = new RectF();
    private float mArcBottomHeight;

    private float mProgress;
    private float mMax;
    private float mArcAngle;
    private float mProgressWidth;
    @ColorInt private int mProgressColor;
    @ColorInt private int mShadowColor;
    @ColorInt private int mShaderColor;
    @ColorInt private int mBackgroundColor;

    private String mText;
    private float mTextSize;
    @ColorInt private int mTextColor;
    @Size(2) private final int[] mShaderColors;

    private String mBottomText;
    private float mBottomTextSize;
    @ColorInt private int mBottomTextColor;

    private static final float SHADOW_ALPHA_FACTOR_LIGHT = 0.1f;
    private static final float SHADOW_ALPHA_FACTOR_DARK = 0.1f;

    public ArcProgress(Context context) {
        this(context, null);
    }

    public ArcProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray attributes = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.ArcProgress, defStyleAttr, 0);
        initialize(attributes);
        attributes.recycle();

        initPaint();

        mShaderColors = new int[] {Color.BLACK, Color.WHITE};
        mShaderWrapper = new DayNightShaderWrapper(
                null, getMeasuredWidth(), getMeasuredHeight(), true, mShaderColors);
    }

    private void initialize(TypedArray attributes) {
        mProgress = attributes.getInt(R.styleable.ArcProgress_progress, 0);
        mMax = attributes.getInt(R.styleable.ArcProgress_max, 100);
        mArcAngle = attributes.getFloat(R.styleable.ArcProgress_arc_angle, 360 * 0.8f);
        mProgressWidth = attributes.getDimension(
                R.styleable.ArcProgress_progress_width, DisplayUtils.dpToPx(getContext(), 8));
        mProgressColor = attributes.getColor(R.styleable.ArcProgress_progress_color, Color.BLACK);
        mShadowColor = Color.argb((int) (0.2 * 255), 0, 0, 0);
        mShaderColor = Color.argb((int) (0.2 * 255), 0, 0, 0);
        mBackgroundColor = attributes.getColor(
                R.styleable.ArcProgress_background_color, Color.GRAY);

        mText = attributes.getString(R.styleable.ArcProgress_text);
        mTextSize = attributes.getDimension(
                R.styleable.ArcProgress_text_size, DisplayUtils.dpToPx(getContext(), 36));
        mTextColor = attributes.getColor(R.styleable.ArcProgress_text_color, Color.DKGRAY);

        mBottomText = attributes.getString(R.styleable.ArcProgress_bottom_text);
        mBottomTextSize = attributes.getDimension(
                R.styleable.ArcProgress_bottom_text_size, DisplayUtils.dpToPx(getContext(), 12));
        mBottomTextColor = attributes.getColor(R.styleable.ArcProgress_bottom_text_color, Color.DKGRAY);
    }

    private void initPaint() {
        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStrokeWidth(mProgressWidth);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);

        mShadowPaint = new Paint();
        mShadowPaint.setAntiAlias(true);
        mShadowPaint.setStyle(Paint.Style.FILL);

        mTextPaint = new TextPaint();
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setAntiAlias(true);
    }

    public float getProgress() {
        return mProgress;
    }

    public void setProgress(float progress) {
        mProgress = progress;
        if (mProgress > getMax()) {
            mProgress = getMax();
        }
        invalidate();
    }

    public float getMax() {
        return mMax;
    }

    public void setMax(float max) {
        if (max > 0) {
            mMax = max;
            invalidate();
        }
    }

    public void setProgressColor(@ColorInt int progressColor, boolean lightTheme) {
        mProgressColor = progressColor;
        mShadowColor = getDarkerColor(progressColor);
        mShaderColor = ColorUtils.setAlphaComponent(
                progressColor,
                (int) (255 * (lightTheme ? SHADOW_ALPHA_FACTOR_LIGHT : SHADOW_ALPHA_FACTOR_DARK))
        );
        invalidate();
    }

    private int getDarkerColor(@ColorInt int color){
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = hsv[1] + 0.15f;
        hsv[2] = hsv[2] - 0.15f;
        return Color.HSVToColor(hsv);
    }

    public void setArcBackgroundColor(@ColorInt int backgroundColor) {
        mBackgroundColor = backgroundColor;
        invalidate();
    }

    public void setText(String text) {
        mText = text;
        invalidate();
    }

    public void setTextColor(@ColorInt int textColor) {
        mTextColor = textColor;
        invalidate();
    }

    public void setBottomText(String bottomText) {
        mBottomText = bottomText;
        invalidate();
    }

    public void setBottomTextColor(@ColorInt int bottomTextColor) {
        mBottomTextColor = bottomTextColor;
        invalidate();
    }

    private void ensureShadowShader() {
        mShaderColors[0] = mShaderColor;
        mShaderColors[1] = Color.TRANSPARENT;

        if (mShaderWrapper.isDifferent(
                getMeasuredWidth(), getMeasuredHeight(), false, mShaderColors)) {
            mShaderWrapper.setShader(
                    new LinearGradient(
                            0, mRectF.top,
                            0, mRectF.bottom,
                            mShaderColors[0], mShaderColors[1],
                            Shader.TileMode.CLAMP
                    ),
                    getMeasuredWidth(), getMeasuredHeight(),
                    false,
                    mShaderColors
            );
        }
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return (int) DisplayUtils.dpToPx(getContext(), 100);
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return (int) DisplayUtils.dpToPx(getContext(), 100);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int arcPadding = (int) DisplayUtils.dpToPx(getContext(), 4);
        mRectF.set(
                mProgressWidth / 2f + arcPadding,
                mProgressWidth / 2f + arcPadding,
                width - mProgressWidth / 2f - arcPadding,
                MeasureSpec.getSize(heightMeasureSpec) - mProgressWidth / 2f - arcPadding
        );
        float radius = (width - 2 * arcPadding) / 2f;
        float angle = (360 - mArcAngle) / 2f;
        mArcBottomHeight = radius * (float) (1 - Math.cos(angle / 180 * Math.PI));

        ensureShadowShader();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float startAngle = 270 - mArcAngle / 2f;
        float progressSweepAngle = (float) (1.0 * mProgress / getMax() * mArcAngle);
        float progressEndAngle = startAngle + progressSweepAngle;
        float deltaAngle = (float) (mProgressWidth / 2 / Math.PI / (mRectF.width() / 2) * 180);

        if (mProgress > 0) {
            ensureShadowShader();
            mShadowPaint.setShader(mShaderWrapper.getShader());
            if (progressEndAngle + deltaAngle >= 360) {
                canvas.drawCircle(
                        mRectF.centerX(),
                        mRectF.centerY(),
                        mRectF.width() / 2,
                        mShadowPaint
                );
            } else if (progressEndAngle + deltaAngle > 180) {
                canvas.drawArc(
                        mRectF,
                        360 - progressEndAngle - deltaAngle,
                        360 - 2 * (360 - progressEndAngle - deltaAngle),
                        false,
                        mShadowPaint
                );
            }
        }

        mProgressPaint.setColor(mBackgroundColor);
        canvas.drawArc(mRectF, startAngle, mArcAngle, false, mProgressPaint);
        if (mProgress > 0) {
            mProgressPaint.setColor(mProgressColor);
            canvas.drawArc(mRectF, startAngle, progressSweepAngle, false, mProgressPaint);
        }

        if (!TextUtils.isEmpty(mText)) {
            mTextPaint.setColor(mTextColor);
            mTextPaint.setTextSize(mTextSize);
            float textHeight = mTextPaint.descent() + mTextPaint.ascent();
            float textBaseline = (getHeight() - textHeight) / 2.0f;
            canvas.drawText(
                    mText,
                    (getWidth() - mTextPaint.measureText(mText)) / 2.0f,
                    textBaseline,
                    mTextPaint
            );
        }

        if(mArcBottomHeight == 0) {
            float radius = getWidth() / 2f;
            float angle = (360 - mArcAngle) / 2f;
            mArcBottomHeight = radius * (float) (1 - Math.cos(angle / 180 * Math.PI));
        }

        if (!TextUtils.isEmpty(mBottomText)) {
            mTextPaint.setColor(mBottomTextColor);
            mTextPaint.setTextSize(mBottomTextSize);
            float bottomTextBaseline = getHeight()
                    - mArcBottomHeight
                    - (mTextPaint.descent() + mTextPaint.ascent()) / 2;
            canvas.drawText(
                    mBottomText,
                    (getWidth() - mTextPaint.measureText(mBottomText)) / 2.0f,
                    bottomTextBaseline,
                    mTextPaint
            );
        }
    }
}