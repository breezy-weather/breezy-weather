package wangdaye.com.geometricweather.common.ui.widgets.insets;

import android.graphics.Insets;
import android.graphics.Rect;
import android.os.Build;
import android.view.DisplayCutout;
import android.view.WindowInsets;

import androidx.core.view.DisplayCutoutCompat;
import androidx.core.view.WindowInsetsCompat;

public class Utils {

    public static Rect getWaterfullInsets(WindowInsets insets) {
        Rect waterfull = new Rect();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            DisplayCutout cutout = insets.getDisplayCutout();
            if (cutout != null) {
                Insets i = cutout.getWaterfallInsets();
                waterfull = new Rect(i.left, i.top, i.right, i.bottom);
            }
        }
        return waterfull;
    }

    public static Rect getWaterfullInsets(WindowInsetsCompat insets) {
        Rect waterfull = new Rect();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            DisplayCutoutCompat cutout = insets.getDisplayCutout();
            if (cutout != null) {
                waterfull = new Rect(cutout.getSafeInsetLeft(), cutout.getSafeInsetTop(),
                        cutout.getSafeInsetRight(), cutout.getSafeInsetBottom());
            }
        }
        return waterfull;
    }
}
