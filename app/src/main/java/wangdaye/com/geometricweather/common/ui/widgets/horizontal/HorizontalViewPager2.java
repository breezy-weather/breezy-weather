package wangdaye.com.geometricweather.common.ui.widgets.horizontal;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.UiThread;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityViewCommand;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.StatefulAdapter;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Locale;

public class HorizontalViewPager2 extends ViewGroup {
    /** @hide */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    @Retention(SOURCE)
    @IntDef({ORIENTATION_HORIZONTAL, ORIENTATION_VERTICAL})
    public @interface Orientation {
    }

    public static final int ORIENTATION_HORIZONTAL = RecyclerView.HORIZONTAL;
    public static final int ORIENTATION_VERTICAL = RecyclerView.VERTICAL;

    /** @hide */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    @Retention(SOURCE)
    @IntDef({SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING})
    public @interface ScrollState {
    }

    /** @hide */
    @SuppressWarnings("WeakerAccess")
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    @Retention(SOURCE)
    @IntDef({OFFSCREEN_PAGE_LIMIT_DEFAULT})
    @IntRange(from = 1)
    public @interface OffscreenPageLimit {
    }

    /**
     * Indicates that the ViewPager2 is in an idle, settled state. The current page
     * is fully in view and no animation is in progress.
     */
    public static final int SCROLL_STATE_IDLE = 0;

    /**
     * Indicates that the ViewPager2 is currently being dragged by the user, or programmatically
     * via fake drag functionality.
     */
    public static final int SCROLL_STATE_DRAGGING = 1;

    /**
     * Indicates that the ViewPager2 is in the process of settling to a final position.
     */
    public static final int SCROLL_STATE_SETTLING = 2;

    /**
     * Value to indicate that the default caching mechanism of RecyclerView should be used instead
     * of explicitly prefetch and retain pages to either side of the current page.
     * @see #setOffscreenPageLimit(int)
     */
    public static final int OFFSCREEN_PAGE_LIMIT_DEFAULT = -1;

    /** Feature flag while stabilizing enhanced a11y */
    static boolean sFeatureEnhancedA11yEnabled = true;

    // reused in layout(...)
    private final Rect mTmpContainerRect = new Rect();
    private final Rect mTmpChildRect = new Rect();

    private final CompositeOnPageChangeCallback mExternalPageChangeCallbacks =
            new CompositeOnPageChangeCallback(3);

    int mCurrentItem;
    boolean mCurrentItemDirty = false;
    private final RecyclerView.AdapterDataObserver mCurrentItemDataSetChangeObserver =
            new DataSetChangeObserver() {
                @Override
                public void onChanged() {
                    mCurrentItemDirty = true;
                    mScrollEventAdapter.notifyDataSetChangeHappened();
                }
            };

    private LinearLayoutManager mLayoutManager;
    private int mPendingCurrentItem = NO_POSITION;
    private Parcelable mPendingAdapterState;
    RecyclerView mRecyclerView;
    private PagerSnapHelper mPagerSnapHelper;
    ScrollEventAdapter mScrollEventAdapter;
    private CompositeOnPageChangeCallback mPageChangeEventDispatcher;
    private FakeDrag mFakeDragger;
    private PageTransformerAdapter mPageTransformerAdapter;
    private RecyclerView.ItemAnimator mSavedItemAnimator = null;
    private boolean mSavedItemAnimatorPresent = false;
    private boolean mUserInputEnabled = true;
    private @OffscreenPageLimit int mOffscreenPageLimit = OFFSCREEN_PAGE_LIMIT_DEFAULT;
    AccessibilityProvider mAccessibilityProvider; // to avoid creation of a synthetic accessor

    public HorizontalViewPager2(@NonNull Context context) {
        super(context);
        initialize(context);
    }

