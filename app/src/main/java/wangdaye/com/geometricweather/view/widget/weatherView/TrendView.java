package wangdaye.com.geometricweather.view.widget.weatherView;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.History;
import wangdaye.com.geometricweather.data.entity.model.Weather;

/**
 * Trend view.
 * */

public class TrendView extends FrameLayout implements View.OnClickListener {
    // widget
    private RelativeLayout dailyContainer;
    private DailyView dailyView;

    private RelativeLayout hourlyContainer;
    private HourlyView hourlyView;

    // data
    private int state;
    public static final int DAILY_STATE = 1;
    public static final int HOURLY_STATE = -1;

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TrendView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize();
    }

    @SuppressLint("InflateParams")
    private void initialize() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.container_trend_view, null);
        addView(view);
        this.setOnClickListener(this);

        dailyContainer = (RelativeLayout) findViewById(R.id.container_temperature_daily);
        dailyView = (DailyView) findViewById(R.id.container_temperature_daily_trendView);

        hourlyContainer = (RelativeLayout) findViewById(R.id.container_temperature_hourly);
        hourlyView = (HourlyView) findViewById(R.id.container_temperature_hourly_trendView);

        viewIn = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.view_in);
        viewIn.addListener(viewInListener);
        viewOut = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.view_out);
        viewOut.addListener(viewOutListener);

        dailyContainer.setVisibility(VISIBLE);
        hourlyContainer.setVisibility(GONE);
    }

    /** <br> state. */

    public void setState(int stateTo) {
        if (stateTo == state) {
            return;
        }
        this.state = stateTo;
        switch (state) {
            case DAILY_STATE:
                viewOut.setTarget(hourlyContainer);
                viewIn.setTarget(dailyContainer);
                break;

            case HOURLY_STATE:
                viewOut.setTarget(dailyContainer);
                viewIn.setTarget(hourlyContainer);
                break;
        }
        setEnabled(false);
        viewOut.start();
    }

    /** data. */

    public void setData(Weather weather, History history) {
        if (weather != null) {
            dailyView.setData(weather, history);
            hourlyView.setData(weather);
        }
    }

    /** <br> listener. */

    private AnimatorListenerAdapter viewOutListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            switch (state) {
                case DAILY_STATE:
                    dailyContainer.setVisibility(VISIBLE);
                    hourlyContainer.setVisibility(GONE);
                    break;

                case HOURLY_STATE:
                    dailyContainer.setVisibility(GONE);
                    hourlyContainer.setVisibility(VISIBLE);
                    break;
            }
            viewIn.start();
        }
    };

    private AnimatorListenerAdapter viewInListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            setEnabled(true);
        }
    };

    /** <br> interface. */

    @Override
    public void onClick(View v) {
        switch (state) {
            case DAILY_STATE:
                setState(HOURLY_STATE);
                break;

            case HOURLY_STATE:
                setState(DAILY_STATE);
                break;
        }
    }
}
