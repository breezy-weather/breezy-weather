package wangdaye.com.geometricweather.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.activity.MainActivity;

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

    public static float dpToPx(Context context, int dp) {
        int dpi = context.getResources().getDisplayMetrics().densityDpi;
        return (float) (dp * (dpi / 160.0));
    }

    public static int getStatusBarHeight(Resources r) {
        int resourceId = r.getIdentifier("status_bar_height", "dimen","android");
        return r.getDimensionPixelSize(resourceId);
    }

    public static void setStatusBarTranslate(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void setDarkTextStatusBar(Window window) {
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    public static void setNavigationBarColor(Activity a, @ColorInt int color) {
        if (color == 0) {
            ContextCompat.getColor(a, R.color.colorPrimary);
        }
        Window w = a.getWindow();
        if (GeometricWeather.getInstance().isColorNavigationBar()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (a instanceof MainActivity) {
                w.setNavigationBarColor(color);
            } else {
                w.setNavigationBarColor(ContextCompat.getColor(a, R.color.colorPrimary));
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            w.setNavigationBarColor(ContextCompat.getColor(a, android.R.color.black));
        }
    }

    public static void setWindowTopColor(Activity a, @ColorInt int color) {
        if (color == 0) {
            ContextCompat.getColor(a, R.color.colorPrimary);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int c = a instanceof MainActivity ?
                    color : ContextCompat.getColor(a, R.color.colorPrimary);

            ActivityManager.TaskDescription taskDescription;
            Bitmap topIcon = BitmapFactory.decodeResource(a.getResources(), R.drawable.ic_launcher);
            taskDescription = new ActivityManager.TaskDescription(
                    a.getString(R.string.geometric_weather),
                    topIcon,
                    c);
            a.setTaskDescription(taskDescription);
            topIcon.recycle();
        }
    }

    public static boolean isTabletDevice(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static void setStatusBarStyleWithScrolling(Window window, View statusBar, boolean overlap) {
        if (overlap && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            statusBar.clearAnimation();
            statusBar.startAnimation(new AlphaAnimation(statusBar, statusBar.getAlpha(), 0.2F));
        } else if (overlap) {
            statusBar.clearAnimation();
            statusBar.startAnimation(new AlphaAnimation(statusBar, statusBar.getAlpha(), 0.1F));
        } else {
            statusBar.clearAnimation();
            statusBar.startAnimation(new AlphaAnimation(statusBar, statusBar.getAlpha(), 0.05F));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (overlap) {
                setDarkTextStatusBar(window);
            } else {
                setStatusBarTranslate(window);
            }
        }
    }
}
