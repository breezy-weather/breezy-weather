package wangdaye.com.geometricweather.common.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import wangdaye.com.geometricweather.R;

public class TagView extends AppCompatTextView {

    private final RectF mOutline;

    private final Paint mPaint;

    private boolean mChecked;

    private @ColorInt int mCheckedBackgroundColor;
    private @ColorInt int mUncheckedBackgroundColor;

    public TagView(Context context) {
        this(context, null);
    }

    public TagView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mOutline = new RectF();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TagView, defStyleAttr, 0);
        setChecked(a.getBoolean(R.styleable.TagView_checked, false));
        setCheckedBackgroundColor(a.getColor(R.styleable.TagView_checked_background_color, Color.WHITE));
        setUncheckedBackgroundColor(a.getColor(R.styleable.TagView_unchecked_background_color, Color.LTGRAY));
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mOutline.set(0, 0, getMeasuredWidth(), getMeasuredHeight());

        setClipToOutline(true);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline viewOutline) {
                viewOutline.setRoundRect(
                        (int) mOutline.left,
                        (int) mOutline.top,
                        (int) mOutline.right,
                        (int) mOutline.bottom,
                        mOutline.height() / 2
                );
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(mChecked ? mCheckedBackgroundColor : mUncheckedBackgroundColor);
        canvas.drawRoundRect(
                mOutline, mOutline.height() / 2, mOutline.height() / 2, mPaint);

        super.onDraw(canvas);
    }

    public final boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        invalidate();
    }

    public int getCheckedBackgroundColor() {
        return mCheckedBackgroundColor;
    }

    public void setCheckedBackgroundColor(int checkedBackgroundColor) {
        mCheckedBackgroundColor = checkedBackgroundColor;
        invalidate();
    }

    public int getUncheckedBackgroundColor() {
        return mUncheckedBackgroundColor;
    }

    public void setUncheckedBackgroundColor(int uncheckedBackgroundColor) {
        mUncheckedBackgroundColor = uncheckedBackgroundColor;
        invalidate();
    }
}
