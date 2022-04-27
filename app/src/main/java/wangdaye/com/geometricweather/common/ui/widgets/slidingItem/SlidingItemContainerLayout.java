package wangdaye.com.geometricweather.common.ui.widgets.slidingItem;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.widget.ImageViewCompat;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public class SlidingItemContainerLayout extends FrameLayout {

    private final AppCompatImageView mIcon;
    private @Nullable View mChild;

    private float mSwipeX;  // total swipe distance: + start, - end

    private @DrawableRes int mIconResStart;
    private @DrawableRes int mIconResEnd;

    private @ColorInt int mTintColorStart;
    private @ColorInt int mTintColorEnd;

    private @ColorInt int mBackgroundColorStart;
    private @ColorInt int mBackgroundColorEnd;

    private boolean mUpdateFlag;

    public SlidingItemContainerLayout(Context context) {
        this(context, null);
    }

    public SlidingItemContainerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingItemContainerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int iconSize = (int) DisplayUtils.dpToPx(context, 56);
        int iconPadding = (int) DisplayUtils.dpToPx(context, 16);

        mIcon = new AppCompatImageView(context);
        mIcon.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        ImageViewCompat.setImageTintList(mIcon, ColorStateList.valueOf(Color.WHITE));
        addView(mIcon, new LayoutParams(iconSize, iconSize, Gravity.CENTER_VERTICAL));

        setBackgroundColor(Color.GRAY);

        mChild = null;

        mSwipeX = 0;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingItemContainerLayout, defStyleAttr, 0);
        mIconResStart = a.getResourceId(R.styleable.SlidingItemContainerLayout_iconResStart, 0);
        mIconResEnd = a.getResourceId(R.styleable.SlidingItemContainerLayout_iconResEnd, 0);
        mBackgroundColorStart = a.getColor(R.styleable.SlidingItemContainerLayout_backgroundColorStart, Color.DKGRAY);
        mBackgroundColorEnd = a.getColor(R.styleable.SlidingItemContainerLayout_backgroundColorEnd, Color.DKGRAY);
        mTintColorStart = a.getColor(R.styleable.SlidingItemContainerLayout_tintColorStart, Color.WHITE);
        mTintColorEnd = a.getColor(R.styleable.SlidingItemContainerLayout_tintColorEnd, Color.WHITE);
        a.recycle();

        mUpdateFlag = true;
    }

    public void swipe(float totalX) {
        if (mSwipeX == totalX) {
            return;
        }
        if (mChild == null) {
            for (int i = 0; i < getChildCount(); i ++) {
                View v = getChildAt(i);
                if (v != mIcon) {
                    mChild = v;
                    break;
                }
            }
        }
        if (mChild == null) {
            return;
        }

        mChild.setTranslationX(totalX);

        float progress = Math.abs(totalX / getMeasuredWidth());
        progress = (float)(1.0f - Math.pow((1.0f - progress), 4));

        if (totalX != 0) { // need to draw background and sliding icon.
            if (totalX * mSwipeX <= 0 || mUpdateFlag) { // need to set background and sliding icon.
                mUpdateFlag = false;
                if (DisplayUtils.isRtl(getContext())) {
                    mIcon.setImageResource(totalX < 0 ? mIconResStart : mIconResEnd);
                    mIcon.setImageTintList(ColorStateList.valueOf(totalX < 0 ? mTintColorStart : mTintColorEnd));
                    setBackgroundColor(totalX < 0 ? mBackgroundColorStart : mBackgroundColorEnd);
                } else {
                    mIcon.setImageResource(totalX > 0 ? mIconResStart : mIconResEnd);
                    mIcon.setImageTintList(ColorStateList.valueOf(totalX > 0 ? mTintColorStart : mTintColorEnd));
                    setBackgroundColor(totalX > 0 ? mBackgroundColorStart : mBackgroundColorEnd);
                }
            }
            if (totalX > 0) {
                mIcon.setTranslationX((float) (
                        0.5 * -mIcon.getMeasuredWidth() + 0.75 * mIcon.getMeasuredWidth() * progress
                ));
            } else { // totalX < 0.
                mIcon.setTranslationX((float) (
                        getMeasuredWidth() - 0.5 * mIcon.getMeasuredWidth() - 0.75 * mIcon.getMeasuredWidth() * progress
                ));
            }
        } else {
            setBackgroundColor(Color.GRAY);
        }

        mSwipeX = totalX;
    }

    public int getIconResStart() {
        return mIconResStart;
    }

    public void setIconResStart(int iconResStart) {
        mIconResStart = iconResStart;
        mUpdateFlag = true;
    }

    public int getIconResEnd() {
        return mIconResEnd;
    }

    public void setIconResEnd(int iconResEnd) {
        mIconResEnd = iconResEnd;
        mUpdateFlag = true;
    }

    public int getBackgroundColorStart() {
        return mBackgroundColorStart;
    }

    public void setBackgroundColorStart(int backgroundColorStart) {
        mBackgroundColorStart = backgroundColorStart;
        mUpdateFlag = true;
    }

    public int getBackgroundColorEnd() {
        return mBackgroundColorEnd;
    }

    public void setBackgroundColorEnd(int backgroundColorEnd) {
        mBackgroundColorEnd = backgroundColorEnd;
        mUpdateFlag = true;
    }

    public int getTintColorStart() {
        return mTintColorStart;
    }

    public void setTintColorStart(int tintColorStart) {
        mTintColorStart = tintColorStart;
        mUpdateFlag = true;
    }

    public int getTintColorEnd() {
        return mTintColorEnd;
    }

    public void setTintColorEnd(int tintColorEnd) {
        mTintColorEnd = tintColorEnd;
        mUpdateFlag = true;
    }
}
