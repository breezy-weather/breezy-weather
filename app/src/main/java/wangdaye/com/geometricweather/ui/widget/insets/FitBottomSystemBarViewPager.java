package wangdaye.com.geometricweather.ui.widget.insets;

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

import wangdaye.com.geometricweather.utils.DisplayUtils;

public class FitBottomSystemBarViewPager extends ViewPager {

    private Rect windowInsets;

    public static class FitBottomSystemBarPagerAdapter extends PagerAdapter {

        private FitBottomSystemBarViewPager pager;
        private List<View> viewList;
        public List<String> titleList;

        private int screenWidth;
        private int adaptiveWidth;

        public FitBottomSystemBarPagerAdapter(FitBottomSystemBarViewPager pager,
                                              List<View> viewList, List<String> titleList) {
            this.pager = pager;
            this.viewList = viewList;
            this.titleList = titleList;

            this.screenWidth = pager.getResources().getDisplayMetrics().widthPixels;
            this.adaptiveWidth = DisplayUtils.getTabletListAdaptiveWidth(pager.getContext(), screenWidth);
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
            int paddingHorizontal = (screenWidth - adaptiveWidth) / 2;
            Rect insets = pager.getWindowInsets();
            viewList.get(position).setPadding(
                    Math.max(paddingHorizontal, insets.left),
                    0,
                    Math.max(paddingHorizontal, insets.right),
                    insets.bottom
            );
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
