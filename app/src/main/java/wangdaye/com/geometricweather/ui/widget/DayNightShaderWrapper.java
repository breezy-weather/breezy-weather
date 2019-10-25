package wangdaye.com.geometricweather.ui.widget;

import android.graphics.Shader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

public class DayNightShaderWrapper {

    @Nullable private Shader shader;

    private int targetWidth;
    private int targetHeight;

    private boolean lightTheme;
    private int[] colors;

    public DayNightShaderWrapper(int targetWidth, int targetHeight) {
        this(null, targetWidth, targetHeight, true, new int[0]);
    }

    public DayNightShaderWrapper(@Nullable Shader shader, int targetWidth, int targetHeight,
                                 boolean lightTheme, @NonNull int[] colors) {
        setShader(shader, targetWidth, targetHeight, lightTheme, colors);
    }

    public boolean isDifferent(int targetWidth, int targetHeight,
                               boolean lightTheme, @NonNull int[] colors) {
        if (this.shader == null
                || this.targetWidth != targetWidth
                || this.targetHeight != targetHeight
                || this.lightTheme != lightTheme
                || this.colors.length != colors.length) {
            return true;
        }

        for (int i = 0; i < colors.length; i ++) {
            if (this.colors[i] != colors[i]) {
                return true;
            }
        }

        return false;
    }

    public void setShader(@Nullable Shader shader, int targetWidth, int targetHeight,
                          boolean lightTheme, @NonNull int[] colors) {
        this.shader = shader;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.lightTheme = lightTheme;
        this.colors = Arrays.copyOf(colors, colors.length);
    }

    @Nullable
    public Shader getShader() {
        return shader;
    }

    public int getTargetWidth() {
        return targetWidth;
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public boolean isLightTheme() {
        return lightTheme;
    }
}