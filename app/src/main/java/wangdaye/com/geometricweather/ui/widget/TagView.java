package wangdaye.com.geometricweather.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.appcompat.widget.AppCompatTextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public class TagView extends AppCompatTextView {

    private RectF outline;

    private Paint paint;

    private boolean checked;
    private @Px float strokeWidth;

    private @ColorInt int checkedBackgroundColor;
    private @ColorInt int uncheckedBackgroundColor;

    public TagView(Context context) {
        this(context, null);
    }

    public TagView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        outline = new RectF();

        paint = new Paint();
        paint.setAntiAlias(true);

        strokeWidth = DisplayUtils.dpToPx(context, 5);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TagView, defStyleAttr, 0);
        setChecked(a.getBoolean(R.styleable.TagView_checked, false));
        setCheckedBackgroundColor(a.getColor(R.styleable.TagView_checked_background_color, Color.WHITE));
        setUncheckedBackgroundColor(a.getColor(R.styleable.TagView_unchecked_background_color, Color.LTGRAY));
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        outline.set(0, 0, getMeasuredWidth(), getMeasuredHeight());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setClipToOutline(true);
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline viewOutline) {
                    viewOutline.setRoundRect(
                            (int) outline.left,
                            (int) outline.top,
                            (int) outline.right,
                            (int) outline.bottom,
                            outline.height() / 2
                    );
                }
            });
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (checked) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(uncheckedBackgroundColor);
            paint.setAlpha(255);
            canvas.drawRoundRect(
                    outline, outline.height() / 2, outline.height() / 2, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(checkedBackgroundColor);
            paint.setAlpha((int) (255 * 0.1));
            canvas.drawRoundRect(
                    outline, outline.height() / 2, outline.height() / 2, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
            paint.setColor(checkedBackgroundColor);
            paint.setAlpha((int) (255 * 0f));
            canvas.drawRoundRect(
                    outline, outline.height() / 2, outline.height() / 2, paint);
        } else {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(uncheckedBackgroundColor);
            paint.setAlpha(255);
            canvas.drawRoundRect(
                    outline, outline.height() / 2, outline.height() / 2, paint);
        }

        super.onDraw(canvas);
    }

    public final boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        invalidate();
    }

    public int getCheckedBackgroundColor() {
        return checkedBackgroundColor;
    }

    public void setCheckedBackgroundColor(int checkedBackgroundColor) {
        this.checkedBackgroundColor = checkedBackgroundColor;
        invalidate();
    }

    public int getUncheckedBackgroundColor() {
        return uncheckedBackgroundColor;
    }

    public void setUncheckedBackgroundColor(int uncheckedBackgroundColor) {
        this.uncheckedBackgroundColor = uncheckedBackgroundColor;
        invalidate();
    }
}
