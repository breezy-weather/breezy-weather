package wangdaye.com.geometricweather.ui.widget.switchButton;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;

/**
 * Recycler switch image button.
 * */

public class RecyclerSwitchImageButton extends ImageView
        implements View.OnClickListener {

    private OnRecyclerSwitchListener listener;

    private boolean animating;

    private int statePosition;
    private List<Integer> resList;
    private List<Integer> stateList;

    public RecyclerSwitchImageButton(Context context) {
        super(context);
        this.initialize();
    }

    public RecyclerSwitchImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public RecyclerSwitchImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    // init.

    private void initialize() {
        this.animating = false;
        setOnClickListener(this);

        this.statePosition = -1;
        this.resList = new ArrayList<>();
        this.stateList = new ArrayList<>();
    }

    // control.

    private void animHide() {
        Animation hide = AnimationUtils.loadAnimation(getContext(), R.anim.switch_image_hide);
        hide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // do nothing.
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                statePosition ++;
                if (statePosition >= stateList.size()) {
                    statePosition = 0;
                }
                setSwitchState(stateList.get(statePosition));
                if (listener != null) {
                    listener.onRecyclerSwitch(stateList.get(statePosition));
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

    public void initSwitchState(int state) {
        clearAnimation();
        setScaleX(1f);
        setScaleY(1f);
        setAlpha(1f);

        this.animating = false;
        setSwitchState(state);
    }

    private void setSwitchState(int state) {
        for (int i = 0; i < stateList.size(); i ++) {
            if (stateList.get(i) == state) {
                statePosition = i;
                Glide.with(getContext())
                        .load(resList.get(i))
                        .dontAnimate()
                        .into(this);
                break;
            }
        }
    }

    public int getState() {
        if (statePosition == -1) {
            return -1;
        } else {
            return stateList.get(statePosition);
        }
    }

    public void setStateAndRes(List<Integer> stateList, List<Integer> resList, int initState) {
        if (stateList.size() == resList.size() && stateList.size() != 0) {
            this.stateList = stateList;
            this.resList = resList;
            this.statePosition = 0;
            for (int i = 0; i < stateList.size(); i ++) {
                if (stateList.get(i) == initState) {
                    statePosition = i;
                    break;
                }
            }
            initSwitchState(stateList.get(statePosition));
        }
    }

    // interface.

    public interface OnRecyclerSwitchListener {
        void onRecyclerSwitch(int newState);
    }

    public void setOnSwitchListener(OnRecyclerSwitchListener l) {
        listener = l;
    }

    @Override
    public void onClick(View v) {
        if (!animating && stateList.size() > 0) {
            animating = true;
            animHide();
        }
    }
}
