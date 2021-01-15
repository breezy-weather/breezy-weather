package wangdaye.com.geometricweather.ui.decotarion;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.manager.ThemeManager;

/**
 * List decoration.
 * */

public class ListDecoration extends RecyclerView.ItemDecoration {

    private final Paint paint;
    private @Px final int dividerDistance;

    public ListDecoration(Context context) {
        this(context, ThemeManager.getInstance(context).getLineColor(context));
    }

    public ListDecoration(Context context, @ColorInt int color) {
        this.dividerDistance = (int) DisplayUtils.dpToPx(context, 1);

        this.paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dividerDistance);
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        for (int i = 0; i < parent.getChildCount(); i ++){
            View child = parent.getChildAt(i);
            c.drawLine(
                    child.getLeft(),
                    child.getBottom() + dividerDistance / 2f,
                    child.getRight(),
                    child.getBottom() + dividerDistance / 2f,
                    paint
            );
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.set(0, 0, 0, dividerDistance);
    }
}
