package com.mbestavros.geometricweather.ui.widget.insets;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;

public class FitBottomSystemBarViewPager extends ViewPager {

    private Rect windowInsets;

    public static class FitBottomSystemBarPagerAdapter extends PagerAdapter {

        private FitBottomSystemBarViewPager pager;
        private List<View> viewList;
        public List<String> titleList;

        public FitBottomSystemBarPagerAdapter(FitBottomSystemBarViewPager pager,
                                              List<View> viewList, List<String> titleList) {
            this.pager = pager;
            this.viewList = viewList;
            this.titleList = titleList;
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            setWindowInsetsForViewTree(viewList.get(position), pager.getWindowInsets());
            container.addView(viewList.get(position));
            return viewList.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(viewList.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
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

    public FitBottomSystemBarViewPager(@NonNull Context context) {
        super(context);
    }

    public FitBottomSystemBarViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public void setOnApplyWindowInsetsListener(OnApplyWindowInsetsListener listener) {
        super.setOnApplyWindowInsetsListener((v, insets) -> {
            Rect waterfull = Utils.getWaterfullInsets(insets);
            fitSystemWindows(
                    new Rect(
                            insets.getSystemWindowInsetLeft() + waterfull.left,
                            insets.getSystemWindowInsetTop() + waterfull.top,
                            insets.getSystemWindowInsetRight() + waterfull.right,
                            insets.getSystemWindowInsetBottom() + waterfull.bottom
                    )
            );
            return listener == null ? insets : listener.onApplyWindowInsets(v, insets);
        });
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        windowInsets = insets;
        return false;
    }

    public Rect getWindowInsets() {
        return windowInsets;
    }
}
