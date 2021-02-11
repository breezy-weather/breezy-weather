package wangdaye.com.geometricweather.ui.widgets;

import android.graphics.Shader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

public class DayNightShaderWrapper {

    @Nullable private Shader mShader;

    private int mTargetWidth;
    private int mTargetHeight;

    private boolean mLightTheme;
    private int[] mColors;

    public DayNightShaderWrapper(int targetWidth, int targetHeight) {
        this(null, targetWidth, targetHeight, true, new int[0]);
    }

    public DayNightShaderWrapper(@Nullable Shader shader, int targetWidth, int targetHeight,
                                 boolean lightTheme, @NonNull int[] colors) {
        setShader(shader, targetWidth, targetHeight, lightTheme, colors);
    }

    public boolean isDifferent(int targetWidth, int targetHeight,
                               boolean lightTheme, @NonNull int[] colors) {
        if (mShader == null
                || mTargetWidth != targetWidth
                || mTargetHeight != targetHeight
                || mLightTheme != lightTheme
                || mColors.length != colors.length) {
            return true;
        }

        for (int i = 0; i < colors.length; i ++) {
            if (mColors[i] != colors[i]) {
                return true;
            }
        }

        return false;
    }

    public void setShader(@Nullable Shader shader, int targetWidth, int targetHeight,
                          boolean lightTheme, @NonNull int[] colors) {
        mShader = shader;
        mTargetWidth = targetWidth;
        mTargetHeight = targetHeight;
        mLightTheme = lightTheme;
        mColors = Arrays.copyOf(colors, colors.length);
    }

    @Nullable
    public Shader getShader() {
        return mShader;
    }

    public int getTargetWidth() {
        return mTargetWidth;
    }

    public int getTargetHeight() {
        return mTargetHeight;
    }

    public boolean isLightTheme() {
        return mLightTheme;
    }
}