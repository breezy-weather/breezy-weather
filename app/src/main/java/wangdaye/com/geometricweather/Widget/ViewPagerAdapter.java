package wangdaye.com.geometricweather.Widget;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * View pager adapter in introduce activity.
 * */

public class ViewPagerAdapter extends PagerAdapter {
    // widget
    private List<View> viewList;

    // constructor
    public ViewPagerAdapter(List<View> viewList) {
        this.viewList = viewList;
    }

    @Override
    public int getCount() {
        return viewList.size();//页卡数
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        // google recommend method
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(viewList.get(position));
        return viewList.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(viewList.get(position));
    }
}
