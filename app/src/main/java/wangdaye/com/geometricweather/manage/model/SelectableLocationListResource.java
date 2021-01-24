package wangdaye.com.geometricweather.manage.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.resource.ListResource;

public class SelectableLocationListResource extends ListResource<Location> {

    public final @Nullable String selectedId;
    public final @Nullable String forceUpdateId;

    public SelectableLocationListResource(@NonNull List<Location> dataList,
                                          @Nullable String selectedId,
                                          @Nullable String forceUpdateId) {
        super(dataList);
        this.selectedId = selectedId;
        this.forceUpdateId = forceUpdateId;
    }
}
