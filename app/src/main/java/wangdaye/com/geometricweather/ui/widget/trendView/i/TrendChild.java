package wangdaye.com.geometricweather.ui.widget.trendView.i;

import androidx.annotation.NonNull;
import androidx.annotation.Px;

public interface TrendChild {

    void setParent(@NonNull TrendParent parent);

    void setWidth(@Px float width);
    void setHeight(@Px float height);
}
