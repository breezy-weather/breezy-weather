package wangdaye.com.geometricweather.main.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.resources.ListResource;

public class SelectableLocationListResource extends ListResource<Location> {

    public final @Nullable String selectedId;
    public final @Nullable String forceUpdateId;
    public final @NonNull Source source;

    public interface Source {
    }

    public static class DataSetChanged implements Source {
    }

    public static class ItemMoved implements Source {

        public final int from;
        public final int to;

        public ItemMoved(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }

    public SelectableLocationListResource(@NonNull List<Location> dataList,
                                          @Nullable String selectedId,
                                          @Nullable String forceUpdateId) {
        this(dataList, selectedId, forceUpdateId, new DataSetChanged());
    }

    public SelectableLocationListResource(@NonNull List<Location> dataList,
                                          @Nullable String selectedId,
                                          @Nullable String forceUpdateId,
                                          @NonNull Source source) {
        super(dataList);
        this.selectedId = selectedId;
        this.forceUpdateId = forceUpdateId;
        this.source = source;
    }
}
