package wangdaye.com.geometricweather.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.decotarion.ListDecoration;

/**
 * Main list decoration.
 * */

public class MainListDecoration extends ListDecoration {

    public MainListDecoration(Context context, @Nullable MainThemePicker themePicker) {
        super(
                context,
                themePicker == null
                        ? ContextCompat.getColor(context, R.color.colorLine) : themePicker.getLineColor(context)
        );
    }
}
