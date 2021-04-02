package wangdaye.com.geometricweather.main.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;

import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

public class MainModuleUtils {

    private static final long BASE_ENTER_DURATION = 500;

    public static boolean needUpdate(Context context, Location location) {
        float pollingIntervalInHour = SettingsOptionManager.getInstance(context)
                .getUpdateInterval()
                .getIntervalInHour();
        return !location.isUsable()
                || location.getWeather() == null
                || !location.getWeather().isValid(pollingIntervalInHour);
    }

    public static Animator getEnterAnimator(View view, int pendingCount) {
        Animator[] animators = DisplayUtils.getFloatingOvershotEnterAnimators(view, 0.4f + 0.2f * pendingCount,
                DisplayUtils.dpToPx(view.getContext(), 120), 1.025f, 1.025f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1f),
                animators[0],
                animators[1],
                animators[2]
        );
        set.setDuration(Math.max(BASE_ENTER_DURATION - pendingCount * 50, BASE_ENTER_DURATION / 2));
        set.setStartDelay(pendingCount * 200);
        return set;
    }
}