    public HorizontalViewPager2(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public HorizontalViewPager2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public HorizontalViewPager2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                                int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context);
    }

    private void initialize(Context context) {
        mAccessibilityProvider = sFeatureEnhancedA11yEnabled
                ? new PageAwareAccessibilityProvider()
                : new BasicAccessibilityProvider();

        mRecyclerView = new RecyclerViewImpl(context);
        mRecyclerView.setId(ViewCompat.generateViewId());
        mRecyclerView.setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);

        mLayoutManager = new LinearLayoutManagerImpl(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
        setOrientation(ORIENTATION_HORIZONTAL);

        mRecyclerView.setLayoutParams(
                new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mRecyclerView.addOnChildAttachStateChangeListener(enforceChildFillListener());

        // Create ScrollEventAdapter before attaching PagerSnapHelper to RecyclerView, because the
        // attach process calls PagerSnapHelperImpl.findSnapView, which uses the mScrollEventAdapter
        mScrollEventAdapter = new ScrollEventAdapter(this);
        // Create FakeDrag before attaching PagerSnapHelper, same reason as above
        mFakeDragger = new FakeDrag(this, mScrollEventAdapter, mRecyclerView);
        mPagerSnapHelper = new PagerSnapHelperImpl();
        mPagerSnapHelper.attachToRecyclerView(mRecyclerView);
        // Add mScrollEventAdapter after attaching mPagerSnapHelper to mRecyclerView, because we
        // don't want to respond on the events sent out during the attach process
        mRecyclerView.addOnScrollListener(mScrollEventAdapter);

        mPageChangeEventDispatcher = new CompositeOnPageChangeCallback(3);
        mScrollEventAdapter.setOnPageChangeCallback(mPageChangeEventDispatcher);

        // Callback that updates mCurrentItem after swipes. Also triggered in other cases, but in
        // all those cases mCurrentItem will only be overwritten with the same value.
        final OnPageChangeCallback currentItemUpdater = new OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (mCurrentItem != position) {
                    mCurrentItem = position;
                    mAccessibilityProvider.onSetNewCurrentItem();
                }
            }

            @Override
            public void onPageScrollStateChanged(int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    updateCurrentItem();
                }
            }
        };

        // Prevents focus from remaining on a no-longer visible page
        final OnPageChangeCallback focusClearer = new OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                clearFocus();
                if (hasFocus()) { // if clear focus did not succeed
                    mRecyclerView.requestFocus(View.FOCUS_FORWARD);
                }
            }
        };

        // Add currentItemUpdater before mExternalPageChangeCallbacks, because we need to update
        // internal state first
        mPageChangeEventDispatcher.addOnPageChangeCallback(currentItemUpdater);
        mPageChangeEventDispatcher.addOnPageChangeCallback(focusClearer);
        // Allow a11y to register its listeners after currentItemUpdater (so it has the
        // right data). TODO: replace ordering comments with a test.
        mAccessibilityProvider.onInitialize(mPageChangeEventDispatcher, mRecyclerView);
        mPageChangeEventDispatcher.addOnPageChangeCallback(mExternalPageChangeCallbacks);

        // Add mPageTransformerAdapter after mExternalPageChangeCallbacks, because page transform
        // events must be fired after scroll events
        mPageTransformerAdapter = new PageTransformerAdapter(mLayoutManager);
        mPageChangeEventDispatcher.addOnPageChangeCallback(mPageTransformerAdapter);

        attachViewToParent(mRecyclerView, 0, mRecyclerView.getLayoutParams());
    }

    /**
     * A lot of places in code rely on an assumption that the page fills the whole ViewPager2.
     *
     * TODO(b/70666617) Allow page width different than width/height 100%/100%
     */
    private RecyclerView.OnChildAttachStateChangeListener enforceChildFillListener() {
        return new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                RecyclerView.LayoutParams layoutParams =
                        (RecyclerView.LayoutParams) view.getLayoutParams();
                if (layoutParams.width != LayoutParams.MATCH_PARENT
                        || layoutParams.height != LayoutParams.MATCH_PARENT) {
                    throw new IllegalStateException(
                            "Pages must fill the whole ViewPager2 (use match_parent)");
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                // nothing
            }
        };
    }

    @RequiresApi(23)
    @Override
    public CharSequence getAccessibilityClassName() {
        if (mAccessibilityProvider.handlesGetAccessibilityClassName()) {
            return mAccessibilityProvider.onGetAccessibilityClassName();
        }
        return super.getAccessibilityClassName();
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.mRecyclerViewId = mRecyclerView.getId();
        ss.mCurrentItem = mPendingCurrentItem == NO_POSITION ? mCurrentItem : mPendingCurrentItem;

        if (mPendingAdapterState != null) {
            ss.mAdapterState = mPendingAdapterState;
        } else {
            RecyclerView.Adapter<?> adapter = mRecyclerView.getAdapter();
            if (adapter instanceof StatefulAdapter) {
                ss.mAdapterState = ((StatefulAdapter) adapter).saveState();
            }
        }

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mPendingCurrentItem = ss.mCurrentItem;
        mPendingAdapterState = ss.mAdapterState;
    }

    private void restorePendingState() {
        if (mPendingCurrentItem == NO_POSITION) {
            // No state to restore, or state is already restored
            return;
        }
        RecyclerView.Adapter<?> adapter = getAdapter();
        if (adapter == null) {
            return;
        }
        if (mPendingAdapterState != null) {
            if (adapter instanceof StatefulAdapter) {
                ((StatefulAdapter) adapter).restoreState(mPendingAdapterState);
            }
            mPendingAdapterState = null;
        }
        // Now we have an adapter, we can clamp the pending current item and set it
        mCurrentItem = Math.max(0, Math.min(mPendingCurrentItem, adapter.getItemCount() - 1));
        mPendingCurrentItem = NO_POSITION;
        mRecyclerView.scrollToPosition(mCurrentItem);
        mAccessibilityProvider.onRestorePendingState();
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        // RecyclerView changed an id, so we need to reflect that in the saved state
        Parcelable state = container.get(getId());
        if (state instanceof SavedState) {
            final int previousRvId = ((SavedState) state).mRecyclerViewId;
            final int currentRvId = mRecyclerView.getId();
            container.put(currentRvId, container.get(previousRvId));
            container.remove(previousRvId);
        }

        super.dispatchRestoreInstanceState(container);

        // State of ViewPager2 and its child (RecyclerView) has been restored now
        restorePendingState();
    }

    static class SavedState extends BaseSavedState {
        int mRecyclerViewId;
        int mCurrentItem;
        Parcelable mAdapterState;

        @RequiresApi(24)
        SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            readValues(source, loader);
        }

        SavedState(Parcel source) {
            super(source);
            readValues(source, null);
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        private void readValues(Parcel source, ClassLoader loader) {
            mRecyclerViewId = source.readInt();
            mCurrentItem = source.readInt();
            mAdapterState = source.readParcelable(loader);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mRecyclerViewId);
            out.writeInt(mCurrentItem);
            out.writeParcelable(mAdapterState, flags);
        }

        public static final Creator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return Build.VERSION.SDK_INT >= 24
                        ? new SavedState(source, loader)
                        : new SavedState(source);
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return createFromParcel(source, null);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    /**
     * <p>Set a new adapter to provide page views on demand.</p>
     *
     * <p>If you're planning to use {@link androidx.fragment.app.Fragment Fragments} as pages,
     * implement {@link androidx.viewpager2.adapter.FragmentStateAdapter FragmentStateAdapter}. If
     * your pages are Views, implement {@link RecyclerView.Adapter} as usual.</p>
     *
     * <p>If your pages contain LayoutTransitions, then those LayoutTransitions <em>must</em> have
     * {@code animateParentHierarchy} set to {@code false}. Note that if you have a ViewGroup with
     * {@code animateLayoutChanges="true"} in your layout xml file, a LayoutTransition is added
     * automatically to that ViewGroup. You will need to manually call {@link
     * android.animation.LayoutTransition#setAnimateParentHierarchy(boolean)
     * getLayoutTransition().setAnimateParentHierarchy(false)} on that ViewGroup after you inflated
     * the xml layout, like this:</p>
     *
     * <pre>
     * View view = layoutInflater.inflate(R.layout.page, parent, false);
     * ViewGroup viewGroup = view.findViewById(R.id.animated_viewgroup);
     * viewGroup.getLayoutTransition().setAnimateParentHierarchy(false);
     * </pre>
     *
     * @param adapter The adapter to use, or {@code null} to remove the current adapter
     * @see androidx.viewpager2.adapter.FragmentStateAdapter
     * @see RecyclerView#setAdapter(RecyclerView.Adapter)
     */
    public void setAdapter(@Nullable @SuppressWarnings("rawtypes") RecyclerView.Adapter adapter) {
        final RecyclerView.Adapter<?> currentAdapter = mRecyclerView.getAdapter();
        mAccessibilityProvider.onDetachAdapter(currentAdapter);
        unregisterCurrentItemDataSetTracker(currentAdapter);
        mRecyclerView.setAdapter(adapter);
        mCurrentItem = 0;
        restorePendingState();
        mAccessibilityProvider.onAttachAdapter(adapter);
        registerCurrentItemDataSetTracker(adapter);
    }

    private void registerCurrentItemDataSetTracker(@Nullable RecyclerView.Adapter<?> adapter) {
        if (adapter != null) {
            adapter.registerAdapterDataObserver(mCurrentItemDataSetChangeObserver);
        }
    }

    private void unregisterCurrentItemDataSetTracker(@Nullable RecyclerView.Adapter<?> adapter) {
        if (adapter != null) {
            adapter.unregisterAdapterDataObserver(mCurrentItemDataSetChangeObserver);
        }
    }

    @SuppressWarnings("rawtypes")
    public @Nullable
    RecyclerView.Adapter getAdapter() {
        return mRecyclerView.getAdapter();
    }

    @Override
    public void onViewAdded(View child) {
        // TODO(b/70666620): consider adding a support for Decor views
        throw new IllegalStateException(
                getClass().getSimpleName() + " does not support direct child views");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO(b/70666622): consider margin support
        // TODO(b/70666626): consider delegating all this to RecyclerView
        // TODO(b/70666625): write automated tests for this

        measureChild(mRecyclerView, widthMeasureSpec, heightMeasureSpec);
        int width = mRecyclerView.getMeasuredWidth();
        int height = mRecyclerView.getMeasuredHeight();
        int childState = mRecyclerView.getMeasuredState();

        width += getPaddingLeft() + getPaddingRight();
        height += getPaddingTop() + getPaddingBottom();

        width = Math.max(width, getSuggestedMinimumWidth());
        height = Math.max(height, getSuggestedMinimumHeight());

        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, childState),
                resolveSizeAndState(height, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = mRecyclerView.getMeasuredWidth();
        int height = mRecyclerView.getMeasuredHeight();

        // TODO(b/70666626): consider delegating padding handling to the RecyclerView to avoid
        // an unnatural page transition effect: http://shortn/_Vnug3yZpQT
        mTmpContainerRect.left = getPaddingLeft();
        mTmpContainerRect.right = r - l - getPaddingRight();
        mTmpContainerRect.top = getPaddingTop();
        mTmpContainerRect.bottom = b - t - getPaddingBottom();

        Gravity.apply(Gravity.TOP | Gravity.START, width, height, mTmpContainerRect, mTmpChildRect);
        mRecyclerView.layout(mTmpChildRect.left, mTmpChildRect.top, mTmpChildRect.right,
                mTmpChildRect.bottom);

        if (mCurrentItemDirty) {
            updateCurrentItem();
        }
    }

    /** Updates {@link #mCurrentItem} based on what is currently visible in the viewport. */
    void updateCurrentItem() {
        if (mPagerSnapHelper == null) {
            throw new IllegalStateException("Design assumption violated.");
        }

        View snapView = mPagerSnapHelper.findSnapView(mLayoutManager);
        if (snapView == null) {
            return; // nothing we can do
        }
        int snapPosition = mLayoutManager.getPosition(snapView);

        if (snapPosition != mCurrentItem && getScrollState() == SCROLL_STATE_IDLE) {
            /** TODO: revisit if push to {@link ScrollEventAdapter} / separate component */
            mPageChangeEventDispatcher.onPageSelected(snapPosition);
        }

        mCurrentItemDirty = false;
    }

    int getPageSize() {
        final RecyclerView rv = mRecyclerView;
        return getOrientation() == ORIENTATION_HORIZONTAL
                ? rv.getWidth() - rv.getPaddingLeft() - rv.getPaddingRight()
                : rv.getHeight() - rv.getPaddingTop() - rv.getPaddingBottom();
    }

    /**
     * Sets the orientation of the ViewPager2.
     *
     * @param orientation {@link #ORIENTATION_HORIZONTAL} or {@link #ORIENTATION_VERTICAL}
     */
    public void setOrientation(@Orientation int orientation) {
        mLayoutManager.setOrientation(orientation);
        mAccessibilityProvider.onSetOrientation();
    }

    public @Orientation
    int getOrientation() {
        return mLayoutManager.getOrientation();
    }

    boolean isRtl() {
        return mLayoutManager.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    /**
     * Set the currently selected page. If the ViewPager has already been through its first
     * layout with its current adapter there will be a smooth animated transition between
     * the current item and the specified item. Silently ignored if the adapter is not set or
     * empty. Clamps item to the bounds of the adapter.
     *
     * TODO(b/123069219): verify first layout behavior
     *
     * @param item Item index to select
     */
    public void setCurrentItem(int item) {
        setCurrentItem(item, true);
    }

    /**
     * Set the currently selected page. If {@code smoothScroll = true}, will perform a smooth
     * animation from the current item to the new item. Silently ignored if the adapter is not set
     * or empty. Clamps item to the bounds of the adapter.
     *
     * @param item Item index to select
     * @param smoothScroll True to smoothly scroll to the new item, false to transition immediately
     */
    public void setCurrentItem(int item, boolean smoothScroll) {
        if (isFakeDragging()) {
            throw new IllegalStateException("Cannot change current item when ViewPager2 is fake "
                    + "dragging");
        }
        setCurrentItemInternal(item, smoothScroll);
    }

    void setCurrentItemInternal(int item, boolean smoothScroll) {

        // 1. Preprocessing (check state, validate item, decide if update is necessary, etc)

        RecyclerView.Adapter<?> adapter = getAdapter();
        if (adapter == null) {
            // Update the pending current item if we're still waiting for the adapter
            if (mPendingCurrentItem != NO_POSITION) {
                mPendingCurrentItem = Math.max(item, 0);
            }
            return;
        }
        if (adapter.getItemCount() <= 0) {
            // Adapter is empty
            return;
        }
        item = Math.max(item, 0);
        item = Math.min(item, adapter.getItemCount() - 1);

        if (item == mCurrentItem && mScrollEventAdapter.isIdle()) {
            // Already at the correct page
            return;
        }
        if (item == mCurrentItem && smoothScroll) {
            // Already scrolling to the correct page, but not yet there. Only handle instant scrolls
            // because then we need to interrupt the current smooth scroll.
            return;
        }

        // 2. Update the item internally

        double previousItem = mCurrentItem;
        mCurrentItem = item;
        mAccessibilityProvider.onSetNewCurrentItem();

        if (!mScrollEventAdapter.isIdle()) {
            // Scroll in progress, overwrite previousItem with actual current position
            previousItem = mScrollEventAdapter.getRelativeScrollPosition();
        }

        // 3. Perform the necessary scroll actions on RecyclerView

        mScrollEventAdapter.notifyProgrammaticScroll(item, smoothScroll);
        if (!smoothScroll) {
            mRecyclerView.scrollToPosition(item);
            return;
        }

        // For smooth scroll, pre-jump to nearby item for long jumps.
        if (Math.abs(item - previousItem) > 3) {
            mRecyclerView.scrollToPosition(item > previousItem ? item - 3 : item + 3);
            // TODO(b/114361680): call smoothScrollToPosition synchronously (blocked by b/114019007)
            mRecyclerView.post(new SmoothScrollToPosition(item, mRecyclerView));
        } else {
            mRecyclerView.smoothScrollToPosition(item);
        }
    }

    /**
     * Returns the currently selected page. If no page can sensibly be selected because there is no
     * adapter or the adapter is empty, returns 0.
     *
     * @return Currently selected page
     */
    public int getCurrentItem() {
        return mCurrentItem;
    }

    /**
     * Returns the current scroll state of the ViewPager2. Returned value is one of can be one of
     * {@link #SCROLL_STATE_IDLE}, {@link #SCROLL_STATE_DRAGGING} or {@link #SCROLL_STATE_SETTLING}.
     *
     * @return The scroll state that was last dispatched to {@link
     *         HorizontalViewPager2.OnPageChangeCallback#onPageScrollStateChanged(int)}
     */
    @ScrollState
    public int getScrollState() {
        return mScrollEventAdapter.getScrollState();
    }

    /**
     * Start a fake drag of the pager.
     *
     * <p>A fake drag can be useful if you want to synchronize the motion of the ViewPager2 with the
     * touch scrolling of another view, while still letting the ViewPager2 control the snapping
     * motion and fling behavior. (e.g. parallax-scrolling tabs.) Call {@link #fakeDragBy(float)} to
     * simulate the actual drag motion. Call {@link #endFakeDrag()} to complete the fake drag and
     * fling as necessary.
     *
     * <p>A fake drag can be interrupted by a real drag. From that point on, all calls to {@code
     * fakeDragBy} and {@code endFakeDrag} will be ignored until the next fake drag is started by
     * calling {@code beginFakeDrag}. If you need the ViewPager2 to ignore touch events and other
     * user input during a fake drag, use {@link #setUserInputEnabled(boolean)}. If a real or fake
     * drag is already in progress, this method will return {@code false}.
     *
     * @return {@code true} if the fake drag began successfully, {@code false} if it could not be
     *         started
     *
     * @see #fakeDragBy(float)
     * @see #endFakeDrag()
     * @see #isFakeDragging()
     */
    public boolean beginFakeDrag() {
        return mFakeDragger.beginFakeDrag();
    }

    /**
     * Fake drag by an offset in pixels. You must have called {@link #beginFakeDrag()} first. Drag
     * happens in the direction of the orientation. Positive offsets will drag to the previous page,
     * negative values to the next page, with one exception: if layout direction is set to RTL and
     * the ViewPager2's orientation is horizontal, then the behavior will be inverted. This matches
     * the deltas of touch events that would cause the same real drag.
     *
     * <p>If the pager is not in the fake dragging state anymore, it ignores this call and returns
     * {@code false}.
     *
     * @param offsetPxFloat Offset in pixels to drag by
     * @return {@code true} if the fake drag was executed. If {@code false} is returned, it means
     *         there was no fake drag to end.
     *
     * @see #beginFakeDrag()
     * @see #endFakeDrag()
     * @see #isFakeDragging()
     */
    public boolean fakeDragBy(@SuppressLint("SupportAnnotationUsage") @Px float offsetPxFloat) {
        return mFakeDragger.fakeDragBy(offsetPxFloat);
    }

    /**
     * End a fake drag of the pager.
     *
     * @return {@code true} if the fake drag was ended. If {@code false} is returned, it means there
     *         was no fake drag to end.
     *
     * @see #beginFakeDrag()
     * @see #fakeDragBy(float)
     * @see #isFakeDragging()
     */
    public boolean endFakeDrag() {
        return mFakeDragger.endFakeDrag();
    }

    /**
     * Returns {@code true} if a fake drag is in progress.
     *
     * @return {@code true} if currently in a fake drag, {@code false} otherwise.
     * @see #beginFakeDrag()
     * @see #fakeDragBy(float)
     * @see #endFakeDrag()
     */
    public boolean isFakeDragging() {
        return mFakeDragger.isFakeDragging();
    }

    /**
     * Snaps the ViewPager2 to the closest page
     */
    void snapToPage() {
        // Method copied from PagerSnapHelper#snapToTargetExistingView
        // When fixing something here, make sure to update that method as well
        View view = mPagerSnapHelper.findSnapView(mLayoutManager);
        if (view == null) {
            return;
        }
        int[] snapDistance = mPagerSnapHelper.calculateDistanceToFinalSnap(mLayoutManager, view);
        //noinspection ConstantConditions
        if (snapDistance[0] != 0 || snapDistance[1] != 0) {
            mRecyclerView.smoothScrollBy(snapDistance[0], snapDistance[1]);
        }
    }

    /**
     * Enable or disable user initiated scrolling. This includes touch input (scroll and fling
     * gestures) and accessibility input. Disabling keyboard input is not yet supported. When user
     * initiated scrolling is disabled, programmatic scrolls through {@link #setCurrentItem(int,
     * boolean) setCurrentItem} still work. By default, user initiated scrolling is enabled.
     *
     * @param enabled {@code true} to allow user initiated scrolling, {@code false} to block user
     *        initiated scrolling
     * @see #isUserInputEnabled()
     */
    public void setUserInputEnabled(boolean enabled) {
        mUserInputEnabled = enabled;
        mAccessibilityProvider.onSetUserInputEnabled();
    }

    /**
     * Returns if user initiated scrolling between pages is enabled. Enabled by default.
     *
     * @return {@code true} if users can scroll the ViewPager2, {@code false} otherwise
     * @see #setUserInputEnabled(boolean)
     */
    public boolean isUserInputEnabled() {
        return mUserInputEnabled;
    }

    /**
     * <p>Set the number of pages that should be retained to either side of the currently visible
     * page(s). Pages beyond this limit will be recreated from the adapter when needed. Set this to
     * {@link #OFFSCREEN_PAGE_LIMIT_DEFAULT} to use RecyclerView's caching strategy. The given value
     * must either be larger than 0, or {@code #OFFSCREEN_PAGE_LIMIT_DEFAULT}.</p>
     *
     * <p>Pages within {@code limit} pages away from the current page are created and added to the
     * view hierarchy, even though they are not visible on the screen. Pages outside this limit will
     * be removed from the view hierarchy, but the {@code ViewHolder}s will be recycled as usual by
     * {@link RecyclerView}.</p>
     *
     * <p>This is offered as an optimization. If you know in advance the number of pages you will
     * need to support or have lazy-loading mechanisms in place on your pages, tweaking this setting
     * can have benefits in perceived smoothness of paging animations and interaction. If you have a
     * small number of pages (3-4) that you can keep active all at once, less time will be spent in
     * layout for newly created view subtrees as the user pages back and forth.</p>
     *
     * <p>You should keep this limit low, especially if your pages have complex layouts. By default
     * it is set to {@code OFFSCREEN_PAGE_LIMIT_DEFAULT}.</p>
     *
     * @param limit How many pages will be kept offscreen on either side. Valid values are all
     *        values {@code >= 1} and {@link #OFFSCREEN_PAGE_LIMIT_DEFAULT}
     * @throws IllegalArgumentException If the given limit is invalid
     * @see #getOffscreenPageLimit()
     */
    public void setOffscreenPageLimit(@OffscreenPageLimit int limit) {
        if (limit < 1 && limit != OFFSCREEN_PAGE_LIMIT_DEFAULT) {
            throw new IllegalArgumentException(
                    "Offscreen page limit must be OFFSCREEN_PAGE_LIMIT_DEFAULT or a number > 0");
        }
        mOffscreenPageLimit = limit;
        // Trigger layout so prefetch happens through getExtraLayoutSize()
        mRecyclerView.requestLayout();
    }

    /**
     * Returns the number of pages that will be retained to either side of the current page in the
     * view hierarchy in an idle state. Defaults to {@link #OFFSCREEN_PAGE_LIMIT_DEFAULT}.
     *
     * @return How many pages will be kept offscreen on either side
     * @see #setOffscreenPageLimit(int)
     */
    @OffscreenPageLimit
    public int getOffscreenPageLimit() {
        return mOffscreenPageLimit;
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return mRecyclerView.canScrollHorizontally(direction);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return mRecyclerView.canScrollVertically(direction);
    }

    /**
     * Add a callback that will be invoked whenever the page changes or is incrementally
     * scrolled. See {@link HorizontalViewPager2.OnPageChangeCallback}.
     *
     * <p>Components that add a callback should take care to remove it when finished.
     *
     * @param callback callback to add
     */
    public void registerOnPageChangeCallback(@NonNull OnPageChangeCallback callback) {
        mExternalPageChangeCallbacks.addOnPageChangeCallback(callback);
    }

    /**
     * Remove a callback that was previously added via
     * {@link #registerOnPageChangeCallback(OnPageChangeCallback)}.
     *
     * @param callback callback to remove
     */
    public void unregisterOnPageChangeCallback(@NonNull OnPageChangeCallback callback) {
        mExternalPageChangeCallbacks.removeOnPageChangeCallback(callback);
    }

    /**
     * Sets a {@link PageTransformer} that will be called for each attached page whenever the
     * scroll position is changed. This allows the application to apply custom property
     * transformations to each page, overriding the default sliding behavior.
     * <p>
     * Note: setting a {@link PageTransformer} disables data-set change animations to prevent
     * conflicts between the two animation systems. Setting a {@code null} transformer will restore
     * data-set change animations.
     *
     * @param transformer PageTransformer that will modify each page's animation properties
     *
     * @see MarginPageTransformer
     * @see CompositePageTransformer
     */
    public void setPageTransformer(@Nullable PageTransformer transformer) {
        if (transformer != null) {
            if (!mSavedItemAnimatorPresent) {
                mSavedItemAnimator = mRecyclerView.getItemAnimator();
                mSavedItemAnimatorPresent = true;
            }
            mRecyclerView.setItemAnimator(null);
        } else {
            if (mSavedItemAnimatorPresent) {
                mRecyclerView.setItemAnimator(mSavedItemAnimator);
                mSavedItemAnimator = null;
                mSavedItemAnimatorPresent = false;
            }
        }

        // TODO: add support for reverseDrawingOrder: b/112892792
        // TODO: add support for pageLayerType: b/112893074
        if (transformer == mPageTransformerAdapter.getPageTransformer()) {
            return;
        }
        mPageTransformerAdapter.setPageTransformer(transformer);
        requestTransform();
    }

    /**
     * Trigger a call to the registered {@link PageTransformer PageTransformer}'s {@link
     * PageTransformer#transformPage(View, float) transformPage} method. Call this when something
     * has changed which has invalidated the transformations defined by the {@code PageTransformer}
     * that did not trigger a page scroll.
     */
    public void requestTransform() {
        if (mPageTransformerAdapter.getPageTransformer() == null) {
            return;
        }
        double relativePosition = mScrollEventAdapter.getRelativeScrollPosition();
        int position = (int) relativePosition;
        float positionOffset = (float) (relativePosition - position);
        int positionOffsetPx = Math.round(getPageSize() * positionOffset);
        mPageTransformerAdapter.onPageScrolled(position, positionOffset, positionOffsetPx);
    }

    @Override
    public void setLayoutDirection(int layoutDirection) {
        super.setLayoutDirection(layoutDirection);
        mAccessibilityProvider.onSetLayoutDirection();
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        mAccessibilityProvider.onInitializeAccessibilityNodeInfo(info);
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (mAccessibilityProvider.handlesPerformAccessibilityAction(action, arguments)) {
            return mAccessibilityProvider.onPerformAccessibilityAction(action, arguments);
        }
        return super.performAccessibilityAction(action, arguments);
    }

    /**
     * Slightly modified RecyclerView to get ViewPager behavior in accessibility and to
     * enable/disable user scrolling.
     */
    private class RecyclerViewImpl extends HorizontalRecyclerView {
        RecyclerViewImpl(@NonNull Context context) {
            super(context);
        }

        @RequiresApi(23)
        @Override
        public CharSequence getAccessibilityClassName() {
            if (mAccessibilityProvider.handlesRvGetAccessibilityClassName()) {
                return mAccessibilityProvider.onRvGetAccessibilityClassName();
            }
            return super.getAccessibilityClassName();
        }

        @Override
        public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(event);
            event.setFromIndex(mCurrentItem);
            event.setToIndex(mCurrentItem);
            mAccessibilityProvider.onRvInitializeAccessibilityEvent(event);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return isUserInputEnabled() && super.onTouchEvent(event);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return isUserInputEnabled() && super.onInterceptTouchEvent(ev);
        }
    }

    private class LinearLayoutManagerImpl extends LinearLayoutManager {
        LinearLayoutManagerImpl(Context context) {
            super(context);
        }

        @Override
        public boolean performAccessibilityAction(@NonNull RecyclerView.Recycler recycler,
                                                  @NonNull RecyclerView.State state, int action, @Nullable Bundle args) {
            if (mAccessibilityProvider.handlesLmPerformAccessibilityAction(action)) {
                return mAccessibilityProvider.onLmPerformAccessibilityAction(action);
            }
            return super.performAccessibilityAction(recycler, state, action, args);
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(@NonNull RecyclerView.Recycler recycler,
                                                      @NonNull RecyclerView.State state, @NonNull AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(recycler, state, info);
            mAccessibilityProvider.onLmInitializeAccessibilityNodeInfo(info);
        }

        @Override
        protected void calculateExtraLayoutSpace(@NonNull RecyclerView.State state,
                                                 @NonNull int[] extraLayoutSpace) {
            int pageLimit = getOffscreenPageLimit();
            if (pageLimit == OFFSCREEN_PAGE_LIMIT_DEFAULT) {
                // Only do custom prefetching of offscreen pages if requested
                super.calculateExtraLayoutSpace(state, extraLayoutSpace);
                return;
            }
            final int offscreenSpace = getPageSize() * pageLimit;
            extraLayoutSpace[0] = offscreenSpace;
            extraLayoutSpace[1] = offscreenSpace;
        }

        @Override
        public boolean requestChildRectangleOnScreen(@NonNull RecyclerView parent,
                                                     @NonNull View child, @NonNull Rect rect, boolean immediate,
                                                     boolean focusedChildVisible) {
            return false; // users should use setCurrentItem instead
        }
    }

    private class PagerSnapHelperImpl extends PagerSnapHelper {
        PagerSnapHelperImpl() {
        }

        @Nullable
        @Override
        public View findSnapView(RecyclerView.LayoutManager layoutManager) {
            // When interrupting a smooth scroll with a fake drag, we stop RecyclerView's scroll
            // animation, which fires a scroll state change to IDLE. PagerSnapHelper then kicks in
            // to snap to a page, which we need to prevent here.
            // Simplifying that case: during a fake drag, no snapping should occur.
            return isFakeDragging() ? null : super.findSnapView(layoutManager);
        }
    }

    private static class SmoothScrollToPosition implements Runnable {
        private final int mPosition;
        private final RecyclerView mRecyclerView;

        SmoothScrollToPosition(int position, RecyclerView recyclerView) {
            mPosition = position;
            mRecyclerView = recyclerView; // to avoid a synthetic accessor
        }

        @Override
        public void run() {
            mRecyclerView.smoothScrollToPosition(mPosition);
        }
    }

    /**
     * Callback interface for responding to changing state of the selected page.
     */
    public abstract static class OnPageChangeCallback {
        /**
         * This method will be invoked when the current page is scrolled, either as part
         * of a programmatically initiated smooth scroll or a user initiated touch scroll.
         *
         * @param position Position index of the first page currently being displayed.
         *                 Page position+1 will be visible if positionOffset is nonzero.
         * @param positionOffset Value from [0, 1) indicating the offset from the page at position.
         * @param positionOffsetPixels Value in pixels indicating the offset from position.
         */
        public void onPageScrolled(int position, float positionOffset,
                                   @Px int positionOffsetPixels) {
        }

        /**
         * This method will be invoked when a new page becomes selected. Animation is not
         * necessarily complete.
         *
         * @param position Position index of the new selected page.
         */
        public void onPageSelected(int position) {
        }

        /**
         * Called when the scroll state changes. Useful for discovering when the user begins
         * dragging, when a fake drag is started, when the pager is automatically settling to the
         * current page, or when it is fully stopped/idle. {@code state} can be one of {@link
         * #SCROLL_STATE_IDLE}, {@link #SCROLL_STATE_DRAGGING} or {@link #SCROLL_STATE_SETTLING}.
         */
        public void onPageScrollStateChanged(@ScrollState int state) {
        }
    }

    /**
     * A PageTransformer is invoked whenever a visible/attached page is scrolled.
     * This offers an opportunity for the application to apply a custom transformation
     * to the page views using animation properties.
     */
    public interface PageTransformer {

        /**
         * Apply a property transformation to the given page.
         *
         * @param page Apply the transformation to this page
         * @param position Position of page relative to the current front-and-center
         *                 position of the pager. 0 is front and center. 1 is one full
         *                 page position to the right, and -2 is two pages to the left.
         *                 Minimum / maximum observed values depend on how many pages we keep
         *                 attached, which depends on offscreenPageLimit.
         *
         * @see #setOffscreenPageLimit(int)
         */
        void transformPage(@NonNull View page, float position);
    }

    /**
     * Add an {@link RecyclerView.ItemDecoration} to this ViewPager2. Item decorations can
     * affect both measurement and drawing of individual item views.
     *
     * <p>Item decorations are ordered. Decorations placed earlier in the list will
     * be run/queried/drawn first for their effects on item views. Padding added to views
     * will be nested; a padding added by an earlier decoration will mean further
     * item decorations in the list will be asked to draw/pad within the previous decoration's
     * given area.</p>
     *
     * @param decor Decoration to add
     */
    public void addItemDecoration(@NonNull RecyclerView.ItemDecoration decor) {
        mRecyclerView.addItemDecoration(decor);
    }

    /**
     * Add an {@link RecyclerView.ItemDecoration} to this ViewPager2. Item decorations can
     * affect both measurement and drawing of individual item views.
     *
     * <p>Item decorations are ordered. Decorations placed earlier in the list will
     * be run/queried/drawn first for their effects on item views. Padding added to views
     * will be nested; a padding added by an earlier decoration will mean further
     * item decorations in the list will be asked to draw/pad within the previous decoration's
     * given area.</p>
     *
     * @param decor Decoration to add
     * @param index Position in the decoration chain to insert this decoration at. If this value
     *              is negative the decoration will be added at the end.
     * @throws IndexOutOfBoundsException on indexes larger than {@link #getItemDecorationCount}
     */
    public void addItemDecoration(@NonNull RecyclerView.ItemDecoration decor, int index) {
        mRecyclerView.addItemDecoration(decor, index);
    }

    /**
     * Returns an {@link RecyclerView.ItemDecoration} previously added to this ViewPager2.
     *
     * @param index The index position of the desired ItemDecoration.
     * @return the ItemDecoration at index position
     * @throws IndexOutOfBoundsException on invalid index
     */
    @NonNull
    public RecyclerView.ItemDecoration getItemDecorationAt(int index) {
        return mRecyclerView.getItemDecorationAt(index);
    }

    /**
     * Returns the number of {@link RecyclerView.ItemDecoration} currently added to this ViewPager2.
     *
     * @return number of ItemDecorations currently added added to this ViewPager2.
     */
    public int getItemDecorationCount() {
        return mRecyclerView.getItemDecorationCount();
    }

    /**
     * Invalidates all ItemDecorations. If ViewPager2 has item decorations, calling this method
     * will trigger a {@link #requestLayout()} call.
     */
    public void invalidateItemDecorations() {
        mRecyclerView.invalidateItemDecorations();
    }

    /**
     * Removes the {@link RecyclerView.ItemDecoration} associated with the supplied index position.
     *
     * @param index The index position of the ItemDecoration to be removed.
     * @throws IndexOutOfBoundsException on invalid index
     */
    public void removeItemDecorationAt(int index) {
        mRecyclerView.removeItemDecorationAt(index);
    }

    /**
     * Remove an {@link RecyclerView.ItemDecoration} from this ViewPager2.
     *
     * <p>The given decoration will no longer impact the measurement and drawing of
     * item views.</p>
     *
     * @param decor Decoration to remove
     * @see #addItemDecoration(RecyclerView.ItemDecoration)
     */
    public void removeItemDecoration(@NonNull RecyclerView.ItemDecoration decor) {
        mRecyclerView.removeItemDecoration(decor);
    }

    // TODO(b/141956012): Suppressed during upgrade to AGP 3.6.
    @SuppressWarnings("ClassCanBeStatic")
    private abstract class AccessibilityProvider {
        void onInitialize(@NonNull CompositeOnPageChangeCallback pageChangeEventDispatcher,
                          @NonNull RecyclerView recyclerView) {
        }

        boolean handlesGetAccessibilityClassName() {
            return false;
        }

        String onGetAccessibilityClassName() {
            throw new IllegalStateException("Not implemented.");
        }

        void onRestorePendingState() {
        }

        void onAttachAdapter(@Nullable RecyclerView.Adapter<?> newAdapter) {
        }

        void onDetachAdapter(@Nullable RecyclerView.Adapter<?> oldAdapter) {
        }

        void onSetOrientation() {
        }

        void onSetNewCurrentItem() {
        }

        void onSetUserInputEnabled() {
        }

        void onSetLayoutDirection() {
        }

        void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        }

        boolean handlesPerformAccessibilityAction(int action, Bundle arguments) {
            return false;
        }

        boolean onPerformAccessibilityAction(int action, Bundle arguments) {
            throw new IllegalStateException("Not implemented.");
        }

        void onRvInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
        }

        boolean handlesLmPerformAccessibilityAction(int action) {
            return false;
        }

        boolean onLmPerformAccessibilityAction(int action) {
            throw new IllegalStateException("Not implemented.");
        }

        void onLmInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfoCompat info) {
        }

        boolean handlesRvGetAccessibilityClassName() {
            return false;
        }

        CharSequence onRvGetAccessibilityClassName() {
            throw new IllegalStateException("Not implemented.");
        }
    }

    class BasicAccessibilityProvider extends AccessibilityProvider {
        @Override
        public boolean handlesLmPerformAccessibilityAction(int action) {
            return (action == AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD
                    || action == AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD)
                    && !isUserInputEnabled();
        }

        @Override
        public boolean onLmPerformAccessibilityAction(int action) {
            if (!handlesLmPerformAccessibilityAction(action)) {
                throw new IllegalStateException();
            }
            return false;
        }

        @Override
        public void onLmInitializeAccessibilityNodeInfo(
                @NonNull AccessibilityNodeInfoCompat info) {
            if (!isUserInputEnabled()) {
                info.removeAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_BACKWARD);
                info.removeAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_FORWARD);
                info.setScrollable(false);
            }
        }

        @Override
        public boolean handlesRvGetAccessibilityClassName() {
            return true;
        }

        @Override
        public CharSequence onRvGetAccessibilityClassName() {
            if (!handlesRvGetAccessibilityClassName()) {
                throw new IllegalStateException();
            }
            return "androidx.viewpager.widget.ViewPager";
        }
    }

    class PageAwareAccessibilityProvider extends AccessibilityProvider {
        private final AccessibilityViewCommand mActionPageForward =
                new AccessibilityViewCommand() {
                    @Override
                    public boolean perform(@NonNull View view,
                                           @Nullable CommandArguments arguments) {
                        HorizontalViewPager2 viewPager = (HorizontalViewPager2) view;
                        setCurrentItemFromAccessibilityCommand(viewPager.getCurrentItem() + 1);
                        return true;
                    }
                };

        private final AccessibilityViewCommand mActionPageBackward =
                new AccessibilityViewCommand() {
                    @Override
                    public boolean perform(@NonNull View view,
                                           @Nullable CommandArguments arguments) {
                        HorizontalViewPager2 viewPager = (HorizontalViewPager2) view;
                        setCurrentItemFromAccessibilityCommand(viewPager.getCurrentItem() - 1);
                        return true;
                    }
                };

        private RecyclerView.AdapterDataObserver mAdapterDataObserver;

        @Override
        public void onInitialize(@NonNull CompositeOnPageChangeCallback pageChangeEventDispatcher,
                                 @NonNull RecyclerView recyclerView) {
            ViewCompat.setImportantForAccessibility(recyclerView,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);

            mAdapterDataObserver = new DataSetChangeObserver() {
                @Override
                public void onChanged() {
                    updatePageAccessibilityActions();
                }
            };

            if (ViewCompat.getImportantForAccessibility(HorizontalViewPager2.this)
                    == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
                ViewCompat.setImportantForAccessibility(HorizontalViewPager2.this,
                        ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
            }
        }

        @Override
        public boolean handlesGetAccessibilityClassName() {
            return true;
        }

        @Override
        public String onGetAccessibilityClassName() {
            if (!handlesGetAccessibilityClassName()) {
                throw new IllegalStateException();
            }
            return "androidx.viewpager.widget.ViewPager";
        }

        @Override
        public void onRestorePendingState() {
            updatePageAccessibilityActions();
        }

        @Override
        public void onAttachAdapter(@Nullable RecyclerView.Adapter<?> newAdapter) {
            updatePageAccessibilityActions();
            if (newAdapter != null) {
                newAdapter.registerAdapterDataObserver(mAdapterDataObserver);
            }
        }

        @Override
        public void onDetachAdapter(@Nullable RecyclerView.Adapter<?> oldAdapter) {
            if (oldAdapter != null) {
                oldAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
            }
        }

        @Override
        public void onSetOrientation() {
            updatePageAccessibilityActions();
        }

        @Override
        public void onSetNewCurrentItem() {
            updatePageAccessibilityActions();
        }

        @Override
        public void onSetUserInputEnabled() {
            updatePageAccessibilityActions();
        }

        @Override
        public void onSetLayoutDirection() {
            updatePageAccessibilityActions();
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            addCollectionInfo(info);
            addScrollActions(info);
        }

        @Override
        public boolean handlesPerformAccessibilityAction(int action, Bundle arguments) {
            return action == AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD
                    || action == AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD;
        }

        @Override
        public boolean onPerformAccessibilityAction(int action, Bundle arguments) {
            if (!handlesPerformAccessibilityAction(action, arguments)) {
                throw new IllegalStateException();
            }

            int nextItem = (action == AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD)
                    ? getCurrentItem() - 1
                    : getCurrentItem() + 1;
            setCurrentItemFromAccessibilityCommand(nextItem);
            return true;
        }

        @Override
        public void onRvInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
            event.setSource(HorizontalViewPager2.this);
            event.setClassName(onGetAccessibilityClassName());
        }

        /**
         * Sets the current item without checking if a fake drag is ongoing. Only call this method
         * from within an accessibility command or other forms of user input. Call is ignored if
         * {@link #isUserInputEnabled() user input is disabled}.
         */
        void setCurrentItemFromAccessibilityCommand(int item) {
            if (isUserInputEnabled()) {
                setCurrentItemInternal(item, true);
            }
        }

        /**
         * Update the ViewPager2's available page accessibility actions. These are updated in
         * response to page, adapter, and orientation changes. Compatible with API >= 21.
         */
        void updatePageAccessibilityActions() {
            HorizontalViewPager2 viewPager = HorizontalViewPager2.this;

            @SuppressLint("InlinedApi")
            final int actionIdPageLeft = android.R.id.accessibilityActionPageLeft;
            @SuppressLint("InlinedApi")
            final int actionIdPageRight = android.R.id.accessibilityActionPageRight;
            @SuppressLint("InlinedApi")
            final int actionIdPageUp = android.R.id.accessibilityActionPageUp;
            @SuppressLint("InlinedApi")
            final int actionIdPageDown = android.R.id.accessibilityActionPageDown;

            ViewCompat.removeAccessibilityAction(viewPager, actionIdPageLeft);
            ViewCompat.removeAccessibilityAction(viewPager, actionIdPageRight);
            ViewCompat.removeAccessibilityAction(viewPager, actionIdPageUp);
            ViewCompat.removeAccessibilityAction(viewPager, actionIdPageDown);

            if (getAdapter() == null) {
                return;
            }

            int itemCount = getAdapter().getItemCount();
            if (itemCount == 0) {
                return;
            }

            if (!isUserInputEnabled()) {
                return;
            }

            if (getOrientation() == ORIENTATION_HORIZONTAL) {
                boolean isLayoutRtl = isRtl();
                int actionIdPageForward = isLayoutRtl ? actionIdPageLeft : actionIdPageRight;
                int actionIdPageBackward = isLayoutRtl ? actionIdPageRight : actionIdPageLeft;

                if (mCurrentItem < itemCount - 1) {
                    ViewCompat.replaceAccessibilityAction(viewPager,
                            new AccessibilityNodeInfoCompat.AccessibilityActionCompat(actionIdPageForward, null), null,
                            mActionPageForward);
                }
                if (mCurrentItem > 0) {
                    ViewCompat.replaceAccessibilityAction(viewPager,
                            new AccessibilityNodeInfoCompat.AccessibilityActionCompat(actionIdPageBackward, null), null,
                            mActionPageBackward);
                }
            } else {
                if (mCurrentItem < itemCount - 1) {
                    ViewCompat.replaceAccessibilityAction(viewPager,
                            new AccessibilityNodeInfoCompat.AccessibilityActionCompat(actionIdPageDown, null), null,
                            mActionPageForward);
                }
                if (mCurrentItem > 0) {
                    ViewCompat.replaceAccessibilityAction(viewPager,
                            new AccessibilityNodeInfoCompat.AccessibilityActionCompat(actionIdPageUp, null), null,
                            mActionPageBackward);
                }
            }
        }

        private void addCollectionInfo(AccessibilityNodeInfo info) {
            int rowCount = 0;
            int colCount = 0;
            if (getAdapter() != null) {
                if (getOrientation() == ORIENTATION_VERTICAL) {
                    rowCount = getAdapter().getItemCount();
                } else {
                    colCount = getAdapter().getItemCount();
                }
            }
            AccessibilityNodeInfoCompat nodeInfoCompat = AccessibilityNodeInfoCompat.wrap(info);
            AccessibilityNodeInfoCompat.CollectionInfoCompat collectionInfo =
                    AccessibilityNodeInfoCompat.CollectionInfoCompat.obtain(rowCount, colCount,
                            /* hierarchical= */false,
                            AccessibilityNodeInfoCompat.CollectionInfoCompat.SELECTION_MODE_NONE);
            nodeInfoCompat.setCollectionInfo(collectionInfo);
        }

        private void addScrollActions(AccessibilityNodeInfo info) {
            final RecyclerView.Adapter<?> adapter = getAdapter();
            if (adapter == null) {
                return;
            }
            int itemCount = adapter.getItemCount();
            if (itemCount == 0 || !isUserInputEnabled()) {
                return;
            }
            if (mCurrentItem > 0) {
                info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD);
            }
            if (mCurrentItem < itemCount - 1) {
                info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD);
            }
            info.setScrollable(true);
        }
    }

    /**
     * Simplified {@link RecyclerView.AdapterDataObserver} for clients interested in any data-set
     * changes regardless of their nature.
     */
    private abstract static class DataSetChangeObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public abstract void onChanged();

        @Override
        public final void onItemRangeChanged(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public final void onItemRangeChanged(int positionStart, int itemCount,
                                             @Nullable Object payload) {
            onChanged();
        }

        @Override
        public final void onItemRangeInserted(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public final void onItemRangeRemoved(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public final void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            onChanged();
        }
    }
}
/**
 * Dispatches {@link HorizontalViewPager2.OnPageChangeCallback} events to subscribers.
 */
