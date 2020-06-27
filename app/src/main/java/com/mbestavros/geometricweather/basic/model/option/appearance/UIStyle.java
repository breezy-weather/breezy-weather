package com.mbestavros.geometricweather.basic.model.option.appearance;

import android.content.Context;

import androidx.annotation.Nullable;

import com.mbestavros.geometricweather.R;
import com.mbestavros.geometricweather.basic.model.option.utils.OptionMapper;

public enum UIStyle {
    CIRCULAR("circular"),
    MATERIAL("material");

    private String styleId;

    UIStyle(String styleId) {
        this.styleId = styleId;
    }

    @Nullable
    public String getUIStyleName(Context context) {
        return OptionMapper.getNameByValue(
                context,
                styleId,
                R.array.ui_styles,
                R.array.ui_style_values
        );
    }
}
