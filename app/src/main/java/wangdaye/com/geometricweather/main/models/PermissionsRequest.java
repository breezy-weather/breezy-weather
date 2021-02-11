package wangdaye.com.geometricweather.main.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import wangdaye.com.geometricweather.basic.models.Location;

public class PermissionsRequest {

    public final @NonNull List<String> permissionList;
    public final @Nullable Location target;
    public final boolean triggeredByUser;

    public PermissionsRequest(@NonNull List<String> permissionList,
                              @Nullable Location target,
                              boolean triggeredByUser) {
        this.permissionList = permissionList;
        this.target = target;
        this.triggeredByUser = triggeredByUser;
    }
}
