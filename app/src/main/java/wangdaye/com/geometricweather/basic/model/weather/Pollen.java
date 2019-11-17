package wangdaye.com.geometricweather.basic.model.weather;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.R;

/**
 * DailyPollen.
 * */
public class Pollen {

    @Nullable private Integer grassIndex;
    @Nullable private Integer grassLevel;
    @Nullable private String grassDescription;

    @Nullable private Integer moldIndex;
    @Nullable private Integer moldLevel;
    @Nullable private String moldDescription;

    @Nullable private Integer ragweedIndex;
    @Nullable private Integer ragweedLevel;
    @Nullable private String ragweedDescription;

    @Nullable private Integer treeIndex;
    @Nullable private Integer treeLevel;
    @Nullable private String treeDescription;

    public Pollen(@Nullable Integer grassIndex, @Nullable Integer grassLevel, @Nullable String grassDescription,
                  @Nullable Integer moldIndex, @Nullable Integer moldLevel, @Nullable String moldDescription,
                  @Nullable Integer ragweedIndex, @Nullable Integer ragweedLevel, @Nullable String ragweedDescription,
                  @Nullable Integer treeIndex, @Nullable Integer treeLevel, @Nullable String treeDescription) {
        this.grassIndex = grassIndex;
        this.grassLevel = grassLevel;
        this.grassDescription = grassDescription;
        this.moldIndex = moldIndex;
        this.moldLevel = moldLevel;
        this.moldDescription = moldDescription;
        this.ragweedIndex = ragweedIndex;
        this.ragweedLevel = ragweedLevel;
        this.ragweedDescription = ragweedDescription;
        this.treeIndex = treeIndex;
        this.treeLevel = treeLevel;
        this.treeDescription = treeDescription;
    }

    @Nullable
    public Integer getGrassIndex() {
        return grassIndex;
    }

    @Nullable
    public Integer getGrassLevel() {
        return grassLevel;
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
    public Integer getMoldLevel() {
        return moldLevel;
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
    public Integer getRagweedLevel() {
        return ragweedLevel;
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
    public Integer getTreeLevel() {
        return treeLevel;
    }

    @Nullable
    public String getTreeDescription() {
        return treeDescription;
    }

    public boolean isValid() {
        return (grassIndex != null && grassIndex > 0 && grassLevel != null)
                || (moldIndex != null && moldIndex > 0 && moldLevel != null)
                || (ragweedIndex != null && ragweedIndex > 0 && ragweedLevel != null)
                || (treeIndex != null && treeIndex > 0 && treeLevel != null);
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
