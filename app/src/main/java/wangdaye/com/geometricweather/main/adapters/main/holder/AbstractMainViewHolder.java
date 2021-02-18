package wangdaye.com.geometricweather.main.adapters.main.holder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.main.utils.MainThemeManager;
import wangdaye.com.geometricweather.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.common.utils.helpters.AsyncHelper;

public abstract class AbstractMainViewHolder extends RecyclerView.ViewHolder {

    protected Context context;
    protected ResourceProvider provider;
    protected MainThemeManager themeManager;
    protected boolean itemAnimationEnabled;
    private boolean mInScreen;

    private @Nullable Animator mItemAnimator;
    private @Nullable AsyncHelper.Controller mDelayController;

    @SuppressLint("ObjectAnimatorBinding")
    public AbstractMainViewHolder(@NonNull View view, MainThemeManager themeManager) {
        super(view);
        this.themeManager = themeManager;
    }

    @CallSuper
    public void onBindView(Context context, @NonNull Location location,
                           @NonNull ResourceProvider provider,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled) {
        this.context = context;
        this.provider = provider;
        this.itemAnimationEnabled = itemAnimationEnabled;
        mInScreen = false;
        mDelayController = null;

        if (listAnimationEnabled) {
            itemView.setAlpha(0f);
        }
    }

    public int getTop() {
        return itemView.getTop();
    }

    public final void enterScreen(List<Animator> pendingAnimatorList,
                                  boolean listAnimationEnabled) {
        if (!mInScreen) {
            mInScreen = true;
            if (listAnimationEnabled) {
                executeEnterAnimator(pendingAnimatorList);
            } else {
                onEnterScreen();
            }
        }
    }

    public final void executeEnterAnimator(List<Animator> pendingAnimatorList) {
        itemView.setAlpha(0f);

        mItemAnimator = getEnterAnimator(pendingAnimatorList);
        mItemAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                pendingAnimatorList.remove(mItemAnimator);
            }
        });

        mDelayController = AsyncHelper.delayRunOnUI(() -> {
            pendingAnimatorList.remove(mItemAnimator);
            onEnterScreen();
        }, mItemAnimator.getStartDelay());

        pendingAnimatorList.add(mItemAnimator);
        mItemAnimator.start();
    }

    @NonNull
    protected Animator getEnterAnimator(List<Animator> pendingAnimatorList) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(
                        itemView, "translationY", DisplayUtils.dpToPx(context, 40), 0f
                )
        );
        set.setDuration(450);
        set.setInterpolator(new DecelerateInterpolator(2f));
        set.setStartDelay(pendingAnimatorList.size() * 150);
        return set;
    }

    public void onEnterScreen() {
        // do nothing.
    }

    public void onRecycleView() {
        if (mDelayController != null) {
            mDelayController.cancel();
            mDelayController = null;
        }
        if (mItemAnimator != null) {
            mItemAnimator.cancel();
            mItemAnimator = null;
        }
    }
}
