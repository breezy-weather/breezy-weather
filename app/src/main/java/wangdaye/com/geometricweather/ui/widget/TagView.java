package wangdaye.com.geometricweather.ui.widget;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public class TagView extends AppCompatTextView {

    private boolean checked;

    private @ColorInt int checkedTextColor;
    private @ColorInt int checkedBackgroundColor;
    private @ColorInt int uncheckedTextColor;
    private @ColorInt int uncheckedBackgroundColor;

    public TagView(Context context) {
        this(context, null);
    }

    public TagView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TagView, defStyleAttr, 0);
        innerSetChecked(a.getBoolean(R.styleable.TagView_checked, false));
        setCheckedTextColor(a.getColor(R.styleable.TagView_checked_text_color, Color.BLACK));
        setCheckedBackgroundColor(a.getColor(R.styleable.TagView_checked_background_color, Color.WHITE));
        setUncheckedTextColor(a.getColor(R.styleable.TagView_unchecked_text_color, Color.DKGRAY));
        setUncheckedBackgroundColor(a.getColor(R.styleable.TagView_unchecked_background_color, Color.LTGRAY));
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(
                            0,
                            0,
                            view.getMeasuredWidth(),
                            view.getMeasuredHeight(),
                            view.getMeasuredHeight() / 2f
                    );
                }
            });
        }
    }

    public boolean isChecked() {
        return checked;
    }

    @SuppressLint("PrivateResource")
    public void setChecked(boolean checked) {
        if (this.checked != checked) {
            innerSetChecked(checked);
        }
    }

    private void innerSetChecked(boolean checked) {
        this.checked = checked;
        if (checked) {
            setTextColor(checkedTextColor);
            setBackgroundColor(checkedBackgroundColor);
        } else {
            setTextColor(uncheckedTextColor);
            setBackgroundColor(uncheckedBackgroundColor);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(
                    new int[] {android.R.attr.enabled},
                    ObjectAnimator.ofFloat(
                            this,
                            "elevation",
                            DisplayUtils.dpToPx(getContext(), 10)
                    ).setDuration(150)
            );
            animator.addState(
                    new int[0],
                    ObjectAnimator.ofFloat(
                            this,
                            "elevation",
                            checked ? DisplayUtils.dpToPx(getContext(), 2) : 0
                    ).setDuration(150)
            );
            setStateListAnimator(animator);
        }
    }

    public int getCheckedTextColor() {
        return checkedTextColor;
    }

    public void setCheckedTextColor(int checkedTextColor) {
        this.checkedTextColor = checkedTextColor;
        if (checked) {
            setTextColor(checkedTextColor);
        }
    }

    public int getCheckedBackgroundColor() {
        return checkedBackgroundColor;
    }

    public void setCheckedBackgroundColor(int checkedBackgroundColor) {
        this.checkedBackgroundColor = checkedBackgroundColor;
        if (checked) {
            setBackgroundColor(checkedBackgroundColor);
        }
    }

    public int getUncheckedTextColor() {
        return uncheckedTextColor;
    }

    public void setUncheckedTextColor(int uncheckedTextColor) {
        this.uncheckedTextColor = uncheckedTextColor;
        if (!checked) {
            setTextColor(uncheckedTextColor);
        }
    }

    public int getUncheckedBackgroundColor() {
        return uncheckedBackgroundColor;
    }

    public void setUncheckedBackgroundColor(int uncheckedBackgroundColor) {
        this.uncheckedBackgroundColor = uncheckedBackgroundColor;
        if (!checked) {
            setBackgroundColor(uncheckedBackgroundColor);
        }
    }
}
