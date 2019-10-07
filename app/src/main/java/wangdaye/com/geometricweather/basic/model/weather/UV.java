package wangdaye.com.geometricweather.basic.model.weather;

import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * UV.
 * */
public class UV {

    @Nullable private Integer index;
    @Nullable private String level;
    @Nullable private String description;

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
}