final class CompositeOnPageChangeCallback extends HorizontalViewPager2.OnPageChangeCallback {
    @NonNull
    private final List<HorizontalViewPager2.OnPageChangeCallback> mCallbacks;

    CompositeOnPageChangeCallback(int initialCapacity) {
        mCallbacks = new ArrayList<>(initialCapacity);
    }

    /**
     * Adds the given callback to the list of subscribers
     */
    void addOnPageChangeCallback(HorizontalViewPager2.OnPageChangeCallback callback) {
        mCallbacks.add(callback);
    }

    /**
     * Removes the given callback from the list of subscribers
     */
    void removeOnPageChangeCallback(HorizontalViewPager2.OnPageChangeCallback callback) {
        mCallbacks.remove(callback);
    }

    /**
     * @see HorizontalViewPager2.OnPageChangeCallback#onPageScrolled(int, float, int)
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, @Px int positionOffsetPixels) {
        try {
            for (HorizontalViewPager2.OnPageChangeCallback callback : mCallbacks) {
                callback.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        } catch (ConcurrentModificationException ex) {
            throwCallbackListModifiedWhileInUse(ex);
        }
    }

    /**
     * @see HorizontalViewPager2.OnPageChangeCallback#onPageSelected(int)
     */
    @Override
    public void onPageSelected(int position) {
        try {
            for (HorizontalViewPager2.OnPageChangeCallback callback : mCallbacks) {
                callback.onPageSelected(position);
            }
        } catch (ConcurrentModificationException ex) {
            throwCallbackListModifiedWhileInUse(ex);
        }
    }

