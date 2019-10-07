package wangdaye.com.geometricweather.basic.model.weather;

import androidx.annotation.Nullable;

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
}
