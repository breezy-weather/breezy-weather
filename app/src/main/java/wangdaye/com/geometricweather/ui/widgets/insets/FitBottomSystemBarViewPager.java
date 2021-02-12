package wangdaye.com.geometricweather.ui.widgets.insets;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;

public class FitBottomSystemBarViewPager extends ViewPager {

    private Rect mWindowInsets = new Rect(0, 0, 0, 0);

    public static class FitBottomSystemBarPagerAdapter extends PagerAdapter {

        private final FitBottomSystemBarViewPager mPager;
        private final List<View> mViewList;
        public List<String> mTitleList;

        public FitBottomSystemBarPagerAdapter(FitBottomSystemBarViewPager pager,
                                              List<View> viewList, List<String> titleList) {
            mPager = pager;
            mViewList = viewList;
            mTitleList = titleList;
        }

        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            setWindowInsetsForViewTree(mViewList.get(position), mPager.getWindowInsets());
            container.addView(mViewList.get(position));
            return mViewList.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(mViewList.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);
        }

        private void setWindowInsetsForViewTree(View view, Rect insets) {
            setWindowInsets(view, insets);
            if (view instanceof ViewGroup) {
                int count = ((ViewGroup) view).getChildCount();
                for (int i = 0; i < count; i ++) {
                    setWindowInsetsForViewTree(((ViewGroup) view).getChildAt(i), insets);
                }
            }
        }

        private void setWindowInsets(View view, Rect insets) {
            if (view instanceof FitBottomSystemBarNestedScrollView) {
                ((FitBottomSystemBarNestedScrollView) view).fitSystemWindows(insets);
            } else if (view instanceof FitBottomSystemBarRecyclerView) {
                ((FitBottomSystemBarRecyclerView) view).fitSystemWindows(insets);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    public FitBottomSystemBarViewPager(@NonNull Context context) {
        super(context);
        ViewCompat.setOnApplyWindowInsetsListener(this, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    public FitBottomSystemBarViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ViewCompat.setOnApplyWindowInsetsListener(this, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public void setOnApplyWindowInsetsListener(OnApplyWindowInsetsListener listener) {
        super.setOnApplyWindowInsetsListener((v, insets) -> {
            if (listener != null) {
                WindowInsets result = listener.onApplyWindowInsets(v, insets);
                fitSystemWindows(
                        new Rect(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom()));
                return result;
            }

            Rect waterfull = Utils.getWaterfullInsets(insets);
            fitSystemWindows(
                    new Rect(
                            insets.getSystemWindowInsetLeft() + waterfull.left,
                            insets.getSystemWindowInsetTop() + waterfull.top,
                            insets.getSystemWindowInsetRight() + waterfull.right,
                            insets.getSystemWindowInsetBottom() + waterfull.bottom
                    )
            );
            return insets;
        });
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        mWindowInsets = insets;
        return false;
    }

    public Rect getWindowInsets() {
        return mWindowInsets;
    }
}