    /**
     * @see HorizontalViewPager2.OnPageChangeCallback#onPageScrollStateChanged(int)
     */
    @Override
    public void onPageScrollStateChanged(@HorizontalViewPager2.ScrollState int state) {
        try {
            for (HorizontalViewPager2.OnPageChangeCallback callback : mCallbacks) {
                callback.onPageScrollStateChanged(state);
            }
        } catch (ConcurrentModificationException ex) {
            throwCallbackListModifiedWhileInUse(ex);
        }
    }

    private void throwCallbackListModifiedWhileInUse(ConcurrentModificationException parent) {
        throw new IllegalStateException(
                "Adding and removing callbacks during dispatch to callbacks is not supported",
                parent
        );
    }

}
final class FakeDrag {
    private final HorizontalViewPager2 mViewPager;
    private final ScrollEventAdapter mScrollEventAdapter;
    private final RecyclerView mRecyclerView;

    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private float mRequestedDragDistance;
    private int mActualDraggedDistance;
    private long mFakeDragBeginTime;

    FakeDrag(HorizontalViewPager2 viewPager, ScrollEventAdapter scrollEventAdapter,
             RecyclerView recyclerView) {
        mViewPager = viewPager;
        mScrollEventAdapter = scrollEventAdapter;
        mRecyclerView = recyclerView;
    }

