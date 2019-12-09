package wangdaye.com.geometricweather.basic.model.weather;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.R;

/**
 * UV.
 * */
public class UV {

    @Nullable private Integer index;
    @Nullable private String level;
    @Nullable private String description;

    public static final int UV_INDEX_LOW = 2;
    public static final int UV_INDEX_MIDDLE = 5;
    public static final int UV_INDEX_HIGH = 7;
    public static final int UV_INDEX_EXCESSIVE = 10;

    public UV(@Nullable Integer index, @Nullable String level, @Nullable String description) {
        this.index = index;
        this.level = level;
        this.description = description;
    }

    @Nullable
    public Integer getIndex() {
        return index;
    }

    @Nullable
    public String getLevel() {
        return level;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public boolean isValid() {
        return index != null || level != null || description != null;
    }

    public boolean isValidIndex() {
        return index != null;
    }

    public String getUVDescription() {
        StringBuilder builder = new StringBuilder();
        if (index != null) {
            builder.append(index);
        }
        if (level != null) {
            builder.append(
                    TextUtils.isEmpty(builder.toString()) ? "" : " "
            ).append(level);
        }
        if (description != null) {
            builder.append(
                    TextUtils.isEmpty(builder.toString()) ? "" : "\n"
            ).append(description);
        }
        return builder.toString();
    }

    public String getShortUVDescription() {
        StringBuilder builder = new StringBuilder();
        if (index != null) {
            builder.append(index);
        }
        if (level != null) {
            builder.append(
                    TextUtils.isEmpty(builder.toString()) ? "" : " "
            ).append(level);
        }
        return builder.toString();
    }

    @ColorInt
    public int getUVColor(Context context) {
        if (index == null) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (index <= UV_INDEX_LOW) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (index <= UV_INDEX_MIDDLE) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (index <= UV_INDEX_HIGH) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (index <= UV_INDEX_EXCESSIVE) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        }
    }
}
