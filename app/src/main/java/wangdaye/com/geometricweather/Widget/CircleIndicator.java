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
 * Created by WangDaYe on 2016/2/17.
 */
public class CircleIndicator extends View {
    // widget
    private Context context;
    private Paint paint;

    // data
    private int pageNum;
    private int pageNow;

    private final int UNIT_WIDTH = 9;
    private final int SPACE_WIDTH = 16;
    private final int TARGET_WIDTH = 13;

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
    }

    public void setData(int pageNum, int pageNow) {
        this.pageNum = pageNum;
        this.pageNow = pageNow;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (pageNum < 0 || pageNow < 0 || pageNow > pageNum) {
            return;
        }

        float centerWidth = getMeasuredWidth() / 2.0f;
        float centerHeight = getMeasuredHeight() / 2.0f;

        int halfNum;
        boolean odd;
        if (pageNum % 2 == 0) {
            odd = false;
            halfNum = pageNum / 2;
        } else {
            odd = true;
            halfNum = (pageNum - 1) / 2;
        }

        float startWidth;
        if (odd) {
            startWidth = centerWidth - halfNum * SPACE_WIDTH - (2 * halfNum + 1) * UNIT_WIDTH;
        } else {
            startWidth = (float) (centerWidth - halfNum * (SPACE_WIDTH - 0.5) - (2 * halfNum) * UNIT_WIDTH);
        }

        for (int i = 0; i < pageNum; i ++) {
            paint.reset();
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            if (pageNow == i + 1) {
                paint.setColor(ContextCompat.getColor(context, R.color.colorTextLight));
                canvas.drawCircle(startWidth, centerHeight, TARGET_WIDTH, paint);
            } else {
                paint.setColor(ContextCompat.getColor(context, R.color.colorTextLight2nd));
                canvas.drawCircle(startWidth, centerHeight, UNIT_WIDTH, paint);
            }
            startWidth += 2 * UNIT_WIDTH + SPACE_WIDTH;
        }
    }
}
