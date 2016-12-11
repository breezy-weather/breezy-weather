package wangdaye.com.geometricweather.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;

/**
 * Show application's details.
 * */

public class AboutActivity extends GeoActivity
        implements View.OnClickListener {
    // widget
    private CoordinatorLayout container;

    /** <br> life cycle. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isStarted()) {
            setStarted();
            initWidget();
        }
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    /** <br> UI. */

    private void initWidget() {
        this.container = (CoordinatorLayout) findViewById(R.id.activity_about_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_about_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setTitle(R.string.action_about);
        toolbar.setNavigationOnClickListener(this);


        ImageView appIcon = (ImageView) findViewById(R.id.container_about_app_appIcon);
        Glide.with(this)
                .load(R.drawable.ic_launcher)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(appIcon);

        findViewById(R.id.container_about_app_github).setOnClickListener(this);
        findViewById(R.id.container_about_app_email).setOnClickListener(this);
        findViewById(R.id.container_about_thx_location).setOnClickListener(this);
        findViewById(R.id.container_about_thx_juhe).setOnClickListener(this);
        findViewById(R.id.container_about_thx_hefeng).setOnClickListener(this);
        findViewById(R.id.container_about_thx_retrofit).setOnClickListener(this);
        findViewById(R.id.container_about_thx_glide).setOnClickListener(this);
    }

    /** <br> interface. */

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case -1:
                finish();
                break;

            case R.id.container_about_app_github:
                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/WangDaYeeeeee/GeometricWeather")));
                break;

            case R.id.container_about_app_email:
                startActivity(
                        new Intent(
                                Intent.ACTION_SENDTO,
                                Uri.parse("mailto:wangdayeeeeee@gmail.com")));
                break;

            case R.id.container_about_thx_location:
                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://lbsyun.baidu.com/index.php?title=android-locsdk")));
                break;

            case R.id.container_about_thx_juhe:
                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.juhe.cn/docs/api/id/73")));
                break;

            case R.id.container_about_thx_hefeng:
                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://apistore.baidu.com/apiworks/servicedetail/478.html")));
                break;

            case R.id.container_about_thx_retrofit:
                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/square/retrofit")));
                break;

            case R.id.container_about_thx_glide:
                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/bumptech/glide")));
                break;
        }
    }
}
