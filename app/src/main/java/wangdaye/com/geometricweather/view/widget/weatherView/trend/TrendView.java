package wangdaye.com.geometricweather.view.widget.weatherView.trend;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.History;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.view.adapter.TrendAdapter;

/**
 * Trend view.
 * */

public class TrendView extends FrameLayout
        implements TrendAdapter.OnTrendItemClickListener {
    // widget
    private TrendRecyclerView recyclerView;

    // data
    private TrendAdapter adapter;
    private Weather weather;
    private History history;

    private int state = TrendItemView.DATA_TYPE_DAILY;

    private boolean animating = false;

    // animator
    private AnimatorSet viewIn;
    private AnimatorSet viewOut;

    /** <br> life cycle. */

    public TrendView(Context context) {
        super(context);
        this.initialize();
    }

    public TrendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public TrendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TrendView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize();
    }

    @SuppressLint("InflateParams")
    private void initialize() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.container_trend_view, null);
        addView(view);

        this.adapter = new TrendAdapter(getContext(), null, null, this);

        this.recyclerView = (TrendRecyclerView) findViewById(R.id.container_trend_view_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);

        viewIn = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.view_in);
        viewIn.setInterpolator(new AccelerateDecelerateInterpolator());
        viewIn.addListener(viewInListener);
        viewIn.setTarget(recyclerView);

        viewOut = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.view_out);
        viewOut.setInterpolator(new AccelerateDecelerateInterpolator());
        viewOut.addListener(viewOutListener);
        viewOut.setTarget(recyclerView);
    }

    /** <br> UI. */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) (getContext().getResources().getDisplayMetrics().widthPixels - 2.0 * DisplayUtils.dpToPx(getContext(), 8));
        int height = TrendItemView.calcHeaderHeight(getContext()) + TrendItemView.calcDrawSpecHeight(getContext());
        getChildAt(0).measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);
    }

    /** data. */

    public void setData(Weather weather, History history) {
        if (weather != null) {
            this.weather = weather;
            this.history = history;
        }
    }

    public void setState(int stateTo, boolean animate) {
        if (animate) {
            if (stateTo == state || animating) {
                return;
            }
            this.state = stateTo;
            viewOut.start();
        } else {
            viewIn.cancel();
            viewOut.cancel();
            this.state = stateTo;

            adapter.setData(weather, history, state);
            adapter.notifyDataSetChanged();

            recyclerView.setData(weather, history, state);
        }
    }

    /** <br> interface. */

    @Override
    public void onTrendItemClick() {
        switch (state) {
            case TrendItemView.DATA_TYPE_DAILY:
                setState(TrendItemView.DATA_TYPE_HOURLY, true);
                break;

            case TrendItemView.DATA_TYPE_HOURLY:
                setState(TrendItemView.DATA_TYPE_DAILY, true);
                break;
        }
    }

    private AnimatorListenerAdapter viewOutListener = new AnimatorListenerAdapter() {

        @Override
        public void onAnimationStart(Animator animation) {
            animating = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            animating = false;

            adapter.setData(weather, history, state);
            adapter.notifyDataSetChanged();

            recyclerView.setData(weather, history, state);

            viewIn.start();
        }
    };

    private AnimatorListenerAdapter viewInListener = new AnimatorListenerAdapter() {

        @Override
        public void onAnimationStart(Animator animation) {
            animating = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            animating = false;
        }
    };
}
