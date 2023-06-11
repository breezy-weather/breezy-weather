package wangdaye.com.geometricweather.common.ui.widgets.insets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;

import wangdaye.com.geometricweather.common.basic.insets.FitBothSideBarHelper;
import wangdaye.com.geometricweather.common.basic.insets.FitBothSideBarView;

public class FitSystemBarViewPager extends ViewPager
        implements FitBothSideBarView {

    final FitBothSideBarHelper helper;

    public static class FitBottomSystemBarPagerAdapter extends PagerAdapter {

        private final FitSystemBarViewPager mPager;
        private final List<View> mViewList;
        public List<String> mTitleList;

        public FitBottomSystemBarPagerAdapter(FitSystemBarViewPager pager,
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
            setWindowInsetsForViewTree(mViewList.get(position), mPager.helper.getWindowInsets());
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
                for (int i = 0; i < count; i++) {
                    setWindowInsetsForViewTree(((ViewGroup) view).getChildAt(i), insets);
                }
            }
        }

        private void setWindowInsets(View view, Rect insets) {
            if (view instanceof FitSystemBarNestedScrollView) {
                ((FitSystemBarNestedScrollView) view).fitSystemWindows(insets);
            } else if (view instanceof FitSystemBarRecyclerView) {
                ((FitSystemBarRecyclerView) view).fitSystemWindows(insets);
            }
        }
    }

    public FitSystemBarViewPager(@NonNull Context context) {
        this(context, null);
    }

    public FitSystemBarViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        helper = new FitBothSideBarHelper(this, SIDE_TOP);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        return helper.onApplyWindowInsets(insets);
    }

    @Override
    public void addFitSide(@FitSide int side) {
        helper.addFitSide(side);
    }

    @Override
    public void removeFitSide(@FitSide int side) {
        helper.removeFitSide(side);
    }

    @Override
    public void setFitSystemBarEnabled(boolean top, boolean bottom) {
        helper.setFitSystemBarEnabled(top, bottom);
    }

    @Override
    public int getTopWindowInset() {
        return helper.top();
    }

    @Override
    public int getBottomWindowInset() {
        return helper.bottom();
    }
}