    boolean isFakeDragging() {
        return mScrollEventAdapter.isFakeDragging();
    }

    @UiThread
    boolean beginFakeDrag() {
        if (mScrollEventAdapter.isDragging()) {
            return false;
        }
        mRequestedDragDistance = mActualDraggedDistance = 0;
        mFakeDragBeginTime = SystemClock.uptimeMillis();
        beginFakeVelocityTracker();

        mScrollEventAdapter.notifyBeginFakeDrag();
        if (!mScrollEventAdapter.isIdle()) {
            // Stop potentially running settling animation
            mRecyclerView.stopScroll();
        }
        addFakeMotionEvent(mFakeDragBeginTime, MotionEvent.ACTION_DOWN, 0, 0);
        return true;
    }

    @UiThread
    boolean fakeDragBy(float offsetPxFloat) {
        if (!mScrollEventAdapter.isFakeDragging()) {
            // Can happen legitimately if user started dragging during fakeDrag and app is still
            // sending fakeDragBy commands
            return false;
        }
        // Subtract the offset, because content scrolls in the opposite direction of finger motion
        mRequestedDragDistance -= offsetPxFloat;
        // Calculate amount of pixels to scroll ...
        int offsetPx = Math.round(mRequestedDragDistance - mActualDraggedDistance);
        // ... and keep track of pixels scrolled so we don't get rounding errors
        mActualDraggedDistance += offsetPx;
        long time = SystemClock.uptimeMillis();

        boolean isHorizontal = mViewPager.getOrientation() == HorizontalViewPager2.ORIENTATION_HORIZONTAL;
        // Scroll deltas use pixels:
        final int offsetX = isHorizontal ? offsetPx : 0;
        final int offsetY = isHorizontal ? 0 : offsetPx;
        // Motion events get the raw float distance:
        final float x = isHorizontal ? mRequestedDragDistance : 0;
        final float y = isHorizontal ? 0 : mRequestedDragDistance;

        mRecyclerView.scrollBy(offsetX, offsetY);
        addFakeMotionEvent(time, MotionEvent.ACTION_MOVE, x, y);
        return true;
    }

