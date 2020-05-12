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
import android.widget.TextView;

import com.turingtechnologies.materialscrollbar.CustomIndicator;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.databinding.ActivitySearchBinding;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.ui.adapter.location.LocationAdapter;
import wangdaye.com.geometricweather.ui.decotarion.ListDecoration;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Search activity.
 * */

public class SearchActivity extends GeoActivity
        implements EditText.OnEditorActionListener, WeatherHelper.OnRequestLocationListener {

    private ActivitySearchBinding binding;

    private LocationAdapter adapter;
    private WeatherHelper weatherHelper;

    private List<Location> currentList;
    private List<Location> locationList;
    private String query = "";

    private int state = STATE_SHOWING;
    private static final int STATE_SHOWING = 1;
    private static final int STATE_LOADING = 2;

    private static class ShowAnimation extends Animation {
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

    private static class HideAnimation extends Animation {
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

    private static class WeatherSourceIndicator extends CustomIndicator {

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
        this.binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        return binding.container;
    }

    // init.

    private void initData() {
        this.currentList = DatabaseHelper.getInstance(this).readLocationList();
        this.locationList = new ArrayList<>();

        this.weatherHelper = new WeatherHelper();
    }

    private void initWidget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.searchBar.setTransitionName(getString(R.string.transition_activity_search_bar));
        }

        binding.backBtn.setOnClickListener(v -> finishSelf(false));
        binding.clearBtn.setOnClickListener(v -> binding.editText.setText(""));

        binding.editText.setOnEditorActionListener(this);
        new Handler().post(() -> {
            binding.editText.requestFocus();
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.showSoftInput(binding.editText, 0);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, RecyclerView.VERTICAL, false);

        this.adapter = new LocationAdapter(
                this,
                locationList,
                (view, formattedId) -> {
                    for (int i = 0; i < currentList.size(); i ++) {
                        if (currentList.get(i).equals(formattedId)) {
                            SnackbarUtils.showSnackbar(this, getString(R.string.feedback_collect_failed));
                            return;
                        }
                    }

                    for (Location l : locationList) {
                        if (l.equals(formattedId)) {
                            DatabaseHelper.getInstance(this).writeLocation(l);
                            finishSelf(true);
                            return;
                        }
                    }
                }
        );
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.addItemDecoration(new ListDecoration(this));
        binding.recyclerView.setAdapter(adapter);

        binding.scrollBar.setIndicator(
                new WeatherSourceIndicator(this).setTextSize(16), true);

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @ColorInt int sourceColor = Color.TRANSPARENT;
            @ColorInt int color;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                color = adapter.getItemSourceColor(layoutManager.findFirstVisibleItemPosition());
                if (color != sourceColor) {
                    binding.scrollBar.setHandleColor(color);
                    binding.scrollBar.setHandleOffColor(color);
                }
            }
        });

        binding.progress.setAlpha(0);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            binding.searchContainer.setAlpha(1f);
        } else {
            AnimatorSet animationSet = (AnimatorSet) AnimatorInflater.loadAnimator(
                    this, R.animator.search_container_in);
            animationSet.setStartDelay(350);
            animationSet.setTarget(binding.searchContainer);
            animationSet.start();
        }
    }

    // control.

    private void finishSelf(boolean selected) {
        setResult(selected ? RESULT_OK : RESULT_CANCELED, null);
        binding.searchContainer.setAlpha(0);
        ActivityCompat.finishAfterTransition(this);
    }

    private void setState(int stateTo) {
        if(state == stateTo) {
            return;
        }

        binding.recyclerView.clearAnimation();
        binding.progress.clearAnimation();

        switch (stateTo) {
            case STATE_SHOWING:
                if (state == STATE_LOADING) {
                    binding.recyclerView.setVisibility(View.VISIBLE);

                    ShowAnimation show = new ShowAnimation(binding.recyclerView);
                    show.setDuration(150);
                    binding.recyclerView.startAnimation(show);

                    HideAnimation hide = new HideAnimation(binding.progress);
                    hide.setDuration(150);
                    binding.progress.startAnimation(hide);
                }
                break;

            case STATE_LOADING:
                if (state == STATE_SHOWING) {
                    binding.recyclerView.setAlpha(0);
                    binding.progress.setAlpha(1);
                    binding.recyclerView.setVisibility(View.GONE);
                }
                break;
        }
        state = stateTo;
    }

    // interface.

    // on editor action listener.

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (!TextUtils.isEmpty(textView.getText().toString())) {
            InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (manager != null) {
                manager.hideSoftInputFromWindow(binding.editText.getWindowToken(), 0);
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
            this.locationList.clear();
            this.locationList.addAll(locationList);
            adapter.update(this.locationList, null);
            setState(STATE_SHOWING);
            if (locationList.size() <= 0) {
                SnackbarUtils.showSnackbar(this, getString(R.string.feedback_search_nothing));
            }
        }
    }

    @Override
    public void requestLocationFailed(String query) {
        if (this.query.equals(query)) {
            this.locationList.clear();
            adapter.update(this.locationList, null);
            setState(STATE_SHOWING);
            SnackbarUtils.showSnackbar(this, getString(R.string.feedback_search_nothing));
        }
    }
}