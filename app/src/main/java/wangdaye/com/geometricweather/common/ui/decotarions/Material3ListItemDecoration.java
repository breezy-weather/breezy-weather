package wangdaye.com.geometricweather.common.ui.decotarions;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;

public class Material3ListItemDecoration extends RecyclerView.ItemDecoration {

    private @Px final int margins;

    public Material3ListItemDecoration(Context context) {
        margins = context.getResources().getDimensionPixelSize(R.dimen.little_margin);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(margins, 0, margins, margins);
    }
}