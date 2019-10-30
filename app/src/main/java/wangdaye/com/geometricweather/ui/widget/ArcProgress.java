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
import androidx.core.content.ContextCompat;
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

    private Paint progressPaint;
    private Paint shadowPaint;
    private Paint textPaint;

    private DayNightShaderWrapper shaderWrapper;

    private RectF rectF = new RectF();
    private float arcBottomHeight;

    private float progress;
    private float max;
    private float arcAngle;
    private float progressWidth;
    @ColorInt private int progressColor;
    @ColorInt private int shadowColor;
    @ColorInt private int shaderColor;
    @ColorInt private int backgroundColor;

    private String text;
    private float textSize;
    @ColorInt private int textColor;
    @Size(2) private int[] shaderColors;

    private String bottomText;
    private float bottomTextSize;
    @ColorInt private int bottomTextColor;

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

        shaderColors = new int[] {Color.BLACK, Color.WHITE};
        shaderWrapper = new DayNightShaderWrapper(
                null, getMeasuredWidth(), getMeasuredHeight(), true, shaderColors);
    }

    private void initialize(TypedArray attributes) {
        progress = attributes.getInt(R.styleable.ArcProgress_progress, 0);
        max = attributes.getInt(R.styleable.ArcProgress_max, 100);
        arcAngle = attributes.getFloat(R.styleable.ArcProgress_arc_angle, 360 * 0.8f);
        progressWidth = attributes.getDimension(
                R.styleable.ArcProgress_progress_width, DisplayUtils.dpToPx(getContext(), 8));
        progressColor = attributes.getColor(
                R.styleable.ArcProgress_progress_color, ContextCompat.getColor(getContext(), R.color.colorAccent));
        shadowColor = Color.argb((int) (0.2 * 255), 0, 0, 0);
        shaderColor = Color.argb((int) (0.2 * 255), 0, 0, 0);
        backgroundColor = attributes.getColor(
                R.styleable.ArcProgress_background_color, ContextCompat.getColor(getContext(), R.color.colorLine));

        text = attributes.getString(R.styleable.ArcProgress_text);
        textSize = attributes.getDimension(
                R.styleable.ArcProgress_text_size, DisplayUtils.dpToPx(getContext(), 36));
        textColor = attributes.getColor(
                R.styleable.ArcProgress_text_color, ContextCompat.getColor(getContext(), R.color.colorTextContent));

        bottomText = attributes.getString(R.styleable.ArcProgress_bottom_text);
        bottomTextSize = attributes.getDimension(
                R.styleable.ArcProgress_bottom_text_size, DisplayUtils.dpToPx(getContext(), 12));
        bottomTextColor = attributes.getColor(
                R.styleable.ArcProgress_bottom_text_color, ContextCompat.getColor(getContext(), R.color.colorTextContent));
    }

    private void initPaint() {
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeWidth(progressWidth);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        shadowPaint = new Paint();
        shadowPaint.setAntiAlias(true);
        shadowPaint.setStyle(Paint.Style.FILL);

        textPaint = new TextPaint();
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        if (this.progress > getMax()) {
            this.progress = getMax();
        }
        invalidate();
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        if (max > 0) {
            this.max = max;
            invalidate();
        }
    }

    public void setProgressColor(@ColorInt int progressColor, boolean lightTheme) {
        this.progressColor = progressColor;
        this.shadowColor = getDarkerColor(progressColor);
        this.shaderColor = ColorUtils.setAlphaComponent(
                progressColor,
                (int) (255 * (lightTheme ? SHADOW_ALPHA_FACTOR_LIGHT : SHADOW_ALPHA_FACTOR_DARK))
        );
        this.invalidate();
    }

    private int getDarkerColor(@ColorInt int color){
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = hsv[1] + 0.15f;
        hsv[2] = hsv[2] - 0.15f;
        return Color.HSVToColor(hsv);
    }

    public void setArcBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.invalidate();
    }

    public void setText(String text) {
        this.text = text;
        this.invalidate();
    }

    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
        this.invalidate();
    }

    public void setBottomText(String bottomText) {
        this.bottomText = bottomText;
        this.invalidate();
    }

    public void setBottomTextColor(@ColorInt int bottomTextColor) {
        this.bottomTextColor = bottomTextColor;
        this.invalidate();
    }

    private void ensureShadowShader() {
        shaderColors[0] = shaderColor;
        shaderColors[1] = Color.TRANSPARENT;

        if (shaderWrapper.isDifferent(
                getMeasuredWidth(), getMeasuredHeight(), false, shaderColors)) {
            shaderWrapper.setShader(
                    new LinearGradient(
                            0, rectF.top,
                            0, rectF.bottom,
                            shaderColors[0], shaderColors[1],
                            Shader.TileMode.CLAMP
                    ),
                    getMeasuredWidth(), getMeasuredHeight(),
                    false,
                    shaderColors
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
        rectF.set(
                progressWidth / 2f + arcPadding,
                progressWidth / 2f + arcPadding,
                width - progressWidth / 2f - arcPadding,
                MeasureSpec.getSize(heightMeasureSpec) - progressWidth / 2f - arcPadding
        );
        float radius = (width - 2 * arcPadding) / 2f;
        float angle = (360 - arcAngle) / 2f;
        arcBottomHeight = radius * (float) (1 - Math.cos(angle / 180 * Math.PI));

        ensureShadowShader();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float startAngle = 270 - arcAngle / 2f;
        float progressSweepAngle = (float) (1.0 * progress / getMax() * arcAngle);
        float progressEndAngle = startAngle + progressSweepAngle;
        float deltaAngle = (float) (progressWidth / 2 / Math.PI / (rectF.width() / 2) * 180);

        if (progress > 0) {
            ensureShadowShader();
            shadowPaint.setShader(shaderWrapper.getShader());
            if (progressEndAngle + deltaAngle >= 360) {
                canvas.drawCircle(
                        rectF.centerX(),
                        rectF.centerY(),
                        rectF.width() / 2,
                        shadowPaint
                );
            } else if (progressEndAngle + deltaAngle > 180) {
                canvas.drawArc(
                        rectF,
                        360 - progressEndAngle - deltaAngle,
                        360 - 2 * (360 - progressEndAngle - deltaAngle),
                        false,
                        shadowPaint
                );
            }
        }

        progressPaint.setColor(backgroundColor);
        canvas.drawArc(rectF, startAngle, arcAngle, false, progressPaint);
        if (progress > 0) {
            progressPaint.setColor(progressColor);
            canvas.drawArc(rectF, startAngle, progressSweepAngle, false, progressPaint);
        }

        if (!TextUtils.isEmpty(text)) {
            textPaint.setColor(textColor);
            textPaint.setTextSize(textSize);
            float textHeight = textPaint.descent() + textPaint.ascent();
            float textBaseline = (getHeight() - textHeight) / 2.0f;
            canvas.drawText(
                    text,
                    (getWidth() - textPaint.measureText(text)) / 2.0f,
                    textBaseline,
                    textPaint
            );
        }

        if(arcBottomHeight == 0) {
            float radius = getWidth() / 2f;
            float angle = (360 - arcAngle) / 2f;
            arcBottomHeight = radius * (float) (1 - Math.cos(angle / 180 * Math.PI));
        }

        if (!TextUtils.isEmpty(bottomText)) {
            textPaint.setColor(bottomTextColor);
            textPaint.setTextSize(bottomTextSize);
            float bottomTextBaseline = getHeight()
                    - arcBottomHeight
                    - (textPaint.descent() + textPaint.ascent()) / 2;
            canvas.drawText(
                    bottomText,
                    (getWidth() - textPaint.measureText(bottomText)) / 2.0f,
                    bottomTextBaseline,
                    textPaint
            );
        }
    }
}