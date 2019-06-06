package wangdaye.com.geometricweather.ui.widget;

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

import wangdaye.com.geometricweather.R;

public class AnimatableIconView extends FrameLayout {

    @Size(3) private AppCompatImageView[] iconImageViews;
    @Size(3) private Animator[] iconAnimators;

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

        iconImageViews = new AppCompatImageView[] {
                new AppCompatImageView(getContext()),
                new AppCompatImageView(getContext()),
                new AppCompatImageView(getContext())
        };
        iconAnimators = new Animator[] {null, null, null};

        for (int i = iconImageViews.length - 1; i >= 0; i --) {
            addView(iconImageViews[i], params);
        }
    }

    public void setAnimatableIcon(@NonNull @Size(3) Drawable[] drawables,
                                  @NonNull @Size(3) Animator[] animators) {
        endAnimators();
        for (int i = 0; i < drawables.length; i ++) {
            iconImageViews[i].setImageDrawable(drawables[i]);
            iconImageViews[i].setVisibility(drawables[i] == null ? GONE : VISIBLE);

            iconAnimators[i] = animators[i];
            if (iconAnimators[i] != null) {
                iconAnimators[i].setTarget(iconImageViews[i]);
            }
        }
    }

    public void startAnimators() {
        for (Animator a : iconAnimators) {
            if (a != null && a.isStarted()) {
                // animating.
                return;
            }
        }
        for (int i = 0; i < iconAnimators.length; i ++) {
            if (iconAnimators[i] != null && iconImageViews[i].getVisibility() == VISIBLE) {
                iconAnimators[i].start();
            }
        }
    }

    private void endAnimators() {
        for (int i = 0; i < iconImageViews.length; i ++) {
            if (iconAnimators[i] != null && iconAnimators[i].isStarted()) {
                iconAnimators[i].cancel();
            }
            resetView(iconImageViews[i]);
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
