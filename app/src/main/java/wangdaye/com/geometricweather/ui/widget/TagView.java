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

    private final RectF mOutline;

    private final Paint mPaint;

    private boolean mChecked;
    private @Px final float mStrokeWidth;

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

        mStrokeWidth = DisplayUtils.dpToPx(context, 5);

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mChecked) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mUncheckedBackgroundColor);
            mPaint.setAlpha(255);
            canvas.drawRoundRect(
                    mOutline, mOutline.height() / 2, mOutline.height() / 2, mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mCheckedBackgroundColor);
            mPaint.setAlpha((int) (255 * 0.1));
            canvas.drawRoundRect(
                    mOutline, mOutline.height() / 2, mOutline.height() / 2, mPaint);

            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mStrokeWidth);
            mPaint.setColor(mCheckedBackgroundColor);
            mPaint.setAlpha((int) (255 * 0f));
            canvas.drawRoundRect(
                    mOutline, mOutline.height() / 2, mOutline.height() / 2, mPaint);
        } else {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mUncheckedBackgroundColor);
            mPaint.setAlpha(255);
            canvas.drawRoundRect(
                    mOutline, mOutline.height() / 2, mOutline.height() / 2, mPaint);
        }

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
