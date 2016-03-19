package wangdaye.com.geometricweather.Widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import wangdaye.com.geometricweather.R;

/**
 * The indicator in introduce activity.
 * */

public class CircleIndicator extends View {
    // widget
    private Context context;
    private Paint paint;

    // data
    private int pageNum;
    private int pageNow;
    private int pageTo;
    private int pageLast;

    private int color;

    private int UNIT_RADIUS = 9;
    private int SPACE_WIDTH = 16;
    private int TARGET_RADIUS = 13;

    private float positionOffset;

    // TAG
//    private final String TAG = "CircleIndicator";

    public CircleIndicator(Context context) {
        super(context);
        this.initialize(context);
    }

    public CircleIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize(context);
    }

    public CircleIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize(context);
    }

    private void initialize(Context context) {
        this.context = context;
        this.paint = new Paint();

        this.pageNum = 3;
        this.pageNow = 1;
        this.pageTo = 2;
        this.pageLast = 1;

        this.color = R.color.notification_background;
    }

    public boolean setData(int pageNum, int pageNow, float positionOffset) {
        this.pageNum = pageNum;
        this.pageNow = pageNow;
        this.positionOffset = positionOffset;
        this.pageLast = pageNow;
        if (pageNow == pageLast) {
            pageTo = pageNow + 1;
            return true;
        } else {
            pageTo = pageNow - 1;
            return false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (pageNum < 0 || pageNow < 0 || pageNow > pageNum) {
            return;
        }

        float dpiLevel = getResources().getDisplayMetrics().density;
        UNIT_RADIUS = (int) (9 * (dpiLevel / 2.625));
        SPACE_WIDTH = (int) (16 * (dpiLevel / 2.625));
        TARGET_RADIUS = (int) (13 * (dpiLevel / 2.625));

        float centerWidth = getMeasuredWidth() / 2;
        float centerHeight = getMeasuredHeight() / 2;

        float startWidth = centerWidth - (UNIT_RADIUS * 2 * (pageNum - 1) + SPACE_WIDTH * (pageNum - 1)) / 2;

        for (int i = 0; i < pageNum; i ++) {
            paint.reset();
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            if (pageNow == i + 1) {
                paint.setColor(ContextCompat.getColor(context, color));
                canvas.drawCircle(startWidth, centerHeight, TARGET_RADIUS - (TARGET_RADIUS - UNIT_RADIUS) * positionOffset, paint);
            } else if (pageTo == i + 1) {
                paint.setColor(ContextCompat.getColor(context, color));
                canvas.drawCircle(startWidth, centerHeight, UNIT_RADIUS + (TARGET_RADIUS - UNIT_RADIUS) * positionOffset, paint);
            } else {
                paint.setColor(ContextCompat.getColor(context, color));
                canvas.drawCircle(startWidth, centerHeight, UNIT_RADIUS, paint);
            }
            startWidth += 2 * UNIT_RADIUS + SPACE_WIDTH;
        }
    }
}
