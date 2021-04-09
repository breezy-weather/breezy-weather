package wangdaye.com.geometricweather.common.snackbar;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import wangdaye.com.geometricweather.common.utils.DisplayUtils;

class Utils extends android.view.animation.AnimationUtils {

    public static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();

    static Animator getEnterAnimator(View view, boolean cardStyle) {
        view.setTranslationY(view.getHeight());
        view.setScaleX(cardStyle ? 1.1f : 1f);
        view.setScaleY(cardStyle ? 1.1f : 1f);

        Animator[] animators = DisplayUtils.getFloatingOvershotEnterAnimators(view);
        if (!cardStyle) {
            animators[0].setInterpolator(DisplayUtils.FLOATING_DECELERATE_INTERPOLATOR);
        }

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animators[0], animators[1], animators[2]);
        return set;
    }

    static void fitKeyboardExpand(View view, Rect insets) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) view.getContext().getSystemService(
                Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getRealMetrics(metrics);

        if (DisplayUtils.isKeyboardExpanded(metrics.heightPixels, insets.bottom)) {
            insets.bottom = 0;
        }
    }
}