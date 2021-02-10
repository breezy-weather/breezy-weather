package wangdaye.com.geometricweather.basic.model.option.appearance;

import android.content.Context;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.utils.OptionMapper;

public enum UIStyle {

    CIRCULAR("circular"),
    MATERIAL("material");

    private final String styleId;

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
