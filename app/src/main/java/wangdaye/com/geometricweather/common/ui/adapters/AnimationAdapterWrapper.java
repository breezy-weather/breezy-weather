package wangdaye.com.geometricweather.common.ui.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;

public abstract class AnimationAdapterWrapper<A extends RecyclerView.Adapter<VH>, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private final A mInner;
    private final Map<Integer, Animator> mAnimatorSet;

    private int mLastPosition;
    private boolean mFirstOnly;

    public AnimationAdapterWrapper(A adapter) {
        this(adapter, true);
    }

    public AnimationAdapterWrapper(A adapter, boolean firstOnly) {
        super.setHasStableIds(adapter.hasStableIds());

        mInner = adapter;
        mAnimatorSet = new HashMap<>();

        mLastPosition = -1;
        mFirstOnly = firstOnly;
    }

    @Nullable
    protected abstract Animator getAnimator(View view, int pendingCount);

    protected abstract void setInitState(View view);

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return mInner.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
        mInner.registerAdapterDataObserver(observer);
    }

    @Override
    public void unregisterAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.unregisterAdapterDataObserver(observer);
        mInner.unregisterAdapterDataObserver(observer);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mInner.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mInner.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VH holder) {
        super.onViewAttachedToWindow(holder);
        mInner.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VH holder) {
        super.onViewDetachedFromWindow(holder);
        mInner.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        mInner.onBindViewHolder(holder, position);

        if (!mFirstOnly || position > mLastPosition) {
            clear(holder.itemView, position);

            Animator a = getAnimator(holder.itemView, mAnimatorSet.size());
            if (a != null) {
                setInitState(holder.itemView);

                a.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimatorSet.remove(position);
                    }
                });
                a.start();

                mAnimatorSet.put(position, a);
                mLastPosition = position;
                return;
            }
        }
        clear(holder.itemView, position);
    }

    private void clear(View view, int position) {
        Animator a = mAnimatorSet.get(position);
        if (a != null) {
            a.cancel();
            mAnimatorSet.remove(position);
        }

        view.setAlpha(1f);

        view.setRotation(0f);
        view.setRotationX(0f);
        view.setRotationY(0f);

        view.setScaleX(1f);
        view.setScaleY(1f);
        view.setTranslationX(0f);
        view.setTranslationY(0f);
        view.setTranslationZ(0f);
    }

    @Override
    public void onViewRecycled(@NonNull VH holder) {
        super.onViewRecycled(holder);
        mInner.onViewRecycled(holder);
        clear(holder.itemView, holder.getBindingAdapterPosition());
    }

    @Override
    public int getItemCount() {
        return mInner.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return mInner.getItemViewType(position);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
        mInner.setHasStableIds(hasStableIds);
    }

    @Override
    public long getItemId(int position) {
        return mInner.getItemId(position);
    }

    public A getWrappedAdapter() {
        return mInner;
    }

    public void setLastPosition(int lastPosition) {
        mLastPosition = lastPosition;
    }

    public void setFirstOnly(boolean firstOnly) {
        mFirstOnly = firstOnly;
    }
}
