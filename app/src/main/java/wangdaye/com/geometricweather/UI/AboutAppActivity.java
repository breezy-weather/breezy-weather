package wangdaye.com.geometricweather.UI;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.Widget.BitmapHelper;
import wangdaye.com.geometricweather.Widget.MyScrollView;

/**
 * Show application's details.
 * */

public class AboutAppActivity extends AppCompatActivity {
    // widget
    private FrameLayout statusBar;
    private MyScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setStatusBarTransparent();
        setContentView(R.layout.activity_about);

        this.initStatusBar();

        scrollView = (MyScrollView) findViewById(R.id.activity_about_scrollView);
        scrollView.setOnScrollViewListener(new MyScrollView.OnScrollViewListener() {
            @Override
            public void onScrollChanged(MyScrollView scrollView, int x, int y, int oldX, int oldY) {
                float dpi = getResources().getDisplayMetrics().density;
                if (y < oldY) {
                    float oldAlpha = statusBar.getAlpha();
                    float newAlpha = (float) (oldAlpha - Math.abs(y - oldY) / (400 / 2.625 * dpi));
                    if (newAlpha < 0) {
                        newAlpha = 0;
                    }
                    statusBar.setAlpha(newAlpha);
                } else {
                    float oldAlpha = statusBar.getAlpha();
                    float newAlpha = (float) (oldAlpha + Math.abs(y - oldY) / (400 / 2.625 * dpi));
                    if (newAlpha > 1) {
                        newAlpha = 1;
                    }
                    statusBar.setAlpha(newAlpha);
                }
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_info_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        setWindowTopColor();
        MainActivity.initNavigationBar(this, getWindow());

        ImageView titleImage = (ImageView) findViewById(R.id.app_info_title);
        titleImage.setImageBitmap(BitmapHelper.readBitMap(this, R.drawable.design_background, 600, 600));

        ImageView iconImage = (ImageView) findViewById(R.id.about_app_icon);
        iconImage.setImageBitmap(BitmapHelper.readBitMap(this, R.drawable.ic_launcher, 300, 300));

        TextView[] textView = new TextView[4];
        textView[0] = (TextView) findViewById(R.id.app_info_name_text);
        textView[1] = (TextView) findViewById(R.id.app_info_tech_text);
        textView[2] = (TextView) findViewById(R.id.app_info_thank_text);
        textView[3] = (TextView) findViewById(R.id.app_info_author_text);
        if (MainActivity.isDay) {
            for (int i = 0; i < 4; i ++) {
                textView[i].setTextColor(ContextCompat.getColor(this, R.color.lightPrimary_3));
            }
        } else {
            for (int i = 0; i < 4; i ++) {
                textView[i].setTextColor(ContextCompat.getColor(this, R.color.darkPrimary_1));
            }
        }

        RelativeLayout introductionContainer = (RelativeLayout) findViewById(R.id.about_app_ic_app_introduce_container);
        introductionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutAppActivity.this, IntroduceActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
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

        statusBar = (FrameLayout) findViewById(R.id.activity_about_statusBar);
        statusBar.setLayoutParams(
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        statusBarHeight
                )
        );
        statusBar.setBackgroundColor(ContextCompat.getColor(this, R.color.design_background));
        statusBar.setAlpha(0);
    }

    private void setWindowTopColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager.TaskDescription taskDescription;
            Bitmap topIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            taskDescription = new ActivityManager.TaskDescription(getString(R.string.app_name),
                    topIcon,
                    ContextCompat.getColor(this, R.color.design_background));
            setTaskDescription(taskDescription);
            topIcon.recycle();
        }
    }
}
