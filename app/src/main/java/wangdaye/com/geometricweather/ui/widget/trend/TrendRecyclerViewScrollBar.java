package wangdaye.com.geometricweather.ui.widget.trend;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.manager.ThemeManager;

public class TrendRecyclerViewScrollBar extends RecyclerView.ItemDecoration {

    private Paint paint = null;
    private int scrollBarWidth;
    private int scrollBarHeight;

    private @ColorInt int centerColor;

    public TrendRecyclerViewScrollBar(Context context) {
        centerColor = ThemeManager.getInstance(context).isLightTheme()
                ? Color.argb((int) (0.02 * 255), 0, 0, 0)
                : Color.argb((int) (0.08 * 255), 0, 0, 0);
        centerColor = DisplayUtils.blendColor(
                centerColor,
                ThemeManager.getInstance(context).getRootColor(context)
        );
    }

    @Override
    public void onDraw(@NonNull Canvas c,
                       @NonNull RecyclerView parent,
                       @NonNull RecyclerView.State state) {

        if (paint == null && parent.getChildCount() > 0) {
            paint = new Paint();
            paint.setAntiAlias(true);
            scrollBarWidth = parent.getChildAt(0).getMeasuredWidth();
            scrollBarHeight = parent.getChildAt(0).getMeasuredHeight();

            paint.setShader(
                    new LinearGradient(
                            0,
                            0,
                            0,
                            scrollBarHeight / 2f,
                            Color.TRANSPARENT, centerColor,
                            Shader.TileMode.MIRROR
                    )
            );
        }

        if (paint != null) {
            int extent = parent.computeHorizontalScrollExtent();
            int range = parent.computeHorizontalScrollRange();
            int offset = parent.computeHorizontalScrollOffset();

            float offsetPercent = 1f * offset / (range - extent);

            float scrollBarOffsetX = offsetPercent * (parent.getMeasuredWidth() - scrollBarWidth);
            c.drawRect(
                    scrollBarOffsetX,
                    0,
                    scrollBarWidth + scrollBarOffsetX,
                    scrollBarHeight,
                    paint
            );
        }
    }
}
