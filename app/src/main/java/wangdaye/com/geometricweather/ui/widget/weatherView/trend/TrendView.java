package wangdaye.com.geometricweather.ui.widget.weatherView.trend;

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
import android.widget.ImageView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.History;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.ui.popup.DailyPreviewSettingsPopupWindow;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.ui.adapter.TrendAdapter;
import wangdaye.com.geometricweather.ui.widget.switchButton.RecyclerSwitchImageButton;
import wangdaye.com.geometricweather.ui.widget.switchButton.SwitchImageButton;
import wangdaye.com.geometricweather.utils.NotificationUtils;
import wangdaye.com.geometricweather.utils.WidgetUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.manager.ChartStyleManager;

/**
 * Trend view.
 * */

public class TrendView extends FrameLayout
        implements View.OnClickListener, TrendAdapter.OnTrendItemClickListener {
    // widget
    private TrendRecyclerView recyclerView;
    private ImageView menuBtn;

    // data
    private TrendAdapter adapter;
    private Weather weather;
    private History history;
    private boolean showDailyPop;
    private boolean showDate;
    private int previewTime;

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

        this.showDailyPop = ChartStyleManager.getInstance(getContext()).isShowDailyPop();
        this.showDate = ChartStyleManager.getInstance(getContext()).isShowDate();
        this.previewTime = ChartStyleManager.getInstance(getContext()).getPreviewTime();

        this.adapter = new TrendAdapter(getContext(), null, null, showDailyPop, showDate, previewTime, this);

        this.recyclerView = (TrendRecyclerView) findViewById(R.id.container_trend_view_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);

        this.menuBtn = (ImageView) findViewById(R.id.container_trend_view_menuBtn);
        menuBtn.setOnClickListener(this);

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

    public void reset() {
        adapter.setData(weather, history, showDailyPop, showDate, previewTime, state);
        adapter.notifyDataSetChanged();

        switch (state) {
            case TrendItemView.DATA_TYPE_DAILY:
                menuBtn.setAlpha(1f);
                menuBtn.setEnabled(true);
                break;

            default:
                menuBtn.setAlpha(0.5f);
                menuBtn.setEnabled(false);
                break;
        }
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

            reset();
            recyclerView.setData(weather, history, state);
        }
    }

    /** <br> interface. */

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.container_trend_view_menuBtn:
                new DailyPreviewSettingsPopupWindow(
                        getContext(), menuBtn,
                        showDailyPop, showDate, previewTime,
                        popSwitchListener, dateSwitchListener, previewTimeSwitchListener);
                break;
        }
    }

    // on trend item click listener.

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

    // animator listener.

    private AnimatorListenerAdapter viewOutListener = new AnimatorListenerAdapter() {

        @Override
        public void onAnimationStart(Animator animation) {
            animating = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            animating = false;

            reset();
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

    // on switch listener.

    private SwitchImageButton.OnSwitchListener popSwitchListener = new SwitchImageButton.OnSwitchListener() {
        @Override
        public void onSwitch(boolean on) {
            showDailyPop = on;
            ChartStyleManager.getInstance(getContext()).setShowDailyPop(getContext(), on);
            adapter.setData(weather, history, showDailyPop, showDate, previewTime, state);
            adapter.notifyDataSetChanged();
        }
    };

    private SwitchImageButton.OnSwitchListener dateSwitchListener = new SwitchImageButton.OnSwitchListener() {
        @Override
        public void onSwitch(boolean on) {
            showDate = on;
            ChartStyleManager.getInstance(getContext()).setShowDate(getContext(), on);
            adapter.setData(weather, history, showDailyPop, showDate, previewTime, state);
            adapter.notifyDataSetChanged();
        }
    };

    // on recycler switch listener.

    private RecyclerSwitchImageButton.OnRecyclerSwitchListener previewTimeSwitchListener
            = new RecyclerSwitchImageButton.OnRecyclerSwitchListener() {
        @Override
        public void onRecyclerSwitch(int newState) {
            previewTime = newState;
            ChartStyleManager.getInstance(getContext()).setPreviewTime(getContext(), newState);
            adapter.setData(weather, history, showDailyPop, showDate, previewTime, state);
            adapter.notifyDataSetChanged();

            Location location = DatabaseHelper.getInstance(getContext()).readLocationList().get(0);
            location.weather = DatabaseHelper.getInstance(getContext()).readWeather(location);
            if (location.weather != null) {
                WidgetUtils.refreshWidgetInNewThread(getContext(), location);
                NotificationUtils.refreshNotificationInNewThread(getContext(), location);
            }
        }
    };
}
