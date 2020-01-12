package wangdaye.com.geometricweather.ui.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.ui.widget.insets.FitBottomSystemBarViewPager;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public class FitBottomSystemBarPagerAdapter extends androidx.viewpager.widget.PagerAdapter {

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
        viewList.get(position).setPadding(paddingHorizontal, 0, paddingHorizontal, (int) pager.getInsetsBottom());
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