    @UiThread
    boolean endFakeDrag() {
        if (!mScrollEventAdapter.isFakeDragging()) {
            // Happens legitimately if user started dragging during fakeDrag
            return false;
        }

        mScrollEventAdapter.notifyEndFakeDrag();

        // Compute the velocity of the fake drag
        final int pixelsPerSecond = 1000;
        final VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(pixelsPerSecond, mMaximumVelocity);
        int xVelocity = (int) velocityTracker.getXVelocity();
        int yVelocity = (int) velocityTracker.getYVelocity();
        // And fling or snap the ViewPager2 to its destination
        if (!mRecyclerView.fling(xVelocity, yVelocity)) {
            // Velocity too low, trigger snap to page manually
            mViewPager.snapToPage();
        }
        return true;
    }

    private void beginFakeVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
            final ViewConfiguration configuration = ViewConfiguration.get(mViewPager.getContext());
            mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void addFakeMotionEvent(long time, int action, float x, float y) {
        final MotionEvent ev = MotionEvent.obtain(mFakeDragBeginTime, time, action, x, y, 0);
        mVelocityTracker.addMovement(ev);
        ev.recycle();
    }
}
/**
 * Translates {@link HorizontalViewPager2.OnPageChangeCallback} events
 * to {@link HorizontalViewPager2.PageTransformer} events.
 */
final class PageTransformerAdapter extends HorizontalViewPager2.OnPageChangeCallback {
    private final LinearLayoutManager mLayoutManager;

    private HorizontalViewPager2.PageTransformer mPageTransformer;

