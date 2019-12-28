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
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public abstract class AbstractMainViewHolder extends RecyclerView.ViewHolder {

    protected Context context;
    protected ResourceProvider provider;
    protected MainThemePicker picker;
    protected boolean itemAnimationEnabled;
    private boolean inScreen;

    private @Nullable Animator itemAnimator;
    private @Nullable Disposable disposable;

    @SuppressLint("ObjectAnimatorBinding")
    public AbstractMainViewHolder(@NonNull View view ) {
        super(view);
    }

    @CallSuper
    public void onBindView(Context context, @NonNull Location location,
                           @NonNull ResourceProvider provider, @NonNull MainThemePicker picker,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled) {
        this.context = context;
        this.provider = provider;
        this.picker = picker;
        this.itemAnimationEnabled = itemAnimationEnabled;
        this.inScreen = false;
        this.disposable = null;

        if (listAnimationEnabled) {
            itemView.setAlpha(0f);
        }
    }

    public int getTop() {
        return itemView.getTop();
    }

    public final void enterScreen(List<Animator> pendingAnimatorList,
                                  boolean listAnimationEnabled) {
        if (!inScreen) {
            inScreen = true;
            if (listAnimationEnabled) {
                executeEnterAnimator(pendingAnimatorList);
            } else {
                onEnterScreen();
            }
        }
    }

    public final void executeEnterAnimator(List<Animator> pendingAnimatorList) {
        itemView.setAlpha(0f);

        itemAnimator = getEnterAnimator(pendingAnimatorList);
        itemAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                pendingAnimatorList.remove(itemAnimator);
            }
        });

        disposable = Observable.timer(itemAnimator.getStartDelay(), TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    pendingAnimatorList.remove(itemAnimator);
                    onEnterScreen();
                }).subscribe();

        pendingAnimatorList.add(itemAnimator);
        itemAnimator.start();
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
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
        if (itemAnimator != null) {
            itemAnimator.cancel();
            itemAnimator = null;
        }
    }
}
