package wangdaye.com.geometricweather.common.snackbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.behavior.SwipeDismissBehavior;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.annotation.Nullable;

import wangdaye.com.geometricweather.R;

public final class Snackbar {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LENGTH_INDEFINITE, LENGTH_SHORT, LENGTH_LONG})
    public @interface Duration {
    }

    public static final int LENGTH_INDEFINITE = -2;
    public static final int LENGTH_SHORT = -1;
    public static final int LENGTH_LONG = 0;

    private static final int ANIMATION_DURATION = 450;
    private static final int ANIMATION_FADE_DURATION = 200;

    private static final Handler sHandler;
    private static final int MSG_SHOW = 0;
    private static final int MSG_DISMISS = 1;

    static {
        sHandler = new Handler(Looper.getMainLooper(), message -> {
            switch (message.what) {
                case MSG_SHOW:
                    ((Snackbar) message.obj).showView();
                    return true;
                case MSG_DISMISS:
                    ((Snackbar) message.obj).hideView(message.arg1);
                    return true;
            }
            return false;
        });
    }

    private final Context mContext;
    private final ViewGroup mParent;
    private final SnackbarLayout mView;
    private final boolean mCardStyle;

    private int mDuration;
    private Callback mCallback;
    private @Nullable Animator mAnimator;

    public static class Callback {

        public static final int DISMISS_EVENT_SWIPE = 0;
        public static final int DISMISS_EVENT_ACTION = 1;
        public static final int DISMISS_EVENT_TIMEOUT = 2;
        public static final int DISMISS_EVENT_MANUAL = 3;
        public static final int DISMISS_EVENT_CONSECUTIVE = 4;

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({DISMISS_EVENT_SWIPE, DISMISS_EVENT_ACTION, DISMISS_EVENT_TIMEOUT,
                DISMISS_EVENT_MANUAL, DISMISS_EVENT_CONSECUTIVE})
        public @interface DismissEvent {
        }

        public void onDismissed(Snackbar snackbar, @DismissEvent int event) {
        }

        public void onShown(Snackbar snackbar) {
        }
    }

    private final SnackbarManager.Callback mManagerCallback = new SnackbarManager.Callback() {
        @Override
        public void show() {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_SHOW, Snackbar.this));
        }

        @Override
        public void dismiss(int event) {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_DISMISS, event, 0, Snackbar.this));
        }
    };

    private Snackbar(ViewGroup parent, boolean cardStyle) {
        mContext = parent.getContext();
        mParent = parent;
        mView = (SnackbarLayout) LayoutInflater.from(mContext).inflate(
                cardStyle
                        ? R.layout.container_snackbar_layout_card
                        : R.layout.container_snackbar_layout,
                mParent,
                false
        );
        mCardStyle = cardStyle;
    }

    @NonNull
    public static Snackbar make(@NonNull View view, @NonNull CharSequence text,
                                @Duration int duration, boolean cardStyle) {
        Snackbar snackbar = new Snackbar(findSuitableParent(view), cardStyle);
        snackbar.setText(text);
        snackbar.setDuration(duration);
        return snackbar;
    }

    @NonNull
    public static Snackbar make(@NonNull View view, @StringRes int resId,
                                @Duration int duration, boolean cardStyle) {
        return make(view, view.getResources().getText(resId), duration, cardStyle);
    }

    private static ViewGroup findSuitableParent(View view) {
        do {
            if (view instanceof CoordinatorLayout) {
                // We've found a CoordinatorLayout, use it
                return (ViewGroup) view;
            } else if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    // If we've hit the decor content view, then we didn't find a CoL in the
                    // hierarchy, so use it.
                    return (ViewGroup) view;
                }
            }

            if (view != null) {
                // Else, we will loop and crawl up the view hierarchy and try to find a parent
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);

        throw new IllegalArgumentException(
                "No suitable parent found from the given view. Please provide a valid view.");
    }

    @NonNull
    public Snackbar setAction(@StringRes int resId, View.OnClickListener listener) {
        return setAction(mContext.getText(resId), listener);
    }

    @NonNull
    public Snackbar setAction(CharSequence text, final View.OnClickListener listener) {
        return setAction(text, true, listener);
    }

    @NonNull
    public Snackbar setAction(CharSequence text, final boolean shouldDismissOnClick,
                              final View.OnClickListener listener) {
        final TextView tv = mView.getActionView();

        if (TextUtils.isEmpty(text) || listener == null) {
            tv.setVisibility(View.GONE);
            tv.setOnClickListener(null);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
            tv.setOnClickListener(view -> {
                listener.onClick(view);
                if(shouldDismissOnClick) {
                    dispatchDismiss(Callback.DISMISS_EVENT_ACTION);
                }
            });
        }
        return this;
    }

    @NonNull
    public Snackbar setActionTextColor(ColorStateList colors) {
        final TextView tv = mView.getActionView();
        tv.setTextColor(colors);
        return this;
    }

    @NonNull
    public Snackbar setActionTextColor(@ColorInt int color) {
        final TextView tv = mView.getActionView();
        tv.setTextColor(color);
        return this;
    }

    @NonNull
    public Snackbar setText(@NonNull CharSequence message) {
        final TextView tv = mView.getMessageView();
        tv.setText(message);
        return this;
    }

    @NonNull
    public Snackbar setText(@StringRes int resId) {
        return setText(mContext.getText(resId));
    }

    @NonNull
    public Snackbar setDuration(@Duration int duration) {
        mDuration = duration;
        return this;
    }

    @Duration
    public int getDuration() {
        return mDuration;
    }

    @NonNull
    public View getView() {
        return mView;
    }

    public void show() {
        SnackbarManager.getInstance().show(mDuration, mManagerCallback);
    }

    public void dismiss() {
        dispatchDismiss(Callback.DISMISS_EVENT_MANUAL);
    }

    private void dispatchDismiss(@Callback.DismissEvent int event) {
        SnackbarManager.getInstance().dismiss(mManagerCallback, event);
    }

    @NonNull
    public Snackbar setCallback(Callback callback) {
        mCallback = callback;
        return this;
    }

    public boolean isShown() {
        return SnackbarManager.getInstance().isCurrent(mManagerCallback);
    }

    public boolean isShownOrQueued() {
        return SnackbarManager.getInstance().isCurrentOrNext(mManagerCallback);
    }

    final void showView() {
        if (mView.getParent() == null) {
            final ViewGroup.LayoutParams lp = mView.getLayoutParams();

            if (lp instanceof CoordinatorLayout.LayoutParams) {
                final Behavior behavior = new Behavior();
                behavior.setStartAlphaSwipeDistance(0.1f);
                behavior.setEndAlphaSwipeDistance(0.6f);
                behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);
                behavior.setListener(new SwipeDismissBehavior.OnDismissListener() {
                    @Override
                    public void onDismiss(View view) {
                        dispatchDismiss(Callback.DISMISS_EVENT_SWIPE);
                    }

                    @Override
                    public void onDragStateChanged(int state) {
                        switch (state) {
                            case SwipeDismissBehavior.STATE_DRAGGING:
                            case SwipeDismissBehavior.STATE_SETTLING:
                                SnackbarManager.getInstance().cancelTimeout(mManagerCallback);
                                break;

                            case SwipeDismissBehavior.STATE_IDLE:
                                SnackbarManager.getInstance().restoreTimeout(mManagerCallback);
                                break;
                        }
                    }
                });
                ((CoordinatorLayout.LayoutParams) lp).setBehavior(behavior);
            }
            mParent.addView(mView);
        }

        mView.setOnAttachStateChangeListener(new SnackbarLayout.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (isShownOrQueued()) {
                    sHandler.post(() -> onViewHidden(Callback.DISMISS_EVENT_MANUAL));
                }
            }
        });

        if (ViewCompat.isLaidOut(mView)) {
            animateViewIn();
        } else {
            mView.setOnLayoutChangeListener((view, left, top, right, bottom) -> {
                animateViewIn();
                mView.setOnLayoutChangeListener(null);
            });
        }
    }

    private void animateViewIn() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }

        mAnimator = wangdaye.com.geometricweather.common.snackbar.Utils.getEnterAnimator(mView, mCardStyle);
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mView.animateChildrenIn(ANIMATION_DURATION - ANIMATION_FADE_DURATION,
                        ANIMATION_FADE_DURATION);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mCallback != null) {
                    mCallback.onShown(Snackbar.this);
                }
                SnackbarManager.getInstance().onShown(mManagerCallback);
            }
        });
        mAnimator.start();
    }

    private void animateViewOut(final int event) {
        if (mAnimator != null) {
            mAnimator.cancel();
        }

        mAnimator = ObjectAnimator.ofFloat(
                mView, "translationY", mView.getTranslationY(), mView.getHeight()
        ).setDuration(ANIMATION_DURATION);
        mAnimator.setInterpolator(wangdaye.com.geometricweather.common.snackbar.Utils.FAST_OUT_SLOW_IN_INTERPOLATOR);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mView.animateChildrenOut(0, ANIMATION_FADE_DURATION);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                onViewHidden(event);
            }
        });
        mAnimator.start();
    }

    final void hideView(int event) {
        if (mView.getVisibility() != View.VISIBLE || isBeingDragged()) {
            onViewHidden(event);
        } else {
            animateViewOut(event);
        }
    }

    private void onViewHidden(int event) {
        SnackbarManager.getInstance().onDismissed(mManagerCallback);

        if (mCallback != null) {
            mCallback.onDismissed(this, event);
        }

        final ViewParent parent = mView.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(mView);
        }
    }

    private boolean isBeingDragged() {
        final ViewGroup.LayoutParams lp = mView.getLayoutParams();
        if (lp instanceof CoordinatorLayout.LayoutParams) {

            final CoordinatorLayout.Behavior<?> behavior
                    = ((CoordinatorLayout.LayoutParams) lp).getBehavior();

            if (behavior instanceof SwipeDismissBehavior) {
                return ((SwipeDismissBehavior<?>) behavior).getDragState()
                        != SwipeDismissBehavior.STATE_IDLE;
            }
        }
        return false;
    }

    public static class SnackbarLayout extends ViewGroup {

        private View mParent;
        private final Rect mWindowInsets;

        private TextView mMessageView;
        private Button mActionView;

        private final int mMaxWidth;

        interface OnLayoutChangeListener {
            void onLayoutChange(View view, int left, int top, int right, int bottom);
        }

        interface OnAttachStateChangeListener {
            void onViewAttachedToWindow(View v);
            void onViewDetachedFromWindow(View v);
        }

        private OnLayoutChangeListener mOnLayoutChangeListener;
        private OnAttachStateChangeListener mOnAttachStateChangeListener;

        public SnackbarLayout(Context context) {
            this(context, null);
        }

        public SnackbarLayout(Context context, AttributeSet attrs) {
            super(context, attrs);

            mParent = null;
            mWindowInsets = new Rect();

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SnackbarLayout);
            mMaxWidth = a.getDimensionPixelSize(R.styleable.SnackbarLayout_android_maxWidth, -1);
            a.recycle();

            setClickable(true);

            LayoutInflater.from(context).inflate(getLayoutId(), this);

            ViewCompat.setAccessibilityLiveRegion(this,
                    ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);

            ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
                fitSystemWindows(
                        new Rect(
                                insets.getSystemWindowInsetLeft(),
                                insets.getSystemWindowInsetTop(),
                                insets.getSystemWindowInsetRight(),
                                insets.getSystemWindowInsetBottom()
                        )
                );
                return insets;
            });
        }

        @Override
        protected boolean fitSystemWindows(Rect insets) {
            mWindowInsets.set(insets.left, insets.top, insets.right, insets.bottom);
            Utils.fitKeyboardExpand(this, mWindowInsets);
            requestLayout();
            return false;
        }

        public @LayoutRes int getLayoutId() {
            return R.layout.container_snackbar_layout_inner;
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            mMessageView = findViewById(R.id.snackbar_text);
            mActionView = findViewById(R.id.snackbar_action);
        }

        TextView getMessageView() {
            return mMessageView;
        }

        Button getActionView() {
            return mActionView;
        }

        @Override
        protected LayoutParams generateLayoutParams(LayoutParams p) {
            return new MarginLayoutParams(p);
        }

        @Override
        protected LayoutParams generateDefaultLayoutParams() {
            return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }

        @Override
        public LayoutParams generateLayoutParams(AttributeSet attrs) {
            return new MarginLayoutParams(getContext(), attrs);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width;
            int height;

            View child = getChildAt(0);
            int widthUsed = mWindowInsets.left + mWindowInsets.right;
            int heightUsed = mWindowInsets.bottom;

            measureChildWithMargins(child, widthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            width = child.getMeasuredWidth() + widthUsed + lp.leftMargin + lp.rightMargin
                    + getPaddingLeft() + getPaddingRight();
            height = child.getMeasuredHeight() + heightUsed + lp.topMargin + lp.bottomMargin
                    + getPaddingTop() + getPaddingBottom();

            if (mMaxWidth > 0 && width > mMaxWidth) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, MeasureSpec.EXACTLY);

                measureChildWithMargins(child, widthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
                lp = (MarginLayoutParams) child.getLayoutParams();

                width = child.getMeasuredWidth() + widthUsed + lp.leftMargin + lp.rightMargin
                        + getPaddingLeft() + getPaddingRight();
                height = child.getMeasuredHeight() + heightUsed + lp.topMargin + lp.bottomMargin
                        + getPaddingTop() + getPaddingBottom();
            }

            setMeasuredDimension(width, height);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            View child = getChildAt(0);
            int x = (getMeasuredWidth() - child.getMeasuredWidth()) / 2;
            child.layout(x, 0, x + child.getMeasuredWidth(), child.getMeasuredHeight());

            if (mOnLayoutChangeListener != null) {
                mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
            }
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewAttachedToWindow(this);
            }
            ViewCompat.requestApplyInsets(this);
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewDetachedFromWindow(this);
            }
        }

        void animateChildrenIn(int delay, int duration) {
            mMessageView.setAlpha(0f);
            ViewCompat.animate(mMessageView)
                    .alpha(1f)
                    .setDuration(duration)
                    .setStartDelay(delay)
                    .start();

            if (mActionView.getVisibility() == VISIBLE) {
                mActionView.setAlpha(0f);
                ViewCompat.animate(mActionView)
                        .alpha(1f)
                        .setDuration(duration)
                        .setStartDelay(delay)
                        .start();
            }
        }

        void animateChildrenOut(int delay, int duration) {
            mMessageView.setAlpha(1f);
            ViewCompat.animate(mMessageView)
                    .alpha(0f)
                    .setDuration(duration)
                    .setStartDelay(delay)
                    .start();

            if (mActionView.getVisibility() == VISIBLE) {
                mActionView.setAlpha(1f);
                ViewCompat.animate(mActionView)
                        .alpha(0f)
                        .setDuration(duration)
                        .setStartDelay(delay)
                        .start();
            }
        }

        void setOnLayoutChangeListener(OnLayoutChangeListener onLayoutChangeListener) {
            mOnLayoutChangeListener = onLayoutChangeListener;
        }

        void setOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
            mOnAttachStateChangeListener = listener;
        }
    }

    public static class CardSnackbarLayout extends SnackbarLayout {

        public CardSnackbarLayout(Context context) {
            super(context);
        }

        public CardSnackbarLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public int getLayoutId() {
            return R.layout.container_snackbar_layout_inner_card;
        }
    }

    final class Behavior extends SwipeDismissBehavior<SnackbarLayout> {

        @Override
        public boolean canSwipeDismissView(@NonNull View child) {
            return child instanceof SnackbarLayout;
        }

        @Override
        public boolean onLayoutChild(@NonNull CoordinatorLayout parent,
                                     @NonNull SnackbarLayout child,
                                     int layoutDirection) {
            return super.onLayoutChild(parent, child, layoutDirection);
        }

        @Override
        public boolean onInterceptTouchEvent(CoordinatorLayout parent, @NonNull SnackbarLayout child,
                                             MotionEvent event) {


            if (parent.isPointInChildBounds(child, (int) event.getX(), (int) event.getY())) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        SnackbarManager.getInstance().cancelTimeout(mManagerCallback);
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        SnackbarManager.getInstance().restoreTimeout(mManagerCallback);
                        break;
                }
            }

            return super.onInterceptTouchEvent(parent, child, event);
        }
    }
}