    PageTransformerAdapter(LinearLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    HorizontalViewPager2.PageTransformer getPageTransformer() {
        return mPageTransformer;
    }

    /**
     * Sets the PageTransformer. The page transformer will be called for each attached page whenever
     * the scroll position is changed.
     *
     * @param transformer The PageTransformer
     */
    void setPageTransformer(@Nullable HorizontalViewPager2.PageTransformer transformer) {
        // TODO: add support for reverseDrawingOrder: b/112892792
        // TODO: add support for pageLayerType: b/112893074
        mPageTransformer = transformer;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mPageTransformer == null) {
            return;
        }

        float transformOffset = -positionOffset;
        for (int i = 0; i < mLayoutManager.getChildCount(); i++) {
            View view = mLayoutManager.getChildAt(i);
            if (view == null) {
                throw new IllegalStateException(String.format(Locale.US,
                        "LayoutManager returned a null child at pos %d/%d while transforming pages",
                        i, mLayoutManager.getChildCount()));
            }
            int currPos = mLayoutManager.getPosition(view);
            float viewOffset = transformOffset + (currPos - position);
            mPageTransformer.transformPage(view, viewOffset);
        }
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
/**
 * Translates {@link RecyclerView.OnScrollListener} events to {@link HorizontalViewPager2.OnPageChangeCallback} events
 * for {@link HorizontalViewPager2}. As part of this process, it keeps track of the current scroll position
 * relative to the pages and exposes this position via ({@link #getRelativeScrollPosition()}.
 */
final class ScrollEventAdapter extends RecyclerView.OnScrollListener {
    /** @hide */
    @Retention(SOURCE)
    @IntDef({STATE_IDLE, STATE_IN_PROGRESS_MANUAL_DRAG, STATE_IN_PROGRESS_SMOOTH_SCROLL,
            STATE_IN_PROGRESS_IMMEDIATE_SCROLL, STATE_IN_PROGRESS_FAKE_DRAG})
    private @interface AdapterState {
    }

    private static final int STATE_IDLE = 0;
    private static final int STATE_IN_PROGRESS_MANUAL_DRAG = 1;
    private static final int STATE_IN_PROGRESS_SMOOTH_SCROLL = 2;
    private static final int STATE_IN_PROGRESS_IMMEDIATE_SCROLL = 3;
    private static final int STATE_IN_PROGRESS_FAKE_DRAG = 4;

    private static final int NO_POSITION = -1;

    private HorizontalViewPager2.OnPageChangeCallback mCallback;
    private final @NonNull HorizontalViewPager2 mViewPager;
    private final @NonNull RecyclerView mRecyclerView;
    private final @NonNull LinearLayoutManager mLayoutManager;

    // state related fields
    private @AdapterState int mAdapterState;
    private @HorizontalViewPager2.ScrollState int mScrollState;
    private ScrollEventAdapter.ScrollEventValues mScrollValues;
    private int mDragStartPosition;
    private int mTarget;
    private boolean mDispatchSelected;
    private boolean mScrollHappened;
    private boolean mDataSetChangeHappened;
    private boolean mFakeDragging;

    ScrollEventAdapter(@NonNull HorizontalViewPager2 viewPager) {
        mViewPager = viewPager;
        mRecyclerView = mViewPager.mRecyclerView;
        //noinspection ConstantConditions
        mLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        mScrollValues = new ScrollEventAdapter.ScrollEventValues();
        resetState();
    }

    private void resetState() {
        mAdapterState = STATE_IDLE;
        mScrollState = HorizontalViewPager2.SCROLL_STATE_IDLE;
        mScrollValues.reset();
        mDragStartPosition = NO_POSITION;
        mTarget = NO_POSITION;
        mDispatchSelected = false;
        mScrollHappened = false;
        mFakeDragging = false;
        mDataSetChangeHappened = false;
    }

    /**
     * This method only deals with some cases of {@link AdapterState} transitions. The rest of
     * the state transition implementation is in the {@link #onScrolled} method.
     */
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        // User started a drag (not dragging -> dragging)
        if ((mAdapterState != STATE_IN_PROGRESS_MANUAL_DRAG
                || mScrollState != HorizontalViewPager2.SCROLL_STATE_DRAGGING)
                && newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            startDrag(false);
            return;
        }

        // Drag is released, RecyclerView is snapping to page (dragging -> settling)
        // Note that mAdapterState is not updated, to remember we were dragging when settling
        if (isInAnyDraggingState() && newState == RecyclerView.SCROLL_STATE_SETTLING) {
            // Only go through the settling phase if the drag actually moved the page
            if (mScrollHappened) {
                dispatchStateChanged(HorizontalViewPager2.SCROLL_STATE_SETTLING);
                // Determine target page and dispatch onPageSelected on next scroll event
                mDispatchSelected = true;
            }
            return;
        }

        // Drag is finished (dragging || settling -> idle)
        if (isInAnyDraggingState() && newState == RecyclerView.SCROLL_STATE_IDLE) {
            boolean dispatchIdle = false;
            updateScrollEventValues();
            if (!mScrollHappened) {
                // Pages didn't move during drag, so either we're at the start or end of the list,
                // or there are no pages at all.
                // In the first case, ViewPager's contract requires at least one scroll event.
                // In the second case, don't send that scroll event
                if (mScrollValues.mPosition != RecyclerView.NO_POSITION) {
                    dispatchScrolled(mScrollValues.mPosition, 0f, 0);
                }
                dispatchIdle = true;
            } else if (mScrollValues.mOffsetPx == 0) {
                // Normally we dispatch the selected page and go to idle in onScrolled when
                // mOffsetPx == 0, but in this case the drag was still ongoing when onScrolled was
                // called, so that didn't happen. And since mOffsetPx == 0, there will be no further
                // scroll events, so fire the onPageSelected event and go to idle now.
                // Note that if we _did_ go to idle in that last onScrolled event, this code will
                // not be executed because mAdapterState has been reset to STATE_IDLE.
                dispatchIdle = true;
                if (mDragStartPosition != mScrollValues.mPosition) {
                    dispatchSelected(mScrollValues.mPosition);
                }
            }
            if (dispatchIdle) {
                // Normally idle is fired in last onScrolled call, but either onScrolled was never
                // called, or we were still dragging when the last onScrolled was called
                dispatchStateChanged(HorizontalViewPager2.SCROLL_STATE_IDLE);
                resetState();
            }
        }

        if (mAdapterState == STATE_IN_PROGRESS_SMOOTH_SCROLL
                && newState == RecyclerView.SCROLL_STATE_IDLE && mDataSetChangeHappened) {
            updateScrollEventValues();
            if (mScrollValues.mOffsetPx == 0) {
                if (mTarget != mScrollValues.mPosition) {
                    dispatchSelected(
                            mScrollValues.mPosition == NO_POSITION ? 0 : mScrollValues.mPosition);
                }
                dispatchStateChanged(HorizontalViewPager2.SCROLL_STATE_IDLE);
                resetState();
            }
        }
    }

    /**
     * This method only deals with some cases of {@link AdapterState} transitions. The rest of
     * the state transition implementation is in the {@link #onScrollStateChanged} method.
     */
    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        mScrollHappened = true;
        updateScrollEventValues();

        if (mDispatchSelected) {
            // Drag started settling, need to calculate target page and dispatch onPageSelected now
            mDispatchSelected = false;
            boolean scrollingForward = dy > 0 || (dy == 0 && dx < 0 == mViewPager.isRtl());

            // "&& values.mOffsetPx != 0": filters special case where we're scrolling forward and
            // the first scroll event after settling already got us at the target
            mTarget = scrollingForward && mScrollValues.mOffsetPx != 0
                    ? mScrollValues.mPosition + 1 : mScrollValues.mPosition;
            if (mDragStartPosition != mTarget) {
                dispatchSelected(mTarget);
            }
        } else if (mAdapterState == STATE_IDLE) {
            // onScrolled while IDLE means RV has just been populated after an adapter has been set.
            // Contract requires us to fire onPageSelected as well.
            int position = mScrollValues.mPosition;
            // Contract forbids us to send position = -1 though
            dispatchSelected(position == NO_POSITION ? 0 : position);
        }

        // If position = -1, there are no items. Contract says to send position = 0 instead.
        dispatchScrolled(mScrollValues.mPosition == NO_POSITION ? 0 : mScrollValues.mPosition,
                mScrollValues.mOffset, mScrollValues.mOffsetPx);

        // Dispatch idle in onScrolled instead of in onScrollStateChanged because RecyclerView
        // doesn't send IDLE event when using setCurrentItem(x, false)
        if ((mScrollValues.mPosition == mTarget || mTarget == NO_POSITION)
                && mScrollValues.mOffsetPx == 0 && !(mScrollState == HorizontalViewPager2.SCROLL_STATE_DRAGGING)) {
            // When the target page is reached and the user is not dragging anymore, we're settled,
            // so go to idle.
            // Special case and a bit of a hack when mTarget == NO_POSITION: RecyclerView is being
            // initialized and fires a single scroll event. This flags mScrollHappened, so we need
            // to reset our state. However, we don't want to dispatch idle. But that won't happen;
            // because we were already idle.
            dispatchStateChanged(HorizontalViewPager2.SCROLL_STATE_IDLE);
            resetState();
        }
    }

    /**
     * Calculates the current position and the offset (as a percentage and in pixels) of that
     * position from the center.
     */
    private void updateScrollEventValues() {
        ScrollEventAdapter.ScrollEventValues values = mScrollValues;

        values.mPosition = mLayoutManager.findFirstVisibleItemPosition();
        if (values.mPosition == RecyclerView.NO_POSITION) {
            values.reset();
            return;
        }
        View firstVisibleView = mLayoutManager.findViewByPosition(values.mPosition);
        if (firstVisibleView == null) {
            values.reset();
            return;
        }

        int leftDecorations = mLayoutManager.getLeftDecorationWidth(firstVisibleView);
        int rightDecorations = mLayoutManager.getRightDecorationWidth(firstVisibleView);
        int topDecorations = mLayoutManager.getTopDecorationHeight(firstVisibleView);
        int bottomDecorations = mLayoutManager.getBottomDecorationHeight(firstVisibleView);

        ViewGroup.LayoutParams params = firstVisibleView.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams margin = (ViewGroup.MarginLayoutParams) params;
            leftDecorations += margin.leftMargin;
            rightDecorations += margin.rightMargin;
            topDecorations += margin.topMargin;
            bottomDecorations += margin.bottomMargin;
        }

        int decoratedHeight = firstVisibleView.getHeight() + topDecorations + bottomDecorations;
        int decoratedWidth = firstVisibleView.getWidth() + leftDecorations + rightDecorations;

        boolean isHorizontal = mLayoutManager.getOrientation() == HorizontalViewPager2.ORIENTATION_HORIZONTAL;
        int start, sizePx;
        if (isHorizontal) {
            sizePx = decoratedWidth;
            start = firstVisibleView.getLeft() - leftDecorations - mRecyclerView.getPaddingLeft();
            if (mViewPager.isRtl()) {
                start = -start;
            }
        } else {
            sizePx = decoratedHeight;
            start = firstVisibleView.getTop() - topDecorations - mRecyclerView.getPaddingTop();
        }

