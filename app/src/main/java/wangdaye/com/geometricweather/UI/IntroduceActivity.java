package wangdaye.com.geometricweather.UI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.Widget.BitmapHelper;
import wangdaye.com.geometricweather.Widget.CircleIndicator;
import wangdaye.com.geometricweather.Widget.ViewPagerAdapter;

/**
 * Show the introduction about the application.
 * */

public class IntroduceActivity extends Activity {
    // widget
    private ViewPager viewPager;
    private View[] views;
    private TextView[][] text;
    private Button button;
    private CircleIndicator indicator;

    private FrameLayout statusBar;

    // data
    private int pageNow;
    private final int PAGE_NUM = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setStatusBarTransParent();

        setContentView(R.layout.activity_introduce);

        this.pageNow = 1;

        MainActivity.initNavigationBar(this, getWindow());
        this.initWidget();
        this.initStatusBar();
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

    private void initWidget() {
        this.viewPager = (ViewPager) findViewById(R.id.introduce_viewPager);
        this.views = new View[PAGE_NUM];

        this.text = new TextView[PAGE_NUM][2];

        this.indicator = (CircleIndicator) findViewById(R.id.introduce_indicator);
        indicator.setData(PAGE_NUM, pageNow, 0);
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

        this.statusBar = (FrameLayout) findViewById(R.id.activity_introduce_statusBar);
    }

    @SuppressLint("InflateParams")
    private void initViewPager() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        views[0] = layoutInflater.inflate(R.layout.introduce_page, null);
        ImageView image1 = (ImageView) views[0].findViewById(R.id.introduce_page_image);
        image1.setImageBitmap(BitmapHelper.readBitMap(this, R.drawable.introduction_1, 400, 400));
        text[0][0] = (TextView) views[0].findViewById(R.id.introduce_page_title);
        text[0][0].setText(getString(R.string.introduce_title_1));
        text[0][1] = (TextView) views[0].findViewById(R.id.introduce_page_text);
        text[0][1].setText(getString(R.string.introduce_text_1));

        views[1] = layoutInflater.inflate(R.layout.introduce_page, null);
        ImageView image2 = (ImageView) views[1].findViewById(R.id.introduce_page_image);
        image2.setImageBitmap(BitmapHelper.readBitMap(this, R.drawable.introduction_2, 400, 400));
        text[1][0] = (TextView) views[1].findViewById(R.id.introduce_page_title);
        text[1][0].setText(getString(R.string.introduce_title_2));
        text[1][1] = (TextView) views[1].findViewById(R.id.introduce_page_text);
        text[1][1].setText(getString(R.string.introduce_text_2));

        views[2] = layoutInflater.inflate(R.layout.introduce_page, null);
        ImageView image3 = (ImageView) views[2].findViewById(R.id.introduce_page_image);
        image3.setImageBitmap(BitmapHelper.readBitMap(this, R.drawable.introduction_3, 400, 400));
        text[2][0] = (TextView) views[2].findViewById(R.id.introduce_page_title);
        text[2][0].setText(getString(R.string.introduce_title_3));
        text[2][1] = (TextView) views[2].findViewById(R.id.introduce_page_text);
        text[2][1].setText(getString(R.string.introduce_text_3));

        views[3] = layoutInflater.inflate(R.layout.introduce_page, null);
        ImageView image4 = (ImageView) views[3].findViewById(R.id.introduce_page_image);
        image4.setImageBitmap(BitmapHelper.readBitMap(this, R.drawable.introduction_4, 400, 400));
        text[3][0] = (TextView) views[3].findViewById(R.id.introduce_page_title);
        text[3][0].setText(getString(R.string.introduce_title_4));
        text[3][1] = (TextView) views[3].findViewById(R.id.introduce_page_text);
        text[3][1].setText(getString(R.string.introduce_text_4));

        final List<View> views = new ArrayList<>();
        views.add(this.views[0]);
        views.add(this.views[1]);
        views.add(this.views[2]);
        views.add(this.views[3]);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(views);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                boolean isRight = indicator.setData(PAGE_NUM, position + 1, positionOffset);
                indicator.invalidate();
                if (isRight) {
                    for (int i = 0; i < PAGE_NUM; i ++) {
                        if (i < position) {
                            text[i][0].setX(0);
                            text[i][1].setX(0);
                            IntroduceActivity.this.views[i].setAlpha(0);
                        } else if (i == position) {
                            float startX = viewPager.getMeasuredWidth() / 2 - text[i][0].getMeasuredWidth() / 2;
                            text[i][0].setX(startX - startX * positionOffset);
                            startX = viewPager.getMeasuredWidth() / 2 - text[i][1].getMeasuredWidth() / 2;
                            text[i][1].setX(startX - startX * positionOffset);
                            IntroduceActivity.this.views[i].setAlpha(1 - positionOffset);
                        } else if (i == position + 1) {
                            float startX = viewPager.getMeasuredWidth() - text[i][0].getMeasuredWidth();
                            text[i][0].setX(startX - (viewPager.getMeasuredWidth() / 2 - text[i][0].getMeasuredWidth() / 2) * positionOffset);
                            startX = viewPager.getMeasuredWidth() - text[i][1].getMeasuredWidth();
                            text[i][1].setX(startX - (viewPager.getMeasuredWidth() / 2 - text[i][1].getMeasuredWidth() / 2) * positionOffset);
                            IntroduceActivity.this.views[i].setAlpha(positionOffset);
                        } else {
                            text[i][0].setX(viewPager.getMeasuredWidth() - text[i][0].getMeasuredWidth());
                            text[i][1].setX(viewPager.getMeasuredWidth() - text[i][1].getMeasuredWidth());
                            IntroduceActivity.this.views[i].setAlpha(0);
                        }
                    }
                } else {
                    for (int i = 0; i < PAGE_NUM; i ++) {
                        if (i < position) {
                            text[i][0].setX(0);
                            text[i][1].setX(0);
                            IntroduceActivity.this.views[i].setAlpha(0);
                        } else if (i == position) {
                            float startX = viewPager.getMeasuredWidth() / 2 - text[i][0].getMeasuredWidth() / 2;
                            text[i][0].setX(startX + startX * positionOffset);
                            startX = viewPager.getMeasuredWidth() / 2 - text[i][1].getMeasuredWidth() / 2;
                            text[i][1].setX(startX + startX * positionOffset);
                            IntroduceActivity.this.views[i].setAlpha(1 - positionOffset);
                        } else if (i == position - 1) {
                            text[i][0].setX((viewPager.getMeasuredWidth() / 2 - text[i][0].getMeasuredWidth() / 2) * positionOffset);
                            text[i][1].setX((viewPager.getMeasuredWidth() / 2 - text[i][1].getMeasuredWidth() / 2) * positionOffset);
                            IntroduceActivity.this.views[i].setAlpha(positionOffset);
                        } else {
                            text[i][0].setX(viewPager.getMeasuredWidth() - text[i][0].getMeasuredWidth());
                            text[i][1].setX(viewPager.getMeasuredWidth() - text[i][1].getMeasuredWidth());
                            IntroduceActivity.this.views[i].setAlpha(0);
                        }
                    }
                }
            }

            @Override
            public void onPageSelected(final int position) {
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

    private void initStatusBar() {
        Class<?> c;
        Object obj;
        Field field;
        int x, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        statusBar.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        statusBarHeight
                )
        );
        statusBar.setBackgroundColor(ContextCompat.getColor(this, R.color.notification_background));
    }
}
