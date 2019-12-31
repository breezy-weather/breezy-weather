package wangdaye.com.geometricweather.ui.activity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.turingtechnologies.materialscrollbar.CustomIndicator;
import com.turingtechnologies.materialscrollbar.DragScrollBar;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.main.adapter.LocationAdapter;
import wangdaye.com.geometricweather.ui.decotarion.ListDecoration;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Search activity.
 * */

public class SearcActivity extends GeoActivity
        implements View.OnClickListener, LocationAdapter.OnLocationItemClickListener,
        EditText.OnEditorActionListener, WeatherHelper.OnRequestLocationListener {

    private CoordinatorLayout container;
    private RelativeLayout searchContainer;
    private EditText editText;

    private RecyclerView recyclerView;
    private CircularProgressView progressView;

    private LocationAdapter adapter;
    private List<Location> locationList;

    private WeatherHelper weatherHelper;
    private String query = "";

    private int state = STATE_SHOWING;
    private static final int STATE_SHOWING = 1;
    private static final int STATE_LOADING = 2;

    private class ShowAnimation extends Animation {
        // widget
        private View v;

        ShowAnimation(View v) {
            this.v = v;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            v.setAlpha(interpolatedTime);
        }
    }

    private class HideAnimation extends Animation {
        // widget
        private View v;

        HideAnimation(View v) {
            this.v = v;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            v.setAlpha(1 - interpolatedTime);
        }
    }

    private class WeatherSourceIndicator extends CustomIndicator {

        public WeatherSourceIndicator(Context context) {
            super(context);
        }

        @Override
        protected int getIndicatorHeight() {
            return 40;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        boolean lightTheme = !DisplayUtils.isDarkMode(this);
        DisplayUtils.setSystemBarStyle(this, getWindow(),
                true, lightTheme, true, lightTheme);

        initData();
        initWidget();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // do nothing.
    }

    @Override
    public void onBackPressed() {
        if (getWindow().getAttributes().softInputMode
                != WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE) {
            finishSelf(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    // init.

    private void initData() {
        this.adapter = new LocationAdapter(
                this,
                new ArrayList<>(),
                null,
                this
        );
        this.locationList = DatabaseHelper.getInstance(this).readLocationList();

        this.weatherHelper = new WeatherHelper();
    }

    private void initWidget() {
        this.container = findViewById(R.id.activity_search_container);

        findViewById(R.id.activity_search_searchBar).setOnClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.activity_search_searchBar)
                    .setTransitionName(getString(R.string.transition_activity_search_bar));
        }

        this.searchContainer = findViewById(R.id.activity_search_searchContainer);

        findViewById(R.id.activity_search_backBtn).setOnClickListener(this);
        findViewById(R.id.activity_search_clearBtn).setOnClickListener(this);

        this.editText = findViewById(R.id.activity_search_editText);
        editText.setOnEditorActionListener(this);
        new Handler().post(() -> {
            editText.requestFocus();
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.showSoftInput(editText, 0);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, RecyclerView.VERTICAL, false);

        this.recyclerView = findViewById(R.id.activity_search_recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new ListDecoration(this));
        recyclerView.setAdapter(adapter);

        DragScrollBar scrollBar = findViewById(R.id.activity_search_scrollBar);
        scrollBar.setIndicator(new WeatherSourceIndicator(this).setTextSize(16), true);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @ColorInt int sourceColor = Color.TRANSPARENT;
            @ColorInt int color;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                color = adapter.getItemSourceColor(layoutManager.findFirstVisibleItemPosition());
                if (color != sourceColor) {
                    scrollBar.setHandleColor(color);
                    scrollBar.setHandleOffColor(color);
                }
            }
        });

        this.progressView = findViewById(R.id.activity_search_progress);
        progressView.setAlpha(0);

        AnimatorSet animationSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.search_container_in);
        animationSet.setStartDelay(350);
        animationSet.setTarget(searchContainer);
        animationSet.start();
    }

    // control.

    private void finishSelf(boolean selected) {
        setResult(selected ? RESULT_OK : RESULT_CANCELED, null);
        searchContainer.setAlpha(0);
        ActivityCompat.finishAfterTransition(this);
    }

    private void setState(int stateTo) {
        if(state == stateTo) {
            return;
        }

        recyclerView.clearAnimation();
        progressView.clearAnimation();

        switch (stateTo) {
            case STATE_SHOWING:
                if (state == STATE_LOADING) {
                    recyclerView.setVisibility(View.VISIBLE);

                    ShowAnimation show = new ShowAnimation(recyclerView);
                    show.setDuration(150);
                    recyclerView.startAnimation(show);

                    HideAnimation hide = new HideAnimation(progressView);
                    hide.setDuration(150);
                    progressView.startAnimation(hide);
                }
                break;

            case STATE_LOADING:
                if (state == STATE_SHOWING) {
                    recyclerView.setAlpha(0);
                    progressView.setAlpha(1);
                    recyclerView.setVisibility(View.GONE);
                }
                break;
        }
        state = stateTo;
    }

    // interface.

    // on click listener.

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_search_backBtn:
                finishSelf(false);
                break;

            case R.id.activity_search_clearBtn:
                editText.setText("");
                break;
        }
    }

    // on location item click listener.

    @Override
    public void onClick(View view, int position) {
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).equals(adapter.itemList.get(position))) {
                SnackbarUtils.showSnackbar(this, getString(R.string.feedback_collect_failed));
                return;
            }
        }

        DatabaseHelper.getInstance(this).writeLocation(adapter.itemList.get(position));
        finishSelf(true);
    }

    // on editor action listener.

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (!TextUtils.isEmpty(textView.getText().toString())) {
            InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (manager != null) {
                manager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }

            query = textView.getText().toString();
            setState(STATE_LOADING);
            weatherHelper.requestLocation(this, query, this);
        }
        return true;
    }

    // on request weather location listener.

    @Override
    public void requestLocationSuccess(String query, List<Location> locationList) {
        if (this.query.equals(query)) {
            adapter.itemList.clear();
            adapter.itemList.addAll(locationList);
            adapter.notifyDataSetChanged();
            setState(STATE_SHOWING);
            if (locationList.size() <= 0) {
                SnackbarUtils.showSnackbar(this, getString(R.string.feedback_search_nothing));
            }
        }
    }

    @Override
    public void requestLocationFailed(String query) {
        if (this.query.equals(query)) {
            adapter.itemList.clear();
            adapter.notifyDataSetChanged();
            setState(STATE_SHOWING);
            SnackbarUtils.showSnackbar(this, getString(R.string.feedback_search_nothing));
        }
    }
}