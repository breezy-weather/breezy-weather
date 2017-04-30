package wangdaye.com.geometricweather.ui.decotarion;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * List decoration.
 * */

public class ListDecoration extends RecyclerView.ItemDecoration {

    private Paint paint;
    private int decorationHeight;

    public ListDecoration(Context context) {
        this.decorationHeight = (int) DisplayUtils.dpToPx(context, 2);

        this.paint = new Paint();
        paint.setColor(ContextCompat.getColor(context, R.color.colorLine));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(decorationHeight);
    }

    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        for (int i = 0; i < parent.getChildCount(); i++){
            View child = parent.getChildAt(i);
            c.drawLine(child.getLeft(), child.getBottom(), child.getRight(), child.getBottom(), paint);
        }
    }

    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(0, 0, 0, decorationHeight);
    }
}
