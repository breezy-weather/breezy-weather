package org.breezyweather.common.ui.widgets

import android.graphics.Shader

class DayNightShaderWrapper @JvmOverloads constructor(
    targetWidth: Int,
    targetHeight: Int,
    lightTheme: Boolean = true,
    colors: IntArray = IntArray(0),
    shader: Shader? = null
) {
    var shader: Shader? = null
        private set
    var targetWidth = 0
        private set
    var targetHeight = 0
        private set
    var isLightTheme = false
        private set
    private var mColors: IntArray = IntArray(0)

    init {
        setShader(shader, targetWidth, targetHeight, lightTheme, colors)
    }

    fun isDifferent(
        targetWidth: Int, targetHeight: Int,
        lightTheme: Boolean, colors: IntArray
    ): Boolean {
        if (shader == null || this.targetWidth != targetWidth || this.targetHeight != targetHeight
            || isLightTheme != lightTheme || mColors.size != colors.size) {
            return true
        }
        for (i in colors.indices) {
            if (mColors[i] != colors[i]) {
                return true
            }
        }
        return false
    }

    fun setShader(
        shader: Shader?,
        targetWidth: Int, targetHeight: Int,
        lightTheme: Boolean, colors: IntArray
    ) {
        this.shader = shader
        this.targetWidth = targetWidth
        this.targetHeight = targetHeight
        isLightTheme = lightTheme
        mColors = colors.copyOf(colors.size)
    }
}