package wangdaye.com.geometricweather.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.Widget.CircleIndicator;
import wangdaye.com.geometricweather.Widget.ViewPagerAdapter;

/**
 * Created by WangDaYe on 2016/2/18.
 */

public class IntroduceActivity extends Activity {
    // widget
    private ViewPager viewPager;
    private Button button;
    private CircleIndicator indicator;

    // data
    private int pageNow;
    private final int PAGE_NUM = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setStatusBarTransParent();

        setContentView(R.layout.activity_introduce);

        this.pageNow = 1;

        this.initNavigationBar();
        this.initWidget();
        this.initViewPager();
    }

    private void setStatusBarTransParent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void initNavigationBar() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    private void initWidget() {
        this.viewPager = (ViewPager) findViewById(R.id.introduce_viewPager);

        this.indicator = (CircleIndicator) findViewById(R.id.introduce_indicator);
        indicator.setData(PAGE_NUM, pageNow);
        indicator.invalidate();

        this.button = (Button) findViewById(R.id.introduce_button);
        button.setText(getString(R.string.next));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(pageNow ++);
                if (pageNow == PAGE_NUM) {
                    button.setText(getString(R.string.done));
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                }
            }
        });
    }

    @SuppressLint("InflateParams")
    private void initViewPager() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view1, view2, view3, view4, view5;

        view1 = layoutInflater.inflate(R.layout.introduce_page, null);
        ImageView image1 = (ImageView) view1.findViewById(R.id.introduce_page_image);
        image1.setImageBitmap(MainActivity.readBitMap(this, R.drawable.introduction_1));
        TextView title1 = (TextView) view1.findViewById(R.id.introduce_page_title);
        title1.setText(getString(R.string.introduce_title_1));
        TextView text1 = (TextView) view1.findViewById(R.id.introduce_page_text);
        text1.setText(getString(R.string.introduce_text_1));

        view2 = layoutInflater.inflate(R.layout.introduce_page, null);
        ImageView image2 = (ImageView) view2.findViewById(R.id.introduce_page_image);
        image2.setImageBitmap(MainActivity.readBitMap(this, R.drawable.introduction_2));
        TextView title2 = (TextView) view2.findViewById(R.id.introduce_page_title);
        title2.setText(getString(R.string.introduce_title_2));
        TextView text2 = (TextView) view2.findViewById(R.id.introduce_page_text);
        text2.setText(getString(R.string.introduce_text_2));

        view3 = layoutInflater.inflate(R.layout.introduce_page, null);
        ImageView image3 = (ImageView) view3.findViewById(R.id.introduce_page_image);
        image3.setImageBitmap(MainActivity.readBitMap(this, R.drawable.introduction_3));
        TextView title3 = (TextView) view3.findViewById(R.id.introduce_page_title);
        title3.setText(getString(R.string.introduce_title_3));
        TextView text3 = (TextView) view3.findViewById(R.id.introduce_page_text);
        text3.setText(getString(R.string.introduce_text_3));

        view4 = layoutInflater.inflate(R.layout.introduce_page, null);
        ImageView image4 = (ImageView) view4.findViewById(R.id.introduce_page_image);
        image4.setImageBitmap(MainActivity.readBitMap(this, R.drawable.introduction_4));
        TextView title4 = (TextView) view4.findViewById(R.id.introduce_page_title);
        title4.setText(getString(R.string.introduce_title_4));
        TextView text4 = (TextView) view4.findViewById(R.id.introduce_page_text);
        text4.setText(getString(R.string.introduce_text_4));

        view5 = layoutInflater.inflate(R.layout.introduce_page, null);
        ImageView image5 = (ImageView) view5.findViewById(R.id.introduce_page_image);
        image5.setImageBitmap(MainActivity.readBitMap(this, R.drawable.introduction_5));
        TextView title5 = (TextView) view5.findViewById(R.id.introduce_page_title);
        title5.setText(getString(R.string.introduce_title_5));
        TextView text5 = (TextView) view5.findViewById(R.id.introduce_page_text);
        text5.setText(getString(R.string.introduce_text_5));

        List<View> views = new ArrayList<>();
        views.add(view1);
        views.add(view2);
        views.add(view3);
        views.add(view4);
        views.add(view5);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(views);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(final int position) {
                indicator.setData(PAGE_NUM, position + 1);
                indicator.invalidate();
                if (position == PAGE_NUM - 1) {
                    button.setText(getString(R.string.done));
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                } else {
                    button.setText(getString(R.string.next));
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            viewPager.setCurrentItem(position + 1);
                            if (position + 1 == PAGE_NUM - 1) {
                                button.setText(getString(R.string.done));
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }
}
