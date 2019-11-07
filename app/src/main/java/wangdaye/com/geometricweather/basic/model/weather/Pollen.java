package wangdaye.com.geometricweather.basic.model.weather;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.R;

/**
 * Pollen.
 * */
public class Pollen {

    @Nullable private Integer grassIndex;
    @Nullable private String grassDescription;

    @Nullable private Integer moldIndex;
    @Nullable private String moldDescription;

    @Nullable private Integer ragweedIndex;
    @Nullable private String ragweedDescription;

    @Nullable private Integer treeIndex;
    @Nullable private String treeDescription;

    public Pollen(@Nullable Integer grassIndex, @Nullable String grassDescription,
                  @Nullable Integer moldIndex, @Nullable String moldDescription,
                  @Nullable Integer ragweedIndex, @Nullable String ragweedDescription,
                  @Nullable Integer treeIndex, @Nullable String treeDescription) {
        this.grassIndex = grassIndex;
        this.grassDescription = grassDescription;
        this.moldIndex = moldIndex;
        this.moldDescription = moldDescription;
        this.ragweedIndex = ragweedIndex;
        this.ragweedDescription = ragweedDescription;
        this.treeIndex = treeIndex;
        this.treeDescription = treeDescription;
    }

    @Nullable
    public Integer getGrassIndex() {
        return grassIndex;
    }

    @Nullable
    public String getGrassDescription() {
        return grassDescription;
    }

    @Nullable
    public Integer getMoldIndex() {
        return moldIndex;
    }

    @Nullable
    public String getMoldDescription() {
        return moldDescription;
    }

    @Nullable
    public Integer getRagweedIndex() {
        return ragweedIndex;
    }

    @Nullable
    public String getRagweedDescription() {
        return ragweedDescription;
    }

    @Nullable
    public Integer getTreeIndex() {
        return treeIndex;
    }

    @Nullable
    public String getTreeDescription() {
        return treeDescription;
    }

    public boolean isValid() {
        return (grassIndex != null && grassIndex > 0)
                || (moldIndex != null && moldIndex > 0)
                || (ragweedIndex != null && ragweedIndex > 0)
                || (treeIndex != null && treeIndex > 0);
    }

    @ColorInt
    public static int getPollenColor(Context context, Integer level) {
        if (level == null) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (level <= 1) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (level <= 2) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (level <= 3) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (level <= 4) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else if (level <= 5) {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_6);
        }
    }
}