        values.mOffsetPx = -start;
        if (values.mOffsetPx < 0) {
            // We're in an error state. Figure out if this might have been caused
            // by animateLayoutChanges and throw a descriptive exception if so
            if (new AnimateLayoutChangeDetector(mLayoutManager).mayHaveInterferingAnimations()) {
                throw new IllegalStateException("Page(s) contain a ViewGroup with a "
                        + "LayoutTransition (or animateLayoutChanges=\"true\"), which interferes "
                        + "with the scrolling animation. Make sure to call getLayoutTransition()"
                        + ".setAnimateParentHierarchy(false) on all ViewGroups with a "
                        + "LayoutTransition before an animation is started.");
            }

            // Throw a generic exception otherwise
            throw new IllegalStateException(String.format(Locale.US, "Page can only be offset by a "
                    + "positive amount, not by %d", values.mOffsetPx));
        }
        values.mOffset = sizePx == 0 ? 0 : (float) values.mOffsetPx / sizePx;
    }

    private void startDrag(boolean isFakeDrag) {
        mFakeDragging = isFakeDrag;
        mAdapterState = isFakeDrag ? STATE_IN_PROGRESS_FAKE_DRAG : STATE_IN_PROGRESS_MANUAL_DRAG;
        if (mTarget != NO_POSITION) {
            // Target was set means we were settling to that target
            // Update "drag start page" to reflect the page that ViewPager2 thinks it is at
            mDragStartPosition = mTarget;
            // Reset target because drags have no target until released
            mTarget = NO_POSITION;
        } else if (mDragStartPosition == NO_POSITION) {
            // ViewPager2 was at rest, set "drag start page" to current page
            mDragStartPosition = getPosition();
        }
        dispatchStateChanged(HorizontalViewPager2.SCROLL_STATE_DRAGGING);
    }

    void notifyDataSetChangeHappened() {
        mDataSetChangeHappened = true;
    }

    /**
     * Let the adapter know a programmatic scroll was initiated.
     */
    void notifyProgrammaticScroll(int target, boolean smooth) {
        mAdapterState = smooth
                ? STATE_IN_PROGRESS_SMOOTH_SCROLL
                : STATE_IN_PROGRESS_IMMEDIATE_SCROLL;
        // mFakeDragging is true when a fake drag is interrupted by an a11y command
        // set it to false so endFakeDrag won't fling the RecyclerView
        mFakeDragging = false;
        boolean hasNewTarget = mTarget != target;
        mTarget = target;
        dispatchStateChanged(HorizontalViewPager2.SCROLL_STATE_SETTLING);
        if (hasNewTarget) {
            dispatchSelected(target);
        }
    }

    /**
     * Let the adapter know that a fake drag has started.
     */
    void notifyBeginFakeDrag() {
        mAdapterState = STATE_IN_PROGRESS_FAKE_DRAG;
        startDrag(true);
    }

    /**
     * Let the adapter know that a fake drag has ended.
     */
    void notifyEndFakeDrag() {
        if (isDragging() && !mFakeDragging) {
            // Real drag has already taken over, no need to post process the fake drag
            return;
        }
        mFakeDragging = false;
        updateScrollEventValues();
        if (mScrollValues.mOffsetPx == 0) {
            // We're snapped, so dispatch an IDLE event
            if (mScrollValues.mPosition != mDragStartPosition) {
                dispatchSelected(mScrollValues.mPosition);
            }
            dispatchStateChanged(HorizontalViewPager2.SCROLL_STATE_IDLE);
            resetState();
        } else {
            // We're not snapped, so dispatch a SETTLING event
            dispatchStateChanged(HorizontalViewPager2.SCROLL_STATE_SETTLING);
        }
    }

    void setOnPageChangeCallback(HorizontalViewPager2.OnPageChangeCallback callback) {
        mCallback = callback;
    }

    int getScrollState() {
        return mScrollState;
    }

    /**
     * @return {@code true} if there is no known scroll in progress
     */
    boolean isIdle() {
        return mScrollState == HorizontalViewPager2.SCROLL_STATE_IDLE;
    }

    /**
     * @return {@code true} if the ViewPager2 is being dragged. Returns {@code false} from the
     *         moment the ViewPager2 starts settling or goes idle.
     */
    boolean isDragging() {
        return mScrollState == HorizontalViewPager2.SCROLL_STATE_DRAGGING;
    }

    /**
     * @return {@code true} if a fake drag is ongoing. Returns {@code false} from the moment the
     *         {@link HorizontalViewPager2#endFakeDrag()} is called.
     */
    boolean isFakeDragging() {
        return mFakeDragging;
    }

    /**
     * Checks if the adapter state (not the scroll state) is in the manual or fake dragging state.
     * @return {@code true} if {@link #mAdapterState} is either {@link
     *         #STATE_IN_PROGRESS_MANUAL_DRAG} or {@link #STATE_IN_PROGRESS_FAKE_DRAG}
     */
    private boolean isInAnyDraggingState() {
        return mAdapterState == STATE_IN_PROGRESS_MANUAL_DRAG
                || mAdapterState == STATE_IN_PROGRESS_FAKE_DRAG;
    }

    /**
     * Calculates the scroll position of the currently visible item of the ViewPager relative to its
     * width. Calculated by adding the fraction by which the first visible item is off screen to its
     * adapter position. E.g., if the ViewPager is currently scrolling from the second to the third
     * page, the returned value will be between 1 and 2. Thus, non-integral values mean that the
     * the ViewPager is settling towards its {@link HorizontalViewPager2#getCurrentItem() current item}, or
     * the user may be dragging it.
     *
     * @return The current scroll position of the ViewPager, relative to its width
     */
    double getRelativeScrollPosition() {
        updateScrollEventValues();
        return mScrollValues.mPosition + (double) mScrollValues.mOffset;
    }

    private void dispatchStateChanged(@HorizontalViewPager2.ScrollState int state) {
        // Callback contract for immediate-scroll requires not having state change notifications,
        // but only when there was no smooth scroll in progress.
        // By putting a suppress statement in here (rather than next to dispatch calls) we are
        // simplifying the code of the class and enforcing the contract in one place.
        if (mAdapterState == STATE_IN_PROGRESS_IMMEDIATE_SCROLL
                && mScrollState == HorizontalViewPager2.SCROLL_STATE_IDLE) {
            return;
        }
        if (mScrollState == state) {
            return;
        }

        mScrollState = state;
        if (mCallback != null) {
            mCallback.onPageScrollStateChanged(state);
        }
    }

    private void dispatchSelected(int target) {
        if (mCallback != null) {
            mCallback.onPageSelected(target);
        }
    }

    private void dispatchScrolled(int position, float offset, int offsetPx) {
        if (mCallback != null) {
            mCallback.onPageScrolled(position, offset, offsetPx);
        }
    }

    private int getPosition() {
        return mLayoutManager.findFirstVisibleItemPosition();
    }

    private static final class ScrollEventValues {
        int mPosition;
        float mOffset;
        int mOffsetPx;

        // to avoid a synthetic accessor
        ScrollEventValues() {
        }

        void reset() {
            mPosition = RecyclerView.NO_POSITION;
            mOffset = 0f;
            mOffsetPx = 0;
        }
    }
}
/**
 * Class used to detect if there are gaps between pages and if any of the pages contain a running
 * change-transition in case we detected an illegal state in the {@link ScrollEventAdapter}.
 *
 * This is an approximation of the detection and could potentially lead to misleading advice. If we
 * hit problems with it, remove the detection and replace with a suggestive error message instead,
 * like "Negative page offset encountered. Did you setAnimateParentHierarchy(false) to all your
 * LayoutTransitions?".
 */
final class AnimateLayoutChangeDetector {
    private static final ViewGroup.MarginLayoutParams ZERO_MARGIN_LAYOUT_PARAMS;

    static {
        ZERO_MARGIN_LAYOUT_PARAMS = new ViewGroup.MarginLayoutParams(MATCH_PARENT, MATCH_PARENT);
        ZERO_MARGIN_LAYOUT_PARAMS.setMargins(0, 0, 0, 0);
    }

    private LinearLayoutManager mLayoutManager;

    AnimateLayoutChangeDetector(@NonNull LinearLayoutManager llm) {
        mLayoutManager = llm;
    }

    boolean mayHaveInterferingAnimations() {
        // Two conditions need to be satisfied:
        // 1) the pages are not laid out contiguously (i.e., there are gaps between them)
        // 2) there is a ViewGroup with a LayoutTransition that isChangingLayout()
        return (!arePagesLaidOutContiguously() || mLayoutManager.getChildCount() <= 1)
                && hasRunningChangingLayoutTransition();
    }

    private boolean arePagesLaidOutContiguously() {
        // Collect view positions
        int childCount = mLayoutManager.getChildCount();
        if (childCount == 0) {
            return true;
        }

        boolean isHorizontal = mLayoutManager.getOrientation() == HorizontalViewPager2.ORIENTATION_HORIZONTAL;
        int[][] bounds = new int[childCount][2];
        for (int i = 0; i < childCount; i++) {
            View view = mLayoutManager.getChildAt(i);
            if (view == null) {
                throw new IllegalStateException("null view contained in the view hierarchy");
            }
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            ViewGroup.MarginLayoutParams margin;
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                margin = (ViewGroup.MarginLayoutParams) layoutParams;
            } else {
                margin = ZERO_MARGIN_LAYOUT_PARAMS;
            }
            bounds[i][0] = isHorizontal
                    ? view.getLeft() - margin.leftMargin
                    : view.getTop() - margin.topMargin;
            bounds[i][1] = isHorizontal
                    ? view.getRight() + margin.rightMargin
                    : view.getBottom() + margin.bottomMargin;
        }

        // Sort them
        Arrays.sort(bounds, new Comparator<int[]>() {
            @Override
            public int compare(int[] lhs, int[] rhs) {
                return lhs[0] - rhs[0];
            }
        });

        // Check for inconsistencies
        for (int i = 1; i < childCount; i++) {
            if (bounds[i - 1][1] != bounds[i][0]) {
                return false;
            }
        }

        // Check that the pages fill the whole screen
        int pageSize = bounds[0][1] - bounds[0][0];
        return bounds[0][0] <= 0 && bounds[childCount - 1][1] >= pageSize;
    }

    private boolean hasRunningChangingLayoutTransition() {
        int childCount = mLayoutManager.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (hasRunningChangingLayoutTransition(mLayoutManager.getChildAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasRunningChangingLayoutTransition(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            LayoutTransition layoutTransition = viewGroup.getLayoutTransition();
            if (layoutTransition != null && layoutTransition.isChangingLayout()) {
                return true;
            }
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (hasRunningChangingLayoutTransition(viewGroup.getChildAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }
}