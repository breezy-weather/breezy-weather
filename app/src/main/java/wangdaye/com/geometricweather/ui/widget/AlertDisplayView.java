package wangdaye.com.geometricweather.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Alert;

public class AlertDisplayView extends FrameLayout {

    private RelativeLayout container;
    private TextView title;
    private TextView indicator;

    private List<Alert> alertList;
    private boolean displaying;
    private int displayIndex;

    private AlphaAnimation animation;

    private static final int DISPLAY_DURATION = 6000;
    private static final int EXCHANGE_DURATION = 300;

    private class AlphaAnimation extends Animation {

        private View view;
        private float fromAlpha;
        private float toAlpha;

        AlphaAnimation(View view, float fromAlpha, float toAlpha) {
            this.view = view;
            this.fromAlpha = fromAlpha;
            this.toAlpha = toAlpha;
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            view.setAlpha(fromAlpha + interpolatedTime * (toAlpha - fromAlpha));
        }
    }

    public AlertDisplayView(@NonNull Context context) {
        super(context);
        initialize();
    }

    public AlertDisplayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public AlertDisplayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AlertDisplayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    @SuppressLint("InflateParams")
    private void initialize() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.container_alert_display_view, null);
        addView(view);

        this.container = findViewById(R.id.container_alert_display_view_container);
        this.title = findViewById(R.id.container_alert_display_view_title);
        this.indicator = findViewById(R.id.container_alert_display_view_indicator);

        this.alertList = null;
        this.displaying = false;
        this.displayIndex = -1;

        this.animation = null;
    }

    public void display(List<Alert> alertList) {
        if (displaying) {
            stop();
        }

        if (alertList == null || alertList.size() == 0) {
            return;
        }
        this.alertList = alertList;

        setTitle(0);
        setIndicator(0, alertList.size());
        container.setAlpha(1F);

        displaying = true;
        displayIndex = 0;

        if (alertList.size() > 1) {
            startHideAnimation();
        }
    }

    public void stop() {
        if (displaying) {
            if (animation != null
                    && animation.hasStarted() && !animation.hasEnded()) {
                animation.cancel();
            }
            animation = null;

            container.setAlpha(0F);
            displaying = false;
        }
    }

    private void startHideAnimation() {
        animation = new AlphaAnimation(container, container.getAlpha(), 0F);
        animation.setDuration(EXCHANGE_DURATION);
        animation.setStartOffset(DISPLAY_DURATION);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // do nothing.
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                displayIndex ++;
                displayIndex %= alertList.size();
                setTitle(displayIndex);
                setIndicator(displayIndex, alertList.size());
                startShowAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing.
            }
        });
        startAnimation(animation);
    }

    private void startShowAnimation() {
        animation = new AlphaAnimation(container, container.getAlpha(), 1F);
        animation.setDuration(EXCHANGE_DURATION);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // do nothing.
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (alertList.size() > 1) {
                    startHideAnimation();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing.
            }
        });
        startAnimation(animation);
    }

    private void setTitle(int index) {
        String text = alertList.get(index).description
                + ", " + alertList.get(index).publishTime;
        title.setText(text);
    }

    private void setIndicator(int index, int size) {
        if (size > 1) {
            String text = (index + 1) + "/" + size;
            indicator.setText(text);
        } else {
            indicator.setText("");
        }
    }
}
