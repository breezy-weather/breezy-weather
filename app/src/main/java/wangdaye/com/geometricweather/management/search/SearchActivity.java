package wangdaye.com.geometricweather.management.search;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.turingtechnologies.materialscrollbar.CustomIndicator;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.databinding.ActivitySearchBinding;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.management.adapter.LocationAdapter;
import wangdaye.com.geometricweather.management.models.LoadableLocationList;
import wangdaye.com.geometricweather.ui.decotarions.ListDecoration;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.helpters.SnackbarHelper;

/**
 * Search activity.
 * */

public class SearchActivity extends GeoActivity
        implements EditText.OnEditorActionListener {

    private ActivitySearchBinding mBinding;
    private SearchActivityViewModel mViewModel;

    private LocationAdapter mAdapter;
    private List<Location> mCurrentList;

    private @Nullable LoadableLocationList.Status mStatus;

    public static final String KEY_LOCATION = "location";

    private static class ShowAnimation extends Animation {

        private final View mView;

        ShowAnimation(View v) {
            mView = v;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            mView.setAlpha(interpolatedTime);
        }
    }

    private static class HideAnimation extends Animation {

        private final View mView;

        HideAnimation(View v) {
            mView = v;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            mView.setAlpha(1 - interpolatedTime);
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
        mBinding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        boolean lightTheme = !DisplayUtils.isDarkMode(this);
        DisplayUtils.setSystemBarStyle(this, getWindow(),
                true, lightTheme, true, lightTheme);

        mCurrentList = DatabaseHelper.getInstance(this).readLocationList();

        initModel();
        initView();
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
            finishSelf(null);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public View getSnackbarContainer() {
        return mBinding.container;
    }

    // init.

    private void initModel() {
        mViewModel = new ViewModelProvider(this).get(SearchActivityViewModel.class);
    }

    private void initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBinding.searchBar.setTransitionName(getString(R.string.transition_activity_search_bar));
        }

        mBinding.backBtn.setOnClickListener(v -> finishSelf(null));
        mBinding.filterBtn.setOnClickListener(v -> {
            mViewModel.switchMultiSourceEnabled();
            if (!TextUtils.isEmpty(mViewModel.getQueryValue())) {
                mViewModel.requestLocationList();
            }
        });

        mBinding.editText.setOnEditorActionListener(this);
        new Handler().post(() -> {
            mBinding.editText.requestFocus();
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.showSoftInput(mBinding.editText, 0);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, RecyclerView.VERTICAL, false);

        mBinding.recyclerView.setLayoutManager(layoutManager);
        mBinding.recyclerView.addItemDecoration(new ListDecoration(this));
        mBinding.recyclerView.setAdapter(mAdapter);

        mBinding.scrollBar.setIndicator(
                new WeatherSourceIndicator(this).setTextSize(16), true);

        mBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @ColorInt int color;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                color = mAdapter.getItemSourceColor(layoutManager.findFirstVisibleItemPosition());
                if (color != Color.TRANSPARENT) {
                    mBinding.scrollBar.setHandleColor(color);
                    mBinding.scrollBar.setHandleOffColor(color);
                }
            }
        });

        mBinding.progress.setAlpha(0);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mBinding.searchContainer.setAlpha(1f);
        } else {
            AnimatorSet animationSet = (AnimatorSet) AnimatorInflater.loadAnimator(
                    this, R.animator.search_container_in);
            animationSet.setStartDelay(350);
            animationSet.setTarget(mBinding.searchContainer);
            animationSet.start();
        }

        mViewModel.getListResource().observe(this, loadableLocationList -> {

            setStatus(loadableLocationList.status);
            mBinding.filterBtn.setEnabled(loadableLocationList.status != LoadableLocationList.Status.LOADING);

            if (mAdapter == null) {
                mAdapter = new LocationAdapter(
                        this,
                        loadableLocationList.dataList,
                        null,
                        (view, formattedId) -> {
                            for (int i = 0; i < mCurrentList.size(); i ++) {
                                if (mCurrentList.get(i).equals(formattedId)) {
                                    SnackbarHelper.showSnackbar(getString(R.string.feedback_collect_failed));
                                    return;
                                }
                            }

                            for (Location l : mViewModel.getLocationList()) {
                                if (l.equals(formattedId)) {
                                    finishSelf(l);
                                    return;
                                }
                            }
                        },
                        null
                );
                mBinding.recyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.update(loadableLocationList.dataList, null, null);
            }
        });

        mViewModel.getMultiSourceEnabled().observe(this, enabled -> {
            mBinding.filterBtn.setImageResource(enabled
                    ? R.drawable.ic_filter_off
                    : R.drawable.ic_filter);
            mBinding.filterBtn.setContentDescription(getString(enabled
                    ? R.string.content_desc_search_filter_off
                    : R.string.content_desc_search_filter_on));
        });
        mViewModel.getQuery().observe(this, query -> {
            mBinding.editText.setText(query);
            mBinding.editText.setSelection(query.length());
        });
    }

    // control.

    private void finishSelf(@Nullable Location location) {
        setResult(
                location != null ? RESULT_OK : RESULT_CANCELED,
                new Intent().putExtra(KEY_LOCATION, location)
        );
        mBinding.searchContainer.setAlpha(0);
        ActivityCompat.finishAfterTransition(this);
    }

    private void setStatus(LoadableLocationList.Status newStatus) {
        if(newStatus == mStatus) {
            return;
        }

        mBinding.recyclerView.clearAnimation();
        mBinding.progress.clearAnimation();

        if (newStatus == LoadableLocationList.Status.LOADING) {
            if (mStatus != LoadableLocationList.Status.LOADING) {
                mBinding.recyclerView.setAlpha(0);
                mBinding.progress.setAlpha(1);
                mBinding.recyclerView.setVisibility(View.GONE);
            }
        } else {
            if (mStatus == null || mStatus == LoadableLocationList.Status.LOADING) {
                mBinding.recyclerView.setVisibility(View.VISIBLE);

                ShowAnimation show = new ShowAnimation(mBinding.recyclerView);
                show.setDuration(150);
                mBinding.recyclerView.startAnimation(show);

                HideAnimation hide = new HideAnimation(mBinding.progress);
                hide.setDuration(150);
                mBinding.progress.startAnimation(hide);
            }
            if (newStatus == LoadableLocationList.Status.ERROR) {
                SnackbarHelper.showSnackbar(getString(R.string.feedback_search_nothing));
            }
        }
        mStatus = newStatus;
    }

    // interface.

    // on editor action listener.

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (!TextUtils.isEmpty(textView.getText().toString())) {
            InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (manager != null) {
                manager.hideSoftInputFromWindow(mBinding.editText.getWindowToken(), 0);
            }

            String query = textView.getText().toString();
            mViewModel.requestLocationList(query);
        }
        return true;
    }
}