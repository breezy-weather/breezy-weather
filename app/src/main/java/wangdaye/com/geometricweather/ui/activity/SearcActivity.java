package wangdaye.com.geometricweather.ui.activity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.LocationHelper;
import wangdaye.com.geometricweather.ui.adapter.LocationAdapter;
import wangdaye.com.geometricweather.ui.decotarion.ListDecoration;

/**
 * Search activity.
 * */

public class SearcActivity extends GeoActivity
        implements View.OnClickListener, LocationAdapter.OnLocationItemClickListener,
        EditText.OnEditorActionListener, LocationHelper.OnRequestWeatherLocationListener {
    // widget
    private CoordinatorLayout container;
    private RelativeLayout searchContainer;
    private EditText editText;

    private RecyclerView recyclerView;
    private CircularProgressView progressView;

    // data
    private LocationAdapter adapter;
    private List<Location> locationList;

    private LocationHelper locationHelper;
    private String query = "";

    private int state = STATE_SHOWING;
    private static final int STATE_SHOWING = 1;
    private static final int STATE_LOADING = 2;

    public static final int SEARCH_ACTIVITY = 1;

    /** <br> life cycle. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isStarted()) {
            setStarted();
            initData();
            initWidget();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SEARCH_ACTIVITY:
                this.adapter = new LocationAdapter(
                        this,
                        DatabaseHelper.getInstance(this).readLocationList(),
                        this);
                recyclerView.setAdapter(adapter);
                break;
        }
    }

    private void finishSelf(boolean selected) {
        setResult(selected ? RESULT_OK : RESULT_CANCELED, null);
        searchContainer.setAlpha(0);
        ActivityCompat.finishAfterTransition(this);
    }

    /** <br> UI. */

    private void initWidget() {
        this.container = (CoordinatorLayout) findViewById(R.id.activity_search_container);

        findViewById(R.id.activity_search_searchBar).setOnClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.activity_search_searchBar)
                    .setTransitionName(getString(R.string.transition_activity_search_bar));
        }

        this.searchContainer = (RelativeLayout) findViewById(R.id.activity_search_searchContainer);

        findViewById(R.id.activity_search_backBtn).setOnClickListener(this);
        findViewById(R.id.activity_search_clearBtn).setOnClickListener(this);

        this.editText = (EditText) findViewById(R.id.activity_search_editText);
        editText.setOnEditorActionListener(this);

        this.recyclerView = (RecyclerView) findViewById(R.id.activity_search_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new ListDecoration(this));
        recyclerView.setAdapter(adapter);

        this.progressView = (CircularProgressView) findViewById(R.id.activity_search_progress);
        progressView.setAlpha(0);

        AnimatorSet animationSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.search_container_in);
        animationSet.setStartDelay(350);
        animationSet.setTarget(searchContainer);
        animationSet.start();
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

    /** <br> data. */

    private void initData() {
        this.adapter = new LocationAdapter(
                this,
                new ArrayList<Location>(),
                this);
        this.locationList = DatabaseHelper.getInstance(this).readLocationList();

        this.locationHelper = new LocationHelper(this);
    }

    /** <br> anim. */

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

    /** <br> listener. */

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
    public void onItemClick(View view, int position) {
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).equals(adapter.itemList.get(position))) {
                SnackbarUtils.showSnackbar(getString(R.string.feedback_collect_failed));
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
            manager.hideSoftInputFromWindow(editText.getWindowToken(), 0);

            query = textView.getText().toString();
            if (query.equals(getString(R.string.local))) {
                for (int j = 0; j < locationList.size(); j ++) {
                    if (locationList.get(j).isLocal()) {
                        SnackbarUtils.showSnackbar(getString(R.string.feedback_collect_failed));
                        editText.setText("");
                        return true;
                    }
                }
                DatabaseHelper.getInstance(this).writeLocation(Location.buildLocal());
                finishSelf(true);
            } else {
                setState(STATE_LOADING);
                locationHelper.requestWeatherLocation(this, query, this);
            }
        }
        return true;
    }

    // on request weather location listener.

    @Override
    public void requestWeatherLocationSuccess(String query, List<Location> locationList) {
        if (this.query.equals(query)) {
            adapter.itemList.clear();
            adapter.itemList.addAll(locationList);
            adapter.notifyDataSetChanged();
            setState(STATE_SHOWING);
            if (locationList.size() <= 0) {
                SnackbarUtils.showSnackbar(getString(R.string.feedback_search_nothing));
            }
        }
    }

    @Override
    public void requestWeatherLocationFailed(String query) {
        if (this.query.equals(query)) {
            adapter.itemList.clear();
            adapter.itemList.addAll(locationList);
            adapter.notifyDataSetChanged();
            setState(STATE_SHOWING);
            SnackbarUtils.showSnackbar(getString(R.string.feedback_search_nothing));
        }
    }
/*
    // text watch.

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (TextUtils.isEmpty(editable.toString())) {
            adapter.itemList = DatabaseHelper.getInstance(this).readCityList();
            adapter.notifyDataSetChanged();
        } else if (editable.toString().equals(getString(R.string.local))) {
            adapter.itemList.clear();
            adapter.itemList.add(Location.buildLocal());
            adapter.notifyDataSetChanged();
        } else {
            if (Location.checkEveryCharIsEnglish(editable.toString())) {
                adapter.itemList = DatabaseHelper.getInstance(this).fuzzySearchOverseaCityList(editable.toString());
                adapter.notifyDataSetChanged();
            } else {
                adapter.itemList = DatabaseHelper.getInstance(this).fuzzySearchCityList(editable.toString());
                adapter.notifyDataSetChanged();
            }
        }
    }*/
}
