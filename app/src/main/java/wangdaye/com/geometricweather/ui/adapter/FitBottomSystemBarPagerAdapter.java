package wangdaye.com.geometricweather.ui.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.ui.widget.insets.FitBottomSystemBarViewPager;

public class FitBottomSystemBarPagerAdapter extends androidx.viewpager.widget.PagerAdapter {

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
        viewList.get(position).setPadding(0, 0, 0, (int) pager.getInsetsBottom());
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