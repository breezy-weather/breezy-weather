package wangdaye.com.geometricweather.common.ui.transitions;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import wangdaye.com.geometricweather.R;

public class RoundCornerTransition extends Transition {

    private final float mRadiusFrom;
    private final float mRadiusTo;

    private static final String PROPNAME_RADIUS = "geometricweather:roundCorner:radius";

    private static final String[] transitionProperties = {
            PROPNAME_RADIUS
    };

    public RoundCornerTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundCornerTransition);
        mRadiusFrom = a.getDimension(R.styleable.RoundCornerTransition_radius_from, 0f);
        mRadiusTo = a.getDimension(R.styleable.RoundCornerTransition_radius_to, 0f);
        a.recycle();
    }

    @Override
    public String[] getTransitionProperties() {
        return transitionProperties;
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        transitionValues.values.put(PROPNAME_RADIUS, mRadiusFrom);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        transitionValues.values.put(PROPNAME_RADIUS, mRadiusTo);
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues,
                                   TransitionValues endValues) {
        if (endValues == null) {
            return null;
        }

        ValueAnimator animator = ValueAnimator.ofFloat(
                (Float) startValues.values.get(PROPNAME_RADIUS),
                (Float) endValues.values.get(PROPNAME_RADIUS)
        );
        animator.addUpdateListener(valueAnimator -> {
            endValues.view.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(
                            0,
                            0,
                            view.getWidth(),
                            view.getHeight(),
                            (Float) valueAnimator.getAnimatedValue()
                    );
                }
            });
            endValues.view.setClipToOutline(true);
        });
        return animator;
    }
}