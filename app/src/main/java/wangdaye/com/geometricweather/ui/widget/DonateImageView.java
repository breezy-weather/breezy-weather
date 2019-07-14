package wangdaye.com.geometricweather.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class DonateImageView extends AppCompatImageView {

    public DonateImageView(Context context) {
        super(context);
    }

    public DonateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = width / 3 * 4;
        setMeasuredDimension(width, height);
    }
}
