package wangdaye.com.geometricweather.ui.decotarion;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import wangdaye.com.geometricweather.R;

public class GridMarginsDecoration extends RecyclerView.ItemDecoration {

    private @Px float marginsVertical;
    private @Px float marginsHorizontal;

    private int spanCount;
    private int spanIndex;
    private int adapterPosition;
    private boolean firstLine;

    private @RecyclerView.Orientation int orientation;

    public GridMarginsDecoration(Context context) {
        this(context.getResources().getDimensionPixelSize(R.dimen.little_margin));
    }

    public GridMarginsDecoration(@Px float margins) {
        this(margins, margins);
    }

    public GridMarginsDecoration(@Px float margins, @RecyclerView.Orientation int orientation) {
        this(margins, margins, orientation);
    }

    public GridMarginsDecoration(@Px float marginsVertical, @Px float marginsHorizontal) {
        this(marginsVertical, marginsHorizontal, RecyclerView.VERTICAL);
    }

    public GridMarginsDecoration(@Px float marginsVertical, @Px float marginsHorizontal,
                                 @RecyclerView.Orientation int orientation) {
        this.marginsVertical = marginsVertical;
        this.marginsHorizontal = marginsHorizontal;

        this.spanCount = 0;
        this.spanIndex = -1;
        this.adapterPosition = -1;
        this.firstLine = false;

        this.orientation = orientation;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager.LayoutParams params
                    = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();

            spanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
            spanIndex = params.getSpanIndex();
            adapterPosition = params.getViewAdapterPosition();

            firstLine = adapterPosition < spanCount;
        } else if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager.LayoutParams params
                    = (GridLayoutManager.LayoutParams) view.getLayoutParams();

            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
            spanIndex = params.getSpanIndex();
            adapterPosition = params.getViewAdapterPosition();

            if (adapterPosition >= spanCount) {
                firstLine = false;
            } else {
                GridLayoutManager.SpanSizeLookup lookup = ((GridLayoutManager) layoutManager).getSpanSizeLookup();
                firstLine = lookup.getSpanGroupIndex(adapterPosition, spanCount)
                        == lookup.getSpanGroupIndex(0, spanCount);
            }
        } else { // linear layout manager.
            spanCount = 1;
            spanIndex = 0;
            adapterPosition = ((RecyclerView.LayoutParams) view.getLayoutParams())
                    .getViewAdapterPosition();
            firstLine = adapterPosition == 0;
        }

        switch (orientation) {
            case RecyclerView.VERTICAL:
                if (spanCount == 1) {
                    outRect.set(
                            (int) marginsHorizontal,
                            (int) (firstLine ? marginsVertical : 0),
                            (int) marginsHorizontal,
                            (int) marginsVertical
                    );
                } else {
                    if (spanIndex == 0) {
                        outRect.set(
                                (int) marginsHorizontal,
                                (int) (firstLine ? marginsVertical : 0),
                                (int) marginsHorizontal,
                                (int) marginsVertical
                        );
                    } else{
                        outRect.set(
                                0,
                                (int) (firstLine ? marginsVertical : 0),
                                (int) marginsHorizontal,
                                (int) marginsVertical
                        );
                    }
                }
                break;

            case RecyclerView.HORIZONTAL:
                if (spanCount == 1) {
                    outRect.set(
                            (int) (firstLine ? marginsHorizontal : 0),
                            (int) marginsVertical,
                            (int) marginsHorizontal,
                            (int) marginsVertical
                    );
                } else {
                    if (spanIndex == 0) {
                        outRect.set(
                                (int) (firstLine ? marginsHorizontal : 0),
                                (int) marginsVertical,
                                (int) marginsHorizontal,
                                (int) marginsVertical
                        );
                    } else{
                        outRect.set(
                                (int) (firstLine ? marginsHorizontal : 0),
                                0,
                                (int) marginsHorizontal,
                                (int) marginsVertical
                        );
                    }
                }
                break;
        }
    }
}