package wangdaye.com.geometricweather.main.adapter.main.holder;

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

import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.helpter.AsyncHelper;
import wangdaye.com.geometricweather.utils.manager.ThemeManager;

public abstract class AbstractMainViewHolder extends RecyclerView.ViewHolder {

    protected Context mContext;
    protected ResourceProvider mProvider;
    protected ThemeManager mThemeManager;
    protected boolean mItemAnimationEnabled;
    private boolean mInScreen;

    private @Nullable Animator mItemAnimator;
    private @Nullable AsyncHelper.Controller mDelayController;

    @SuppressLint("ObjectAnimatorBinding")
    public AbstractMainViewHolder(@NonNull View view) {
        super(view);
        mThemeManager = ThemeManager.getInstance(view.getContext());
    }

    @CallSuper
    public void onBindView(Context context, @NonNull Location location,
                           @NonNull ResourceProvider provider,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled) {
        mContext = context;
        mProvider = provider;
        mItemAnimationEnabled = itemAnimationEnabled;
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
                        itemView, "translationY", DisplayUtils.dpToPx(mContext, 40), 0f
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
