package wangdaye.com.geometricweather.ui.decotarion;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;

public class GridMarginsDecoration extends RecyclerView.ItemDecoration {

    private @Px float marginsVertical;
    private @Px float marginsHorizontal;

    public GridMarginsDecoration(Context context, RecyclerView recyclerView) {
        this(context.getResources().getDimensionPixelSize(R.dimen.little_margin), recyclerView);
    }

    public GridMarginsDecoration(@Px float margins, RecyclerView recyclerView) {
        this(margins, margins, recyclerView);
    }

    public GridMarginsDecoration(@Px float marginsVertical, @Px float marginsHorizontal, RecyclerView recyclerView) {
        this.marginsVertical = marginsVertical;
        this.marginsHorizontal = marginsHorizontal;
        recyclerView.setClipToPadding(false);
        recyclerView.setPadding(
                (int) marginsHorizontal / 2,
                (int) marginsVertical / 2,
                (int) marginsHorizontal / 2,
                (int) marginsVertical / 2
        );
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(
                (int) (marginsHorizontal / 2),
                (int) (marginsVertical / 2),
                (int) (marginsHorizontal / 2),
                (int) (marginsVertical / 2)
        );
    }
}