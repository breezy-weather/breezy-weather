package wangdaye.com.geometricweather.common.ui.decotarions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.common.utils.managers.ThemeManager;

/**
 * List decoration.
 * */

public class ListDecoration extends RecyclerView.ItemDecoration {

    private final Paint mPaint;
    private @Px final int mDividerDistance;

    public ListDecoration(Context context) {
        this(context, ThemeManager.getInstance(context).getLineColor(context));
    }

    public ListDecoration(Context context, @ColorInt int color) {
        mDividerDistance = (int) DisplayUtils.dpToPx(context, 1);

        mPaint = new Paint();
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mDividerDistance);
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        for (int i = 0; i < parent.getChildCount(); i ++){
            View child = parent.getChildAt(i);
            c.drawLine(
                    child.getLeft(),
                    child.getBottom() + mDividerDistance / 2f,
                    child.getRight(),
                    child.getBottom() + mDividerDistance / 2f,
                    mPaint
            );
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.set(0, 0, 0, mDividerDistance);
    }
}
