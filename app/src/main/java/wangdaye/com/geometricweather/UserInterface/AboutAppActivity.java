package wangdaye.com.geometricweather.UserInterface;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;

/**
 * Created by WangDaYe on 2016/2/8.
 */

public class AboutAppActivity extends AppCompatActivity {
    // TAG
//    private final String TAG = "AboutAppActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setStatusBarTransParent();

        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_info_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        ImageView titleImage = (ImageView) findViewById(R.id.app_info_title);
        if (MainActivity.isDay) {
            titleImage.setImageBitmap(MainActivity.readBitMap(this, R.drawable.about_title_day));
        } else {
            titleImage.setImageBitmap(MainActivity.readBitMap(this, R.drawable.about_title_night));
        }

        ImageView iconImage = (ImageView) findViewById(R.id.about_app_icon);
        iconImage.setImageBitmap(MainActivity.readBitMap(this, R.drawable.ic_launcher));

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
}
