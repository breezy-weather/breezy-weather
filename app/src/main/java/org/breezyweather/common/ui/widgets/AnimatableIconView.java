package org.breezyweather.common.ui.widgets;

import android.animation.Animator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.appcompat.widget.AppCompatImageView;

import org.breezyweather.R;

public class AnimatableIconView extends FrameLayout {

    @Size(3) private AppCompatImageView[] mIconImageViews;
    @Size(3) private Animator[] mIconAnimators;

    public AnimatableIconView(@NonNull Context context) {
        this(context, null);
    }

    public AnimatableIconView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatableIconView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray attributes = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.AnimatableIconView, defStyleAttr, 0);
        initialize(attributes);
        attributes.recycle();
    }

    private void initialize(TypedArray attributes) {
        int innerMargin = attributes.getDimensionPixelSize(
                R.styleable.AnimatableIconView_inner_margins, 0);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(innerMargin, innerMargin, innerMargin, innerMargin);

        mIconImageViews = new AppCompatImageView[] {
                new AppCompatImageView(getContext()),
                new AppCompatImageView(getContext()),
                new AppCompatImageView(getContext())
        };
        mIconAnimators = new Animator[] {null, null, null};

        for (int i = mIconImageViews.length - 1; i >= 0; i --) {
            addView(mIconImageViews[i], params);
        }
    }

    public void setAnimatableIcon(@NonNull @Size(3) Drawable[] drawables,
                                  @NonNull @Size(3) Animator[] animators) {
        endAnimators();
        for (int i = 0; i < drawables.length; i++) {
            mIconImageViews[i].setImageDrawable(drawables[i]);
            mIconImageViews[i].setVisibility(drawables[i] == null ? GONE : VISIBLE);

            mIconAnimators[i] = animators[i];
            if (mIconAnimators[i] != null) {
                mIconAnimators[i].setTarget(mIconImageViews[i]);
            }
        }
    }

    public void startAnimators() {
        for (Animator a : mIconAnimators) {
            if (a != null && a.isStarted()) {
                // animating.
                return;
            }
        }
        for (int i = 0; i < mIconAnimators.length; i++) {
            if (mIconAnimators[i] != null && mIconImageViews[i].getVisibility() == VISIBLE) {
                mIconAnimators[i].start();
            }
        }
    }

    private void endAnimators() {
        for (int i = 0; i < mIconImageViews.length; i++) {
            if (mIconAnimators[i] != null && mIconAnimators[i].isStarted()) {
                mIconAnimators[i].cancel();
            }
            resetView(mIconImageViews[i]);
        }
    }

    private void resetView(View view) {
        view.setAlpha(1f);
        view.setScaleX(1f);
        view.setScaleY(1f);
        view.setRotation(0f);
        view.setRotationX(0f);
        view.setRotationY(0f);
        view.setTranslationX(0f);
        view.setTranslationY(0f);
    }
}
