package wangdaye.com.geometricweather.settings.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.settings.adapters.AboutAdapter;

/**
 * About activity.
 * */

public class AboutActivity extends GeoActivity {

    private CoordinatorLayout mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initWidget();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // do nothing.
    }

    @Override
    public View getSnackbarContainer() {
        return mContainer;
    }

    private void initWidget() {
        mContainer = findViewById(R.id.activity_about_container);

        Toolbar toolbar = findViewById(R.id.activity_about_toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.activity_about_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AboutAdapter(this));
    }
}
