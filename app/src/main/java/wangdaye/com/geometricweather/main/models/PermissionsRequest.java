package wangdaye.com.geometricweather.main.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import wangdaye.com.geometricweather.common.basic.models.Location;

public class PermissionsRequest {

    public final @NonNull List<String> permissionList;
    public final @Nullable Location target;
    public final boolean triggeredByUser;

    private boolean mConsumed;

    public PermissionsRequest(@NonNull List<String> permissionList,
                              @Nullable Location target,
                              boolean triggeredByUser) {
        this.permissionList = permissionList;
        this.target = target;
        this.triggeredByUser = triggeredByUser;

        mConsumed = false;
    }

    public boolean consume() {
        if (mConsumed) {
            return false;
        }
        mConsumed = true;
        return true;
    }
}
