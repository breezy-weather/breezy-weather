package wangdaye.com.geometricweather.Widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

/**
 * Card view.
 * */

public class RippleCardView extends CardView {
    // widget
//    private Paint paint;
    // data
//    private int drawTime;
//    private float width;
//    private float height;
//    private static final int TOTAL_TIME = 20;
    // TAG
//    private static final String TAG = "SCGCardView";

    public RippleCardView(Context context) {
        super(context);
//        this.initialize();
    }

    public RippleCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        this.initialize();
    }

    public RippleCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        this.initialize();
    }
/*
    public void initialize() {
        this.drawTime = 0;
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
    }
*/
    @Override
    protected void onDraw(Canvas canvas) {
        /*
        drawTime ++;
        this.width = this.getMeasuredWidth();
        this.height = this.getMeasuredHeight();
        float drawWidth = this.width / TOTAL_TIME * drawTime;
        float drawHeight = this.height / TOTAL_TIME * drawTime;
        if (width > height) {
            canvas.drawCircle(width / 2, height / 2, drawWidth, paint);
        } else {
            canvas.drawCircle(width / 2, height / 2, drawHeight, paint);
        }
        // canvas.drawRect(width - drawWidth, 0, width + drawWidth, height, paint);
        if (drawTime < TOTAL_TIME) {
            invalidate();
        } else {
            super.onDraw(canvas);
            drawTime = 0;
        }
        */
        super.onDraw(canvas);
    }
}
