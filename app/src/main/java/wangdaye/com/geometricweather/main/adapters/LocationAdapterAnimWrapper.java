package wangdaye.com.geometricweather.main.adapters;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.common.ui.adapters.AnimationAdapterWrapper;
import wangdaye.com.geometricweather.common.ui.adapters.location.LocationAdapter;
import wangdaye.com.geometricweather.common.ui.adapters.location.LocationHolder;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public class LocationAdapterAnimWrapper
        extends AnimationAdapterWrapper<LocationAdapter, LocationHolder> {

    private boolean mStartAnimation;

    private final float mDY;

    public LocationAdapterAnimWrapper(Context context, LocationAdapter adapter) {
        super(adapter, 400, new DecelerateInterpolator(), true);
        mStartAnimation = false;
        mDY = DisplayUtils.dpToPx(context, 128);
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

        Animator translation = ObjectAnimator
                .ofFloat(view, "translationY", mDY, 0f)
                .setDuration(getDuration());
        translation.setInterpolator(getInterpolator());

        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                translation,
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).setDuration(getDuration()),
                ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f).setDuration(1),
                ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f).setDuration(1)
        );
        set.setStartDelay(pendingCount * 50);
        return set;
    }

    @Override
    protected void setInitState(View view) {
        view.setTranslationY(mDY);
        view.setAlpha(0f);
        view.setScaleX(0f);
        view.setScaleY(0f);
    }
}
