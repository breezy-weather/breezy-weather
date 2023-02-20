package wangdaye.com.geometricweather.common.ui.transitions;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.ViewGroup;

import wangdaye.com.geometricweather.R;

public class ScaleTransition extends Transition {

    private final boolean mShow;

    private static final int TYPE_SHOW = 1;
    private static final int TYPE_HIDE = 2;

    private static final String PROPNAME_X = "geometricweather:scale:x";
    private static final String PROPNAME_Y = "geometricweather:scale:y";

    private static final String[] transitionProperties = {
            PROPNAME_X, PROPNAME_Y
    };

    public ScaleTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScaleTransition);
        mShow = a.getInt(R.styleable.ScaleTransition_scale_type, TYPE_SHOW) == TYPE_SHOW;
        a.recycle();
    }

    @Override
    public String[] getTransitionProperties() {
        return transitionProperties;
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        transitionValues.values.put(PROPNAME_X, mShow ? 0f : 1f);
        transitionValues.values.put(PROPNAME_Y, mShow ? 0f : 1f);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        transitionValues.values.put(PROPNAME_X, mShow ? transitionValues.view.getScaleX() : 0f);
        transitionValues.values.put(PROPNAME_Y, mShow ? transitionValues.view.getScaleY() : 0f);
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues,
                                   TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }

        if (mShow) {
            endValues.view.setScaleX(0f);
            endValues.view.setScaleY(0f);
        }

        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(
                        endValues.view,
                        "scaleX",
                        (Float) startValues.values.get(PROPNAME_X),
                        (Float) endValues.values.get(PROPNAME_X)
                ),
                ObjectAnimator.ofFloat(
                        endValues.view,
                        "scaleY",
                        (Float) startValues.values.get(PROPNAME_Y),
                        (Float) endValues.values.get(PROPNAME_Y)
                )
        );
        return set;
    }
}