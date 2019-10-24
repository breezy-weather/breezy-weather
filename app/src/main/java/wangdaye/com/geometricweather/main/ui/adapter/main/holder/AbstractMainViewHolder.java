package wangdaye.com.geometricweather.main.ui.adapter.main.holder;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public abstract class AbstractMainViewHolder extends RecyclerView.ViewHolder {

    protected Context context;
    protected @Nullable LinearLayout container;
    protected ResourceProvider provider;
    protected MainColorPicker picker;
    protected boolean itemAnimationEnabled;
    private boolean inScreen;
    private @Nullable Disposable disposable;

    @SuppressLint("ObjectAnimatorBinding")
    public AbstractMainViewHolder(Context context, @NonNull View view,
                                  @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                                  @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                                  @Px float cardRadius, @Px float cardElevation,
                                  boolean itemAnimationEnabled) {
        super(view);

        this.context = context;
        if (view instanceof CardView) {
            CardView card = (CardView) view;

            card.setRadius(cardRadius);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                card.setElevation(cardElevation);
            }

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) card.getLayoutParams();
            params.setMargins((int) cardMarginsHorizontal, 0, (int) cardMarginsHorizontal, (int) cardMarginsVertical);
            card.setLayoutParams(params);

            View child = card.getChildAt(0);
            if (child instanceof LinearLayout) {
                container = (LinearLayout) child;
            }
        }
        this.provider = provider;
        this.picker = picker;
        this.itemAnimationEnabled = itemAnimationEnabled;
        this.inScreen = false;
        this.disposable = null;
    }

    public abstract void onBindView(@NonNull Location location);

    public int getTop() {
        return itemView.getTop();
    }

    public @Nullable LinearLayout getContainer() {
        return container;
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

    public void executeEnterAnimator(List<Animator> pendingAnimatorList) {
        int popupDistance = (int) DisplayUtils.dpToPx(context, 40);
        itemView.setAlpha(0f);
        itemView.setTranslationY(popupDistance);

        long delay = pendingAnimatorList.size() * 150;

        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(itemView, "translationY", popupDistance, 0f)
        );
        set.setDuration(450);
        set.setInterpolator(new DecelerateInterpolator(2f));
        set.setStartDelay(delay);

        pendingAnimatorList.add(set);
        disposable = Observable.timer(100, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    pendingAnimatorList.remove(set);
                    onEnterScreen();
                }).subscribe();
        set.start();
    }

    public void onEnterScreen() {
        // do nothing.
    }

    public void onDestroy() {
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
    }
}
