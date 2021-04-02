package wangdaye.com.geometricweather.common.snackbar;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

public class SnackbarContainer {

    public final @Nullable LifecycleOwner owner;
    public final @NonNull ViewGroup container;
    public final boolean cardStyle;

    public SnackbarContainer(@Nullable LifecycleOwner owner, @NonNull ViewGroup container,
                             boolean cardStyle) {
        this.owner = owner;
        this.container = container;
        this.cardStyle = cardStyle;
    }
}
