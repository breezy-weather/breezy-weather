package wangdaye.com.geometricweather.view.widget.switchButton;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import wangdaye.com.geometricweather.R;

/**
 * Switch image button.
 * */

public class SwitchImageButton extends ImageView
        implements View.OnClickListener {
    // widget
    private OnSwitchListener listener;

    // data
    private boolean switchOn;
    private boolean animating;

    private int drawableIdOn;
    private int drawableIdOff;

    /** <br> life cycle. */

    public SwitchImageButton(Context context) {
        super(context);
        this.initialize(null, 0, 0);
    }

    public SwitchImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize(attrs, 0, 0);
    }

    public SwitchImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize(attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SwitchImageButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize(attrs, defStyleAttr, defStyleRes);
    }

    private void initialize(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.animating = false;
        setOnClickListener(this);

        TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SwitchImageButton, defStyleAttr, defStyleRes);
        drawableIdOn = a.getResourceId(R.styleable.SwitchImageButton_drawable_res_on, R.drawable.ic_water_percent_on);
        drawableIdOff = a.getResourceId(R.styleable.SwitchImageButton_drawable_res_off, R.drawable.ic_water_percent_off);
        a.recycle();
    }

    /** <br> UI. */

    private void animHide() {
        Animation hide = AnimationUtils.loadAnimation(getContext(), R.anim.switch_image_hide);
        hide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // do nothing.
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setSwitchState(!switchOn);
                if (listener != null) {
                    listener.onSwitch(switchOn);
                }
                animShow();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing.
            }
        });
        startAnimation(hide);
    }

    private void animShow() {
        Animation show = AnimationUtils.loadAnimation(getContext(), R.anim.switch_image_show);
        show.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // do nothing.
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing.
            }
        });
        startAnimation(show);
    }

    public void initSwitchState(boolean on) {
        clearAnimation();
        setScaleX(1f);
        setScaleY(1f);
        setAlpha(1f);

        this.animating = false;
        setSwitchState(on);
    }

    private void setSwitchState(boolean on) {
        this.switchOn = on;
        if (switchOn) {
            Glide.with(getContext())
                    .load(drawableIdOn)
                    .dontAnimate()
                    .into(this);
        } else {
            Glide.with(getContext())
                    .load(drawableIdOff)
                    .dontAnimate()
                    .into(this);
        }
    }

    /** <br> data. */

    public boolean isSwitchOn() {
        return switchOn;
    }

    /** <br> interface. */

    public interface OnSwitchListener {
        void onSwitch(boolean on);
    }

    public void setOnSwitchListener(OnSwitchListener l) {
        listener = l;
    }

    @Override
    public void onClick(View v) {
        if (!animating) {
            animating = true;
            animHide();
        }
    }
}
