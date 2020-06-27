package com.mbestavros.geometricweather.ui.decotarion;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.mbestavros.geometricweather.utils.DisplayUtils;
import com.mbestavros.geometricweather.utils.manager.ThemeManager;

/**
 * List decoration.
 * */

public class ListDecoration extends RecyclerView.ItemDecoration {

    private Paint paint;
    private @Px int decorationWidth;

    public ListDecoration(Context context) {
        this(context, ThemeManager.getInstance(context).getLineColor(context));
    }

    public ListDecoration(Context context, @ColorInt int color) {
        this.decorationWidth = (int) DisplayUtils.dpToPx(context, 1);

        this.paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(decorationWidth);
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        for (int i = 0; i < parent.getChildCount(); i ++){
            View child = parent.getChildAt(i);
            c.drawLine(
                    child.getLeft(),
                    child.getBottom() + decorationWidth / 2,
                    child.getRight(),
                    child.getBottom() + decorationWidth / 2,
                    paint
            );
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.set(0, 0, 0, decorationWidth);
    }
}
