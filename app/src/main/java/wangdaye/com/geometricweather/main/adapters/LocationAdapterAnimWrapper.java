package wangdaye.com.geometricweather.main.adapters;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.view.View;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.ui.adapters.AnimationAdapterWrapper;
import wangdaye.com.geometricweather.common.ui.adapters.location.LocationAdapter;
import wangdaye.com.geometricweather.common.ui.adapters.location.LocationHolder;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public class LocationAdapterAnimWrapper
        extends AnimationAdapterWrapper<LocationAdapter, LocationHolder> {

    private boolean mStartAnimation;
    private boolean mScrolled;

    private final float mDY;
    private final float mDZ;

    private static final float SCALE_FROM = 1.1f;

    private static final long BASE_DURATION = 600;
    private static final int MAX_TENSOR_COUNT = 6;

    public LocationAdapterAnimWrapper(Context context, LocationAdapter adapter) {
        super(adapter, true);

        mStartAnimation = false;
        mScrolled = false;

        mDY = DisplayUtils.dpToPx(context, 256);
        mDZ = DisplayUtils.dpToPx(context, 10);
    }

    @Nullable
    @Override
    protected Animator getAnimator(View view, int pendingCount) {
        if (pendingCount == 0) {
            if (mStartAnimation) {
                setLastPosition(Integer.MAX_VALUE);
                return null;
            } else {
                mStartAnimation = true;
            }
        }

        final long duration = Math.max(
                BASE_DURATION - pendingCount * 50,
                BASE_DURATION - MAX_TENSOR_COUNT * 50
        );
        final long delay = mScrolled
                ? 50
                : (pendingCount * 100);
        final float overShootTensor = 0.2f + Math.min(
                pendingCount * 0.4f,
                MAX_TENSOR_COUNT * 0.4f
        );

        Animator alpha = ObjectAnimator
                .ofFloat(view, "alpha", 0f, 1f).setDuration(duration / 4 * 3);
        alpha.setInterpolator(DisplayUtils.FLOATING_DECELERATE_INTERPOLATOR);

        Animator[] animators = DisplayUtils.getFloatingOvershotEnterAnimators(view, overShootTensor,
                mDY, 1.1f, 1.1f);
        for (Animator a : animators) {
            a.setDuration(duration);
        }

        AnimatorSet set = new AnimatorSet();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            set.playTogether(alpha, animators[0], animators[1], animators[2]);
        } else {
            Animator z = ObjectAnimator
                    .ofFloat(view, "translationZ", mDZ, 0f).setDuration(duration);
            z.setInterpolator(DisplayUtils.FLOATING_DECELERATE_INTERPOLATOR);

            set.playTogether(alpha, animators[0], animators[1], animators[2], z);
        }
        set.setStartDelay(delay);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                setItemStateListAnimator(view, true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setItemStateListAnimator(view, true);
            }
        });
        return set;
    }

    @Override
    protected void setInitState(View view) {
        view.setAlpha(0f);
        view.setTranslationY(mDY);
        view.setScaleX(SCALE_FROM);
        view.setScaleY(SCALE_FROM);
        setItemStateListAnimator(view, false);
    }

    private void setItemStateListAnimator(View view, boolean enabled) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        view.setStateListAnimator(enabled
                ? AnimatorInflater.loadStateListAnimator(view.getContext(), R.animator.touch_raise)
                : null);
    }

    public void setScrolled() {
        mScrolled = true;
    }
}
