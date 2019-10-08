package wangdaye.com.geometricweather.ui.transition;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.os.Build;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import androidx.annotation.RequiresApi;

import wangdaye.com.geometricweather.R;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class RoundCornerTransition extends Transition {

    private final float radiusFrom;
    private final float radiusTo;

    private static final String PROPNAME_RADIUS = "geometricweather:roundCorner:radius";

    private static final String[] transitionProperties = {
            PROPNAME_RADIUS
    };

    public RoundCornerTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundCornerTransition);
        radiusFrom = a.getDimension(R.styleable.RoundCornerTransition_radius_from, 0f);
        radiusTo = a.getDimension(R.styleable.RoundCornerTransition_radius_to, 0f);
        a.recycle();
    }

    @Override
    public String[] getTransitionProperties() {
        return transitionProperties;
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        transitionValues.values.put(PROPNAME_RADIUS, radiusFrom);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        transitionValues.values.put(PROPNAME_RADIUS, radiusTo);
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
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(
                            view.getPaddingLeft(),
                            view.getPaddingTop(),
                            view.getWidth() - view.getPaddingRight(),
                            view.getHeight() - view.getPaddingBottom(),
                            (Float) valueAnimator.getAnimatedValue()
                    );
                }
            });
            endValues.view.setClipToOutline(true);
        });
        return animator;
    }
}