package wangdaye.com.geometricweather.Widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by WangDaYe on 2016/2/21.
 */
public class CircleRefreshView extends View {
    public CircleRefreshView(Context context) {
        super(context);
    }

    public CircleRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleRefreshView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
