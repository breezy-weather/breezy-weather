package wangdaye.com.geometricweather.common.utils;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.annotation.Size;
import androidx.annotation.StyleRes;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.resources.TextAppearance;

import org.jetbrains.annotations.NotNull;

public class DisplayUtils {

    public static final Interpolator FLOATING_DECELERATE_INTERPOLATOR
            = new DecelerateInterpolator(1f);

    private static final int MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_PHONE = 512;
    private static final int MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_TABLET = 600;

    public static final float DEFAULT_CARD_LIST_ITEM_ELEVATION_DP = 2f;

    public static float dpToPx(Context context, float dp) {
        return dp * (context.getResources().getDisplayMetrics().densityDpi / 160f);
    }

    public static float spToPx(Context context, int sp) {
        return sp * (context.getResources().getDisplayMetrics().scaledDensity);
    }

    public static float pxToDp(Context context, @Px int px) {
        return px / (context.getResources().getDisplayMetrics().densityDpi / 160f);
    }

    public static void setSystemBarStyle(
            Context context,
            Window window,
            boolean lightStatus,
            boolean lightNavigation
    ) {
        setSystemBarStyle(
                context,
                window,
                false,
                lightStatus,
                false,
                lightNavigation
        );
    }

    public static void setSystemBarStyle(
            Context context,
            Window window,
            boolean statusShader,
            boolean lightStatus,
            boolean navigationShader,
            boolean lightNavigation
    ) {
        int visibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

        // status bar.
        if (lightStatus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                lightStatus = false;
                statusShader = true;
            }
        }

        // navigation bar.
        if (lightNavigation) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                visibility |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            } else {
                lightNavigation = false;
                navigationShader = true;
            }
        }
        navigationShader &= Build.VERSION.SDK_INT < Build.VERSION_CODES.Q;

        // flags.
        window.getDecorView().setSystemUiVisibility(visibility);

        // colors.
        if (!statusShader) {
            window.setStatusBarColor(Color.TRANSPARENT);
        } else {
            window.setStatusBarColor(
                    ColorUtils.setAlphaComponent(
                            lightStatus ? Color.WHITE : Color.BLACK,
                            (int) ((lightStatus ? 0.5 : 0.2) * 255)
                    )
            );
        }
        if (!navigationShader) {
            window.setNavigationBarColor(Color.TRANSPARENT);
        } else {
            window.setNavigationBarColor(
                    ColorUtils.setAlphaComponent(
                            lightNavigation ? Color.WHITE : Color.BLACK,
                            (int) ((lightNavigation ? 0.5 : 0.2) * 255)
                    )
            );
        }
    }

    public static boolean isTabletDevice(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean isRtl(Context context) {
        return context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public static boolean isDarkMode(Context context) {
        return (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean isMotionReduced(@NotNull Context context) {
        try {
            return Settings.Global.getFloat(context.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE) == 0f
                    && Settings.Global.getFloat(context.getContentResolver(), Settings.Global.TRANSITION_ANIMATION_SCALE) == 0f
                    && Settings.Global.getFloat(context.getContentResolver(), Settings.Global.WINDOW_ANIMATION_SCALE) == 0f;
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
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

    @Px
    public static int getTabletListAdaptiveWidth(Context context, @Px int width) {
        if (!isTabletDevice(context) && !isLandscape(context)) {
            return width;
        }
        return (int) Math.min(
                width,
                DisplayUtils.dpToPx(
                        context,
                        isTabletDevice(context)
                                ? MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_TABLET
                                : MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_PHONE
                )
        );
    }

    @ColorInt
    public static int blendColor(@ColorInt int foreground, @ColorInt int background) {
        int scr = Color.red(foreground);
        int scg = Color.green(foreground);
        int scb = Color.blue(foreground);
        int sa = foreground >>> 24;
        int dcr = Color.red(background);
        int dcg = Color.green(background);
        int dcb = Color.blue(background);
        int color_r = dcr * (0xff - sa) / 0xff + scr * sa / 0xff;
        int color_g = dcg * (0xff - sa) / 0xff + scg * sa / 0xff;
        int color_b = dcb * (0xff - sa) / 0xff + scb * sa / 0xff;
        return ((color_r << 16) + (color_g << 8) + color_b) | (0xff000000);
    }

    public static boolean is12Hour(Context context) {
        return !DateFormat.is24HourFormat(context);
    }

    // translationY, scaleX, scaleY
    @Size(3)
    public static Animator[] getFloatingOvershotEnterAnimators(View view) {
        return getFloatingOvershotEnterAnimators(view, 1.5f);
    }

    @Size(3)
    public static Animator[] getFloatingOvershotEnterAnimators(View view, float overshootFactor) {
        return getFloatingOvershotEnterAnimators(view, overshootFactor,
                view.getTranslationY(), view.getScaleX(), view.getScaleY());
    }

    @Size(3)
    public static Animator[] getFloatingOvershotEnterAnimators(View view,
                                                               float overshootFactor,
                                                               float translationYFrom,
                                                               float scaleXFrom,
                                                               float scaleYFrom) {
        Animator translation = ObjectAnimator.ofFloat(
                view, "translationY", translationYFrom, 0f);
        translation.setInterpolator(new OvershootInterpolator(overshootFactor));

        Animator scaleX = ObjectAnimator.ofFloat(
                view, "scaleX", scaleXFrom, 1f);
        scaleX.setInterpolator(FLOATING_DECELERATE_INTERPOLATOR);

        Animator scaleY = ObjectAnimator.ofFloat(
                view, "scaleY", scaleYFrom, 1f);
        scaleY.setInterpolator(FLOATING_DECELERATE_INTERPOLATOR);

        return new Animator[] {translation, scaleX, scaleY};
    }

    public static void getVisibleDisplayFrame(View view, Rect rect) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            DisplayMetrics metrics = new DisplayMetrics();
//            WindowManager wm = (WindowManager) view.getContext().getSystemService(
//                    Context.WINDOW_SERVICE);
//            wm.getDefaultDisplay().getRealMetrics(metrics);
//
//            WindowInsets insets = view.getRootWindowInsets();
//
//            rect.set(
//                    insets.getSystemWindowInsetLeft(),
//                    insets.getSystemWindowInsetTop(),
//                    metrics.widthPixels - insets.getSystemWindowInsetRight(),
//                    metrics.heightPixels - insets.getSystemWindowInsetBottom()
//            );
//        } else {
//            view.getWindowVisibleDisplayFrame(rect);
//        }
        // looks like has a good performance.
        view.getWindowVisibleDisplayFrame(rect);
    }

    @SuppressLint({"RestrictedApi", "VisibleForTests"})
    public static Typeface getTypefaceFromTextAppearance(
            Context context,
            @StyleRes int textAppearanceId
    ) {
        return new TextAppearance(context, textAppearanceId).getFont(context);
    }

    @ColorInt
    public static int getWidgetSurfaceColor(
            float elevationDp,
            @ColorInt int tintColor,
            @ColorInt int surfaceColor
    ) {
        if (elevationDp == 0) {
            return surfaceColor;
        }

        int foreground = ColorUtils.setAlphaComponent(
                tintColor,
                (int) (((4.5f * Math.log(elevationDp + 1)) + 2f) / 100f * 255)
        );
        return blendColor(foreground, surfaceColor);
    }
}
