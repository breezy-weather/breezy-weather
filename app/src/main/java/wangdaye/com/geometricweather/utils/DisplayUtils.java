package wangdaye.com.geometricweather.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.main.MainActivity;

/**
 * Display utils.
 * */

public class DisplayUtils {

    private static class AlphaAnimation extends Animation {

        private View v;
        private float from;
        private float to;

        AlphaAnimation(View v, float from, float to) {
            this.v = v;
            this.from = from;
            this.to = to;

            setDuration(200);
            setInterpolator(new AccelerateDecelerateInterpolator());
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            v.setAlpha(from + (to - from) * interpolatedTime);
        }
    }

    public static float dpToPx(Context context, float dp) {
        return dp * (context.getResources().getDisplayMetrics().densityDpi / 160f);
    }

    public static float spToPx(Context context, int sp) {
        return sp * (context.getResources().getDisplayMetrics().scaledDensity);
    }

    public static int getStatusBarHeight(Resources r) {
        int resourceId = r.getIdentifier("status_bar_height", "dimen","android");
        return r.getDimensionPixelSize(resourceId);
    }

    public static int getNavigationBarHeight(Context context) {
        if (!isNavigationBarVisible(context)){
            return 0;
        }
        int result = 0;
        int resourceId = context.getResources().getIdentifier(
                "navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private static boolean isNavigationBarVisible(Context context){
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (manager == null) {
            return false;
        }

        Display display = manager.getDefaultDisplay();
        Point size = new Point();
        Point realSize = new Point();
        display.getSize(size);
        display.getRealSize(realSize);
        return realSize.y != size.y;
    }

    public static void setSystemBarStyle(Context context, Window window,
                                         boolean lightStatus,
                                         boolean navigationShader, boolean lightNavigation) {
        setSystemBarStyle(context, window, false, lightStatus, navigationShader, lightNavigation);
    }

    @SuppressLint("InlinedApi")
    public static void setSystemBarStyle(Context context, Window window,
                                         boolean statusShader, boolean lightStatus,
                                         boolean navigationShader, boolean lightNavigation) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        lightStatus &= Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
        lightNavigation &= Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
        // navigationShader ^= Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

        int visibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (lightStatus) {
            visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        if (lightNavigation) {
            visibility |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        window.getDecorView().setSystemUiVisibility(visibility);

        if (!statusShader) {
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (!lightStatus) {
            window.setStatusBarColor(ColorUtils.setAlphaComponent(
                    ContextCompat.getColor(context, R.color.colorRoot_dark), (int) (0.8 * 255)
            ));
        } else {
            window.setStatusBarColor(ColorUtils.setAlphaComponent(
                    ContextCompat.getColor(context, R.color.colorRoot_light), (int) (0.8 * 255)
            ));
        }
        if (!navigationShader) {
            window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (!lightNavigation) {
            window.setNavigationBarColor(ColorUtils.setAlphaComponent(
                    ContextCompat.getColor(context, R.color.colorRoot_dark), (int) (0.8 * 255)
            ));
        } else {
            window.setNavigationBarColor(ColorUtils.setAlphaComponent(
                    ContextCompat.getColor(context, R.color.colorRoot_light), (int) (0.8 * 255)
            ));
        }
    }

    public static void setSystemBarStyleWithScrolling(Activity activity, View statusBar,
                                                      boolean topChanged, boolean topOverlap,
                                                      boolean bottomChanged, boolean bottomOverlap,
                                                      boolean lightTheme) {
        float alpha;

        if (topChanged) {
            if (topOverlap) {
                alpha = 0.1f;
            } else {
                alpha = 0.05f;
            }
            statusBar.clearAnimation();
            statusBar.startAnimation(new AlphaAnimation(statusBar, statusBar.getAlpha(), alpha));
        }

        if (bottomChanged) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                alpha = 0;
            } else if (bottomOverlap) {
                alpha = 0.15f;
            } else {
                alpha = 0.1f;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.getWindow().setNavigationBarColor(Color.argb((int) (255 * alpha), 0, 0, 0));
            }
        }

        setSystemBarStyle(
                activity,
                activity.getWindow(),
                false, topOverlap && lightTheme,
                false, bottomOverlap && lightTheme
        );
    }

    public static void setWindowTopColor(Activity a, @ColorInt int color) {
        if (color == Color.TRANSPARENT) {
            ContextCompat.getColor(a, R.color.colorPrimary);
        }

        int c = a instanceof MainActivity ?
                color : ContextCompat.getColor(a, R.color.colorPrimary);
        ActivityManager.TaskDescription taskDescription;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            taskDescription = new ActivityManager.TaskDescription(
                    a.getString(R.string.geometric_weather),
                    R.mipmap.ic_launcher,
                    c
            );
            a.setTaskDescription(taskDescription);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bitmap topIcon = BitmapFactory.decodeResource(a.getResources(), R.drawable.ic_launcher);
            taskDescription = new ActivityManager.TaskDescription(
                    a.getString(R.string.geometric_weather),
                    topIcon,
                    c
            );
            a.setTaskDescription(taskDescription);
            topIcon.recycle();
        }
    }

    public static boolean isTabletDevice(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isDarkMode(Context context) {
        return (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    @NonNull
    public static Bitmap drawableToBitmap(@NonNull Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @ColorInt
    public static int bitmapToColorInt(@NonNull Bitmap bitmap) {
        return ThumbnailUtils.extractThumbnail(bitmap, 1, 1)
                .getPixel(0, 0);
    }

    public static boolean isLightColor(@ColorInt int color) {
        int alpha = 0xFF << 24;
        int grey = color;
        int red = ((grey & 0x00FF0000) >> 16);
        int green = ((grey & 0x0000FF00) >> 8);
        int blue = (grey & 0x000000FF);

        grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
        grey = alpha | (grey << 16) | (grey << 8) | grey;
        return grey > 0xffbdbdbd;
    }
}
