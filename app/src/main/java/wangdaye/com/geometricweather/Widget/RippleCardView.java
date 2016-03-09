package wangdaye.com.geometricweather.Widget;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Card view.
 * */

public class RippleCardView extends CardView {
    // data
    public float viewStartX;
    public float viewStartY;

    public RippleCardView(Context context) {
        super(context);
    }

    public RippleCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RippleCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
