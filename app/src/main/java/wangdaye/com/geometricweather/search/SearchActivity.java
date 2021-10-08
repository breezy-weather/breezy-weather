package wangdaye.com.geometricweather.search;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.SharedElementCallback;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.gordonwong.materialsheetfab.MaterialSheetFabEventListener;
import com.turingtechnologies.materialscrollbar.CustomIndicator;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.theme.DefaultThemeManager;
import wangdaye.com.geometricweather.common.ui.adapters.location.LocationAdapter;
import wangdaye.com.geometricweather.common.ui.decotarions.ListDecoration;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.common.utils.helpers.SnackbarHelper;
import wangdaye.com.geometricweather.databinding.ActivitySearchBinding;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.search.ui.FabView;
import wangdaye.com.geometricweather.search.ui.adapter.WeatherSourceAdapter;

/**
 * Search activity.
 * */

@AndroidEntryPoint
public class SearchActivity extends GeoActivity
        implements EditText.OnEditorActionListener {

    private ActivitySearchBinding mBinding;
    private SearchActivityViewModel mViewModel;

    private LocationAdapter mAdapter;
    private List<Location> mCurrentList;

    private MaterialSheetFab<FabView> mMaterialSheetFab;
    private @Nullable WeatherSourceAdapter mSourceAdapter;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            mBinding.searchBar.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mBinding.searchBar.getViewTreeObserver().removeOnPreDrawListener(this);
                    startPostponedEnterTransition();
                    return true;
                }
            });
        }

        boolean lightTheme = !DisplayUtils.isDarkMode(this);
        DisplayUtils.setSystemBarStyle(this, getWindow(),
                true, lightTheme, true, lightTheme);

        mCurrentList = DatabaseHelper.getInstance(this).readLocationList();

        initModel();
        initView();
    }

    @Override
    public void onBackPressed() {
        if (mMaterialSheetFab.isSheetVisible()) {
            mMaterialSheetFab.hideSheet();
        } else {
            finishSelf(null);
        }
    }

    // init.

    private void initModel() {
        mViewModel = new ViewModelProvider(this).get(SearchActivityViewModel.class);
    }

    private void initView() {
        mBinding.backBtn.setOnClickListener(v -> finishSelf(null));

        mBinding.editText.setOnEditorActionListener(this);
        new Handler().post(() -> {
            mBinding.editText.requestFocus();
            InputMethodManager inputManager = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.showSoftInput(mBinding.editText, 0);
            }
        });

        mAdapter = new LocationAdapter(
                this,
                new ArrayList<>(),
                null,
                (view, formattedId) -> {
                    for (int i = 0; i < mCurrentList.size(); i ++) {
                        if (mCurrentList.get(i).getFormattedId().equals(formattedId)) {
                            SnackbarHelper.showSnackbar(getString(R.string.feedback_collect_failed));
                            return;
                        }
                    }

                    for (Location l : mViewModel.getLocationList()) {
                        if (l.getFormattedId().equals(formattedId)) {
                            finishSelf(l);
                            return;
                        }
                    }
                }, null, new DefaultThemeManager()
        );

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, RecyclerView.VERTICAL, false);

        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.recyclerView.setLayoutManager(layoutManager);
        while (mBinding.recyclerView.getItemDecorationCount() > 0) {
            mBinding.recyclerView.removeItemDecorationAt(0);
        }
        mBinding.recyclerView.addItemDecoration(new ListDecoration(
                this, ContextCompat.getColor(this, R.color.colorLine)));

        mBinding.scrollBar.setIndicator(
                new WeatherSourceIndicator(this).setTextSize(16), true);
        mBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            private @ColorInt int mColor;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                mColor = mAdapter.getItemSourceColor(layoutManager.findFirstVisibleItemPosition());
                if (mColor != Color.TRANSPARENT) {
                    mBinding.scrollBar.setHandleColor(mColor);
                    mBinding.scrollBar.setHandleOffColor(mColor);
                }
            }
        });

        mBinding.progress.setAlpha(0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onSharedElementStart(List<String> sharedElementNames,
                                                 List<View> sharedElements,
                                                 List<View> sharedElementSnapshots) {
                    mBinding.searchContainer.setAlpha(0f);

                    AnimatorSet animationSet = (AnimatorSet) AnimatorInflater.loadAnimator(
                            SearchActivity.this, R.animator.search_container_in);
                    animationSet.setStartDelay(400);
                    animationSet.setTarget(mBinding.searchContainer);
                    animationSet.start();
                }
            });
        }

        mMaterialSheetFab = new MaterialSheetFab<>(
                mBinding.fab,
                mBinding.fabSheet,
                mBinding.overlay,
                getResources().getColor(R.color.colorRoot),
                getResources().getColor(R.color.colorPrimary)
        );
        mMaterialSheetFab.setEventListener(new MaterialSheetFabEventListener() {
            @Override
            public void onShowSheet() {
                mBinding.sourceList.setAdapter(
                        mSourceAdapter = new WeatherSourceAdapter(mViewModel.getEnabledSourcesValue())
                );
            }
        });

        mBinding.sourceList.setLayoutManager(new LinearLayoutManager(this));
        mBinding.sourceEnter.setOnClickListener(v -> {
            if (mMaterialSheetFab.isSheetVisible()) {
                mMaterialSheetFab.hideSheet();
            }
            if (mSourceAdapter != null) {
                mViewModel.setEnabledSources(mSourceAdapter.getValidWeatherSources());
            }
            if (!TextUtils.isEmpty(mViewModel.getQueryValue())) {
                mViewModel.requestLocationList();
            }
        });

        mViewModel.getListResource().observe(this, loadableLocationList -> {
            setStatus(loadableLocationList.status);
            mBinding.sourceEnter.setEnabled(
                    loadableLocationList.status != LoadableLocationList.Status.LOADING);
            mBinding.sourceEnter.setAlpha(mBinding.sourceEnter.isEnabled() ? 1 : 0.5f);
            mAdapter.update(loadableLocationList.dataList, null, null);
        });

        mViewModel.getEnabledSources().observe(this, enabled -> {
        });
        mViewModel.getQuery().observe(this, query -> {
            mBinding.editText.setText(query);
            mBinding.editText.setSelection(query.length());
        });
    }

    // control.

    private void finishSelf(@Nullable Location location) {
        setResult(RESULT_OK, new Intent().putExtra(KEY_LOCATION, location));
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

    private void hideKeyboard() {
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(mBinding.editText.getWindowToken(), 0);
        }
    }

    // interface.

    // on editor action listener.

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (!TextUtils.isEmpty(textView.getText().toString())) {
            hideKeyboard();

            String query = textView.getText().toString();
            mViewModel.requestLocationList(query);
        }
        return true;
    }
}