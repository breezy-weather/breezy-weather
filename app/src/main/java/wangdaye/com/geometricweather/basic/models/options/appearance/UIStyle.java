package wangdaye.com.geometricweather.basic.models.options.appearance;

import android.content.Context;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.models.options._utils.Utils;

public enum UIStyle {

    CIRCULAR("circular"),
    MATERIAL("material");

    private final String styleId;

    UIStyle(String styleId) {
        this.styleId = styleId;
    }

    @Nullable
    public String getUIStyleName(Context context) {
        return Utils.getNameByValue(
                context.getResources(),
                styleId,
                R.array.ui_styles,
                R.array.ui_style_values
        );
    }

    public static UIStyle getInstance(String value) {
        switch (value) {
            case  "circular":
                return CIRCULAR;

            default:
                return MATERIAL;
        }
    }
}
